(ns little-couch.core
  (:require [cheshire.core :refer :all])
  (:require [clj-http.client :as client]))

(use '[clojure.tools.namespace.repl :only (refresh)])


(defn ^:private login [x]
 (client/post (str (:address x) ":" (:port x) "/_session/")
                            {:as :json, :throw-entire-message? true
                             :body (generate-string {:name (:username x), :password (:password x) })
                             :content-type :json}))

(defn ^:private auth-session
  [x]
   (cond
     (not-any? nil? [(:username x) (:password x)])
          (:cookies (login x))
      :else ""))

(defn ^:private http-options
   ([x] {:as :json, :throw-entire-message? true, :cookies (auth-session x)})
   ([x options](merge options (http-options x))))


(defn ^:private database_address
   ([x] (str (:address x) ":" (:port x) "/" (:database x)))
   ([x doc-id] (str (:address x) ":" (:port x) "/" (:database x) "/" doc-id)))

 (defn db-setup
  [& args]
  (let [{:keys [database address port username password], :or {database "" address "http://127.0.0.1" port "5984" username nil password nil} }
               (first args)]
              {:database database :address address :port port :username username :password password}))

(defn create [x]
   (:body (client/put (database_address x) (http-options x {:content-type :json}))))


(defn delete [x]
  (:body (client/delete (database_address x) (http-options x))))

(defn create-doc [x, doc-id, doc]
  (:body (client/put (database_address x doc-id) (http-options x {:body (generate-string doc)} ) )))

(defn get-doc [x, doc-id]
  (:body (client/get (database_address x doc-id) (http-options x))))

 (defn delete-doc
   ([x, doc-id] (delete-doc x doc-id (:_rev (get-doc x, doc-id))))
   ([x, doc-id, rev]
      (:body (client/delete (str (database_address x doc-id) "?rev=" rev) (http-options x)))))

 (defn update-doc [x, doc-id, data]
   (:body (client/put (database_address x doc-id) (http-options x {:body (generate-string data)}))))

 (defn edit-doc [x, doc-id, data-map]
    (update-doc x doc-id
            (assoc data-map :_rev (:_rev
                                    (get-doc x doc-id)))))

 (defn view [x, design-doc-name, view-name, & rest]
   (:body (client/get (str (database_address x design-doc-name) "/_view/" view-name) (http-options x {:query-params (first rest)} ))))




