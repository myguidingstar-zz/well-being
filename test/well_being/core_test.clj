(ns well-being.core-test
  (:require [clojure.test :refer :all]
            [cheshire.core :as json]
            [clojure.java.io :as io]
            [well-being.core :refer :all]))

(deftest initialize-aggregated-numbers-tests
  (testing "entry for foo not found, add one"
    (is (= (initialize-aggregated-numbers {} "foo")
           {:aggregated-numbers {"foo" [0 0]}})))
  (testing "entry for bar exists, nothing changes"
   (is (= (initialize-aggregated-numbers
           {:aggregated-numbers {"bar" [1 2]}} "bar")
          {:aggregated-numbers {"bar" [1 2]}}))))

(deftest aggregate-tests
  (let [init {:number-functional 0
              :aggregated-numbers {}}
        data [{"communities_villages" "foo" "water_functioning" "yes"}
              {"communities_villages" "bar" "water_functioning" "yes"}
              {"communities_villages" "foo" "water_functioning" "no"}
              {"communities_villages" "foo" "water_functioning" "na"}]]
    (is (= (reduce aggregate init data)
           (transduce completing aggregate init data)
           {:number-functional 2,
            :aggregated-numbers {"foo" [1 3], "bar" [0 1]}}))
    (is (= (reductions aggregate init data)
           [init
            ;; adding one functional water point in foo
            {:number-functional 1,
             :aggregated-numbers {"foo" [0 1]}}
            ;; adding one functional water point in bar
            {:number-functional 2,
             :aggregated-numbers {"foo" [0 1], "bar" [0 1]}}
            ;; adding one broken water point in foo
            {:number-functional 2,
             :aggregated-numbers {"foo" [1 2], "bar" [0 1]}}
            ;; adding one water point in foo whose functioning state
            ;; is unknown
            {:number-functional 2,
             :aggregated-numbers {"foo" [1 3], "bar" [0 1]}}]))))

(deftest number-water-points-tests
  (is (= (number-water-points {"foo" [1 3], "bar" [0 1]})
         {"foo" 3, "bar" 1})))

(deftest percentage-broken-tests
  (is (= (percentage-broken {"foo" [1 3], "bar" [0 1]})
         {"foo" 1/3, "bar" 0})))

(deftest community-ranking-tests
  (is (= (community-ranking {"foo" [1 3], "bar" [1 4] "loo" [1 5] "lar" [1 6]})
         {"lar" 0, "loo" 1, "bar" 2, "foo" 3})))

(deftest finalize-tests
  (is (= (finalize {:number-functional 2,
                    :aggregated-numbers {"foo" [1 3], "bar" [0 1]}})
         {:number-functional 2,
          :number-water-points {"foo" 3, "bar" 1},
          :community-ranking {"bar" 0, "foo" 1}})))

(deftest process-tests
  (let [raw-data (->> "water_points.json"
                   io/resource
                   io/reader
                   json/parse-stream)]
    (is (= (process raw-data)
           {:number-functional 623, :number-water-points {"Kpikpaluk" 3, "Jaata" 8, "Badomsa" 27, "Abanyeri" 4, "Kurugu" 9, "Suik" 1, "Nabulugu" 31, "Luisa" 8, "Tankangsa" 6, "Kubore" 18, "Kanwaasa" 9, "Akpari-yeri" 3, "Chanpolinsa" 4, "Longsa" 9, "Selinvoya" 13, "Loagri_1_" 18, "Gbaarigu" 5, "Bandem" 7, "Arigu" 12, "Zanwara" 10, "Nyandema" 3, "Guuta" 32, "Guuta-Nasa" 11, "Zuedema" 18, "Soo" 7, "Fiisa" 5, "Vundema" 5, "Alavanyo" 3, "Jagsa" 38, "Nyankpiensa" 8, "Zukpeni" 6, "Kaasa" 25, "Jiningsa-Yipaala" 3, "Jiniensa" 1, "Kpatarigu" 51, "Gumaryili" 1, "Logvasgsa" 4, "Mwalorinsa" 8, "Garigu" 1, "Dorinsa" 17, "Nawaasa" 6, "Bechinsa" 26, "Kalaasa" 1, "Jiningsa" 7, "Banyangsa" 10, "Kom" 6, "Namgurima" 8, "Piisa" 5, "Dibisi" 2, "Zua" 28, "Nayoku" 35, "Kulbugu" 11, "Chansa" 9, "Jiriwiensa" 8, "Gbima" 3, "Chondema" 4, "Tantala" 22, "Zogsa" 6, "Zundem" 30, "Kanbangsa" 8, "Tuisa" 4, "Kunkwah" 3, "Zangu-Vuga" 13, "Gaadem" 2, "Sikabsa" 3}, :community-ranking {"Kpikpaluk" 0, "Jaata" 1, "Badomsa" 2, "Abanyeri" 3, "Kurugu" 60, "Suik" 4, "Nabulugu" 43, "Luisa" 5, "Tankangsa" 6, "Kubore" 53, "Kanwaasa" 42, "Akpari-yeri" 7, "Chanpolinsa" 51, "Longsa" 49, "Selinvoya" 39, "Loagri_1_" 50, "Gbaarigu" 63, "Bandem" 59, "Arigu" 61, "Zanwara" 58, "Nyandema" 8, "Guuta" 38, "Guuta-Nasa" 9, "Zuedema" 10, "Soo" 54, "Fiisa" 11, "Vundema" 12, "Alavanyo" 56, "Jagsa" 46, "Nyankpiensa" 13, "Zukpeni" 64, "Kaasa" 14, "Jiningsa-Yipaala" 15, "Jiniensa" 16, "Kpatarigu" 41, "Gumaryili" 17, "Logvasgsa" 18, "Mwalorinsa" 19, "Garigu" 20, "Dorinsa" 37, "Nawaasa" 21, "Bechinsa" 22, "Kalaasa" 23, "Jiningsa" 24, "Banyangsa" 55, "Kom" 25, "Namgurima" 62, "Piisa" 26, "Dibisi" 27, "Zua" 44, "Nayoku" 48, "Kulbugu" 40, "Chansa" 28, "Jiriwiensa" 29, "Gbima" 57, "Chondema" 30, "Tantala" 52, "Zogsa" 47, "Zundem" 31, "Kanbangsa" 32, "Tuisa" 33, "Kunkwah" 34, "Zangu-Vuga" 45, "Gaadem" 35, "Sikabsa" 36}}))))
