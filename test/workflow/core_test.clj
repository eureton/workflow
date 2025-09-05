(ns workflow.core-test
  (:require [clojure.test :refer [deftest is use-fixtures]]
            [spy.core :refer [spy called-once? not-called?]]
            [workflow.core :as wf]))

(defn- validate-email [env]
  (count (get-in env [:in :email])))

(defn- update-in-database [env]
  (get-in env [:in :db-ok?]))

(defn- notify-stakeholders [_]
  (/ 4 0))

(defn- validate-username [env]
  (->> env (:in) (:username) (re-find #"abc")))

(defn- fetch-from-database [env]
  (when (-> env :in :connection-string nil?)
    wf/FAIL))

(defn- with-spies [f]
  (with-redefs [validate-email (spy validate-email)
                update-in-database (spy update-in-database)
                notify-stakeholders (spy notify-stakeholders)
                validate-username (spy validate-username)
                fetch-from-database (spy fetch-from-database)]
    (f)))

(use-fixtures :each #'with-spies)

(deftest one
  (let [workflow (wf/make
                   [:validate-email validate-email])
        {:keys [ok? results out]} (workflow {:email "john@doe.com"})
        [label status result] (get results 0)]
    (is (= true ok?))
    (is (= 1 (count results)))
    (is (= :validate-email label))
    (is (= :ok status))
    (is (some? result))
    (is (= 12 out))
    (is (called-once? validate-email))))

(deftest two
  (let [workflow (wf/make
                   [:validate-email validate-email]
                   [:update-in-database update-in-database])
        {:keys [ok? results out]} (workflow {:email "john@doe.com"})
        [result-1 result-2] results]
    (is (= true ok?))
    (is (= 2 (count results)))
    (is (= :validate-email     (get result-1 0)))
    (is (= :update-in-database (get result-2 0)))
    (is (= :ok (get result-1 1)))
    (is (= :ok (get result-2 1)))
    (is (some? (get result-1 2)))
    (is (nil?  (get result-2 2)))
    (is (nil? out))
    (is (called-once? validate-email))
    (is (called-once? update-in-database))))

(deftest three
  (let [workflow (wf/make
                   [:validate-email validate-email]
                   [:update-in-database update-in-database]
                   [:notify-stakeholders notify-stakeholders])
        {:keys [ok? results out]} (workflow {:email "john@doe.com"})
        [result-1 result-2 result-3] results]
    (is (= false ok?))
    (is (= 3 (count results)))
    (is (= :validate-email      (get result-1 0)))
    (is (= :update-in-database  (get result-2 0)))
    (is (= :notify-stakeholders (get result-3 0)))
    (is (= :ok    (get result-1 1)))
    (is (= :ok    (get result-2 1)))
    (is (= :error (get result-3 1)))
    (is (some?                         (get result-1 2)))
    (is (nil?                          (get result-2 2)))
    (is (instance? ArithmeticException (get result-3 2)))
    (is (instance? ArithmeticException out))
    (is (called-once? validate-email))
    (is (called-once? update-in-database))
    (is (called-once? notify-stakeholders))))

(deftest short-circuit
  (let [workflow (wf/make
                   [:validate-email validate-email]
                   [:validate-username validate-username]
                   [:update-in-database update-in-database]
                   [:notify-stakeholders notify-stakeholders])
        {:keys [ok? results out]} (workflow {:email "john@doe.com"})
        [result-1 result-2] results]
    (is (= false ok?))
    (is (= 2 (count results)))
    (is (= :validate-email    (get result-1 0)))
    (is (= :validate-username (get result-2 0)))
    (is (= :ok    (get result-1 1)))
    (is (= :error (get result-2 1)))
    (is (some?                          (get result-1 2)))
    (is (instance? NullPointerException (get result-2 2)))
    (is (instance? NullPointerException out))
    (is (called-once? validate-email))
    (is (called-once? validate-username))
    (is (not-called? update-in-database))
    (is (not-called? notify-stakeholders))))

(deftest provoke-failure
  (let [workflow (wf/make
                   [:validate-email validate-email]
                   [:fetch-from-database fetch-from-database]
                   [:update-in-database update-in-database]
                   [:notify-stakeholders notify-stakeholders])
        {:keys [ok? results out]} (workflow {:email "john@doe.com"})
        [result-1 result-2] results]
    (is (= false ok?))
    (is (= 2 (count results)))
    (is (= :validate-email      (get result-1 0)))
    (is (= :fetch-from-database (get result-2 0)))
    (is (= :ok    (get result-1 1)))
    (is (= :error (get result-2 1)))
    (is (some?     (get result-1 2)))
    (is (= wf/FAIL (get result-2 2)))
    (is (= wf/FAIL out))
    (is (called-once? validate-email))
    (is (called-once? fetch-from-database))
    (is (not-called? update-in-database))
    (is (not-called? notify-stakeholders))))

(defn- adder [from]
  (fn [env]
    (-> env
        (get-in [:temp from])
        (+ 100))))

(deftest destination
  (let [workflow (wf/make
             [:add-to-w (adder :w) :x]
             [:add-to-x (adder :x) :y]
             [:add-to-y (adder :y) :z])
        {:keys [ok? out]} (workflow {:w 4})]
    (is (= true ok?))
    (is (= 304 out))))
