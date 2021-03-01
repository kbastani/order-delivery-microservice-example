# Order Microservice

This is the parent project that contains modules of a microservice deployment for the _Order_ domain context.

## Order Web

The `order-web` module is a web application that produces a REST API that can be used by consumers to interact with and manage domain objects in the `Order` context. _Domain Events_ can be triggered directly over HTTP, and will also be produced in the response to actions that alter the state of the `Order` object. This web service also provides built-in hypermedia support for looking up the event logs on an aggregate domain object.

## Usage

The `order-web` application provides a hypermedia-driven REST API for managing order deliveries.

To create a new order, we can send an HTTP POST request to `/v1/orders`.

```json
{
    "accountId": 1234
}
```

If the request was successful, a hypermedia response will be returned back.

```json
{
    "createdAt": 1614617770138,
    "lastModified": 1614617770138,
    "status": "ORDER_CREATED",
    "accountId": 1234,
    "_links": {
        "commands": {
            "href": "http://localhost:50014/v1/orders/1/commands"
        },
        "events": {
            "href": "http://localhost:50014/v1/orders/1/events"
        },
        "self": {
            "href": "http://localhost:50014/v1/orders/1"
        }
    },
    "orderId": 1
}
```

The snippet above is the response that was returned after creating the new order. We can see the field `status` has a value of `ORDER_CREATED`. Notice the embedded links in the response, which provide context for the resource. In this case, we have two links of interest: `events` and `commands`. Let's take a look at both of these resources, starting with `commands`.

The `commands` resource provides us with an additional set of hypermedia links for each command that can be applied to the `Order` resource.

_GET_ `/v1/orders/1/commands`

```json
{
    "_links": {
        "assignOrder": {
            "href": "http://localhost:50014/v1/orders/1/commands/assignOrder{?restaurantId}",
            "templated": true
        },
        "updateOrderLocation": {
            "href": "http://localhost:50014/v1/orders/1/commands/updateOrderLocation{?lat,lon}",
            "templated": true
        },
        "prepareOrder": {
            "href": "http://localhost:50014/v1/orders/1/commands/prepareOrder"
        },
        "orderReady": {
            "href": "http://localhost:50014/v1/orders/1/commands/orderReady"
        },
        "orderPickedUp": {
            "href": "http://localhost:50014/v1/orders/1/commands/orderPickedUp"
        },
        "deliverOrder": {
            "href": "http://localhost:50014/v1/orders/1/commands/deliverOrder"
        },
        "orderDelivered": {
            "href": "http://localhost:50014/v1/orders/1/commands/orderDelivered"
        },
        "updateOrderStatus": {
            "href": "http://localhost:50014/v1/order/1/commands/updateOrderStatus{?status}",
            "templated": true
        }
    }
}
```

Here we have a list of commands that can be applied to the `Order` resource. Not all commands will return back a successful result, since the `Order` resource has a state machine that defines what state this order can transition to and from. Right now, the valid next command is to assign the order to a restaurant. In the next API call, I will issue a `PUT` request to the listed command resource for `assignOrder`.

_PUT_ `/v1/orders/1/commands/assignOrder?restaurantId=101`

```json
{
    "createdAt": 1614617770138,
    "lastModified": 1614618409141,
    "status": "ORDER_ASSIGNED",
    "accountId": 1,
    "restaurantId": 101,
    "_links": {
        "commands": {
            "href": "http://localhost:50014/v1/orders/1/commands"
        },
        "events": {
            "href": "http://localhost:50014/v1/orders/1/events"
        },
        "self": {
            "href": "http://localhost:50014/v1/orders/1"
        }
    },
    "orderId": 1
}
```

In an event-driven architecture, the state of an object can only change in response to an event, which is usually triggered by a command. Events can be generated in multiple different contexts within the domain. The generation of an event might be in response to another event. While there is no single best practice, it's important to look at an HTTP resource, such as `Order` as a command-driven API. Commands mutate the state of a domain object, and generate projected views that can be queried. There is some confusion as to what a command is in relation to an event. 

Commands are best looked at as actions that generate an event. Events are best looked at as a record of a mutation in state. If a command mutates the state of a domain entity, an event becomes generated that describes the mutated state of the domain. Subscribers to a domain entity can monitor for such an event, and automatically apply another command to the domain as a result, potentially generating another event.

Now, let's go back to the parent `Order` resource to see the state of our domain entity.

_GET_ `/v1/orders/1`

```json
{
    "createdAt": 1614617770138,
    "lastModified": 1614618409141,
    "status": "ORDER_ASSIGNED",
    "accountId": 1,
    "restaurantId": 101,
    "_links": {
        "commands": {
            "href": "http://localhost:50014/v1/orders/1/commands"
        },
        "events": {
            "href": "http://localhost:50014/v1/orders/1/events"
        },
        "self": {
            "href": "http://localhost:50014/v1/orders/1"
        }
    },
    "orderId": 1
}
```

Sending a new _GET_ request to the parent `Order` resource returns the object with an updated `status` value, as well as a new field and value for `restaurantId`. The status value is now `ORDER_ASSIGNED`, which previously was `ORDER_CREATED`.

To understand exactly what has happened to this `Order` resource, we can trace the events that led to its current state. Sending a GET request to the attached hypermedia link named `events` returns the object's event log.

_GET_ `/v1/orders/1/events`

```json
{
    "_embedded": {
        "orderEventList": [
            {
                "eventId": 2,
                "orderId": 1,
                "type": "ORDER_CREATED",
                "orderStatus": "ORDER_CREATED",
                "createdAt": 1614617770227,
                "lastModified": 1614617770227,
                "_links": {
                    "self": {
                        "href": "http://localhost:50014/v1/orders/1/events/2"
                    },
                    "order": {
                        "href": "http://localhost:50014/v1/orders/1"
                    }
                }
            },
            {
                "eventId": 3,
                "orderId": 1,
                "type": "ORDER_ASSIGNED",
                "orderStatus": "ORDER_ASSIGNED",
                "restaurantId": 101,
                "createdAt": 1614618409180,
                "lastModified": 1614618409180,
                "_links": {
                    "self": {
                        "href": "http://localhost:50014/v1/orders/1/events/3"
                    },
                    "order": {
                        "href": "http://localhost:50014/v1/orders/1"
                    }
                }
            }
        ]
    }
}
```

The response returned is an ordered collection of `Order` events. The log describes the events that caused the `status` of the `Order` resource to be changed.


By using hypermedia to drive the state of domain resources, a subscriber of a domain event only needs to know the structure of how domain resources connect by hypermedia links.
