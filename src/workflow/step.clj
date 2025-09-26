(ns workflow.step
  (:require [clojure.set :as cljset]))

(def ^:const FAIL :__fail__)

(defn log [label _]
  (fn [step]
    (fn [env]
      (let [result (try
                     (-> env :temp step)
                     (catch Exception e e))
            log [label
                 (if (or (= FAIL result)
                         (instance? Exception result))
                   :error
                   :ok)
                 result]]
        (-> env
            (assoc :ok? (-> log (get 1) (= :ok)))
            (assoc :out result)
            (update :results conj log))))))

(defn maybe [_ _]
  (fn [step]
    (fn [env]
      (cond-> env
        (:ok? env) (step)))))

(defn cache [label destination]
  (fn [step]
    (fn [env]
      (let [env (step env)
            result (->> env
                        (:results)
                        (filter (comp #{label} first))
                        (first)
                        (peek))]
        (cond-> env
          destination (assoc-in [:temp destination] result))))))

(defn embellish [[label step-fn destination]]
  (let [f (->> [label destination]
               (apply (juxt cache maybe log))
               (apply comp))]
    (f step-fn)))

(defmacro |=| [params & body]
  (let [params (-> params
                   (set)
                   (cljset/difference #{'data}))]
    `(fn [~'data]
       (let [{:keys [~@params]} ~'data]
         ~@body))))

(defmacro defstep [fn-name params & tail]
  `(def ~fn-name (|=| ~params ~@tail)))

(defn nest [workflow]
  (fn [data]
    (-> data workflow :out)))
