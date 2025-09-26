(ns workflow.core
  (:require [workflow.step :as step]))

(def FAIL step/FAIL)

(def ^:macro |=| #'step/|=|)

(def ^:macro defstep #'step/defstep)

(def nest step/nest)

(defn make [& steps]
  (fn [params]
    (let [f (->> steps
                 (reverse)
                 (map step/embellish)
                 (apply comp))]
      (f {:in params
          :temp params
          :ok? true
          :results []}))))
