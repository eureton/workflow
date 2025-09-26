(defproject com.eureton/workflow "0.5.0"
  :description "Idiomatic ROP-based workflow DSL in Clojure."
  :url "https://github.com/eureton/workflow"
  :license {:name "MIT"
            :url "https://github.com/eureton/workflow/blob/master/LICENSE"}
  :dependencies [[org.clojure/clojure "1.11.1"]]
  :profiles {:dev {:dependencies [[tortue/spy "2.15.0"]]}}
  :repl-options {:init-ns workflow.core})
