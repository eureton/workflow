(ns workflow.step-test
  (:require [clojure.test :refer [deftest is]]
            [workflow.core :as wf]
            [workflow.step :refer [defstep]]))

(defstep calculate-x [w]
  (+ w 1))

(defstep calculate-y [x]
  (+ x 10))

(defstep calculate-z [y]
  (+ y 100))

(deftest destructure-with-symbols
  (let [workflow (wf/make
                   [:calculate-x calculate-x :x]
                   [:calculate-y calculate-y :y]
                   [:calculate-z calculate-z :z])
        {:keys [x y z]} (-> {:w 3} workflow :temp)]
    (is (=   4 x))
    (is (=  14 y))
    (is (= 114 z))))
