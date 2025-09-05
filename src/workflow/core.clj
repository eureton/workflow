(ns workflow.core
  (:require [workflow.step :as step]))

(def ^:const FAIL :__fail__)

(defn make [& steps]
  (fn [params]
    (let [f (->> steps
                 (reverse)
                 (map step/embellish)
                 (apply comp))]
      (f {:params params
          :ok? true
          :results []}))))
