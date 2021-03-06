(ns fr.driffaud.hallinto.datalevin
  (:require [com.stuartsierra.component :as component]
            [datalevin.core :as d]))

;; =============================================================================
;; Database schema
(def schema {:bank/name    {:db/valueType :db.type/string
                            :db/unique    :db.unique/identity}
             :account/name {:db/valueType :db.type/string}
             :account/bank {:db/valueType :db.type/ref}})

;; =============================================================================
;; Database component
(defrecord Database [dir connection]
  component/Lifecycle

  (start [component]
    (println ";; Starting database")
    (let [conn (d/get-conn dir schema)]
      (assoc component :connection conn)))

  (stop [component]
    (println ";; Stopping database")
    (d/close connection)
    (assoc component :connection nil)))

(defn new-database [db-directory]
  (map->Database {:dir db-directory}))

;; =============================================================================
;; Database queries
(defn list-accounts [database]
  (-> (d/q '[:find (pull ?e [:bank/name
                             {:account/_bank [:db/id :account/name]}])
             :where
             [?e :bank/name ?name]]
           @(:connection database))
      first))

(defn get-account [database id]
  (d/q '[:find (pull ?e [*])
         :in $ ?id
         :where
         [?e :db/id ?id]]
       @(:connection database)
       id))