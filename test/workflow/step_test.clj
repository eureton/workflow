(ns workflow.step-test
  (:require [clojure.test :refer [deftest is]]
            [workflow.core :as wf]
            [workflow.step :refer [defstep |=|]]))

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

(deftest destructure-inline
  (let [workflow (wf/make
                   [:calculate-x (|=| [w] (* w 2)) :x]
                   [:calculate-y (|=| [x] (* x 3)) :y]
                   [:calculate-z (|=| [y] (* y 3)) :z])
        {:keys [x y z]} (-> {:w 3} workflow :temp)]
    (is (=  6 x))
    (is (= 18 y))
    (is (= 54 z))))
