(ns imstransport-web-site.component.message-repository-test
  (:require [clojure.test :refer :all]
            [imstransport-web-site.component.message-repository :refer :all]
            [com.stuartsierra.component :as component]))

(def repo (component/start
           (message-repository {:filename "testmsg.properties"})))

(deftest basic-repo-behaviour
  (testing "Repo will return a message for flag"
    (is (= (get-message repo :flag-1 nil) "Message1")))
  (testing "Repo will return a message for flag and empty args list"
    (is (= (get-message repo :flag-1 '()) "Message1")))
  (testing "Repo will return a message for flag and args list"
    (is (= (get-message repo :flag-1 '(1)) "Message1")))
  (testing "Repo will return argumented message with one arg"
    (is (= (get-message repo :flag-2 "A") "Message2 with A")))
  (testing "Repo will return argumented message with one arg list"
    (is (= (get-message repo :flag-2 '("A")) "Message2 with A")))
  (testing "Repo will return argumented message with additional arguments list"
    (is (= (get-message repo :flag-2 '("A" "B")) "Message2 with A")))
  (testing "Repo will return argumented message with multiple arguments list"
    (is (= (get-message repo :flag-3 ["A" 1]) "Message with A and 1")))
  (testing "Repo will return actual message with no args list"
    (is (= (get-message repo :flag-3 nil) "Message with %s and %d"))))
