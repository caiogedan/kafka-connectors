# Running in development

```
mvn clean package
export CLASSPATH="$(find target/ -type f -name '*.jar'| grep '\-package' | tr '\n' ':')"
$CONFLUENT_HOME/bin/connect-standalone $CONFLUENT_HOME/etc/schema-registry/connect-avro-standalone.properties config/MySourceConnector.properties
```

# Running in Docker Container

```
mvn clean package
Copy *-fat.jar to connectors folder, restart connector service.
scp target/...-fat.jar ${USER}@<IP HOST>:/<Connectors Path>
sudo docker-composer stop <container name>
sudo docker-compose up <container name> 

Obs: Start the container with (up) to see the log. 

```

# Creating a new Connector
```
- To verify if the plugin has been created. 
curl http://localhost:8083/connector-plugins | jq

- To Create a new connector

curl -iX POST http://localhost:8083/connectors/ \
  -H 'Content-Type: application/json' \
  -H 'Accept: application/json' \
  -d '{
    "name": "<connector_name>",
    "config": {
		"tasks.max": "1",
       "connector.class": "Sink/Source Class",
       .
       .
       .
       <Others configurations>
		}
    }'
    
```

# Updating a Connector
```
curl -iX PUT http://localhost:8083/connectors/<connector_name>/config \
  -H 'Content-Type: application/json' \
  -H 'Accept: application/json' \
  -d '{
		"tasks.max": "1",
       "connector.class": "Sink/Source Class",
       .
       .
       .
       <Others configurations>
    }'
```

# Kafka Connect General Commands
```
1 - To list connectors:
curl http://localhost:8083/connectors | jq

2 - To show a connector status:
curl http://localhost:8083/connectors/<connector_name>/status | jq

3 - To show a connector tasks:
curl http://localhost:8083/connectors/<connector_name>/tasks | jq

4 - To restart a connector: 
curl -iX POST http://localhost:8083/connectors/<connector_name>/restart

5 - To pause a Connector 
curl -iX PUT http://localhost:8083/connectors/<connector_name>/pause

6 - To resume a paused Connector
curl -iX PUT http://localhost:8083/connectors/<connector_name>/resume

7 - to show a Connector Config
curl http://localhost:8083/connectors/<connector_name>/config | jq

8 - To DELETE a Connector 
curl -iX DELETE http://localhost:8083/connectors/<connector_name>
```
