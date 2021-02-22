(ns fr.driffaud.hallinto.handlers
  (:require [fr.driffaud.hallinto.datalevin :as data]
            [ring.util.response :refer [response]]
            [selmer.parser :as selmer]))

;; ============================================================================
;; Pages
(defn home [db]
  (response
   (selmer/render-file "templates/index.html"
                       {:accounts (data/list-accounts db)
                        :debug    true})))

(defn account-form []
  (response
   "Nothing here at the moment"))

(comment
  (selmer/parse-file "templates/index.html" {}))