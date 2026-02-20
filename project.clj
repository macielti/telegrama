(defproject net.clojars.macielti/telegrama "1.2.0"
  :description "A simple SDK to interact with the Telegram Bot API. It is based on the Morse. And intended to be compatible with GraalVM native-image."

  :url "https://github.com/macielti/telegrama"

  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url  "https://www.eclipse.org/legal/epl-2.0/"}

  :dependencies [[org.clojure/clojure "1.12.4"]
                 [net.clojars.macielti/common-clj "46.1.1"]
                 [net.clojars.macielti/http-client-component "3.2.2"]
                 [io.pedestal/pedestal.interceptor "0.8.1"]
                 [jarohen/chime "0.3.3"]]

  :profiles {:dev {:test-paths   ^:replace ["test/unit" "test/integration" "test/helpers"]

                   :plugins      [[lein-cloverage "1.2.4"]
                                  [com.github.clojure-lsp/lein-clojure-lsp "2.0.13"]
                                  [com.github.liquidz/antq "RELEASE"]]

                   :dependencies [[hashp "0.2.2"]]

                   :injections   [(require 'hashp.core)]

                   :aliases      {"clean-ns"     ["clojure-lsp" "clean-ns" "--dry"] ;; check if namespaces are clean
                                  "format"       ["clojure-lsp" "format" "--dry"] ;; check if namespaces are formatted
                                  "diagnostics"  ["clojure-lsp" "diagnostics"]
                                  "lint"         ["do" ["clean-ns"] ["format"] ["diagnostics"]]
                                  "clean-ns-fix" ["clojure-lsp" "clean-ns"]
                                  "format-fix"   ["clojure-lsp" "format"]
                                  "lint-fix"     ["do" ["clean-ns-fix"] ["format-fix"]]}}})
