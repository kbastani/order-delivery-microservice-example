package demo.driver.domain;

import demo.order.client.DriverServiceClient;
import demo.order.client.OrderServiceClient;
import demo.order.domain.Order;
import demo.order.domain.OrderStatus;
import demo.order.util.DistanceUnit;
import demo.order.util.GeoUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.IntStream;

public class DriverActor {
    private final Logger log = LoggerFactory.getLogger(this.getClass().getName());
    private final ScheduledExecutorService scheduledExecutor = Executors.newSingleThreadScheduledExecutor();
    private final ScheduledExecutorService newDriverScheduler = Executors.newSingleThreadScheduledExecutor();
    private final ExecutorService parallelEventExecutor = Executors.newSingleThreadExecutor();
    private final ExecutorService driverSchedulingRetryExecutor = Executors.newSingleThreadExecutor();
    private final ExecutorService eventProcessorRetryExecutor = Executors.newSingleThreadExecutor();
    private ScheduledFuture<?> driverScheduler;
    private ScheduledFuture<?> eventScheduler;
    private Long driverPreparedTime = 0L;
    private final DriverProperties properties;
    private final DriverScheduler deliveryScheduler = new DriverScheduler();
    private DriverServiceClient driverServiceClient;
    private OrderServiceClient orderServiceClient;
    private Driver driver;
    private DriverWorkflow currentWorkflow;

    public DriverActor(DriverProperties properties) {
        this.properties = properties;
    }

    public void init(DriverServiceClient driverServiceClient, OrderServiceClient orderServiceClient) {
        this.driverServiceClient = driverServiceClient;
        this.orderServiceClient = orderServiceClient;
    }

    public ScheduledFuture<?> getDriverScheduler() {
        return driverScheduler;
    }

    public void setDriverScheduler(ScheduledFuture<?> driverScheduler) {
        this.driverScheduler = driverScheduler;
    }

    public ScheduledExecutorService getScheduledExecutor() {
        return scheduledExecutor;
    }

    public void newOrderRequest() {

        if (driverServiceClient.get(driver.getDriverId()).getActivityStatus().equals("DRIVER_WAITING")) {
            // Check for a new order request
            DriverOrderRequest orderRequest = null;

            try {
                orderRequest = driverServiceClient.fetchOrderRequest(driver.getDriverId());
            } catch (Exception ex) {
                log.trace(String.format("Driver{id=%s} could not fetch order: ",
                        driver.getDriverId()) + ex.getMessage());
            }

            if (orderRequest != null) {
                try {
                    if (orderServiceClient.get(orderRequest.getOrderId()).getStatus() == OrderStatus.ORDER_PREPARED) {
                        Order order = orderServiceClient.assignDriver(orderRequest.getOrderId(), driver.getDriverId());
                        this.driver = driverServiceClient.get(driver.getDriverId());
                        driver.setOrder(order);
                        driver.setOrderId(order.getOrderId());

                        if (orderServiceClient.get(orderRequest.getOrderId()).getDriverId().equals(driver.getDriverId())) {
                            executeDriverWorkflow();
                        } else {
                            throw new RuntimeException("Order has been assigned to another driver");
                        }
                    }
                } catch (Exception ex) {
                    this.driver = driverServiceClient.get(driver.getDriverId());
                    if (!driver.getActivityStatus().equals("DRIVER_WAITING")) {
                        driver.setActivityStatus("DRIVER_WAITING");
                        driverServiceClient.update(driver);
                    }
                    // Driver is in an invalid state or the order was already claimed
                    log.info(String.format("Driver{id=%s} could not fetch order: ",
                            driver.getDriverId()) + ex.getMessage());
                }
            }
        } else {
            if (driver.getOrderId() != null && !orderServiceClient.get(driver.getOrderId()).getDriverId()
                    .equals(driver.getDriverId())) {
                if (currentWorkflow != null) {
                    currentWorkflow.setActive(false);
                }
                driver.setActivityStatus("DRIVER_WAITING");
                driverServiceClient.update(driver);
            }
        }
    }

    private void executeDriverWorkflow() {
        if (currentWorkflow != null) {
            currentWorkflow.setActive(false);
        }
        // Schedule the order pickup and delivery
        DriverWorkflow workflow = DriverWorkflow.build(deliveryScheduler);
        workflow.setActive(true);

        // Simulate the driver delivery location updates on the way to a randomized customer location
        IntStream.range(0, 10).forEachOrdered(i ->
                workflow.addToWorkflow(this,
                        (event) -> event.setDeliveryTime(deliveryScheduler.getPosition() + 2),
                        DriverEventType.LOCATION_UPDATED, (driverItem) -> {
                            Driver currentDriver = driverServiceClient.get(this.getDriver().getDriverId());
                            Order currentOrder = orderServiceClient.get(this.getDriver().getOrderId());
                            currentDriver.setOrder(currentOrder);
                            setDriver(currentDriver);

                            double pickupBearing = Math.toRadians(GeoUtils.bearing(driver.getLat(), driver.getLon(),
                                    driver.getOrder().getLat(),
                                    driver.getOrder().getLon()));

                            double pickupDistance = GeoUtils.distance(driver.getLat(), driver.getLon(), driver.getOrder().getLat(),
                                    driver.getOrder().getLon(), DistanceUnit.KILOMETERS);
                            double pickupDistanceIncrement = (pickupDistance / (10.0 - (double) i));

                            double[] newDriverPosition = GeoUtils.findPointAtDistanceFrom(
                                    new double[]{driver.getLat(), driver.getLon()}, pickupBearing, pickupDistanceIncrement);
                            driver = driverServiceClient.updateDriverLocation(driver.getDriverId(), newDriverPosition[0],
                                    newDriverPosition[1]);
                            driver.setOrder(currentOrder);
                            return driver;
                        }));

        workflow.addToWorkflow(this,
                (event) -> event.setDeliveryTime(deliveryScheduler.getPosition() + 2),
                DriverEventType.ORDER_PICKED_UP, (driverItem) -> {
                    driver.setOrder(orderServiceClient.orderPickedUp(driver.getOrderId()));
                    return driver;
                }).addToWorkflow(this,
                (event) -> event.setDeliveryTime(deliveryScheduler.getPosition() + 2),
                DriverEventType.ORDER_DELIVERING, (driverItem) -> {
                    driver.setOrder(orderServiceClient.deliverOrder(driver.getOrderId()));
                    return driver;
                });

        IntStream.range(0, 10).forEachOrdered(i ->
                workflow.addToWorkflow(this,
                        (event) -> event.setDeliveryTime(deliveryScheduler.getPosition() + 2),
                        DriverEventType.LOCATION_UPDATED, (driverItem) -> {
                            Driver currentDriver = driverServiceClient.get(driver.getDriverId());
                            Order currentOrder = orderServiceClient.get(driver.getOrderId());
                            currentDriver.setOrder(currentOrder);
                            driver = currentDriver;

                            double deliveryBearing = Math.toRadians(GeoUtils.bearing(driver.getLat(), driver.getLon(),
                                    driver.getOrder().getDeliveryLat(),
                                    driver.getOrder().getDeliveryLon()));
                            double deliveryDistance = GeoUtils.distance(driver.getLat(), driver.getLon(),
                                    driver.getOrder().getDeliveryLat(), driver.getOrder().getDeliveryLon(),
                                    DistanceUnit.KILOMETERS);
                            double deliveryDistanceIncrement = (deliveryDistance / (10.0 - (double) i));

                            double[] newDriverPosition = GeoUtils.findPointAtDistanceFrom(
                                    new double[]{driver.getLat(), driver.getLon()}, deliveryBearing,
                                    deliveryDistanceIncrement);

                            driver = driverServiceClient.updateDriverLocation(driver.getDriverId(),
                                    newDriverPosition[0], newDriverPosition[1]);

                            currentOrder = orderServiceClient.updateOrderLocation(driver.getOrderId(),
                                    newDriverPosition[0], newDriverPosition[1]);

                            driver.setOrder(currentOrder);

                            return driver;
                        }));

        workflow.addToWorkflow(this,
                (event) -> event.setDeliveryTime(deliveryScheduler.getPosition() + 2),
                DriverEventType.ORDER_DELIVERED, (driverItem) -> {
                    Order currentOrder = orderServiceClient.get(driver.getOrderId());
                    if (currentOrder.getStatus() != OrderStatus.ORDER_DELIVERED) {
                        orderServiceClient.orderDelivered(driver.getOrderId());
                    } else {
                        driver = driverServiceClient.get(driver.getDriverId());
                        driver.setActivityStatus("DRIVER_WAITING");
                        driver = driverServiceClient.update(driver);
                    }

                    return driver;
                });

        currentWorkflow = workflow;

        workflow.execute();
    }

    private long getFutureTimeFrame(double timeWindowRate) {
        return (1 + ((Math.round(Math.random() * timeWindowRate))));
    }

    public void open() {
        this.close();
        createEventScheduler();
        createDriverScheduler();
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

    private void createDriverScheduler() {
        this.setDriverScheduler(newDriverScheduler.scheduleAtFixedRate(this::newOrderRequest,
                properties.getNewDriverTime(), properties.getNewDriverTime(), TimeUnit.MILLISECONDS));

        driverSchedulingRetryExecutor.submit(() -> {
            try {
                driverScheduler.get();
            } catch (ExecutionException e) {
                Throwable rootException = e.getCause();
                log.error("Error scheduling tasks", rootException);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                createDriverScheduler();
            }
        });
    }

    private void processScheduledEvents() {
        if (!deliveryScheduler.isEmpty()) {
            List<DriverEvent> deliveryEvents = deliveryScheduler.nextFrame();

            if (deliveryEvents != null && deliveryEvents.size() > 0) {
                deliveryEvents.forEach(event -> {
                    if (event.getDriverWorkflow().isActive()) {
                        Driver driver = event.getDriverAction().apply(event.getDriver());

                        // Roll the schedule forward for the driver if everything looks good
                        event.setDriver(driver);
                        event.getDriverWorkflow().setCurrentDriverState(this);
                        event.getDriverWorkflow().scheduleNext();

                        log.info("[DRIVER_EVENT]: " + this.toString() + ": " + Arrays.toString(deliveryEvents
                                .toArray(DriverEvent[]::new)));
                    }
                });
            }
        }
    }

    public void close() {
        if (driverScheduler != null) {
            driverScheduler.cancel(false);
        }
    }

    public Driver getDriver() {
        return driver;
    }

    public void setDriver(Driver driver) {
        this.driver = driver;
    }

    @Override
    public String toString() {
        return "DriverActor{" +
                "deliveryScheduler=" + deliveryScheduler +
                ", driver=" + driver +
                '}';
    }
}
