(ns little-couch.core
  (:require [cheshire.core :refer :all])
  (:require [clj-http.client :as client]))


(defn setup [& args]
  (def settings (first args))
  (def database (get settings :database ""))
  (def address (get settings :address "http://127.0.0.1"))
  (def port (get settings :port "5984"))
  (def username (get settings :username))
  (def password (get settings :password))
   settings)

(defn create []
  (parse-string (get (client/put (str address ":" port "/" database) {:content-type :json}) :body)
    true))

