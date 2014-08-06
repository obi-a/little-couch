(ns little-couch.core-test
  (:require [clojure.test :refer :all]
            [little-couch.core :refer :all]))

(defn db [db-name & others]
  (let [[port & more] others]
  (db-setup {:database db-name
             :port (or port
                       "5984")})))

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

 (defn testing-create-doc [x]
   (testing "it creates a document"
     (is (= true
            (:ok (create-doc x "linda" {})))))
   (testing "it can create a document with keyword or number id"
     (is (= true
            (:ok (create-doc x 1 {})))))
     (is (= true
            (:ok (create-doc x :linda {}))))
   (testing "it throws an exception when document already exists"
     (is (thrown? clojure.lang.ExceptionInfo (do (create-doc x "samedoc" {})
                                                 (create-doc x "samedoc" {}))))))

(deftest test-create-doc
  (let [x (unique-db)]
    (do (create x))
    (testing-create-doc x)
    (do (delete x))))

(defn testing-delete-doc [x id rev]
  (testing "it deletes a document with revision"
    (is (= true
           (:ok (delete-doc x id rev)))))
  (testing "raises an exception when revision is not found"
    (is (thrown? clojure.lang.ExceptionInfo (delete-doc x id "unknown-revision"))))
  (testing "it deletes a document without a revision"
    (do (create-doc x "goodstuff" {}))
    (is (= true
           (:ok (delete-doc x "goodstuff"))))))

(deftest test-delete-doc
  (let [x (unique-db)]
    (do (create x)
        (create-doc x "nancy" {}))
    (testing-delete-doc x
                        "nancy"
                        (:_rev (get-doc x "nancy")))
    (do (delete x))))

(defn testing-get-doc [x id]
  (testing "it gets a document"
    (is (= id
           (:_id (get-doc x id)))))
  (testing "it throws an exception when document is not found"
    (is (thrown? clojure.lang.ExceptionInfo (get-doc x "dont_exist")))))

(deftest test-get-doc
  (let [x (unique-db)]
    (do (create x)
        (create-doc x "john" {}))
    (testing-get-doc x "john")
    (do (delete x))))

(defn testing-update-doc [x id rev]
  (testing "updates a document using a revision"
    (is (= true
           (:ok (update-doc x id {:_rev rev
                                  :lastname "huey"}))))
    (is (= "huey"
           (:lastname (get-doc x id))))))

(deftest test-update-doc
  (let [x (unique-db)]
    (do (create x))
    (testing-update-doc x
                        "james"
                        (:rev (create-doc x "james" {:firstname "james"
                                                     :lastname "brown"})))
    (do (delete x))))

(deftest test-edit-doc
  (let [x (unique-db)]
    (do (create x)
        (create-doc x "simpson" {:firstname "bart" :lastname "simpson"}))
    (testing "it edits a document's existing data"
      (is (= true
             (:ok (edit-doc x "simpson" {:firstname "homer"}))))
      (is (= "homer"
             (:firstname (get-doc x "simpson")))))
    (testing "it edits a document adding new data"
      (is (= true
             (:ok (edit-doc x "simpson" {:email "simpson@example.com"}))))
      (is (= "simpson@example.com"
             (:email (get-doc x "simpson")))))
    (testing "it raises an exception when document cannot be found"
      (is (thrown? clojure.lang.ExceptionInfo
                   (edit-doc x "dont_exist" {:address "something"}))))
    (do (delete x))))


(deftest test-security-object
  (let [x (unique-db)]
    (do (create x))
    (testing "it sets a security object"
      (is (= true
             (:ok (set-security-object x {:admins {:names ["david"], :roles ["admin"]},
                                          :readers {:names ["david"], :roles ["admin"]}}))))
      (is (= (get-security-object x)
             {:admins {:names ["david"], :roles ["admin"]},
              :readers {:names ["david"], :roles ["admin"]}})))
    (testing "it clears the security object"
      (do (set-security-object x {}))
      (is (= {}
             (get-security-object x))))
    (do (delete x))))

(deftest test-queries
  (let [x (unique-db)]
    (do (create x)
        (create-doc x "christina" {:firstname "christina", :state "new york", :gender "female", :city "bronx", :age 22})
        (create-doc x "james" {:firstname "james", :state "new york", :gender "male", :city "manhattan", :age 23})
        (create-doc x "kevin" {:firstname "kevin", :state "new york", :gender "male", :city "bronx", :age 37})
        (create-doc x "lisa" {:firstname "lisa", :state "new york", :gender "female", :city "manhattan", :age 31})
        (create-doc x "martin" {:firstname "martin", :state "new york", :gender "male", :city "manhattan", :age 29})
        (create-doc x "nancy" {:firstname "nancy", :state "new york", :gender "female", :city "bronx", :age 25})
        (create-doc x "susan" {:firstname "susan", :state "new york", :gender "female", :age 35, :fullname ["susan", "Lee"]})
        (create-doc x "_design/my_doc" {:language "javascript",
                                        :views {
                                           :by_gender {
                                                :map "function(doc){ if(doc.gender) emit(doc.gender); }"
                                            }}})
        (create-doc x "_design/ages"  {:language "javascript",
                                       :views {
                                          :people_by_age {
                                             :map "function(doc){ if(doc.age) emit(doc.age); }"
                                          }}})
       (create-doc x "_design/gender_city" {:language "javascript",
                                            :views {
                                               :people_by_gender_and_city {
                                                 :map "function(doc){ if(doc.gender && doc.city && doc.age) emit([doc.gender, doc.city, doc.age]);}"
                                               }}}))
    (testing "view: queries a permanent view"
      (let [results (view x "_design/my_doc" "by_gender")]
        (is (= 7
               (count (:rows results))))
        (is (= "christina"
               (:id (first (:rows results)))))))
    (do (delete x))))
