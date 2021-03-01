# Order Delivery Microservice Example

In an event-driven microservices architecture, the concept of a domain event is central to the behavior of each service. Popular practices such as _CQRS_ (Command Query Responsibility Segregation) in combination with _Event Sourcing_ are becoming more common in applications as microservice architectures continue to rise in popularity.

This reference architecture and sample project demonstrates an event-driven microservice architecture that use Spring Boot and Spring Cloud.

Demonstrated concepts:

- Event Sourcing
- Event Stream Processing
- Change Data Capture (CDC)
- Change Data Analytics
- Hypermedia Event Logs
- Real-time Analytics Dashboards

## Use cases

This application is a work in progress. The full list of initial requirements are listed below. This application is intended to show a modern microservice architecture that requires real-time analytics and change data capture.

### Order Service

API usage information for the `order-web` service can be found [here](order/README.md). 

- Includes an order web service that tracks new order deliveries.
- Includes a load simulator that realistically simulates a fleet of drivers delivering restaurant orders to customers.
- Uses a list of real Starbucks restaurants to simulate order life cycles across all locations in the United States.
- Generates fake delivery locations within 30 miles (ca. 48 km) of each Starbucks.
- Generates realistic delivery scenarios and simulates supply/demand based on pre-seeded variables for restaurant locations.
- Generates semi-realistic geospatial updates that tracks the location of an order as it makes its way to a customer’s delivery location.
- Simulates driver availability based on location and distance from a restaurant location.

### Dashboards

- Real-time geospatial dashboard of current deliveries
  - Show current deliveries by restaurant id
  - Show current deliveries by restaurant city

# License

This project is an open source product licensed under Apache License v2.
