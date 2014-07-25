(ns little-couch.core-test
  (:require [clojure.test :refer :all]
            [little-couch.core :refer :all]))

(defn db [db-name & others]
  (db-setup {:database db-name
             :port (or (first others)
                       "5984")}))

(deftest test-create
  (testing "it creates a database."
    (let [x (db "test-db")]
      (is (= {:ok true} (create x)))
        (do (delete x))))
  (testing "it throws an exception when database already exists"
    (let [x (db "mytest-db")]
      (do (create x))
        (is (thrown? Exception (create x)))
          (do (delete x))))
  (testing "it throws an exception when it cannot connect to database"
    (let [x (db "mytest-db" "9999")]
      (is (thrown? java.net.ConnectException (create x)))))
  (testing "it throws an exception when database name is non string"
    (let [x (db 1)]
      (is (thrown? clojure.lang.ExceptionInfo (create x))))))


(deftest test-delete
  (testing "it deletes a database"
    (let [x (db (str "testdb" (System/currentTimeMillis)))]
      (do (create x))
        (is (= {:ok true} (delete x)))))
  (testing "it throws an exception when the database doesn't exist"
    (let [x (db "dont_exist")]
      (is (thrown? clojure.lang.ExceptionInfo (delete x))))))

