(ns user
  (:require [clojure.data.xml :as xml]
            [clojure.data.zip.xml :as zip-xml]
            [clojure.java.io :as io]
            [clojure.string :as string]
            [clojure.zip :as zip]
            [java-time :as t]))

(def glib-first-julian-day (t/local-date 1 1 1))

(defn julian->gregorian-day
  "Convert GLIB Julian days to Gregorian calendar date format."
  [jday]
  (t/plus glib-first-julian-day (t/days jday)))

(comment

  (def root
    (-> file
        io/reader
        xml/parse
        zip/xml-zip))

  (->> (zip-xml/xml-> root :homebank)
       ffirst
       :content
       (map #(:tag %))
       distinct)

  ;; Home bank file properties
  (->> (zip-xml/xml-> root :homebank :properties #(keep :attrs %)))

  ;; Currencies
  (->> (zip-xml/xml-> root :homebank :cur #(keep :attrs %)))

  ;; Accounts
  (->> (zip-xml/xml-> root :homebank :account #(keep :attrs %)))

  ;; Beneficiaires
  (def payees (zip-xml/xml-> root :homebank :pay #(keep :attrs %)))

  ;; Categories
  (def categories (zip-xml/xml-> root :homebank :cat #(keep :attrs %)))

  ;; Echeances recurrentes
  (->> (zip-xml/xml-> root :homebank :fav #(keep :attrs %)))

  ;; Operations
  (def opes (zip-xml/xml-> root :homebank :ope #(keep :attrs %)))

  (first payees)

  (def payees (reduce (fn [res val] (assoc res (:key val) (:name val))) {} payees))

  (first opes)

  (->> opes
       (sort-by :date)
       (take 3)
       (map (fn [ope]
              {:date     (-> ope :date Integer/parseInt julian->gregorian-day)
               :payee    (-> ope :payee payees)
               :account  (-> ope :account)
               :amount   (-> ope :amount Float/parseFloat)
               :paymode  (-> ope :paymode)
               :flags    (-> ope :flags)
               :category (-> ope :category)
               :wording  (-> ope :wording str)})))

  (->> opes
       (sort-by :date)
       last
       :date
       Integer/parseInt
       julian->gregorian-day)

  (->> (t/plus glib-first-julian-day (t/days 737925))
       (t/format "dd/MM/yyyy")))
