package demo.restaurant.domain;

import demo.order.domain.Order;
import scheduler.*;

import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Logger;

public class DeliveryScheduler extends Track<DeliveryEvent> {

    private final Logger log = Logger.getLogger(this.getClass().getName());

    public DeliveryScheduler() {
        super(new ExpandingResource<Cart<DeliveryEvent>, DeliveryEvent>(new Cart[]{}, Cart::new,
                new DeliveryRepository()));
    }

    public DeliveryWorkflow addToWorkflow(DeliveryWorkflow workflow, Order order,
                                          Consumer<ScheduledEvent<DeliveryEvent>> frame, DeliveryEventType type,
                                          Function<Order, Order> action) {
        Long eventId = (long) Math.abs(UUID.randomUUID().hashCode());
        return addToWorkflow(workflow, order, eventId, frame, type, action);
    }

    public DeliveryWorkflow addToWorkflow(DeliveryWorkflow workflow, Order order, Long eventId,
                                          Consumer<ScheduledEvent<DeliveryEvent>> frame, DeliveryEventType type,
                                          Function<Order, Order> action) {
        DeliveryEvent deliveryEvent = new DeliveryEvent(eventId, order, type, action);
        deliveryEvent.setDeliveryWorkflow(workflow);

        // Create a new scheduled event that executes an action at a specified key frame in the future
        ScheduledEvent<DeliveryEvent> scheduledEvent =
                new ScheduledEvent<>(eventId, Resource.of(deliveryEvent), frame);

        return workflow.addEvent(scheduledEvent);
    }

    public void schedule(DeliveryWorkflow workflow) {
        ScheduledEvent<DeliveryEvent> scheduledEvent = workflow.scheduleNext();
        log.info("[EVENT_SCHEDULED]: " + this.toString() + ": " + scheduledEvent.toString());
        this.schedule(scheduledEvent);
    }
}
