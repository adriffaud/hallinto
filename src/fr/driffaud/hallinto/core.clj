(ns fr.driffaud.hallinto.core
  (:require [clojure.data.xml :as xml]
            [clojure.data.zip.xml :as zip-xml]
            [clojure.java.io :as io]
            [clojure.string :as string]
            [clojure.zip :as zip]
            [datomic.client.api :as d]
            [java-time :as t]))

;; =============================================================================
;; Database
(def client (d/client {:server-type :dev-local
                       :system      "dev"}))

(def conn (d/connect client {:db-name "hallinto"}))

(def schema [{:db/ident       :track/id
              :db/valueType   :db.type/uuid
              :db/cardinality :db.cardinality/one
              :db/unique      :db.unique/identity
              :db/doc         "The UUID of the track"}

             #_{:db/ident       :track.category/type
                :db/valueType   :db.type/string
                :db/cardinality :db.cardinality/one
                :db/doc         "The type of track recording"}

             {:db/ident       :track/name
              :db/valueType   :db.type/string
              :db/cardinality :db.cardinality/one
              :db/doc         "The name of the track"}

             {:db/ident       :track/desc
              :db/valueType   :db.type/string
              :db/cardinality :db.cardinality/one
              :db/doc         "A description of the track"}

             {:db/ident       :track/type
              :db/valueType   :db.type/ref
              :db/cardinality :db.cardinality/one
              :db/doc         "One of the track types"}

             {:db/ident       :track.point/time
              :db/valueType   :db.type/instant
              :db/cardinality :db.cardinality/one}

             {:db/ident       :track.point/lat
              :db/valueType   :db.type/double
              :db/cardinality :db.cardinality/one}

             {:db/ident       :track.point/lon
              :db/valueType   :db.type/double
              :db/cardinality :db.cardinality/one}

             {:db/ident       :track.point/ele
              :db/valueType   :db.type/long
              :db/cardinality :db.cardinality/one}

             {:db/ident       :track.point/speed
              :db/valueType   :db.type/float
              :db/cardinality :db.cardinality/one}

             {:db/ident       :track.point/heartrate
              :db/valueType   :db.type/double
              :db/cardinality :db.cardinality/one}

             {:db/ident       :track/points
              :db/valueType   :db.type/ref
              :db/cardinality :db.cardinality/many
              :db/doc         "The points of the track"
              :db/isComponent true}])

(comment
  (d/list-databases client {})
  (d/create-database client {:db-name "hallinto"})

  (d/transact conn {:tx-data schema})

  (d/transact conn {:tx-data [{:track/id ()}]})

  (d/q '[:find ?track
         :where
         [?track :track/id]]
       (d/db conn)))

;; =============================================================================
;; GPX parsing
(defn get-xml-root
  "Given a file absolute path, returns the zipped root XML element."
  [filename]
  (-> filename
      io/reader
      xml/parse
      zip/xml-zip))

(defn map-point
  [p]
  {:track.point/time      (-> p
                              (zip-xml/xml1-> :time #(keep :content %))
                              first
                              t/instant)
   :track.point/lat       (-> p (zip-xml/attr :lat) Double/parseDouble)
   :track.point/lon       (-> p (zip-xml/attr :lon) Double/parseDouble)
   :track.point/ele       (-> p
                              (zip-xml/xml1-> :ele #(keep :content %))
                              first
                              Float/parseFloat)
   :track.point/speed     (-> p
                              (zip-xml/xml1-> :extensions :TrackPointExtension :speed #(keep :content %))
                              first
                              Float/parseFloat)
   :track.point/heartrate (-> p
                              (zip-xml/xml1-> :extensions :TrackPointExtension :hr #(keep :content %))
                              first
                              Integer/parseInt)})

(defn convert-gpx
  [root]
  {:track/name   (-> root (zip-xml/xml1-> :trk :name #(keep :content %)) first)
   :track/desc   (-> root (zip-xml/xml1-> :trk :desc #(keep :content %)) first str)
   :track/type   (-> root (zip-xml/xml1-> :trk :type #(keep :content %)) first str)
   :track/points (mapv map-point (zip-xml/xml-> root :trk :trkseg :trkpt))})

(defn convert-file
  [filename]
  (-> filename
      get-xml-root
      convert-gpx))

(defn convert-files
  [dir]
  (println (str "Reading files in " dir))
  (->> dir
       io/file
       .listFiles
       (filter #(not (.isDirectory %)))
       (filter #(string/ends-with? (.getName %) ".gpx"))
       (mapv convert-file)))