(ns coveo.core
  (:require [coveo.data :as data]
            [coveo.producer :as producer]
            [coveo.consumer :as consumer]
            [clojure.data.json :as json])
  (:gen-class))

(def ^:private topics ["clicks" "custom-events" "groups" "keywords" "searches"])
(def ^:private click-average-file "./resources/output/click-average.txt")
(def ^:private click-cause-file "./resources/output/click-cause.txt")

(defn- produce
  []
  (let [data (data/load-all-data)]
    (doall
      (pmap (fn [{:keys [data topic data-key]}]
             (producer/produce-event (producer/create-producer) topic data-key data)) data))))

(defn- consume
  []
  (consumer/consume topics))

(defn- compute-average
  [clicks-data]
  (/ (reduce + (vals clicks-data)) (count clicks-data)))

(defn- display-average-clicks
  [clicks-data]
  (let [average (compute-average clicks-data)
        last-seven-day-average (->> clicks-data
                               (into (sorted-map))
                               (take-last 7)
                               (compute-average))]
    (prn "Average click rank")
    (prn "---------------------")
    (prn (format "Last 7 day: %.4f" last-seven-day-average))
    (prn (format "Global: %.4f" average))
    (prn "")))

(defn- display-click-cause
  [clicks-data]
  (let [total-clicks (reduce + (vals clicks-data))
        sorted-data (into (sorted-map-by (fn [key1 key2]
                                           (compare
                                            (get clicks-data key2)
                                            (get clicks-data key1)))) clicks-data)]
    (prn "Most frequent click causes")
    (prn "---------------------")
    (doall (map-indexed
            (fn [i [click-cause amount]]
              (prn (format "%d. %s - %d - %.2f%%"
                           (inc i)
                           click-cause
                           amount
                           (* 100 (/ (float amount) total-clicks))))) sorted-data))
    (prn "")))

(defn- display-stats
  []
  (display-average-clicks (json/read-str (slurp click-average-file)))
  (display-click-cause (json/read-str (slurp click-cause-file))))

(defn -main
  [& [args]]
  (case args
    "produce" (produce)
    "consume" (consume)
    "stats" (display-stats)))
