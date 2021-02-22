(ns fr.driffaud.hallinto.server
  (:require [com.stuartsierra.component :as component]
            [compojure.core :refer [routes GET]]
            [compojure.route :refer [not-found]]
            [fr.driffaud.hallinto.datalevin :as data]
            [fr.driffaud.hallinto.request-handler :as handler]
            [org.httpkit.server :as server]
            [ring.util.response :refer [response]]
            [selmer.parser :as selmer]))

(defn home [_request]
  (response
   (selmer/render-file "templates/index.html" {:accounts (data/list-accounts)})))

;; =============================================================================
;; Routing

(defn app-routes
  "Returns the web handler function as a closure over the application
   component."
  []
  (routes
   (GET "/" request (home request))
   (GET "/account-form" request (handler/account-form request))
   (not-found "Page not found")))

;; =============================================================================
;; Component definition

(defrecord Webserver [db port server]
  component/Lifecycle
  (start [component]
    (let [server (server/run-server (app-routes) {:port port})]
      (assoc component
             :http-server server
             :database db)))

  (stop [component]
    (server :timeout 100)
    component))

(defn new-web-server
  "Returns a new instance of the web server component which creates its handler
   dynamically."
  [port]
  (map->Webserver {:port port}))