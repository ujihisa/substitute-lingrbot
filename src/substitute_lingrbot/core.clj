(ns substitute-lingrbot.core
  #_(:require [leiningen.core.project])
  #_(:refer-clojure :exclude [replace])
  (:use [compojure.core :only (defroutes GET POST)]
        [clojure.data.json :only (read-json)]
        [ring.adapter.jetty :only (run-jetty)]
        [clojure.string :only (join)])
  (:require [clojure.string :as s])
  (:import java.util.concurrent.ExecutionException)
  (:gen-class))

(def version
  "dummy"
  #_(:version (leiningen.core.project/read)))

(def previous-text (atom {}))
(def latest-texts (atom [""]))

(defn handle-post [body]
  (for [message (map :message (:events (read-json (slurp body))))
        :let [text (:text message)
              nick (:nickname message)]]
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
          (let [new-text
                (s/replace (get @previous-text target-nick "")
                                        (re-pattern (s/replace left #"\\(.)" "$1"))
                                        (s/replace right #"\\(.)" "$1"))]
            (swap! previous-text assoc target-nick new-text)
            (format "%s" new-text))
          (let [latest-text (last @latest-texts) ; TODO
                new-text
                (s/replace latest-text
                                        (re-pattern (s/replace left #"\\(.)" "$1"))
                                        (s/replace right #"\\(.)" "$1"))]
            (swap! latest-texts conj new-text)
            (format "%s" new-text)))
        (do
          (swap! previous-text assoc nick text)
          (swap! latest-texts conj text)
          "")))))

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
         (str {:version version
               :homepage "https://github.com/ujihisa/substitute-lingrbot"
               :from start-time
               :author "ujihisa"
               :previous-text @previous-text})))
  (POST "/" {body :body}
        (join "\n" (handle-post body)))
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
