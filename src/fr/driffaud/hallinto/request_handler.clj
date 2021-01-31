(ns fr.driffaud.hallinto.request-handler
  (:require [ring.util.response :refer [response]]
            [hiccup.core :refer [html]]
            [hiccup.page :refer [html5 include-js include-css]]
            [hiccup.element :refer [link-to]]))

(defn home [request]
  (response
   (html5
    {:lang "fr"}
    [:head
     [:meta {:charset "utf-8"}]]
    [:body
     [:section
      [:h1 "Hello"]]])))


(comment
  (html5
   {:lang "fr"})
  ;; => "<!DOCTYPE html>\n<html lang=\"fr\"></html>"

  (html5
   {:lang "fr"}
   [:header]
   [:body
    [:h1 "Test"]])
  ;; => "<!DOCTYPE html>\n<html lang=\"fr\"><header></header><body><h1>Test</h1></body></html>"
  )