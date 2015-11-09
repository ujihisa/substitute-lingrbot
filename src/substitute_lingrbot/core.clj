(ns substitute-lingrbot.core
  #_(:refer-clojure :exclude [replace])
  (:require [clojure.string :as s]
            [compojure.core :refer (defroutes GET POST ANY)]
            [clojure.data.json :refer (read-json)]
            [ring.adapter.jetty :refer (run-jetty)]
            [clojure.string :refer (join)])
  (:gen-class))

(def previous-text (atom {}))
(def latest-texts (atom {}))

(defn- case1 [room left right target-nick]
  (let [new-text
        (s/replace (get (get @previous-text target-nick {}) room "")
                   (re-pattern (s/replace left #"\\(.)" "$1"))
                   (s/replace right #"\\(.)" "$1"))]
    (swap! previous-text assoc target-nick
           (assoc (get @previous-text target-nick {}) room new-text))
    (format "%s" new-text)))

(defn- case2 [room left right]
  (let [latest-text (last (get @latest-texts room [""])) ; TODO
        new-text
        (s/replace latest-text
                   (re-pattern (s/replace left #"\\(.)" "$1"))
                   (s/replace right #"\\(.)" "$1"))]
    (let [texts (get @latest-texts room [""])]
      (swap! latest-texts assoc room (conj texts new-text)))
    (format "%s" new-text)))

(defn- case3 [room text nick]
  (swap! previous-text assoc nick
         (assoc (get @previous-text nick {}) room text))
  (let [texts (get @latest-texts room [""])]
    (swap! latest-texts assoc room (conj texts text)))
  "")

(defn handle-post [body-str]
  (for [message (map :message (:events (read-json body-str)))
        :let [text (:text message)
              nick (:nickname message)
              room (:room message)]]
    (if (re-find #"^!help$" text)
      "* s/regexp/text/
       replaces the latest previous message.
       * s/regexp/text/ < nickname
       replaces the latest previous message by the nickname user.
       * s/regexp/text/ < @id
       replaces the latest previous message by the id user.
       NOT IMPLMENENTED YET
       * s/regexp/text/ < me
       replaces the latest previous message by yourself.
       NOT IMPLMENENTED YET
       * (NOT IMPLEMENTED) it looks up older messages if regexp didn't match
       * http://substitute-lingrbot.herokuapp.com/"
      (if-let [[_ left right _ target-nick]
               (re-find #"^s/((?:\\.|[^/])+)/((?:\\.|[^/])*)/g?\s*(<\s*@?(.*))?$" text)]
        (if target-nick
          (case1 room left right target-nick)
          (case2 room left right))
        (case3 room text nick)))))

(defn my-safe-eval [stri]
  (let [to-eval
        `(binding [*ns* *ns*]
          (ns substitute-lingrbot.core
            #_(:refer-clojure :exclude [replace])
            #_(:use [compojure.core :only (defroutes GET POST)]
                  [clojure.data.json :only (read-json)]
                  [ring.adapter.jetty :only (run-jetty)]
                  [clojure.string :only (replace join)])
            #_(:import java.util.concurrent.ExecutionException)
            #_(:gen-class))
          (eval (read-string ~stri)))]
    (try (str (eval to-eval))
      (catch Exception e (str e)))))

(defn any-handler [body headers]
  "overwrite me via repl")

(defroutes routes
  (let [start-time (java.util.Date.)]
    (GET "/" []
         (str {:version "dummy-version"
               :homepage "https://github.com/ujihisa/substitute-lingrbot"
               :from start-time
               :author "ujihisa"
               #_:previous-text #_@previous-text})))
  (POST "/" {body :body}
    (join "\n" (handle-post (slurp body))))
  (POST "/dev" {body :body headers :headers}
    (when (#{"64.46.24.16"} (headers "x-forwarded-for"))
      (my-safe-eval (slurp body))
      #_(let [body-parsed (try
                          (read-string (slurp body))
                          (catch RuntimeException e e))]
        (my-safe-eval (get body-parsed "code" nil)))))
  (ANY "*" {body :body headers :headers}
    (any-handler body headers)))

(defn -main []
  (let [port (Integer/parseInt (or (System/getenv "PORT") "8080"))]
    (run-jetty routes {:port port :join? false})))

; vim: set lispwords+=defroutes,GET,POST,ANY :
