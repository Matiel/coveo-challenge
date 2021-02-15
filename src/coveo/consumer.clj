(ns coveo.consumer
  (:require [clojure.java.io :as io])
  (:import (org.apache.kafka.clients.consumer KafkaConsumer ConsumerConfig ConsumerRecords$ConcatenatedIterable ConsumerRecord)
           (org.apache.kafka.common.serialization ByteArrayDeserializer)
           (java.time Duration)
           (java.util UUID)
           (com.fasterxml.jackson.databind ObjectMapper)))

(def ^:private consumer-config
  {ConsumerConfig/BOOTSTRAP_SERVERS_CONFIG "localhost:29092"
   ConsumerConfig/GROUP_ID_CONFIG (str (UUID/randomUUID))
   ConsumerConfig/AUTO_OFFSET_RESET_CONFIG "earliest"})

(def click-atom (atom {}))
(def click-cause-atom (atom {}))

(defn create-consumer []
  (KafkaConsumer. consumer-config
                  (ByteArrayDeserializer.)
                  (ByteArrayDeserializer.)))

(defn- update-click-atom
  [old-value rank]
  (if old-value
    (-> old-value
        (update :rank-sum #(+ % rank))
        (update :count inc))
    {:rank-sum rank :count 1}))

(defn- compute-click-average
  []
  (reduce (fn [acc [date {:keys [rank-sum count]}]] (assoc acc date (float (/ rank-sum count)))) {} @click-atom))

(defn- write-click-average
  []
  (let [click-average (compute-click-average)]
  (spit (format "./resources/output/click-average.txt") (str click-average "\n"))))

(defn- write-click-cause
  []
  (spit (format "./resources/output/click-cause.txt") (str @click-cause-atom "\n")))

(defn- update-clicks-stats
  [data]
  (let [rank (.asInt (.get data ":clickRank"))
        cause (.asText (.get data ":clickCause"))
        date (subs (.asText (.get data ":datetime")) 0 10)]
  (swap! click-atom #(update % date update-click-atom rank))
  (swap! click-cause-atom #(update % cause (fnil inc 0)))
  (write-click-average)
  (write-click-cause)))

(defn- process-data
  [^ConsumerRecord record]
  (let [topic (.topic record)
        key (.asText (.readTree (.reader (ObjectMapper.)) (.key record)))
        data (.readTree (.reader (ObjectMapper.)) (.value record))
        filepath (format "./resources/output/%s/%s.txt" topic key)]
    (io/make-parents filepath)
    (spit filepath (str data "\n"))
    (when (= topic "clicks")
      (update-clicks-stats data))))

(defn consume
  [topics]
  (with-open [consumer (create-consumer)]
    (.subscribe consumer topics)
    (loop [records []]
      (if records
        (doall (map process-data records)))
      (println "Waiting for message in KafkaConsumer.poll")
      (recur (seq (.poll consumer (Duration/ofSeconds 1)))))))
