(ns little-couch.core
  (:require [cheshire.core :refer :all])
  (:require [clj-http.client :as client]))


(defn db-setup
  [& args]
  (let [{:keys [database address port username password], :or {database "" address "http://127.0.0.1" port "5984" username "" password ""} }
               (first args)]
              {:database database :address address :port port :username username :password password}))

(defn create [x]
  (parse-string (get (client/put (str (:address x) ":" (:port x) "/" (:database x)) {:content-type :json}) :body)
    true))
