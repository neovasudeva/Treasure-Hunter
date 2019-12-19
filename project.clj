(defproject adventure "0.1.0-SNAPSHOT"
  :description "CS 296 final project"
  :url "http://example.com/FIXME"
  :license "Eclipse Public License 1.0"
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [io.aviso/pretty "0.1.37"]]
  :profiles {:dev {:dependencies [[midje "1.9.9"]]}}
  :middleware [io.aviso.lein-pretty/inject]
  :plugins [[lein-marginalia "0.9.1"]
            [io.aviso/pretty "0.1.37"]
            [lein-midje "3.2.1"]
          ]
  :repl-options {:init-ns adventure.core}
  )

