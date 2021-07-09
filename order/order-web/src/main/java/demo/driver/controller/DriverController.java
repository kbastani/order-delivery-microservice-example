package demo.driver.controller;

import demo.domain.BadRequestException;
import demo.driver.domain.Driver;
import demo.driver.domain.DriverOrderRequest;
import demo.driver.domain.DriverService;
import demo.driver.domain.DriverStatus;
import demo.driver.event.DriverEvent;
import demo.driver.event.DriverEventService;
import demo.event.Events;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.LinkBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.lang.reflect.Method;
import java.util.Optional;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@RestController
@RequestMapping("/v1")
public class DriverController {

    private final DriverService driverService;
    private final DriverEventService eventService;

    public DriverController(DriverService driverService, DriverEventService eventService) {
        this.driverService = driverService;
        this.eventService = eventService;
    }


    @PostMapping(path = "/drivers")
    public Mono<ResponseEntity<EntityModel<Driver>>> createDriver(@RequestBody Driver driver) {
        return Optional.ofNullable(createDriverResource(driver))
                .map(e -> Mono.just(new ResponseEntity<>(e, HttpStatus.CREATED)))
                .orElseThrow(() -> new BadRequestException(HttpStatus.BAD_REQUEST, "Driver creation failed"));
    }


    @PutMapping(path = "/drivers/{id}")
    public Mono<ResponseEntity<EntityModel<Driver>>> updateDriver(@RequestBody Driver driver, @PathVariable Long id) {
        return Optional.ofNullable(updateDriverResource(id, driver))
                .map(e -> Mono.just(new ResponseEntity<>(e, HttpStatus.OK)))
                .orElseThrow(() -> new BadRequestException(HttpStatus.BAD_REQUEST, "Driver update failed"));
    }


    @RequestMapping(path = "/drivers/{id}")
    public Mono<ResponseEntity<EntityModel<Driver>>> getDriver(@PathVariable Long id) {
        return Optional.ofNullable(getDriverResource(driverService.get(id)))
                .map(e -> Mono.just(new ResponseEntity<>(e, HttpStatus.OK)))
                .orElseThrow();
    }


    @RequestMapping(path = "/drivers/{id}/events")
    public Mono<ResponseEntity<Events>> getDriverEvents(@PathVariable Long id) {
        return Optional.of(getDriverEventCollectionModel(id))
                .map(e -> Mono.just(new ResponseEntity<>(e, HttpStatus.OK)))
                .orElseThrow(() -> new BadRequestException(HttpStatus.BAD_REQUEST, "Could not get driver events"));
    }


    @RequestMapping(path = "/drivers/{id}/events/{eventId}")
    public Mono<ResponseEntity<DriverEvent>> getDriverEvent(@PathVariable Long id, @PathVariable Long eventId) {
        return Optional.of(getEventResource(eventId))
                .map(e -> Mono.just(new ResponseEntity<>(e, HttpStatus.OK)))
                .orElseThrow(() -> new BadRequestException(HttpStatus.BAD_REQUEST, "Could not get driver events"));
    }


    @PostMapping(path = "/drivers/{id}/events")
    public Mono<ResponseEntity<EntityModel<DriverEvent>>> createDriver(@PathVariable Long id, @RequestBody DriverEvent event) {
        return Optional.ofNullable(appendEventResource(id, event))
                .map(e -> Mono.just(new ResponseEntity<>(e, HttpStatus.CREATED)))
                .orElseThrow(() -> new BadRequestException(HttpStatus.BAD_REQUEST, "Append driver event failed"));
    }


    @RequestMapping(path = "/drivers/{id}/commands")
    public Mono<ResponseEntity<RepresentationModel>> getCommands(@PathVariable Long id) {
        return Optional.ofNullable(getCommandsCollectionModel(id))
                .map(e -> Mono.just(new ResponseEntity<>(e, HttpStatus.OK)))
                .orElseThrow(() -> new BadRequestException(HttpStatus.BAD_REQUEST, "The driver could not be found"));
    }

    @PostMapping(path = "/drivers/{id}/commands/updateDriverLocation")
    public Mono<ResponseEntity<EntityModel<Driver>>> updateDriverLocation(@PathVariable Long id, @RequestParam(value = "lat") Double lat,
                                                                        @RequestParam(value = "lon") Double lon) {
        return Optional.ofNullable(driverService.get(id)
                .updateDriverLocation(lat, lon))
                .map(e -> Mono.just(new ResponseEntity<>(getDriverResource(e), HttpStatus.OK)))
                .orElseThrow(() -> new BadRequestException(HttpStatus.BAD_REQUEST, "The command could not be applied"));
    }

    @PostMapping(path = "/drivers/{id}/commands/updateDriverStatus")
    public Mono<ResponseEntity<EntityModel<Driver>>> updateDriverStatus(@PathVariable Long id, @RequestParam(value = "status") DriverStatus status) {
        return Optional.ofNullable(driverService.get(id).updateDriverStatus(status))
                .map(e -> Mono.just(new ResponseEntity<>(getDriverResource(e), HttpStatus.OK)))
                .orElseThrow(() -> new BadRequestException(HttpStatus.BAD_REQUEST, "The command could not be applied"));
    }

    @PostMapping(path = "/drivers/{id}/commands/activateAccount")
    public Mono<ResponseEntity<EntityModel<Driver>>> activateAccount(@PathVariable Long id) {
        return Optional.ofNullable(driverService.get(id).activateAccount())
                .map(e -> Mono.just(new ResponseEntity<>(getDriverResource(e), HttpStatus.OK)))
                .orElseThrow(() -> new BadRequestException(HttpStatus.BAD_REQUEST, "The command could not be applied"));
    }

    @PostMapping(path = "/drivers/{id}/commands/driverOnline")
    public Mono<ResponseEntity<EntityModel<Driver>>> driverOnline(@PathVariable Long id) {
        return Optional.ofNullable(driverService.get(id).driverOnline())
                .map(e -> Mono.just(new ResponseEntity<>(getDriverResource(e), HttpStatus.OK)))
                .orElseThrow(() -> new BadRequestException(HttpStatus.BAD_REQUEST, "The command could not be applied"));
    }

    @PostMapping(path = "/drivers/{id}/commands/driverOffline")
    public Mono<ResponseEntity<EntityModel<Driver>>> driverOffline(@PathVariable Long id) {
        return Optional.ofNullable(driverService.get(id).driverOffline())
                .map(e -> Mono.just(new ResponseEntity<>(getDriverResource(e), HttpStatus.OK)))
                .orElseThrow(() -> new BadRequestException(HttpStatus.BAD_REQUEST, "The command could not be applied"));
    }

    @RequestMapping(path = "/drivers/{id}/commands/fetchOrderRequest")
    public Mono<ResponseEntity<DriverOrderRequest>> fetchOrderRequest(@PathVariable Long id) {
        return Optional.ofNullable(driverService.get(id).fetchOrderRequest())
                .map(e -> Mono.just(new ResponseEntity<>(e, HttpStatus.OK)))
                .orElse(Mono.just(new ResponseEntity<>(null, HttpStatus.OK)));
    }

    /**
     * Creates a new {@link Driver} entity and persists the result to the repository.
     *
     * @param driver is the {@link Driver} model used to create a new driver
     * @return a hypermedia resource for the newly created {@link Driver}
     */
    private EntityModel<Driver> createDriverResource(Driver driver) {
        Assert.notNull(driver, "Driver body must not be null");

        // Create the new driver
        driver = driverService.registerDriver(driver);

        return getDriverResource(driver);
    }

    /**
     * Update a {@link Driver} entity for the provided identifier.
     *
     * @param id    is the unique identifier for the {@link Driver} update
     * @param driver is the entity representation containing any updated {@link Driver} fields
     * @return a hypermedia resource for the updated {@link Driver}
     */
    private EntityModel<Driver> updateDriverResource(Long id, Driver driver) {
        driver.setIdentity(id);
        return getDriverResource(driverService.update(driver));
    }

    /**
     * Appends an {@link DriverEvent} domain event to the event log of the {@link Driver} aggregate with the
     * specified driverId.
     *
     * @param driverId is the unique identifier for the {@link Driver}
     * @param event   is the {@link DriverEvent} that attempts to alter the state of the {@link Driver}
     * @return a hypermedia resource for the newly appended {@link DriverEvent}
     */
    private EntityModel<DriverEvent> appendEventResource(Long driverId, DriverEvent event) {
        EntityModel<DriverEvent> eventResource = null;

        driverService.get(driverId).appendEvent(event);

        if (event != null) {
            eventResource = EntityModel.of(event,
                    linkTo(DriverController.class)
                            .slash("drivers")
                            .slash(driverId)
                            .slash("events")
                            .slash(event.getEventId())
                            .withSelfRel(),
                    linkTo(DriverController.class)
                            .slash("drivers")
                            .slash(driverId)
                            .withRel("driver")
            );
        }

        return eventResource;
    }

    private DriverEvent getEventResource(Long eventId) {
        return eventService.findOne(eventId);
    }

    private Events getDriverEventCollectionModel(Long id) {
        return eventService.find(id);
    }

    private LinkBuilder linkBuilder(String name, Long id) {
        Method method;

        try {
            method = DriverController.class.getMethod(name, Long.class);
        } catch (NoSuchMethodException e) {
            throw new BadRequestException(HttpStatus.BAD_REQUEST, e.getMessage());
        }

        return linkTo(DriverController.class, method, id);
    }

    /**
     * Get a hypermedia enriched {@link Driver} entity.
     *
     * @param driver is the {@link Driver} to enrich with hypermedia links
     * @return is a hypermedia enriched resource for the supplied {@link Driver} entity
     */
    private EntityModel<Driver> getDriverResource(Driver driver) {
        if (driver == null) return null;

        if (!driver.hasLink("commands")) {
            // Add command link
            driver.add(linkBuilder("getCommands", driver.getIdentity()).withRel("commands"));
        }

        if (!driver.hasLink("events")) {
            // Add get events link
            driver.add(linkBuilder("getDriverEvents", driver.getIdentity()).withRel("events"));
        }

        if (!driver.hasLink("self")) {
            // Add self link
            driver.add(driver.getId());
        }

        return EntityModel.of(driver);
    }

    private RepresentationModel getCommandsCollectionModel(Long id) {
        Driver driver = new Driver();
        driver.setIdentity(id);
        return EntityModel.of(driver.getCommands());
    }
}
