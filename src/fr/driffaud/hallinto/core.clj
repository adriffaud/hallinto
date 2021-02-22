(ns fr.driffaud.hallinto.core
  (:gen-class)
  (:require [com.stuartsierra.component :as component]
            [fr.driffaud.hallinto.datalevin :as db]
            [fr.driffaud.hallinto.server :as server]))

(defn hallinto [port]
  (component/system-map
   :db (db/new-database (or (System/getenv "HALLINTO_DATA_DIR")
                            "database"))
   :app (component/using (server/new-web-server port)
                         [:db])))

(defn main [] (-> (Integer. (or (System/getenv "PORT")
                                8080))
                  hallinto
                  component/start))

;; =============================================================================
;; REPL
(comment
  (def system (hallinto 8080))

  (alter-var-root #'system component/start)

  (alter-var-root #'system component/stop))
