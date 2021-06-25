package demo.restaurant.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import demo.order.client.OrderServiceClient;
import demo.order.domain.Order;
import demo.order.util.GeoUtils;
import demo.restaurant.config.RestaurantProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.IntStream;

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
public class Restaurant {

    private final Logger log = LoggerFactory.getLogger(this.getClass().getName());
    private RestaurantProperties properties;
    private final ScheduledExecutorService scheduledExecutor = Executors.newScheduledThreadPool(2);
    private final ExecutorService orderSchedulingRetryExecutor = Executors.newSingleThreadExecutor();
    private final ExecutorService eventProcessorRetryExecutor = Executors.newSingleThreadExecutor();
    private ScheduledFuture<?> orderScheduler;
    private ScheduledFuture<?> eventScheduler;
    private Long orderCount = 0L;
    private Long orderPreparedTime = 0L;
    private final DeliveryScheduler deliveryScheduler = new DeliveryScheduler();
    private OrderServiceClient orderServiceClient;
    private String city;
    private String name;
    private String country;
    private Double longitude;
    private Double latitude;
    private Integer storeId;

    public Restaurant() {
    }

    public Restaurant(RestaurantProperties properties, OrderServiceClient orderServiceClient) {
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
        orderCount++;

        // Create a new order request with a random account ID
        Order order = orderServiceClient.create(new Order(Math.round(Math.random() * 100000000.0)));

        orderPreparedTime += getFutureTimeFrame(properties.getPreparationRate());
        final long preparedTime = orderPreparedTime;

        DeliveryWorkflow workflow = deliveryScheduler
                .addToWorkflow(DeliveryWorkflow.build(deliveryScheduler), order,
                        (event) -> event.setDeliveryTime(deliveryScheduler.getPosition() + getFutureTimeFrame(2.0)),
                        DeliveryEventType.ORDER_ASSIGNED, (orderItem) ->
                                orderServiceClient.assignOrder(orderItem.getOrderId(), this.getStoreId()))
                .addToWorkflow(order,
                        (event) -> event.setDeliveryTime(deliveryScheduler.getPosition() + 1),
                        DeliveryEventType.ORDER_LOCATION_UPDATED, (orderItem) ->
                                orderServiceClient.updateOrderLocation(order.getOrderId(), latitude, longitude))
                .addToWorkflow(order,
                        (event) -> event.setDeliveryTime(deliveryScheduler.getPosition() + getFutureTimeFrame(3.0)),
                        DeliveryEventType.ORDER_PREPARING, (orderItem) ->
                                orderServiceClient.prepareOrder(order.getOrderId()))
                .addToWorkflow(order,
                        (event) -> event.setDeliveryTime(preparedTime),
                        DeliveryEventType.ORDER_PREPARED, (orderItem) ->
                                orderServiceClient.orderReady(order.getOrderId()))
                .addToWorkflow(order,
                        (event) -> event.setDeliveryTime(deliveryScheduler.getPosition() + getFutureTimeFrame(15.0)),
                        DeliveryEventType.ORDER_PICKED_UP, (orderItem) ->
                                orderServiceClient.orderPickedUp(order.getOrderId()))
                .addToWorkflow(order,
                        (event) -> event.setDeliveryTime(deliveryScheduler.getPosition() + getFutureTimeFrame(15.0)),
                        DeliveryEventType.ORDER_DELIVERING, (orderItem) ->
                                orderServiceClient.deliverOrder(order.getOrderId()));

        // Generate an initial delivery bearing that is either N,E,S,W to simulate driving around in a city
        AtomicReference<Double> deliveryBearing =
                new AtomicReference<>(((int) (Math.round(Math.random() * 3.0) + 1.0)) * 90.0);

        // Simulate the driver delivery location updates on the way to a randomized customer location
        IntStream.range(0, 20).forEachOrdered(i -> {
            final double newBearing = deliveryBearing.updateAndGet((val) -> getSimulatedRouteBearing(val, i + 1));
            workflow.addToWorkflow(order,
                    (event) -> event.setDeliveryTime(deliveryScheduler.getPosition() + getFutureTimeFrame(4.0)),
                    DeliveryEventType.ORDER_LOCATION_UPDATED, (orderItem) -> {
                        Order updatedOrder = orderServiceClient.get(order.getOrderId());
                        if(updatedOrder != null && updatedOrder.getLat() != null && updatedOrder.getLon() != null) {
                            double[] newDriverPosition = GeoUtils.findPointAtDistanceFrom(
                                    new double[]{updatedOrder.getLat(), updatedOrder.getLon()}, newBearing,
                                    (Math.random() / 2.0) + .25);
                            return orderServiceClient.updateOrderLocation(order.getOrderId(), newDriverPosition[0],
                                    newDriverPosition[1]);
                        } else {
                            return orderItem;
                        }
                    });
        });

        workflow.addToWorkflow(order,
                (event) -> event.setDeliveryTime(deliveryScheduler.getPosition() + getFutureTimeFrame(3.0)),
                DeliveryEventType.ORDER_DELIVERED, (orderItem) ->
                        orderServiceClient.orderDelivered(order.getOrderId()));

        workflow.execute();
    }

    private double getSimulatedRouteBearing(double deliveryBearing, int i) {
        return ((deliveryBearing + ((i % 2) == 0 ? (Math.random() >= .5 ? 1 : -1) * 90.0 : 0)) + 360) % 360;
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
        this.setOrderScheduler(getScheduledExecutor().scheduleAtFixedRate(this::orderReceived,
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
                deliveryEvents.parallelStream().forEach(event -> {
                    Order order = event.getDeliveryAction().apply(event.getOrder());
                    event.setOrder(order);
                    event.getDeliveryWorkflow().setCurrentOrderState(order);
                    event.getDeliveryWorkflow().scheduleNext();
                });

                log.info("[ORDER_EVENT]: " + this.toString() + ": " + Arrays.toString(deliveryEvents
                        .toArray(DeliveryEvent[]::new)));
            }
        }
    }

    public void close() {
        if (orderScheduler != null) {
            orderScheduler.cancel(false);
        }
    }

    public static Restaurant from(RestaurantProperties config, OrderServiceClient orderServiceClient) {
        return new Restaurant(config, orderServiceClient);
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    @JsonProperty("store_id")
    public Integer getStoreId() {
        return storeId;
    }

    public void setStoreId(Integer storeId) {
        this.storeId = storeId;
    }

    @Override
    public String toString() {
        return "Restaurant{" +
                "city='" + city + '\'' +
                ", name='" + name + '\'' +
                ", longitude=" + longitude +
                ", latitude=" + latitude +
                ", storeId=" + storeId +
                '}';
    }
}
