(ns workflow.core
  (:require [clojure.string :as string]))

(def ^:const FAIL :__fail__)

(defn fn->str [f]
  (let [match #(re-find #"^(.*)@" %)]
    (-> f
        (str)
        (match)
        (second)
        (string/replace \$ \/)
        (string/replace \_ \-))))

(defn run-step [step env]
  (let [label (fn->str step)
        log (try
              (let [result (step env)]
                [label (if (not= FAIL result) :ok :error) result])
              (catch Exception e
                [label :error e]))]
    (-> env
        (assoc :ok? (-> log (get 1) (= :ok)))
        (update :results conj log))))

(defn- embellish [step]
  (fn [env]
    (cond->> env
      (:ok? env) (run-step step))))

(defn make [& steps]
  (fn [params]
    (let [f (->> steps
                 (reverse)
                 (map embellish)
                 (apply comp))]
      (f {:params params
          :ok? true
          :results []}))))
