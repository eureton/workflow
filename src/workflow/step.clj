(ns workflow.step)

(def ^:const FAIL :__fail__)

(defn log [label _]
  (fn [step]
    (fn [env]
      (let [result (try
                     (step env)
                     (catch Exception e e))
            log [label
                 (if (or (= FAIL result)
                         (instance? Exception result))
                   :error
                   :ok)
                 result]]
        (-> env
            (assoc :ok? (-> log (get 1) (= :ok)))
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
          destination (assoc-in [:params destination] result))))))

(defn embellish [[label step-fn destination]]
  (let [f (->> [label destination]
               (apply (juxt cache maybe log))
               (apply comp))]
    (f step-fn)))
