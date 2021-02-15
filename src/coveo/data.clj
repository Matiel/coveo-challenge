(ns coveo.data
  (:require [clojure.java.io :as io]
            [clojure.data.csv :as csv]
            [clojure.string :as str]))

(def ^:private clicks-file "./resources/data/clicks.csv")
(def ^:private custom-events-file "./resources/data/custom_events.csv")
(def ^:private groups-file "./resources/data/groups.csv")
(def ^:private keywords-file "./resources/data/keywords.csv")
(def ^:private searches-file "./resources/data/searches.csv")

(defn- extract-data-format
  [csv-data]
  (->> csv-data
       first
       (map #(str/replace % #"﻿" ""))
       (map keyword)))

(defn- format-data
  [data-keys csv-data]
  (zipmap data-keys csv-data))

(defn load-data
  [file keys-to-remove]
  (with-open [reader (io/reader file)]
    (let [csv-data (csv/read-csv reader)
          data-format (extract-data-format csv-data)]
    (->> csv-data
         (drop 1)
         (map #(format-data data-format %))
         (map #(apply dissoc % keys-to-remove))
         doall))))

(defn load-all-data
  []
  [{:data (load-data clicks-file [:username :userId])
    :topic "clicks"
    :data-key :searchId}
   {:data (load-data custom-events-file [:userName :userId])
    :topic "custom-events"
    :data-key :lastSearchId}
   {:data (load-data groups-file [])
    :topic "groups"
    :data-key :searchId}
   {:data (load-data keywords-file [])
    :topic "keywords"
    :data-key :searchId}
   {:data (load-data searches-file [:username :userId])
    :topic "searches"
    :data-key :id}])
