(ns little-couch.core-test
  (:require [clojure.test :refer :all]
            [little-couch.core :refer :all]))

(deftest test-create
  (testing "it creates a database."
    (let [x (db-setup {:database "test-db"})]
      (is (= {:ok true} (create x)))
        (do (delete x))))
  (testing "it throws an exception when database already exists"
    (let [x (db-setup {:database "mytest-db"})]
      (do (create x))
        (is (thrown? Exception (create x)))
          (do (delete x))))
  (testing "throws an exception when it cannot connect to database"
    (let [x (db-setup {:database "mytest-db" :port "9999"})]
      (is (thrown? java.net.ConnectException (create x))))))
