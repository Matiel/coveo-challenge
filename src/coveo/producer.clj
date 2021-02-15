(ns coveo.producer
  (:require [clojure.tools.logging :as log])
  (:import (org.apache.kafka.common.serialization ByteArraySerializer)
           (org.apache.kafka.clients.producer KafkaProducer ProducerConfig ProducerRecord)
           (com.fasterxml.jackson.databind JsonNode ObjectMapper)
           (com.fasterxml.jackson.core JsonProcessingException)))

(def ^:private producer-config
  {ProducerConfig/BOOTSTRAP_SERVERS_CONFIG "localhost:29092"})

(defn create-producer []
  (KafkaProducer. producer-config
                  (ByteArraySerializer.)
                  (ByteArraySerializer.)))

(defn- serialize [^JsonNode data]
  (try
    (.writeValueAsBytes (ObjectMapper.) data)
    (catch JsonProcessingException _
      (log/error "Unable to serialize data"))))

(defn- produce
  [producer topic key value]
  (try
    (.send producer (ProducerRecord. topic (serialize key) (serialize value)))
    (catch Exception _
      (log/error "Unable to produce data: " key value))))

(defn produce-event
  [producer topic data-key data]
  (->> data
       (map #(produce producer topic (data-key %) %))
       doall))
