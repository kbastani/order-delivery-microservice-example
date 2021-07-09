package demo.driver.domain;

import scheduler.ScheduledEvent;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;
import java.util.function.Function;

public class DriverWorkflow {

    private DriverScheduler scheduler;
    private DriverActor currentDriverState;
    private ConcurrentLinkedQueue<ScheduledEvent<DriverEvent>> workflowEvents = new ConcurrentLinkedQueue<>();
    private ScheduledEvent<DriverEvent> lastScheduledEvent;

    public static DriverWorkflow build(DriverScheduler scheduler) {
        DriverWorkflow workflow = new DriverWorkflow();
        workflow.setScheduler(scheduler);
        return workflow;
    }

    /**
     * Executes the workflow from the first event, and schedules subsequent events after each step is completed.
     */
    public void execute() {
        scheduler.schedule(this);
    }

    public DriverWorkflow addToWorkflow(DriverActor driver, Consumer<ScheduledEvent<DriverEvent>> frame,
                                        DriverEventType type,
                                        Function<Driver, Driver> action) {
        return scheduler.addToWorkflow(this, driver, frame, type, action);
    }

    public DriverWorkflow addToWorkflow(DriverActor driver, Long eventId, Consumer<ScheduledEvent<DriverEvent>> frame,
                                        DriverEventType type,
                                        Function<Driver, Driver> action) {
        return scheduler.addToWorkflow(this, driver, eventId, frame, type, action);
    }

    public DriverWorkflow addEvent(ScheduledEvent<DriverEvent> event) {
        pushEvent(event);
        return this;
    }

    private void pushEvent(ScheduledEvent<DriverEvent> event) {
        workflowEvents.offer(event);
    }

    public ScheduledEvent<DriverEvent> scheduleLast() {
        ScheduledEvent<DriverEvent> scheduledEvent = lastScheduledEvent;
        if (scheduledEvent != null) {
            scheduledEvent.getUpdateDeliveryTime().accept(scheduledEvent);
            scheduler.schedule(scheduledEvent);
        }
        return scheduledEvent;
    }

    public ScheduledEvent<DriverEvent> scheduleNext() {
        ScheduledEvent<DriverEvent> scheduledEvent = workflowEvents.poll();
        lastScheduledEvent = scheduledEvent;
        if (scheduledEvent != null) {
            scheduledEvent.getUpdateDeliveryTime().accept(scheduledEvent);
            scheduler.schedule(scheduledEvent);
        }
        return scheduledEvent;
    }

    public DriverActor getCurrentDriverState() {
        return currentDriverState;
    }

    public void setCurrentDriverState(DriverActor currentDriverState) {
        this.currentDriverState = currentDriverState;
    }

    public void setScheduler(DriverScheduler scheduler) {
        this.scheduler = scheduler;
    }
}
