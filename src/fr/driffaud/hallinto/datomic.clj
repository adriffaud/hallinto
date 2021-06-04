(ns fr.driffaud.hallinto.datomic
  (:require [datomic.client.api :as d]))

(def get-client
  (memoize
   #(d/client {:server-type :dev-local
               :system      "dev"})))

(def get-conn
  (memoize
   #(d/connect (get-client) {:db-name "hallinto"})))

(comment
  (d/list-databases (get-client) {})

  (d/create-database (get-client) {:db-name "hallinto"}))