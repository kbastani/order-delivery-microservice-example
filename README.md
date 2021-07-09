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

***

![Driver Delivery Tracking with Kepler.gl](https://i.imgur.com/o0npUqx.gif)

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

## Build and run

JDK 16+ is required to build all the project artifacts for this example. Use the following terminal commands to build and launch a docker compose recipe for this example.

```bash
$ mvn clean verify
```

After you have succesfully built the project and docker containers, you can now run the example on a single machine in one of two modes.

The two recipes below for running this example on a single machine have very different system resource requirements. For most developers, it's recommended that you use the **light mode** recipe to get up and running without any performance issues. 

Before running either of the modes, make sure that you create the following Docker network using the following terminal command.

```bash
$ docker network create PinotNetwork
```

### Light mode

```bash
$ docker-compose -f docker-compose-light.yml up -d
$ docker-compose -f docker-compose-light.yml logs -f --tail 100 load-simulator
```
The `docker-compose-light.yml` is configured to use less containers and compute resources, but does not come with a Superset deployment that visualizes the CDC event data for order deliveries. To visualize the event data, you can use http://kepler.gl by exporting CSV datasets from queries executed in the Apache Pinot query console.

#### Usage

The current log output from your terminal should be targeted on the `load-simulator` application. By default, you will see a list of restaurants that are configured to start fulfilling order delivery requests. The load simulator is a high-throughput realistic state machine and conductor for driving the state of a restaurant, drivers, and order deliveries to a customer's location. Documentation on the load simulator and how it works will be made available in the future.

At the point where you begin to see a flurry of log output from the `load-simulator` that tracks the state of orders and their state change events, you'll know that your cluster is fully up and running. Before we can see any of the event data being produced by the `order-delivery-service` we need to configure a Debezium connector to start sending event table updates to an Apache Kafka topic. The following shell script will fully bootstrap your cluster to enable CDC outbox messages from MySQL to Kafka, as well as configure Apache Pinot to start consuming and ingesting those events for running real-time analytical queries.

```bash
$ sh ./bootstrap-light.sh
```

After this script finishes its tasks, you will now be able to use Apache Pinot to query the real-time stream of order delivery events that are generated from MySQL. A new browser window should be opened and navigated to http://localhost:9000.

To start querying data, navigate to the query console and click the `orders` table to execute your first query. If everythiing worked correctly, you should be seeing at least ten rows from the generated SQL query. Should you run into any issues, please create an issue here to get assistance.

#### Kepler.gl Visualizations

The SQL query below can be used to create a http://kepler.gl geospatial visualization using CSV export directly from the Pinot query console UI.

```sql
SELECT orderId as id, lat as point_latitude_2, lon as point_longitude_2, restaurantLat as point_latitude_1, restaurantLon as point_longitude_1, lastModified as start_time, status, restaurantId, accountId
FROM orders
WHERE ST_DISTANCE(location_st_point, ST_Point(-122.44469, 37.75680, 1)) < 6500
LIMIT 100000
option(skipUpsert=true)
```

Notice that in this SQL query I've disabled upserts using `skipUpsert=true`. This means that I want to see the full log of `order` events for each `orderId`. If I were to remove this option or set it to `false`, then I would only get back the most recent state of the `order` object with the primary key `orderId`. This is a very useful feature, as there are many types of analytical queries where we only want to see the current state of a single aggregate. For the purposes of a good geospatial visualization, we'll want to capture all of the geolocation updates as a driver navigates from a restaurant to a delivery location.

You can play around with this query to generate different result sets. In the `WHERE` clause, I've used a Pinot UDF that only fetches order delivery data that is within a 6.5km radius of the specified GPS coordinate. _The coordinate I've provided is located at the center of San Francisco._

### Heavy mode

Running the example in normal mode requires at least 16GB of system memory and it's recommended that your development machine have at least 32GB of memory and at least 12 CPU cores. Please use the light mode recipe above to run the example if your system doesn't meet these resource requirements. If you have previously started the **light mode** recipe, please make sure you destroy your cluster before proceeding.

```bash
$ docker-compose -f docker-compose-light.yml down
$ docker volume create --name=db_data
$ docker-compose -f docker-compose.yml up -d
$ docker-compose -f docker-compose.yml logs -f --tail 100 load-simulator
```

#### Usage

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
    -t debezium.Order
```

Registering the Debezium MySQL connector (this is configured in the `bootstrap.sh` script):

```
$ curl -i -X PUT -H "Accept:application/json" -H  "Content-Type:application/json" \
    http://localhost:8083/connectors/orderweb/config -d @debezium-mysql-connector-outbox.json
```

Getting status of "orderweb" connector:

```
$ curl -i -X GET -H "Accept:application/json" -H  "Content-Type:application/json" \
    http://localhost:8083/connectors/orderweb/status
```

## License

This project is an open source product licensed under Apache License v2.
