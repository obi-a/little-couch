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
   (cond (not-any? nil? [(:username x) (:password x)]) (:cookies (login x))
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
      {:database database, :address address, :port port, :username username, :password password}))

(defn create [x]
  (:body (client/put (database_address x)
                     (http-options x
                                   {:content-type :json}))))


(defn delete [x]
  (:body (client/delete (database_address x)
                        (http-options x))))

(defn create-doc [x, doc-id, doc]
  (:body (client/put (database_address x doc-id)
                     (http-options x
                                   {:body (generate-string doc)} ) )))

(defn get-doc [x, doc-id]
  (:body (client/get (database_address x doc-id)
                     (http-options x))))

 (defn delete-doc
   ([x, doc-id] (delete-doc x doc-id (:_rev (get-doc x, doc-id))))
   ([x, doc-id, rev]
     (:body (client/delete (str (database_address x doc-id) "?rev=" rev)
                           (http-options x)))))

 (defn update-doc [x, doc-id, data]
   (:body (client/put (database_address x doc-id)
                      (http-options x
                                    {:body (generate-string data)}))))

 (defn edit-doc
   [x, doc-id, data-map]
   (update-doc x
               doc-id
               (assoc data-map
                      :_rev (:_rev (get-doc x
                                            doc-id)))))

 (defn view
   [x, design-doc-name, view-name, & rest]
   (:body (client/get (str (database_address x design-doc-name) "/_view/" view-name)
                      (http-options x
                                    {:query-params (first rest)} ))))

 (defn view-only-docs
   [x, design-doc-name, view-name, & rest]
   (print-str
   (map :doc
        (:rows (view x design-doc-name view-name (assoc (first rest)
                                                        :include_docs true))))))

 (defn ^:private index [keys]
   (clojure.string/join "_" keys))

 (defn ^:private view-name [keys]
   (str "find_by_keys_"
        (index keys)))

 (defn ^:private design-doc-name [keys]
   (str "_design/" (index keys) "_keys_finder"))

 (defn ^:private add-multiple-finder [x keys]
   (create-doc x
               (design-doc-name keys)
               {:language "javascript",
                :views {
                          (view-name keys) {
                            :map (str "function(doc){ if(doc." (clojure.string/join " && doc." keys) ") emit([doc." (clojure.string/join ",doc." keys) "]);}")
                          }
                       }
                }))

 (defn ^:private dynamic-query [x attributes options-map]
   (view-only-docs x
                   (design-doc-name (map name (keys attributes)))
                   (view-name (map name (keys attributes)))
                   (assoc options-map :startkey (generate-string (vals attributes))
                                      :endkey (generate-string (vals attributes)))))

 (defn where
   [x, attributes & others ]
   (try
     (dynamic-query x attributes (first others))
     (catch Exception e
       (do
         ;;(println (.getMessage e))
         (add-multiple-finder x (map name (keys attributes)))
         (dynamic-query x attributes (first others))))))

 (defn get-security-object
   [x]
   (:body (client/get (str (database_address x) "/_security/")
                      (http-options x))))

 (defn set-security-object
   [x security-settings]
   (:body (client/put (str (database_address x) "/_security/")
                      (http-options x
                                    {:body (generate-string security-settings)}))))

 (defn ^:private couchdb-config-url
   [x section option]
   (str (:address x) ":" (:port x) "/_config/" section "/" option ))

 (defn get-config [x section option]
   (:body (client/get (couchdb-config-url x section option)
                      (http-options x))))

 (defn set-config
   [x section option value]
   (boolean (:body (client/put (couchdb-config-url x section option)
                               (http-options x
                                             {:body (generate-string value)})))))

 (defn delete-config
   [x section option]
   (boolean (:body (client/delete (couchdb-config-url x section option)
                                  (http-options x)))))





