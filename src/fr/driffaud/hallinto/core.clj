(ns fr.driffaud.hallinto.core
  (:gen-class)
  (:require [compojure.core :refer [defroutes GET]]
            [org.httpkit.server :as server]
            [fr.driffaud.hallinto.request-handler :as handler]))

;; =============================================================================
;; Routing
(defroutes app
  (GET "/" [] handler/home))

;; =============================================================================
;; System
(defonce app-server-instance (atom nil))

(defn app-server-stop
  "Gracefully shutdown the server, waiting 100ms"
  []
  (when-not (nil? @app-server-instance)
    (@app-server-instance :timeout 100)
    (reset! app-server-instance nil)
    (println "INFO: Application server shutting down...")))

(defn app-server-start
  "Starts the application server an run the application"
  [port]
  (println "INFO: Starting server on port: " port)
  (reset! app-server-instance
          (server/run-server #'app {:port port})))

(defn -main
  "Start the application server on a specific port"
  [& [port]]
  (let [port (Integer. (or port
                           (System/getenv "PORT")
                           8080))]
    (app-server-start port)))

(defn app-server-restart
  "Convenience function to stop and start application server"
  []
  (app-server-stop)
  (-main))

;; =============================================================================
;; REPL
(comment
  (app-server-restart)

  (app-server-stop))