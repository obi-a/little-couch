(ns little-couch.core-test
  (:require [clojure.test :refer :all]
            [little-couch.core :refer :all]))

(defn db [db-name & others]
  (db-setup {:database db-name
             :port (or (first others)
                       "5984")}))

(defn unique-db []
  (db (str "testdb" (System/currentTimeMillis))))

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
    (let [x (unique-db)]
      (do (create x))
        (is (= {:ok true} (delete x)))))
  (testing "it throws an exception when the database doesn't exist"
    (let [x (db "dont_exist")]
      (is (thrown? clojure.lang.ExceptionInfo (delete x))))))

(deftest test-create-doc
  (let [x (unique-db)]
    (do (create x))
      (testing "it creates a document"
        (is (= "linda"
               (:id (create-doc x "linda" {})))))
      (testing "it can create a document with keyword or number id"
        (is (= "1"
               (:id (create-doc x 1 {})))))
        (is (= ":linda"
               (:id (create-doc x :linda {}))))
      (testing "it throws an exception when document already exists"
        (is (thrown? clojure.lang.ExceptionInfo  (do (create-doc x "samedoc" {})
                                                     (create-doc x "samedoc" {})))))
    (do (delete x))))


