(ns fr.driffaud.hallinto.server
  (:require [com.stuartsierra.component :as component]
            [org.httpkit.server :as server]))

(defrecord Webserver [router port http-server]
  component/Lifecycle
  (start [component]
    (println ";; Starting server")
    (let [server (server/run-server (:routes router) {:port port})]
      (assoc component
             :http-server server)))

  (stop [component]
    (println ";; Stopping server")
    (http-server :timeout 100)
    component))

(defn new-web-server
  "Returns a new instance of the web server component which creates its handler
   dynamically."
  [port]
  (map->Webserver {:port port}))