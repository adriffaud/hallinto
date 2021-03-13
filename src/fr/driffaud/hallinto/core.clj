(ns fr.driffaud.hallinto.core
  (:gen-class)
  (:require [com.stuartsierra.component :as component]
            [fr.driffaud.hallinto.datalevin :as db]
            [fr.driffaud.hallinto.router :as router]
            [fr.driffaud.hallinto.server :as server]))

(defn hallinto [port]
  (component/system-map
   :db     (db/new-database
            (or (System/getenv "HALLINTO_DATA_DIR")
                "database"))
   :router (component/using
            (router/new-router)
            [:db])
   :app    (component/using
            (server/new-web-server port)
            [:router])))

(defn -main [& _args]
  (-> (or (System/getenv "PORT")
          8080)
      Integer.
      hallinto
      component/start))

;; =============================================================================
;; REPL
(comment
  (selmer.parser/cache-off!)

  (def system (hallinto 8080))

  (alter-var-root #'system component/start)

  (alter-var-root #'system component/stop))
