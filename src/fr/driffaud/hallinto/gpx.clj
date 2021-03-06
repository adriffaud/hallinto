(ns fr.driffaud.hallinto.gpx
  (:require [clojure.java.io :as io]
            [clojure.xml :as xml]
            [clojure.zip :as zip]
            [clojure.data.zip.xml :refer [xml-> xml1-> attr text]]))

(defn zipxml
  "Loads an XML file from the given path, parse it and creates a zipper."
  [path]
  (let [file (io/file path)]
    (if (.exists file)
      (zip/xml-zip (xml/parse file))
      (throw (Exception. (str "File not found: " path))))))

(def parse-double #(Double/parseDouble %))

(defn parse-names [gpx]
  (remove empty?
          [(xml1-> gpx :name text)
           (xml1-> gpx :trk :name text)
           (xml1-> gpx :metadata :name text)]))

(defn parse-name [gpx]
  (first (parse-names gpx)))

(defn parse-trkpt [trkpt]
  {:lat  (parse-double (attr trkpt :lat))
   :lon  (parse-double (attr trkpt :lon))
   :ele  (xml1-> trkpt :ele text parse-double)
   :time (xml1-> trkpt :time text)})

(defn parse-segment [trkseg]
  {:points (map parse-trkpt (xml-> trkseg :trkpt))})

(defn parse-segments [gpx]
  (map parse-segment (xml-> gpx :trk :trkseg)))

(defn parse-wpt [wpt]
  {:lat  (parse-double (attr wpt :lat))
   :lon  (parse-double (attr wpt :lon))
   :ele  (xml1-> wpt :ele text parse-double)
   :time (xml1-> wpt :time text)
   :name (xml1-> wpt :name text)
   :sym  (xml1-> wpt :sym text)
   :type (xml1-> wpt :type text)})

(defn parse-wpts [gpx]
  (map parse-wpt (xml-> gpx :wpt)))

(defn parse-metadata [metadata]
  {:name (xml1-> metadata :name text)
   :desc (xml1-> metadata :desc text)
   :time (xml1-> metadata :time text)
   :link (xml1-> metadata :link (attr :href))})

(defn parse-gpx [gpx]
  {:name     (parse-name gpx)
   :segment  (parse-segments gpx)
   :waypoint (parse-wpts gpx)
   :metadata (xml1-> gpx :metadata parse-metadata)})

(defn parse-gpx-file [file]
  (parse-gpx
   (zipxml file)))
