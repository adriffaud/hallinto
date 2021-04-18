(ns fr.driffaud.hallinto.core
  (:require [clojure.data.zip.xml :as zip-xml]
            [clojure.java.io :as io]
            [clojure.string :as string]
            [clojure.xml :as xml]
            [clojure.zip :as zip]
            [datomic.client.api :as d]))

;; =============================================================================
;; Database
(def client (d/client {:server-type :dev-local
                       :system      "dev"
                       #_:storage-dir #_"custom-dir-here"}))

(def conn (d/connect client {:db-name "hallinto"}))

(def schema [{:db/ident       :track/id
              :db/valueType   :db.type/uuid
              :db/cardinality :db.cardinality/one
              :db/doc         "The UUID of the track"}

             {:db/ident       :track.category/type
              :db/valueType   :db.type/string
              :db/cardinality :db.cardinality/one
              :db/doc         "The type of track recording"}

             {:db/ident       :track/category
              :db/valueType   :db.type/ref
              :db/cardinality :db.cardinality/one
              :db/doc         "One of the track types"}])

;; =============================================================================
;; GPX parsing
(defn get-xml-root
  "Given a file absolute path, returns the zipped root XML element."
  [filename]
  (-> filename
      xml/parse
      zip/xml-zip))

(defn map-point
  [p]
  {:time      (-> p
                  (zip-xml/xml1-> :time #(keep :content %))
                  first)
   :lat       (zip-xml/attr p :lat)
   :lon       (zip-xml/attr p :lon)
   :ele       (-> p
                  (zip-xml/xml1-> :ele #(keep :content %))
                  first)
   :speed     (-> p
                  (zip-xml/xml1-> :extensions :gpxtpx:TrackPointExtension :gpxtpx:speed #(keep :content %))
                  first)
   :heartrate (-> p
                  (zip-xml/xml1-> :extensions :gpxtpx:TrackPointExtension :gpxtpx:hr #(keep :content %))
                  first)})

(defn convert-gpx
  [root]
  {:name   (-> root (zip-xml/xml1-> :trk :name #(keep :content %)) first)
   :desc   (-> root (zip-xml/xml1-> :trk :desc #(keep :content %)) first)
   :type   (-> root (zip-xml/xml1-> :trk :type #(keep :content %)) first)
   :points (mapv map-point (zip-xml/xml-> root :trk :trkseg :trkpt))})

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