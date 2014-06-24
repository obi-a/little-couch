(ns little-couch.core
  (:require [cheshire.core :refer :all])
  (:require [clj-http.client :as client]))

 (defn ^:private database_address
    ([x] (str (:address x) ":" (:port x) "/" (:database x)))
    ([x doc_id] (str (:address x) ":" (:port x) "/" (:database x) "/" doc_id)))

(defn db-setup
  [& args]
  (let [{:keys [database address port username password], :or {database "" address "http://127.0.0.1" port "5984" username "" password ""} }
               (first args)]
              {:database database :address address :port port :username username :password password}))

(defn create [x]
   (parse-string (:body (client/put (database_address x) {:content-type :json :throw-entire-message? true} ))
    true))

(defn delete [x]
   (parse-string (:body (client/delete (database_address x) {:throw-entire-message? true} ))
    true))

 (defn create_doc [x, doc_id, doc]
   (parse-string (:body (client/put (database_address x doc_id) {:body (generate-string doc), :throw-entire-message? true} ))
    true))

 (defn get_doc [x, doc_id]
   (parse-string (:body (client/get (database_address x doc_id) {:throw-entire-message? true} ))
    true))
