#!/bin/bash

# Increase the max_connections to 10,000 for the MySQL database instance
docker-compose exec mysql bash -c 'mysql -u root -p$MYSQL_ROOT_PASSWORD orderweb -e "SET GLOBAL max_connections = 10000;"'

curl -i -X PUT -H "Accept:application/json" -H  "Content-Type:application/json" \
    http://localhost:8083/connectors/order/config -d @debezium-mysql-connector-order-outbox.json

curl -i -X PUT -H "Accept:application/json" -H  "Content-Type:application/json" \
    http://localhost:8083/connectors/driver/config -d @debezium-mysql-connector-driver-outbox.json

# Copy the Pinot table and schema configurations to Pinot container and execute the add table command
docker cp pinot/order-table-definition.json order-delivery-microservice-example_pinot_1:/opt/pinot
docker cp pinot/order-schema-definition-cdc.json order-delivery-microservice-example_pinot_1:/opt/pinot
docker cp pinot/driver-table-definition.json order-delivery-microservice-example_pinot_1:/opt/pinot
docker cp pinot/driver-schema-definition-cdc.json order-delivery-microservice-example_pinot_1:/opt/pinot
docker exec order-delivery-microservice-example_pinot_1 bash -c "/opt/pinot/bin/pinot-admin.sh AddTable -tableConfigFile /opt/pinot/order-table-definition.json -schemaFile /opt/pinot/order-schema-definition-cdc.json -exec"
docker exec order-delivery-microservice-example_pinot_1 bash -c "/opt/pinot/bin/pinot-admin.sh AddTable -tableConfigFile /opt/pinot/driver-table-definition.json -schemaFile /opt/pinot/driver-schema-definition-cdc.json -exec"

# Open the browser for Pinot
open http://localhost:9000
