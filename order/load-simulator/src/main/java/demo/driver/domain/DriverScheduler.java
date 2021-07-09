package demo.driver.domain;

import scheduler.*;

import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Logger;

public class DriverScheduler extends Track<DriverEvent> {

    private final Logger log = Logger.getLogger(this.getClass().getName());

    public DriverScheduler() {
        super(new ExpandingResource<Cart<DriverEvent>, DriverEvent>(new Cart[]{}, Cart::new,
                new DriverRepository()));
    }

    public DriverWorkflow addToWorkflow(DriverWorkflow workflow, DriverActor driver,
                                          Consumer<ScheduledEvent<DriverEvent>> frame, DriverEventType type,
                                          Function<Driver, Driver> action) {
        Long eventId = (long) Math.abs(UUID.randomUUID().hashCode());
        return addToWorkflow(workflow, driver, eventId, frame, type, action);
    }

    public DriverWorkflow addToWorkflow(DriverWorkflow workflow, DriverActor driver, Long eventId,
                                        Consumer<ScheduledEvent<DriverEvent>> frame, DriverEventType type,
                                        Function<Driver, Driver> action) {
        DriverEvent driverEvent = new DriverEvent(eventId, driver.getDriver(), type, action);
        driverEvent.setDriverWorkflow(workflow);

        // Create a new scheduled event that executes an action at a specified key frame in the future
        ScheduledEvent<DriverEvent> scheduledEvent =
                new ScheduledEvent<>(eventId, Resource.of(driverEvent), frame);

        return workflow.addEvent(scheduledEvent);
    }

    public void schedule(DriverWorkflow workflow) {
        ScheduledEvent<DriverEvent> scheduledEvent = workflow.scheduleNext();
        log.info("[EVENT_SCHEDULED]: " + this.toString() + ": " + scheduledEvent.toString());
        this.schedule(scheduledEvent);
    }
}
