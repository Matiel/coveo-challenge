(defproject coveo "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :main coveo.core
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [org.clojure/tools.logging "1.1.0"]
                 [org.clojure/data.csv "1.0.0"]
                 [org.slf4j/slf4j-api "1.7.30"]
                 [org.slf4j/slf4j-log4j12 "1.7.25"]
                 [org.apache.kafka/kafka-clients "2.0.0"]
                 [org.clojure/data.json "1.0.0"]
                 [com.fasterxml.jackson.core/jackson-databind "2.12.1"]]
  :repl-options {:init-ns coveo.core}
  :jvm-opts ["-Dlog4j.configuration=log4j.properties"])
