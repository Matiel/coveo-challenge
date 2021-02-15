# Coding challenge

Streaming coding challenge for the Coveo interview.

The application is made of three parts.

1. Producer  
The producer reads the data from the csv file, removes the username and userId for
privacy reasons and sends it to Kafka.

2. Consumer  
The consumer reads the data in Kafka and writes saves it grouped by event-type. It
also stores some information that are useful to compute statistics.

3. Display statistics  
This part of the application uses data saved by the consumer to display useful stats
to the end user.

## Setup

1. Make sure docker is installed and running.
   Then, run
    ```
    docker-compose up
    ```

2. Install [lein](https://leiningen.org/)
3. ```lein deps```

## Usage

Start the producer and then the consumer.
You can manually view the statistics generated in this folder `./resources/output`

### Producer
```lein run producer```

### Consumer
```lein run consumer```

### Display stats
```lein run stats```
