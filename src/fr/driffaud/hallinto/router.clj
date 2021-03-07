(ns fr.driffaud.hallinto.router
  (:require [com.stuartsierra.component :as component]
            [compojure.core :refer [routes GET]]
            [compojure.route :refer [not-found]]
            [fr.driffaud.hallinto.handlers :as handlers]
            [selmer.middleware :refer [wrap-error-page]]))

(defn app-routes
  "Returns the web handler function as a closure over the application
   component."
  [db]
  (wrap-error-page
   (routes
    (GET "/" [] (handlers/home db))
    (GET "/accounts" [] (handlers/home db))
    (GET "/account/:id" [id] (handlers/account db id))
    (GET "/account-form" [] (handlers/account-form))
    (GET "/tracks" [] (handlers/tracks db))
    (not-found "Page not found"))))

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