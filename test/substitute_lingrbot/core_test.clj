(ns substitute-lingrbot.core-test
  (:require [clojure.test :refer :all]
            [substitute-lingrbot.core :refer :all]))

(defn- dummy-lingr-msg [text]
  (format "{\"events\":[{\"message\":{\"text\":%s,\"nick\":\"aaa\",\"room\":\"bbb\"}}]}"
          (prn-str text)))

(deftest handle-post-test
  (testing "ignore non-substitute messages"
    (handle-post (dummy-lingr-msg "hello"))
    (is (= [""] (handle-post (dummy-lingr-msg "world")))))

  (testing ""))
