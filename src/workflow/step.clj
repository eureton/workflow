(ns workflow.step)

(def ^:const FAIL :__fail__)

(defn log [label]
  (fn [step]
    (fn [env]
      (let [log (try
                  (let [result (step env)]
                    [label (if (not= FAIL result) :ok :error) result])
                  (catch Exception e
                    [label :error e]))]
        (-> env
            (assoc :ok? (-> log (get 1) (= :ok)))
            (update :results conj log))))))

(defn maybe [step]
  (fn [env]
    (cond-> env
      (:ok? env) (step))))

(defn embellish [label]
  (comp maybe (log label)))
