(ns substitute-lingrbot.core-test
  (:require [clojure.test :refer :all]
            [substitute-lingrbot.core :refer :all]))

(defn- dummy-lingr-msg [text room nickname]
  (format "{\"events\":[{\"message\":{\"text\":%s,\"nickname\":\"%s\",\"room\":\"%s\"}}]}"
          (prn-str text)
          (str nickname)
          (str room)))

(deftest handle-post-test
  (testing "ignore non-substitute messages"
    (is (= [""] (handle-post (dummy-lingr-msg "hello" :room1 :you))))
    (is (= [""] (handle-post (dummy-lingr-msg "world" :room1 :you)))))

  (testing "substitute previous message"
    (is (= [""] (handle-post (dummy-lingr-msg "hello" :room1 :you)) ))
    (is (= ["he__o"] (handle-post (dummy-lingr-msg "s/l/_/" :room1 :you)))))

  (testing "substitute previous message, ignoring message of another room"
    (is (= [""] (handle-post (dummy-lingr-msg "hello" :room1 :you)) ))
    (is (= [""] (handle-post (dummy-lingr-msg "world" :room2 :you)) ))
    (is (= ["hell_"] (handle-post (dummy-lingr-msg "s/o/_/" :room1 :you))))))
