(ns workflow.core
  (:require [workflow.step :as step]))

(def ^:const FAIL :__fail__)

(defn make [& steps]
  (fn [params]
    (let [f (->> steps
                 (reverse)
                 (map (fn [[label step destination]]
                        ((step/embellish label destination) step)))
                 (apply comp))]
      (f {:params params
          :ok? true
          :results []}))))
