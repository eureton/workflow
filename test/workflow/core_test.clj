(ns workflow.core-test
  (:require [clojure.test :refer [deftest is]]
            [workflow.core :as wf]))

(defn- validate-email [env]
  (seq (get-in env [:params :email])))

(defn- update-in-database [env]
  (get-in env [:params :db-ok?]))

(defn- notify-stakeholders [_]
  (/ 4 0))

(defn- validate-username [env]
  (->> env (:params) (:username) (re-find #"abc")))

(deftest one
  (let [workflow (wf/make validate-email)
        {:keys [ok? results]} (workflow {:email "john@doe.com"})
        [label status result] (get results 0)]
    (is (= true                                ok?))
    (is (= 1                                   (count results)))
    (is (= "workflow.core-test/validate-email" label))
    (is (= :ok                                 status))
    (is (some? result))))

(deftest two
  (let [workflow (wf/make validate-email update-in-database)
        {:keys [ok? results]} (workflow {:email "john@doe.com"})
        [result-1 result-2] results]
    (is (= true ok?))
    (is (= 2 (count results)))
    (is (= "workflow.core-test/validate-email"     (get result-1 0)))
    (is (= "workflow.core-test/update-in-database" (get result-2 0)))
    (is (= :ok (get result-1 1)))
    (is (= :ok (get result-2 1)))
    (is (some? (get result-1 2)))
    (is (nil?  (get result-2 2)))))

(deftest three
  (let [workflow (wf/make validate-email update-in-database notify-stakeholders)
        {:keys [ok? results]} (workflow {:email "john@doe.com"})
        [result-1 result-2 result-3] results]
    (is (= false ok?))
    (is (= 3 (count results)))
    (is (= "workflow.core-test/validate-email"      (get result-1 0)))
    (is (= "workflow.core-test/update-in-database"  (get result-2 0)))
    (is (= "workflow.core-test/notify-stakeholders" (get result-3 0)))
    (is (= :ok    (get result-1 1)))
    (is (= :ok    (get result-2 1)))
    (is (= :error (get result-3 1)))
    (is (some?                         (get result-1 2)))
    (is (nil?                          (get result-2 2)))
    (is (instance? ArithmeticException (get result-3 2)))))

(deftest short-circuit
  (let [workflow (wf/make
                   validate-email
                   validate-username
                   update-in-database
                   notify-stakeholders)
        {:keys [ok? results]} (workflow {:email "john@doe.com"})
        [result-1 result-2] results]
    (is (= false ok?))
    (is (= 2 (count results)))
    (is (= "workflow.core-test/validate-email"    (get result-1 0)))
    (is (= "workflow.core-test/validate-username" (get result-2 0)))
    (is (= :ok    (get result-1 1)))
    (is (= :error (get result-2 1)))
    (is (some?                          (get result-1 2)))
    (is (instance? NullPointerException (get result-2 2)))))
