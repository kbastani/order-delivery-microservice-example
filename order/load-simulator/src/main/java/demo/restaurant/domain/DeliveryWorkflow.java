package demo.restaurant.domain;

import demo.order.domain.Order;
import scheduler.ScheduledEvent;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;
import java.util.function.Function;

public class DeliveryWorkflow {

    private DeliveryScheduler scheduler;
    private Order currentOrderState;
    private ConcurrentLinkedQueue<ScheduledEvent<DeliveryEvent>> workflowEvents = new ConcurrentLinkedQueue<>();
    private ScheduledEvent<DeliveryEvent> lastScheduledEvent;

    public static DeliveryWorkflow build(DeliveryScheduler scheduler) {
        DeliveryWorkflow workflow = new DeliveryWorkflow();
        workflow.setScheduler(scheduler);
        return workflow;
    }

    /**
     * Executes the workflow from the first event, and schedules subsequent events after each step is completed.
     */
    public void execute() {
        scheduler.schedule(this);
    }

    public DeliveryWorkflow addToWorkflow(Order order, Consumer<ScheduledEvent<DeliveryEvent>> frame,
                                          DeliveryEventType type,
                                          Function<Order, Order> action) {
        return scheduler.addToWorkflow(this, order, frame,type, action);
    }

    public DeliveryWorkflow addToWorkflow(Order order, Long eventId, Consumer<ScheduledEvent<DeliveryEvent>>  frame,
                                          DeliveryEventType type,
                                          Function<Order, Order> action) {
        return scheduler.addToWorkflow(this, order, eventId, frame,type, action);
    }

    public DeliveryWorkflow addEvent(ScheduledEvent<DeliveryEvent> event) {
        pushEvent(event);
        return this;
    }

    private void pushEvent(ScheduledEvent<DeliveryEvent> event) {
        workflowEvents.offer(event);
    }

    public ScheduledEvent<DeliveryEvent> scheduleLast() {
        ScheduledEvent<DeliveryEvent> scheduledEvent = lastScheduledEvent;
        if(scheduledEvent != null) {
            scheduledEvent.getUpdateDeliveryTime().accept(scheduledEvent);
            scheduler.schedule(scheduledEvent);
        }
        return scheduledEvent;
    }

    public ScheduledEvent<DeliveryEvent> scheduleNext() {
        ScheduledEvent<DeliveryEvent> scheduledEvent = workflowEvents.poll();
        lastScheduledEvent = scheduledEvent;
        if(scheduledEvent != null) {
            scheduledEvent.getUpdateDeliveryTime().accept(scheduledEvent);
            scheduler.schedule(scheduledEvent);
        }
        return scheduledEvent;
    }

    public Order getCurrentOrderState() {
        return currentOrderState;
    }

    public void setCurrentOrderState(Order currentOrderState) {
        this.currentOrderState = currentOrderState;
    }

    public void setScheduler(DeliveryScheduler scheduler) {
        this.scheduler = scheduler;
    }
}
