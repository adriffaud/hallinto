(ns fr.driffaud.hallinto.accounts
  (:require [clojure.data.xml :as xml]
            [clojure.data.zip.xml :as zip-xml]
            [clojure.java.io :as io]
            [clojure.zip :as zip]
            [datomic.client.api :as d]
            [fr.driffaud.hallinto.datomic :as db]
            [fr.driffaud.hallinto.utils :as utils]
            [java-time :as t]))

; ==============================================================================
; Dates utils
(def glib-first-julian-day (t/local-date 1 1 1))

(defn julian->gregorian-day
  "Convert GLIB Julian days to Gregorian calendar date format."
  [jday]
  (t/plus glib-first-julian-day (t/days jday)))

; ==============================================================================
; Accounts schema
(def schema [;; Accounts
             {:db/ident       :account/id
              :db/valueType   :db.type/long
              :db/cardinality :db.cardinality/one
              :db/unique      :db.unique/identity
              :db/doc         "The ID of the bank account"}

             {:db/ident       :account/name
              :db/valueType   :db.type/string
              :db/cardinality :db.cardinality/one
              :db/doc         "The bank account name"}

             {:db/ident       :account/minimum
              :db/valueType   :db.type/double
              :db/cardinality :db.cardinality/one
              :db/doc         "The minimum amount allowed on the account"}

             {:db/ident       :account/maximum
              :db/valueType   :db.type/double
              :db/cardinality :db.cardinality/one
              :db/doc         "The maximum amount allowed on the account"}

             {:db/ident       :account/initial
              :db/valueType   :db.type/double
              :db/cardinality :db.cardinality/one
              :db/doc         "The initial amount on the account"}

             {:db/ident       :account/pos
              :db/valueType   :db.type/long
              :db/cardinality :db.cardinality/one
              :db/doc         "The position of the account in the UI"}

             {:db/ident       :account/cheque
              :db/valueType   :db.type/long
              :db/cardinality :db.cardinality/one
              :db/doc         "The last cheque number used"}

             {:db/ident       :account/type
              :db/valueType   :db.type/ref
              :db/cardinality :db.cardinality/one
              :db/doc         "The account type"}

             {:db/ident :account.type/bank}
             {:db/ident :account.type/cash}
             {:db/ident :account.type/asset}
             {:db/ident :account.type/credit-card}
             {:db/ident :account.type/liability}
             {:db/ident :account.type/checking}
             {:db/ident :account.type/savings}
             {:db/ident :acconut.type/maxvalue}])

(comment
  (d/transact (db/get-conn) {:tx-data schema})

  (d/q '[:find (pull ?account [*])
         :where
         [?account :account/name]]
       (d/db (db/get-conn))))

; ==============================================================================
; Homebank file parsing

(def id->type
  "Maps an account type id to its enum."
  {"1" :account.type/bank
   "2" :account.type/cash
   "3" :account.type/asset
   "4" :account.type/credit-card
   "5" :account.type/liability
   "6" :account.type/checking
   "7" :account.type/savings
   "8" :acconut.type/maxvalue})

(defn import-accounts
  "Retrieve, convert and store in database accounts from Homebank file."
  [conn file]
  (let [root             (utils/get-xml-root file)
        accounts         (zip-xml/xml-> root :homebank :account #(keep :attrs %))
        account->account (fn [acc] (merge
                                    {:account/id      (-> acc :key Integer/parseInt)
                                     :account/name    (:name acc)
                                     :account/type    (-> acc :type id->type)
                                     :account/minimum (-> acc :minimum Double/parseDouble)
                                     :account/maximum (-> acc :maximum Double/parseDouble)
                                     :account/initial (-> acc :initial Double/parseDouble)
                                     :account/pos     (-> acc :pos Integer/parseInt)}
                                    (when (acc :cheque1)
                                      {:account/cheque (-> acc :cheque1 Long/parseLong)})))
        tx-data          (map account->account accounts)]
    (d/transact conn {:tx-data tx-data})))

(comment

  (import-accounts conn file)

  (def root (utils/get-xml-root file))

  (->> (zip-xml/xml-> root :homebank)
       ffirst
       :content
       (map #(:tag %))
       distinct)

  ;; Home bank file properties
  (->> (zip-xml/xml-> root :homebank :properties #(keep :attrs %)))

  ;; Currencies
  (->> (zip-xml/xml-> root :homebank :cur #(keep :attrs %)))

  ;; Beneficiaires
  (def payees (zip-xml/xml-> root :homebank :pay #(keep :attrs %)))

  ;; Categories
  (def categories (zip-xml/xml-> root :homebank :cat #(keep :attrs %)))

  ;; Echeances recurrentes
  (->> (zip-xml/xml-> root :homebank :fav #(keep :attrs %)))

  ;; Operations
  (def opes (zip-xml/xml-> root :homebank :ope #(keep :attrs %)))

  (first payees)

  (def payees (reduce (fn [res val] (assoc res (:key val) (:name val))) {} payees))

  (first opes)

  (->> opes
       (sort-by :date)
       (take 3)
       (map (fn [ope]
              {:date     (-> ope :date Integer/parseInt julian->gregorian-day)
               :payee    (-> ope :payee payees)
               :account  (-> ope :account)
               :amount   (-> ope :amount Float/parseFloat)
               :paymode  (-> ope :paymode)
               :flags    (-> ope :flags)
               :category (-> ope :category)
               :wording  (-> ope :wording str)})))

  (->> opes
       (sort-by :date)
       last
       :date
       Integer/parseInt
       julian->gregorian-day)

  (->> (t/plus glib-first-julian-day (t/days 737925))
       (t/format "dd/MM/yyyy")))