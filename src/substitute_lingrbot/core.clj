(ns substitute-lingrbot.core
  #_(:require [leiningen.core.project])
  (:use [compojure.core :only (defroutes GET POST)]
        [clojure.data.json :only (read-json)]
        [ring.adapter.jetty :only (run-jetty)])
  (:import java.util.concurrent.ExecutionException)
  (:gen-class))

(def version
  "dummy"
  #_(:version (leiningen.core.project/read)))

(def start-time
  (java.util.Date.))

(def previous-text (atom {}))

(defroutes routes
  (GET "/" []
       (str {:version version
             :homepage "https://github.com/ujihisa/substitute-lingrbot"
             :from start-time
             :author "ujihisa"
             :previous-text @previous-text}))
  (POST "/" {body :body headers :headers}
        (let [results
              (for [message (map :message (:events (read-json (slurp body))))
                    :let [text (:text message)
                          room (:room message)]]
                (if-let [[_ left right] (re-find #"^s/([^/]+)/([^/]+)/g?$" text)]
                  (clojure.string/replace (get @previous-text room) (re-pattern left) right)
                  (do
                    (swap! previous-text assoc room text)
                    "")))]
          (clojure.string/join "\n" results))))

(defn -main []
  (let [port (Integer/parseInt (or (System/getenv "PORT") "8080"))]
    (run-jetty routes {:port port :join? false})))

; vim: set lispwords+=defroutes :
