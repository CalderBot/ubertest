(defproject ubertest "1.00"
  :dependencies [[org.clojure/clojure "1.10.3" :scope "provided"]
                 [techascent/tech.ml.dataset "6.084" :exclusions [cnuernber/dtype-next]]
                 [cnuernber/dtype-next "9.028"]
                 [tick "0.5.0-RC2"]]
  :profiles {:uberjar {:aot          :all
                       :main         ubertest.main
                       :uberjar-name "dataset.jar"}})
