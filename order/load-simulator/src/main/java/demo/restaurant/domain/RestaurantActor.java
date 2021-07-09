package demo.restaurant.domain;

import demo.order.client.OrderServiceClient;
import demo.order.domain.Order;
import demo.restaurant.config.RestaurantProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.HttpClientErrorException;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * The restaurant actor drives the state of an order forward after customer creation and until a driver pickup.
 * Each restaurant needs to maintain the state of its capacity to fulfill orderRequests within a specified period of time.
 * <p>
 * For each of the restaurant actors, there are multiple variables that are responsible for determining the
 * supply-demand capacity for fulfilling an online order. In addition to online orderRequests, a restaurant must also fulfill
 * new orderRequests from dine-in customers. The initial state of a chef is dependent on an order fulfillment rate, which
 * represents the chef's average order fulfillment over a period of time. The number and fulfillment rate for each
 * chef actor should be initialized with the restaurant location.
 */
public class RestaurantActor {

    private final Logger log = LoggerFactory.getLogger(this.getClass().getName());
    private RestaurantProperties properties;
    private final ScheduledExecutorService scheduledExecutor = Executors.newScheduledThreadPool(2);
    private final ScheduledExecutorService newOrderScheduler = Executors.newSingleThreadScheduledExecutor();
    private final ExecutorService parallelEventExecutor = Executors.newFixedThreadPool(5);
    private final ExecutorService orderSchedulingRetryExecutor = Executors.newSingleThreadExecutor();
    private final ExecutorService eventProcessorRetryExecutor = Executors.newSingleThreadExecutor();
    private ScheduledFuture<?> orderScheduler;
    private ScheduledFuture<?> eventScheduler;
    private Long orderPreparedTime = 0L;
    private final DeliveryScheduler deliveryScheduler = new DeliveryScheduler();
    private OrderServiceClient orderServiceClient;
    private Restaurant restaurant;

    public RestaurantActor() {
    }

    public RestaurantActor(RestaurantProperties properties, OrderServiceClient orderServiceClient) {
        this.properties = properties;
        this.orderServiceClient = orderServiceClient;
    }

    public void init(RestaurantProperties properties, OrderServiceClient orderServiceClient) {
        this.properties = properties;
        this.orderServiceClient = orderServiceClient;
    }

    public ScheduledFuture<?> getOrderScheduler() {
        return orderScheduler;
    }

    public void setOrderScheduler(ScheduledFuture<?> orderScheduler) {
        this.orderScheduler = orderScheduler;
    }

    public ScheduledExecutorService getScheduledExecutor() {
        return scheduledExecutor;
    }

    public void orderReceived() {
        // Create a new order request with a random account ID
        Order order = orderServiceClient.create(new Order(Math.round(Math.random() * 100000000.0)));

        orderPreparedTime += getFutureTimeFrame(properties.getPreparationRate());
        final long preparedTime = orderPreparedTime;

        DeliveryWorkflow workflow = deliveryScheduler
                .addToWorkflow(DeliveryWorkflow.build(deliveryScheduler), order,
                        (event) -> event.setDeliveryTime(deliveryScheduler.getPosition() + getFutureTimeFrame(20.0)),
                        DeliveryEventType.ORDER_ASSIGNED, (orderItem) ->
                                orderServiceClient.assignOrder(orderItem.getOrderId(), restaurant.getStoreId()))
                .addToWorkflow(order,
                        (event) -> event.setDeliveryTime(deliveryScheduler.getPosition() + 1),
                        DeliveryEventType.ORDER_LOCATION_UPDATED, (orderItem) ->
                                orderServiceClient.updateOrderLocation(order.getOrderId(), restaurant.getLatitude(), restaurant.getLongitude()))
                .addToWorkflow(order,
                        (event) -> event.setDeliveryTime(deliveryScheduler.getPosition() + getFutureTimeFrame(20.0)),
                        DeliveryEventType.ORDER_PREPARING, (orderItem) ->
                                orderServiceClient.prepareOrder(order.getOrderId()))
                .addToWorkflow(order,
                        (event) -> event.setDeliveryTime(preparedTime),
                        DeliveryEventType.ORDER_PREPARED, (orderItem) ->
                                orderServiceClient.orderReady(order.getOrderId()));

        workflow.execute();
    }

    private long getFutureTimeFrame(double timeWindowRate) {
        return (1 + ((Math.round(Math.random() * timeWindowRate))));
    }

    public void open() {
        this.close();
        createEventScheduler();
        createOrderScheduler();
    }

    private void createEventScheduler() {
        eventScheduler = scheduledExecutor.scheduleWithFixedDelay(this::processScheduledEvents,
                properties.getPreparationTime(), properties.getPreparationTime(), TimeUnit.MILLISECONDS);

        eventProcessorRetryExecutor.submit(() -> {
            try {
                eventScheduler.get();
            } catch (ExecutionException e) {
                Throwable rootException = e.getCause();
                log.error("Error processing scheduled events", rootException);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                createEventScheduler();
            }
        });
    }

    private void createOrderScheduler() {
        this.setOrderScheduler(newOrderScheduler.scheduleAtFixedRate(this::orderReceived,
                properties.getNewOrderTime(), properties.getNewOrderTime(), TimeUnit.MILLISECONDS));

        orderSchedulingRetryExecutor.submit(() -> {
            try {
                orderScheduler.get();
            } catch (ExecutionException e) {
                Throwable rootException = e.getCause();
                log.error("Error scheduling tasks", rootException);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                createOrderScheduler();
            }
        });
    }

    private void processScheduledEvents() {
        if (!deliveryScheduler.isEmpty()) {
            List<DeliveryEvent> deliveryEvents = deliveryScheduler.nextFrame();

            if (deliveryEvents != null && deliveryEvents.size() > 0) {

                List<Callable<Order>> events = deliveryEvents.stream().map(event -> (Callable<Order>) (() -> {
                    Order order = null;
                    try {
                        order = event.getDeliveryAction().apply(event.getOrder());
                    } catch (HttpClientErrorException e) {
                        if (e.getStatusCode().is4xxClientError()) {
                            // Check the order status on the server
                            order = orderServiceClient.get(event.getOrder().getOrderId());
                        }
                    }

                    if (order != null) {
                        // Roll the schedule forward for the order if everything looks good
                        event.setOrder(order);
                        event.getDeliveryWorkflow().setCurrentOrderState(order);
                        event.getDeliveryWorkflow().scheduleNext();
                    } else {
                        // Roll back the order state to the current client state and schedule the last event
                        order = orderServiceClient.update(event.getOrder());
                        event.setOrder(order);
                        event.getDeliveryWorkflow().setCurrentOrderState(order);
                        event.getDeliveryWorkflow().scheduleLast();
                    }

                    log.info("[ORDER_EVENT]: " + this.toString() + ": " + Arrays.toString(deliveryEvents
                            .toArray(DeliveryEvent[]::new)));

                    return order;
                })).collect(Collectors.toList());

                try {
                    parallelEventExecutor.invokeAll(events);
                } catch (InterruptedException e) {
                    log.error("Event handler task failed with error", e);
                }
            }
        }
    }

    public void close() {
        if (orderScheduler != null) {
            orderScheduler.cancel(false);
        }
    }

    public static RestaurantActor from(RestaurantProperties config, OrderServiceClient orderServiceClient) {
        return new RestaurantActor(config, orderServiceClient);
    }

    public Restaurant getRestaurant() {
        return restaurant;
    }

    public void setRestaurant(Restaurant restaurant) {
        this.restaurant = restaurant;
    }

    @Override
    public String toString() {
        return "RestaurantActor{" +
                "restaurant=" + restaurant +
                '}';
    }
}
