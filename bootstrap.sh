#!/bin/bash

# Increase the max_connections to 10,000 for the MySQL database instance
docker-compose exec mysql bash -c 'mysql -u root -p$MYSQL_ROOT_PASSWORD orderweb -e "SET GLOBAL max_connections = 10000;"'

# Copy the Pinot table and schema configurations to Pinot container and execute the add table command
docker cp pinot/order-delivery-table-definition.json order-delivery-microservice-example_pinot_1:/opt/pinot
docker cp pinot/order-delivery-schema-definition.json order-delivery-microservice-example_pinot_1:/opt/pinot
docker exec order-delivery-microservice-example_pinot_1 bash -c "/opt/pinot/bin/pinot-admin.sh AddTable -tableConfigFile /opt/pinot/order-delivery-table-definition.json -schemaFile /opt/pinot/order-delivery-schema-definition.json -exec"

# Initializes the Superset application and imports datasources and dashboards
docker exec -ti -u root superset_app bash -c "sh ./docker-init.sh"

# Open the browser for Pinot
open http://localhost:9000
