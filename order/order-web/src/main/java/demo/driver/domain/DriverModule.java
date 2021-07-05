package demo.driver.domain;

import demo.domain.Module;
import demo.driver.event.DriverEvent;
import demo.event.EventService;

@org.springframework.stereotype.Service
public class DriverModule extends Module<Driver> {

    private final DriverService driverService;
    private final EventService<DriverEvent, Long> eventService;

    public DriverModule(DriverService driverService, EventService<DriverEvent, Long> eventService) {
        this.driverService = driverService;
        this.eventService = eventService;
    }

    public DriverService getDriverService() {
        return driverService;
    }

    public EventService<DriverEvent, Long> getEventService() {
        return eventService;
    }

    @Override
    public DriverService getDefaultService() {
        return driverService;
    }

    @Override
    public EventService<DriverEvent, Long> getDefaultEventService() {
        return eventService;
    }

}
