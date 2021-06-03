package demo.restaurant.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import demo.order.client.OrderClient;
import demo.order.domain.Order;
import demo.restaurant.config.RestaurantProperties;
import scheduler.OrderRequest;
import scheduler.Resource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
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
public class Restaurant {

    private final Logger log = Logger.getLogger(this.getClass().getName());
    private RestaurantProperties properties;
    private final ScheduledExecutorService scheduledExecutor = Executors.newScheduledThreadPool(2);
    private ScheduledFuture<?> orderScheduler;
    private Long orderCount = 0L;
    private Long deliveryTime = 0L;
    private final Kitchen kitchen = new Kitchen();
    private OrderClient orderClient;
    private String city;
    private String name;
    private String country;
    private Double longitude;
    private Double latitude;
    private Integer storeId;

    public Restaurant() {
    }

    public Restaurant(RestaurantProperties properties, OrderClient orderClient) {
        this.properties = properties;
        this.orderClient = orderClient;
    }

    public void init(RestaurantProperties properties, OrderClient orderClient) {
        this.properties = properties;
        this.orderClient = orderClient;
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

    public void newOrder() {
        Order orderResponse = orderClient.create(new Order(Math.round(Math.random() * 100000000.0)));
        orderResponse = orderClient.assignOrder(orderResponse.getOrderId(), this.getStoreId());
        orderResponse = orderClient.prepareOrder(orderResponse.getOrderId());
        deliveryTime += (1 + ((Math.round(Math.random() * properties.getPreparationRate()))));
        orderCount++;
        OrderRequest<OrderDelivery> orderRequest = new OrderRequest<>(orderCount, deliveryTime,
                Resource.of(new OrderDelivery(orderCount, orderResponse)));
        log.info("[SCHEDULED]: " + this.toString() + ": " + orderRequest.toString());
        kitchen.schedule(orderRequest);
    }

    public void open() {
        this.close();

        scheduledExecutor.scheduleWithFixedDelay(() -> {
            if (!kitchen.isEmpty()) {
                ArrayList<ArrayList<OrderDelivery>> orderRequests = kitchen.deliver();
                if (orderRequests != null && orderRequests.stream().mapToLong(Collection::size).sum() > 0) {
                    orderRequests.stream().flatMap(Collection::stream).forEach(orderDelivery -> {
                        orderDelivery.setOrder(orderClient.orderReady(orderDelivery.getOrder().getOrderId()));
                    });
                    log.info("[DELIVERED]: " + this.toString() + ": " + Arrays.toString(orderRequests.stream()
                            .flatMap(Collection::stream)
                            .collect(Collectors.toList()).toArray(OrderDelivery[]::new)));
                }
            }
        }, properties.getPreparationTime(), properties.getPreparationTime(), TimeUnit.MILLISECONDS);

        this.setOrderScheduler(getScheduledExecutor().scheduleAtFixedRate(this::newOrder, properties.getNewOrderTime(),
                properties.getNewOrderTime(), TimeUnit.MILLISECONDS));
    }

    public void close() {
        if (orderScheduler != null) {
            orderScheduler.cancel(false);
        }
    }

    public static Restaurant from(RestaurantProperties config, OrderClient orderClient) {
        return new Restaurant(config, orderClient);
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
