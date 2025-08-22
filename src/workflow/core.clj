(ns workflow.core
  (:require [clojure.string :as string]))

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
        result (try
                 [label :ok (step env)]
                 (catch Exception e
                   [label :error e]))]
    (-> env
        (assoc :ok? (-> result (get 1) (= :ok)))
        (update :results conj result))))

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
