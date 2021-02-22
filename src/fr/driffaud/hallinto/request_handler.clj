(ns fr.driffaud.hallinto.request-handler
  (:require [ring.util.response :refer [response]]
            [selmer.parser :as selmer]))

;; ============================================================================
;; Pages
(defn home [_request]
  (response
   (selmer/parse-file "templates/index.html" {})))

(defn account-form [_request]
  (response
   "Nothing here at the moment"))

(comment
  (selmer/parse-file "templates/index.html" {}))