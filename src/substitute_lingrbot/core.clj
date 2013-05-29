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
(def latest-text (ref ""))

(defn handle-post [body]
  (for [message (map :message (:events (read-json (slurp body))))
        :let [text (:text message)
              nick (:nickname message)]]
    (if-let [[_ left right _ target-nick]
             (re-find #"^s/([^/]+)/([^/]+)/g?\s*(<\s*@?(.*))?$" text)]
      (if target-nick
        (let [new-text
              (clojure.string/replace (get @previous-text target-nick "")
                                      (re-pattern left)
                                      right)]
          (swap! previous-text assoc target-nick new-text)
          (format "%s" new-text))
        (let [new-text
              (clojure.string/replace @latest-text
                                      (re-pattern left)
                                      right)]
          (dosync (ref-set latest-text new-text))
          (format "%s" new-text)))
      (do
        (swap! previous-text assoc nick text)
        (dosync (ref-set latest-text new-text))
        ""))))

(defroutes routes
  (GET "/" []
       (str {:version version
             :homepage "https://github.com/ujihisa/substitute-lingrbot"
             :from start-time
             :author "ujihisa"
             :previous-text @previous-text}))
  (POST "/" {body :body}
        (clojure.string/join "\n" (handle-post body))))

(defn -main []
  (let [port (Integer/parseInt (or (System/getenv "PORT") "8080"))]
    (run-jetty routes {:port port :join? false})))

; vim: set lispwords+=defroutes :
