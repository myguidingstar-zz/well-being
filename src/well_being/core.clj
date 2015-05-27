(ns well-being.core
  "Feed me some data!"
  (:require [cheshire.core :as json]
            [clojure.java.io :as io]))

(defn initialize-aggregated-numbers
  "Helper function for `aggregate`. If an entry for the given
  community/village is not found in the given hash-map, add one with
  the initial value of `[0 0]`."
  [m community|village]
  (update-in m [:aggregated-numbers community|village] #(or % [0 0])))

(defn aggregate
  "A reducing function that iterates through all the raw data set and
  aggregates some numbers. See Step 2 in Architecture document for
  details."
  [result next-water-point]
  (let [community|village (get next-water-point "communities_villages")
        functioning       (get next-water-point "water_functioning")
        result            (initialize-aggregated-numbers result community|village)]
    (-> (case functioning
          ;; if the current water point is functional, increases
          ;; `:number-functional` by 1
          "yes"
          (update-in result [:number-functional] inc)
          ;; if the current water point is NOT functional, increases
          ;; the first number `:` by 1
          "no"
          (update-in result [:aggregated-numbers community|village 0] inc)

          ;; functioning state is unknown? skip to next step
          result)
      ;; always increase the number of water points for current
      ;; community/village by 1 no matter what functioning state is.
      (update-in [:aggregated-numbers community|village 1] inc))))

(defn number-water-points
  "Iterates through the list of aggregated numbers. Returns numeber of
  water points for each community/village - the second number of the
  aggregated two."
  [aggregated-numbers]
  (reduce-kv (fn [m k v] (assoc m k (second v)))
             {} aggregated-numbers))

(defn percentage-broken
  "Iterates through the list of aggregated numbers. Returns numeber of
  percentage of broken water points for each community/village by
  applying `/` to the aggregated two numbers."
  [aggregated-numbers]
  ;; `aggregate` ensure that divisor (number of water points) is
  ;; always >= 1
  (reduce-kv (fn [m k v] (assoc m k (apply / v)))
             {} aggregated-numbers))

(defn community-ranking
  "Derives community-ranking from aggregated-numbers."
  [aggregated-numbers]
  (->> aggregated-numbers
    percentage-broken
    (sort-by second)
    (into {} (map-indexed (fn [i [c _]] [c i])))))

(defn finalize
  "Transforms temporarily aggregated numbers to wanted data."
  [{:keys [aggregated-numbers] :as m}]
  (-> m
    (assoc :number-water-points (number-water-points aggregated-numbers))
    (assoc :community-ranking (community-ranking aggregated-numbers))
    (dissoc :aggregated-numbers)))

(def ^{:doc "A transducer that prepares raw data before processing."}
  x-prepare-raw-data
  (comp (filter #(not-empty (get % "communities_villages")))
        ;; striping unused keys is not mandatory
        (map #(select-keys % ["communities_villages" "water_functioning"]))))

(defn process
  "Does the heavy-lifting calculation to produce wanted data."
  [raw-data]
  (->> raw-data
    (transduce x-prepare-raw-data (completing aggregate)
               {:number-functional 0 :aggregated-numbers {}})
    finalize))

;; outermost function
(defn calculate
  "Receives an URL whose content is a dataset of water points in JSON
  format. Returns processed data in JSON as stream."
  [url]
  (->> url io/reader json/parse-stream process json/generate-stream))
