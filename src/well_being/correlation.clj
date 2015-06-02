(ns well-being.correlation)

(defn percentage
  [m]
  (reduce-kv (fn [m k v] (assoc m k (apply / v))) {} m))

(defn percentage-broken-by*
  [k1 k2]
  (fn [result next-water-point]
    (let [v1                (get next-water-point k1)
          v2 (get next-water-point k2)]
      (-> result
        (update v1 #(or % [0 0]))
        (update-in [v1 0] (if (= "no" v2)
                            inc identity))
        (update-in [v1 1] inc)))))

(defn percentage-broken-by
  [k1 k2 water-points]
  {:road-available (->> water-points
                     (reduce (percentage-broken-by* k1 k2) {})
                     percentage)})
