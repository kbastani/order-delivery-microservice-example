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

## Build

Use the following terminal commands to build and launch the docker compose recipe for this example.

```bash
$ mvn clean verify -DskipTests=true
$ docker-compose up -d
$ docker-compose logs -f --tail 100
```

### Single machine

This example is a real multi-container production-ready application designed for real-time analytics at Uber's scale. For this reason, evaluating the example on a single machine requires at least _12 GB of available system memory_. Attempting to run this example using anything less than the recommended amount of system memory may cause your machine to run out of memory or result in a non-functional demo.

## Usage

After building and launching the docker compose recipe, you'll be able to launch a real-time dashboard of a simulated order delivery scenario using Superset.

```bash
$ open http://localhost:8088
```

Sign-in to the superset web interface using the credentials *admin/admin*. Navigate to the order delivery dashboard. To see order delivery data after first launching the simulation, you should remove the default filter for order status by removing it. This will show you all the orders with their status in real-time as they change. Also, you can set the refresh interval on the dashboard to *10s*, which is done through a configuration button at the top right of the dashboard page.

## Change Data Capture

This section provides you with a collection of useful commands for interacting and exploring the CDC features of this example application that are implemented with Debezium.

### Useful commands

Getting a shell in MySQL:

```
$ docker run --tty --rm -i \
    --network PinotNetwork \
    debezium/tooling:1.1 \
    bash -c 'mycli mysql://mysqluser@mysql:3306/orderweb --password mysqlpw'
```

Listing all topics in Kafka:

```
$ docker-compose exec kafka /kafka/bin/kafka-topics.sh --zookeeper zookeeper:2181 --list
```

Reading contents of the "order" topic:

```
$ docker run --tty --rm \
    --network PinotNetwork \
    debezium/tooling:1.1 \
    kafkacat -b kafka:9092 -C -o beginning -q \
    -t order
```

Registering the Debezium MySQL connector:

```
$ curl -i -X PUT -H "Accept:application/json" -H  "Content-Type:application/json" \
    http://localhost:8083/connectors/orderweb/config -d @debezium-mysql-connector.json
```

Getting status of "orderweb" connector:

```
$ curl -i -X GET -H "Accept:application/json" -H  "Content-Type:application/json" \
    http://localhost:8083/connectors/orderweb/status
```

## License

This project is an open source product licensed under Apache License v2.
