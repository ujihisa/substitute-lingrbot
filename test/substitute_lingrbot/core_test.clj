(ns substitute-lingrbot.core-test
  (:require [clojure.test :refer :all]
            [substitute-lingrbot.core :refer :all]))

(deftest handle-post-test
  (testing "ignore non-substitute messages"
    (handle-post "{\"events\":[{\"message\":{\"text\":\"hello\",\"nick\":\"aaa\",\"room\":\"bbb\"}}]}")
    (is (= [""] (handle-post "{\"events\":[{\"message\":{\"text\":\"hello\",\"nick\":\"aaa\",\"room\":\"bbb\"}}]}"))))

  (testing ""))
