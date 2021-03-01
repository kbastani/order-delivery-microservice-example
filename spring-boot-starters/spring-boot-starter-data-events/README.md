# Spring Boot Starter Data Events

This starter project provides auto-configuration support classes for building event-driven Spring Data applications.

* Uses a familiar _Spring Data_ repository pattern for creating an `EventRepository<T, ID>`
* The `EventRepository` provides trait specific features for managing an event log that is attached to an existing domain entity
* Provides a set of event abstractions that can be extended to use any Spring Data repository (JPA, Mongo, Neo4j, Redis..)
* Provides an `EventService` bean that can be used to publish events to a _Spring Cloud Stream_ output channel

## Usage

In your Spring Boot project, add the starter project dependency to your class path. For Maven, add the following dependency to your `pom.xml`.

```xml
<dependencies>
    <dependency>
        <groupId>org.kbastani</groupId>
        <artifactId>spring-boot-starter-data-events</artifactId>
        <version>1.0-SNAPSHOT</version>
    </dependency>
    ...
</dependencies>
```

Next, configure your _Spring Cloud Stream_ output bindings. Add the following snippet to the `application.properties|yaml` file of your Spring Boot application. Replace the destination value with the name of your message channel for the event stream.

```yaml
spring:
  cloud:
    stream:
      bindings:
        output:
          destination: payment
```

Next, you'll need to create a custom `Event` entity. The snippet below extends the provided `Event<T, E, ID>` interface. This example uses Spring Data JPA, but you can use any Spring Data project for implementing your event entities.

```java
@Entity
@EntityListeners(AuditingEntityListener.class)
public class PaymentEvent extends Event<Payment, PaymentEventType, Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long eventId;

    @Enumerated(EnumType.STRING)
    private PaymentEventType type;

    @OneToOne(cascade = CascadeType.MERGE, fetch = FetchType.LAZY)
    @JsonIgnore
    private Payment entity;

    @CreatedDate
    private Long createdAt;

    @LastModifiedDate
    private Long lastModified;
 
    ...
}
```

To start managing events you'll need to extend the `EventRepository<T, ID>` interface. The `PaymentEvent` is the JPA entity we defined in the last snippet.

```java
public interface PaymentEventRepository extends EventRepository<PaymentEvent, Long> {
}
```

That's it! You're ready to start sending domain events to the stream binding's output channel using the auto-configured `EventService`. The example snippet below shows how to create and append a new `PaymentEvent` to a `Payment` entity before publishing the event over AMQP to the configured event stream's output channel.

```java
@Service
public class PaymentService {
    private final EventService<PaymentEvent, Long> eventService;
    
    public PaymentController(EventService<PaymentEvent, Long> eventService) {
        this.eventService = eventService;
    }
    
    public PaymentEvent appendCreateEvent(Payment payment) {
        PaymentEvent paymentEvent = new PaymentEvent(PaymentEventType.PAYMENT_CREATED);
        paymentEvent.setEntity(payment);
        paymentEvent = eventService.save(event);
        
        // Send the event to the Spring Cloud stream binding
        eventService.sendAsync(paymentEvent);
    }
    
    ...
}
    
```

A default `EventController` is also provided with the starter project. The `EventController` provides a basic REST API with hypermedia resource support for managing the `Event` log of a domain entity over HTTP. The following cURL snippet gets the `PaymentEvent` we created in the last example from the `EventController`.

```bash
curl -X GET "http://localhost:8082/v1/events/1"
```

Response:

```json
{
  "eventId": 1,
  "type": "PAYMENT_CREATED",
  "createdAt": 1482749707006,
  "lastModified": 1482749707006,
  "_links": {
    "self": {
      "href": "http://localhost:8082/v1/events/1"
    },
    "payment": {
      "href": "http://localhost:8082/v1/payments/1"
    }
  }
}
```

In the snippet above we can see the `EventController` responded with a `hal+json` formatted resource. Since the `PaymentEvent` has a reference to the `Payment` entity, we see a _payment_ link is available to fetch the related resource.