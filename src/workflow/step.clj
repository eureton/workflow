(ns workflow.step)

(def ^:const FAIL :__fail__)

(defn log [label]
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

(defn maybe [step]
  (fn [env]
    (cond-> env
      (:ok? env) (step))))

(defn cache [label destination]
  (fn [step]
    (fn [env]
      (let [env (step env)
            result (->> env
                        (:results)
                        (filter (comp #{label} first))
                        (first)
                        (peek))]
        (assoc-in env [:params destination] result)))))

(defn embellish [label destination]
  (comp (cache label destination)
        maybe
        (log label)))
