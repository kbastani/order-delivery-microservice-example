package demo.order.controller;

import demo.event.EventService;
import demo.event.Events;
import demo.order.domain.Order;
import demo.order.domain.OrderService;
import demo.order.domain.OrderStatus;
import demo.order.event.OrderEvent;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.server.EntityLinks;
import org.springframework.hateoas.server.LinkBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Method;
import java.util.Optional;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@RestController
@RequestMapping("/v1")
public class OrderController {

    private final OrderService orderService;
    private final EventService<OrderEvent, Long> eventService;
    private final EntityLinks entityLinks;

    public OrderController(OrderService orderService, EventService<OrderEvent, Long> eventService,
                           EntityLinks entityLinks) {
        this.orderService = orderService;
        this.eventService = eventService;
        this.entityLinks = entityLinks;
    }

    @PostMapping(path = "/orders")
    public ResponseEntity createOrder(@RequestBody Order order) {
        return Optional.ofNullable(createOrderResource(order))
                .map(e -> new ResponseEntity<>(e, HttpStatus.CREATED))
                .orElseThrow(() -> new RuntimeException("Order creation failed"));
    }

    @PutMapping(path = "/orders/{id}")
    public ResponseEntity updateOrder(@RequestBody Order order, @PathVariable Long id) {
        return Optional.ofNullable(updateOrderResource(id, order))
                .map(e -> new ResponseEntity<>(e, HttpStatus.OK))
                .orElseThrow(() -> new RuntimeException("Order update failed"));
    }

    @RequestMapping(path = "/orders/{id}")
    public ResponseEntity getOrder(@PathVariable Long id) {
        return Optional.ofNullable(getOrderResource(orderService.get(id)))
                .map(e -> new ResponseEntity<>(e, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @RequestMapping(path = "/orders/{id}/events")
    public ResponseEntity getOrderEvents(@PathVariable Long id) {
        return Optional.of(getOrderEventCollectionModel(id))
                .map(e -> new ResponseEntity<>(e, HttpStatus.OK))
                .orElseThrow(() -> new RuntimeException("Could not get order events"));
    }

    @RequestMapping(path = "/orders/{id}/events/{eventId}")
    public ResponseEntity getOrderEvent(@PathVariable Long id, @PathVariable Long eventId) {
        return Optional.of(getEventResource(eventId))
                .map(e -> new ResponseEntity<>(e, HttpStatus.OK))
                .orElseThrow(() -> new RuntimeException("Could not get order events"));
    }

    @PostMapping(path = "/orders/{id}/events")
    public ResponseEntity createOrder(@PathVariable Long id, @RequestBody OrderEvent event) {
        return Optional.ofNullable(appendEventResource(id, event))
                .map(e -> new ResponseEntity<>(e, HttpStatus.CREATED))
                .orElseThrow(() -> new RuntimeException("Append order event failed"));
    }

    @RequestMapping(path = "/orders/{id}/commands")
    public ResponseEntity getCommands(@PathVariable Long id) {
        return Optional.ofNullable(getCommandsCollectionModel(id))
                .map(e -> new ResponseEntity<>(e, HttpStatus.OK))
                .orElseThrow(() -> new RuntimeException("The order could not be found"));
    }

    @PostMapping(path = "/orders/{id}/commands/assignOrder")
    public ResponseEntity assignOrder(@PathVariable Long id, @RequestParam(value = "restaurantId") Long restaurantId) {
        return Optional.ofNullable(orderService.get(id)
                .assignOrder(restaurantId))
                .map(e -> new ResponseEntity<>(getOrderResource(e), HttpStatus.OK))
                .orElseThrow(() -> new RuntimeException("The command could not be applied"));
    }

    @PostMapping(path = "/orders/{id}/commands/updateOrderLocation")
    public ResponseEntity updateOrderLocation(@PathVariable Long id, @RequestParam(value = "lat") Double lat,
                                              @RequestParam(value = "lon") Double lon) {
        return Optional.ofNullable(orderService.get(id)
                .updateOrderLocation(lat, lon))
                .map(e -> new ResponseEntity<>(getOrderResource(e), HttpStatus.OK))
                .orElseThrow(() -> new RuntimeException("The command could not be applied"));
    }

    @PostMapping(path = "/orders/{id}/commands/prepareOrder")
    public ResponseEntity prepareOrder(@PathVariable Long id) {
        return Optional.ofNullable(orderService.get(id)
                .prepareOrder())
                .map(e -> new ResponseEntity<>(getOrderResource(e), HttpStatus.OK))
                .orElseThrow(() -> new RuntimeException("The command could not be applied"));
    }

    @PostMapping(path = "/orders/{id}/commands/orderReady")
    public ResponseEntity orderReady(@PathVariable Long id) {
        return Optional.ofNullable(orderService.get(id)
                .orderReady())
                .map(e -> new ResponseEntity<>(getOrderResource(e), HttpStatus.OK))
                .orElseThrow(() -> new RuntimeException("The command could not be applied"));
    }

    @PostMapping(path = "/orders/{id}/commands/orderPickedUp")
    public ResponseEntity orderPickedUp(@PathVariable Long id) {
        return Optional.ofNullable(orderService.get(id)
                .orderPickedUp())
                .map(e -> new ResponseEntity<>(getOrderResource(e), HttpStatus.OK))
                .orElseThrow(() -> new RuntimeException("The command could not be applied"));
    }

    @PostMapping(path = "/orders/{id}/commands/deliverOrder")
    public ResponseEntity deliverOrder(@PathVariable Long id) {
        return Optional.ofNullable(orderService.get(id)
                .deliverOrder())
                .map(e -> new ResponseEntity<>(getOrderResource(e), HttpStatus.OK))
                .orElseThrow(() -> new RuntimeException("The command could not be applied"));
    }

    @PostMapping(path = "/orders/{id}/commands/orderDelivered")
    public ResponseEntity orderDelivered(@PathVariable Long id) {
        return Optional.ofNullable(orderService.get(id).orderDelivered())
                .map(e -> new ResponseEntity<>(getOrderResource(e), HttpStatus.OK))
                .orElseThrow(() -> new RuntimeException("The command could not be applied"));
    }

    @PostMapping(path = "/order/{id}/commands/updateOrderStatus")
    public ResponseEntity updateOrderStatus(@PathVariable Long id, @RequestParam(value = "status") OrderStatus status) {
        return Optional.ofNullable(orderService.get(id).updateOrderStatus(status))
                .map(e -> new ResponseEntity<>(getOrderResource(e), HttpStatus.OK))
                .orElseThrow(() -> new RuntimeException("The command could not be applied"));
    }

    @RequestMapping(path = "/orders/search/findOrdersByAccountId")
    public ResponseEntity findOrdersByAccountId(@RequestParam("accountId") Long accountId) {
        return Optional.ofNullable(orderService.findOrdersByAccountId(accountId))
                .map(e -> new ResponseEntity<>(new CollectionModel<Order>(e), HttpStatus.OK))
                .orElseThrow(() -> new RuntimeException("The command could not be applied"));
    }

    /**
     * Creates a new {@link Order} entity and persists the result to the repository.
     *
     * @param order is the {@link Order} model used to create a new order
     * @return a hypermedia resource for the newly created {@link Order}
     */
    private EntityModel<Order> createOrderResource(Order order) {
        Assert.notNull(order, "Order body must not be null");

        // Create the new order
        order = orderService.registerOrder(order);

        return getOrderResource(order);
    }

    /**
     * Update a {@link Order} entity for the provided identifier.
     *
     * @param id    is the unique identifier for the {@link Order} update
     * @param order is the entity representation containing any updated {@link Order} fields
     * @return a hypermedia resource for the updated {@link Order}
     */
    private EntityModel<Order> updateOrderResource(Long id, Order order) {
        order.setIdentity(id);
        return getOrderResource(orderService.update(order));
    }

    /**
     * Appends an {@link OrderEvent} domain event to the event log of the {@link Order} aggregate with the
     * specified orderId.
     *
     * @param orderId is the unique identifier for the {@link Order}
     * @param event   is the {@link OrderEvent} that attempts to alter the state of the {@link Order}
     * @return a hypermedia resource for the newly appended {@link OrderEvent}
     */
    private EntityModel<OrderEvent> appendEventResource(Long orderId, OrderEvent event) {
        EntityModel<OrderEvent> eventResource = null;

        orderService.get(orderId)
                .sendAsyncEvent(event);

        if (event != null) {
            eventResource = EntityModel.of(event,
                    linkTo(OrderController.class)
                            .slash("orders")
                            .slash(orderId)
                            .slash("events")
                            .slash(event.getEventId())
                            .withSelfRel(),
                    linkTo(OrderController.class)
                            .slash("orders")
                            .slash(orderId)
                            .withRel("order")
            );
        }

        return eventResource;
    }

    private OrderEvent getEventResource(Long eventId) {
        return eventService.findOne(eventId);
    }

    private Events getOrderEventCollectionModel(Long id) {
        return eventService.find(id);
    }

    private LinkBuilder linkBuilder(String name, Long id) {
        Method method;

        try {
            method = OrderController.class.getMethod(name, Long.class);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }

        return linkTo(OrderController.class, method, id);
    }

    /**
     * Get a hypermedia enriched {@link Order} entity.
     *
     * @param order is the {@link Order} to enrich with hypermedia links
     * @return is a hypermedia enriched resource for the supplied {@link Order} entity
     */
    private EntityModel<Order> getOrderResource(Order order) {
        if (order == null) return null;

        if (!order.hasLink("commands")) {
            // Add command link
            order.add(linkBuilder("getCommands", order.getIdentity()).withRel("commands"));
        }

        if (!order.hasLink("events")) {
            // Add get events link
            order.add(linkBuilder("getOrderEvents", order.getIdentity()).withRel("events"));
        }

        if(!order.hasLink("self")) {
            // Add self link
            order.add(order.getId());
        }

        return EntityModel.of(order);
    }

    private RepresentationModel getCommandsCollectionModel(Long id) {
        Order order = new Order();
        order.setIdentity(id);
        return EntityModel.of(order.getCommands());
    }
}
