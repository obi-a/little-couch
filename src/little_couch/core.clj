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

(defn create-doc [x, doc_id, doc]
  (parse-string (:body (client/put (database_address x doc_id) {:body (generate-string doc), :throw-entire-message? true} ))
    true))

(defn get-doc [x, doc_id]
  (parse-string (:body (client/get (database_address x doc_id) {:throw-entire-message? true}))
    true))

 (defn delete-doc
   ([x, doc_id] (delete-doc x doc_id (:_rev (get-doc x, doc_id))))
   ([x, doc_id, rev]
   (parse-string (:body (client/delete (str (database_address x doc_id) "?rev=" rev) {:throw-entire-message? true} ))
    true)))

 (defn update-doc [x, doc_id, data]
   (parse-string (:body (client/put (database_address x doc_id) {:body (generate-string data), :throw-entire-message? true} ))
    true))

 (defn edit-doc [x, doc_id, data-map]
    (update-doc x doc_id
            (assoc data-map :_rev (:_rev
                                    (get-doc x doc_id)))))



