(ns substitute-lingrbot.core-test
  (:require [clojure.test :refer :all]
            [substitute-lingrbot.core :refer :all]))

(defn- dummy-lingr-msg [text nickname room]
  (format "{\"events\":[{\"message\":{\"text\":%s,\"nickname\":\"%s\",\"room\":\"%s\"}}]}"
          (prn-str text)
          (str nickname)
          (str room)))

(deftest handle-post-test
  (testing "ignore non-substitute messages"
    (is (= [""] (handle-post (dummy-lingr-msg "hello" :you :room1))))
    (is (= [""] (handle-post (dummy-lingr-msg "world" :you :room1)))))

  (testing "substitute previous message"
    (is (= [""] (handle-post (dummy-lingr-msg "hello" :you :room1)) ))
    (is (= ["he__o"] (handle-post (dummy-lingr-msg "s/l/_/" :you :room1)))))

  (testing "substitute previous message, ignoring message of another room"
    (is (= [""] (handle-post (dummy-lingr-msg "hello" :you :room1)) ))
    (is (= [""] (handle-post (dummy-lingr-msg "world" :you :room2)) ))
    (is (= ["hell_"] (handle-post (dummy-lingr-msg "s/o/_/" :you :room1))))))
