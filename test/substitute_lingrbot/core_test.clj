(ns substitute-lingrbot.core-test
  (:require [clojure.test :refer :all]
            [substitute-lingrbot.core :refer :all]))

(defn- dummy-lingr-msg [text room]
  (format "{\"events\":[{\"message\":{\"text\":%s,\"nickname\":\"aaa\",\"room\":\"%s\"}}]}"
          (prn-str text)
          (str room)))

(deftest handle-post-test
  (testing "ignore non-substitute messages"
    (is (= [""] (handle-post (dummy-lingr-msg "hello" :room1)) ))
    (is (= [""] (handle-post (dummy-lingr-msg "world" :room1)))))

  (testing "substitute previous message"
    (is (= [""] (handle-post (dummy-lingr-msg "hello" :room1)) ))
    (is (= ["he__o"] (handle-post (dummy-lingr-msg "s/l/_/" :room1)))))

  (testing "substitute previous message, ignoring message of another room"
    (is (= [""] (handle-post (dummy-lingr-msg "hello" :room1)) ))
    (is (= [""] (handle-post (dummy-lingr-msg "world" :room2)) ))
    (is (= ["hell_"] (handle-post (dummy-lingr-msg "s/o/_/" :room1))))))
