(ns fr.driffaud.hallinto.utils
  (:require [clojure.data.xml :as xml]
            [clojure.java.io :as io]
            [clojure.zip :as zip]))

(defn get-xml-root
  "Given a file absolute path, returns the zipped root XML element."
  [filename]
  (-> filename
      io/reader
      xml/parse
      zip/xml-zip))