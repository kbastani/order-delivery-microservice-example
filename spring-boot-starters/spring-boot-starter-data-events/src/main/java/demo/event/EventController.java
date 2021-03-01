package demo.event;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.Serializable;
import java.util.Optional;

/**
 * The default controller for managing {@link Event} entities.
 *
 * @author Kenny Bastani
 */
public class EventController<T extends Event, ID extends Serializable> {

    private final EventService<T, Long> eventService;

    public EventController(EventService<T, Long> eventService) {
        this.eventService = eventService;
    }

    @PostMapping(path = "/events/{id}")
    public ResponseEntity createEvent(@RequestBody T event, @PathVariable Long id) {
        return Optional.ofNullable(eventService.save(id, event))
                .map(e -> new ResponseEntity<>(e, HttpStatus.CREATED))
                .orElseThrow(() -> new RuntimeException("Event creation failed"));
    }

    @PutMapping(path = "/events/{id}")
    public ResponseEntity updateEvent(@RequestBody T event, @PathVariable Long id) {
        return Optional.ofNullable(eventService.save(id, event))
                .map(e -> new ResponseEntity<>(e, HttpStatus.OK))
                .orElseThrow(() -> new RuntimeException("Event update failed"));
    }

    @GetMapping(path = "/events/{id}")
    public ResponseEntity getEvent(@PathVariable Long id) {
        return Optional.ofNullable(eventService.findOne(id))
                .map(e -> new ResponseEntity<>(e, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
}
