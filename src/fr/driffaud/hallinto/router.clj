(ns fr.driffaud.hallinto.router
  (:require [com.stuartsierra.component :as component]
            [compojure.core :refer [routes GET]]
            [compojure.route :refer [not-found]]
            [fr.driffaud.hallinto.handlers :as handlers]))

(defn app-routes
  "Returns the web handler function as a closure over the application
   component."
  [db]
  (routes
   (GET "/" request (handlers/home request db))
   (GET "/account-form" request (handlers/account-form request))
   (not-found "Page not found")))

(defrecord Router [db]
  component/Lifecycle
  (start [component]
    (println ";; Starting router")
    (assoc component :routes (app-routes db)))
  (stop [component]
    (println ";; Stopping router")
    (assoc component :routes nil)))

(defn new-router []
  (map->Router {}))