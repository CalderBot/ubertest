(ns ubertest.main
  (:require
    [tick.core :as t]
    [tech.v3.dataset :as ds]
    [tech.v3.dataset.rolling :as dsr]
    [tech.v3.datatype.functional :as dtf]
    [clojure.data :as data])
  (:gen-class))


(defn test-fn [df]
  (as-> df st

        ;; 1. add the gain column - gains not equal
        (ds/group-by st :s)
        (vals st)
        (map #(assoc % :gain (dtf// (% :o) (dtf/shift (% :o) 2))) st)
        (apply ds/concat st)
        ;; to make the problem smaller, I'll look first at a single day.
        ;; to proceed, comment out these two lines and uncomment the rest
        (ds/filter-column st :date #(= (t/date "2022-05-13") %))
        (ds/sort-by st :s)

        ;;; 2. add the big drop rates - it gets messed up here
        ;(ds/group-by st :date)
        ;(vals st)
        ;(map (fn [df]
        ;       (let [res {:date  (:date (ds/row-at df 0))
        ;                  :n     (ds/row-count df)
        ;                  :drops (-> (ds/filter-column df :gain #(< % 0.85)) (ds/row-count))}]
        ;         (assoc res :big-drop-rate (double (/ (:drops res) (:n res)))))) st)
        ;(ds/->dataset st)
        ;
        ;;; 3. add the rolling - new discrepancies here... notice the last line is not calculating :max-drop-rate correctly
        ;(dsr/rolling st
        ;               {:window-type              :fixed
        ;                :window-size              3
        ;                :relative-window-position :left}
        ;               {:max-drop-rate (dsr/max :big-drop-rate)})
        ))

(defn -main [& args]
  (let [stored (ds/->dataset "test-rolling.nippy")
        ds-1 (as-> stored st
                   ;; start 4-01
                   (ds/filter-column st :date #(t/< (t/date "2022-04-01") %))
                   (test-fn st))
        ds-2 (as-> stored st
              ;; start 5-01
              (ds/filter-column st :date #(t/< (t/date "2022-05-01") %))
              (test-fn st))]
   ;; the filter-column by date should not matter here, because in test-fn we restrict to 2022-5-13

   (println "Are ds-1 and ds-2 equal?" (= ds-1 ds-2))
   (println "Maybe it is just ordering or something, so find the diff in the first 1000 rows:")
   (->> (data/diff
          (->> ds-1 (ds/rows) (take 1000) (set))
          (->> ds-2 (ds/rows) (take 1000) (set)))
        (take 2)
        (println)))

  (shutdown-agents))
