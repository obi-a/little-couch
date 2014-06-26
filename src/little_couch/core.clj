(ns little-couch.core
  (:require [cheshire.core :refer :all])
  (:require [clj-http.client :as client]))

(use '[clojure.tools.namespace.repl :only (refresh)])

(defn ^:private database_address
   ([x] (str (:address x) ":" (:port x) "/" (:database x)))
   ([x doc_id] (str (:address x) ":" (:port x) "/" (:database x) "/" doc_id)))

(defn http-options
   ([] {:as :json, :throw-entire-message? true})
   ([option-map](merge option-map (http-options))))

(defn auth-session
  ;fix the errors in the request format
  [x]
   (cond
     (not-any? nil? [(:username x) (:password x)])
           (client/post (str
                            (database_address x) "/_session/")
                            (http-options
                               {:body (str "name=" (:username x) "&password=" (:password x))
                                           :content_type "application/x-www-form-urlencoded"}))
      :else ""))

 (defn db-setup
  [& args]
  (let [{:keys [database address port username password], :or {database "" address "http://127.0.0.1" port "5984" username nil password nil} }
               (first args)]
              {:database database :address address :port port :username username :password password}))

(defn create [x]
   (:body (client/put (database_address x) (http-options {:content-type :json}))))


(defn delete [x]
  (:body (client/delete (database_address x) (http-options))))

(defn create-doc [x, doc_id, doc]
  (:body (client/put (database_address x doc_id) (http-options {:body (generate-string doc)}) )))

(defn get-doc [x, doc_id]
  (:body (client/get (database_address x doc_id) (http-options))))

 (defn delete-doc
   ([x, doc_id] (delete-doc x doc_id (:_rev (get-doc x, doc_id))))
   ([x, doc_id, rev]
      (:body (client/delete (str (database_address x doc_id) "?rev=" rev) (http-options)))))

 (defn update-doc [x, doc_id, data]
   (:body (client/put (database_address x doc_id) (http-options {:body (generate-string data)}))))

 (defn edit-doc [x, doc_id, data-map]
    (update-doc x doc_id
            (assoc data-map :_rev (:_rev
                                    (get-doc x doc_id)))))



