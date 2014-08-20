# little-couch

Clojure Interface to CouchDB, I'm using this to learn Clojure. A clojure re-write of [Ruby's Leanback gem](https://github.com/obi-a/leanback).

## Usage

### Installation:

#### Basic Operations

Database setup uses the db-setup function:
```clojure
(def x (db-setup {:database "my_database"}))
```
When username and password is required, i.e. CouchDB is not in admin party mode:
```clojure
(def x (db-setup {:database "my_database"
                  :username "admin"
                  :password "123456"}))
```
When using a different port and address for CouchDB not the default (http://127.0.0.1:5984):
```clojure
(def x (db-setup {:database "my_database"
                  :address "https://obi.iriscouch.com"
                  :port "6984"}))
```
Create the database:
```clojure
(create x)
;; => {:ok true}
```
Delete the database:
```clojure
(delete x)
;; => {:ok true}
```
Create a document with id "linda":
```clojure
(create-doc x "linda" {:firstname "linda"
                       :lastname "smith"})
;; => {:ok true, :id "linda", :rev "1-ff286690ab5b446a727840ce7420843a"}
```
The created document will be:
```javascript
{
   "_id": "linda",
   "_rev": "1-ff286690ab5b446a727840ce7420843a",
   "firstname": "linda",
   "lastname": "smith"
}
```
Fetch document:
```clojure
(get-doc x "linda")
;; => {:_id "linda",
;;  :_rev "1-ff286690ab5b446a727840ce7420843a",
;;  :firstname "linda",
;;  :lastname "smith"}
```
Delete document using a revision value:
```clojure
(delete-doc x "linda" "1-ff286690ab5b446a727840ce7420843a")
;; => {:ok true, :id "linda", :rev "2-d689d9b5b9f2ded6a2157fc9cc84a00f"}
```
Document can also be deleted with no revision:
```clojure
(delete-doc x "linda")
;; => {:ok true, :id "linda", :rev "4-5d1a6851ec7562378caa4ce4adef9ee4"}
```
Update the document, this replaces the old document with new data, and requires a revision (_rev) to be included in the data:
```clojure
(update-doc x "linda" {:firstname "nancy"
                       :lastname "drew"
                       :_rev "5-74894db03ef6d22e6a0e4ef90b5a85fb"})
;; => {:ok true, :id "linda", :rev "6-950d16c8c39daa77fad11de85b9467fc"}
```
The resulting document after the update will be:
```javascript
{
   "_id": "linda",
   "_rev": "6-950d16c8c39daa77fad11de85b9467fc",
   "firstname": "nancy",
   "lastname": "drew"
}
```
Edit parts of a document, no revision required
```clojure
(edit-doc x "linda" {:lastname "brown"
                     :phone "777-777-7777"})
;; => {:ok true, :id "linda", :rev "7-cd16bd09becdd8db756dbc52c5aeab06"}
```
The edited version of the document will be:
```javascript
{
   "_id": "linda",
   "_rev": "7-cd16bd09becdd8db756dbc52c5aeab06",
   "firstname": "nancy",
   "phone": "777-777-7777",
   "lastname": "brown"
}
```

####Working with Desgin Documents and views

Create a design document
```clojure
(create-doc x
            "_design/my_doc"
            {:language "javascript"
             :views {
                 :by_gender {
                   :map "function(doc){ if(doc.gender) emit(doc.gender); }"
                 }}})
;; => {:ok true, :id "_design/my_doc", :rev "3-222b1f1716a195012fa291750e742e8e"}
```
Query a permanent view
```clojure
(view x "_design/my_doc" "by_gender")
;; => {:total_rows 7,
;;  :offset 0,
;;  :rows
;;  [{:id "christina", :key "female", :value nil}
;;   {:id "lisa", :key "female", :value nil}
;;   {:id "nancy", :key "female", :value nil}
;;   {:id "susan", :key "female", :value nil}
;;   {:id "james", :key "male", :value nil}
;;   {:id "kevin", :key "male", :value nil}
;;   {:id "martin", :key "male", :value nil}]}
```
The view function can also optionally take the following CouchDB query options in a map as arguments: key, limit, skip, descending, include_docs, reduce, startkey, starkey_docid, endkey, endkey_docid, inclusive_end, stale, group, group_level.

To query a permanent view by key
```clojure
(view x "_design/my_doc" "by_gender" {:key "\"male\""})
;; => {:total_rows 7,
;;  :offset 4,
;;  :rows
;;  [{:id "james", :key "male", :value nil}
;;   {:id "kevin", :key "male", :value nil}
;;   {:id "martin", :key "male", :value nil}]}
```
The above example sends a query to the view using the key "male" and returns all documents with "gender" equal to "male".

To include actual documents in the query results, add include_docs to the query options
```clojure
(view x "_design/my_doc" "by_gender" {:key "\"male\""
                                      :include_docs true})
;; => {:total_rows 7,
;;  :offset 4,
;;  :rows
;;  [{:id "james",
;;    :key "male",
;;   :value nil,
;;   :doc
;;   {:_id "james",
;;    :_rev "1-56ff4f73369bdf8350615a58e12e4c3b",
;;    :firstname "james",
;;    :state "new york",
;;    :gender "male",
;;    :city "manhattan",
;;    :age 23}}
;;  {:id "kevin",
;;   :key "male",
;;   :value nil,
;;   :doc
;;   {:_id "kevin",
;;    :_rev "1-3c6381603d9f15cb966948eb29218cf7",
;;    :firstname "kevin",
;;    :state "new york",
;;    :gender "male",
;;    :city "bronx",
;;    :age 37}}
;;  {:id "martin",
;;   :key "male",
;;   :value nil,
;;   :doc
;;   {:_id "martin",
;;    :_rev "1-41956cd527d75643171919731abd97c0",
;;    :firstname "martin",
;;    :state "new york",
;;    :gender "male",
;;    :city "manhattan",
;;    :age 29}}]}
```
To return results in descending order:
```clojure
(view x "_design/my_doc" "by_gender" {:key "\"male\""
                                      :descending true})
;; => {:total_rows 7,
;;  :offset 0,
;;  :rows
;;  [{:id "martin", :key "male", :value nil}
;;   {:id "kevin", :key "male", :value nil}
;;   {:id "james", :key "male", :value nil}]}
```
To limit the number of documents returned from the query
```clojure
(view x "_design/my_doc" "by_gender" {:limit 4})
;; => {:total_rows 7,
;;  :offset 0,
;;  :rows
;;  [{:id "christina", :key "female", :value nil}
;;   {:id "lisa", :key "female", :value nil}
;;   {:id "nancy", :key "female", :value nil}
;;   {:id "susan", :key "female", :value nil}]}
```
Skip some documents in the query
```clojure
(view x "_design/my_doc" "by_gender" {:skip 2})
;; => {:total_rows 7,
;;  :offset 2,
;;  :rows
;;  [{:id "nancy", :key "female", :value nil}
;;   {:id "susan", :key "female", :value nil}
;;   {:id "james", :key "male", :value nil}
;;   {:id "kevin", :key "male", :value nil}
;;   {:id "martin", :key "male", :value nil}]}
```

####Dynamic Queries
Dynamic queries can be performed on documents using the ```where``` function, example to fetch all documents that match the attributes {city: "bronx", gender: "female"}. This returns a sequence of all the documents with the match attributes.
```clojure
(where x {:city "bronx", :gender "female"})
;; => ({:_id "christina",
;;  :_rev "1-e9782aa92f7d88eb5dc5e1a878c8e193",
;;  :firstname "christina",
;;  :state "new york",
;;  :gender "female",
;;  :city "bronx",
;;  :age 22}
;; {:_id "nancy",
;;  :_rev "1-44ac471d9e6433eaa6e67607c7a175c9",
;;  :firstname "nancy",
;;  :state "new york",
;;  :gender "female",
;;  :city "bronx",
;;  :age 25})
```
```where``` supports options skip, limit, descending. Example to return documents in descending order;
```clojure
(where x
       {:city "bronx", :gender "female"}
       {:descending true})
;; => ({:_id "nancy",
;;  :_rev "1-44ac471d9e6433eaa6e67607c7a175c9",
;;  :firstname "nancy",
;;  :state "new york",
;;  :gender "female",
;;  :city "bronx",
;;  :age 25}
;; {:_id "christina",
;;  :_rev "1-e9782aa92f7d88eb5dc5e1a878c8e193",
;;  :firstname "christina",
;;  :state "new york",
;;  :gender "female",
;;  :city "bronx",
;;  :age 22})
```
See Ruby's [Leanback](https://github.com/obi-a/leanback/blob/master/README.md#dynamic-queries) for details on how ```where``` works

####Security Object
To set the database security object
```clojure
(set-security-object x {:admins {:names ["david"], :roles ["admin"]},
                        :readers {:names ["david"], :roles ["admin"]}})
;; => {:ok true}
```
Retrieve the security object
```clojure
(get-security-object x)
;; => {:readers {:roles ["admin"], :names ["david"]},
;; :admins {:roles ["admin"], :names ["david"]}}
```
####CouchDB Configuration API
To retrieve a couchDB configuration
```clojure
(def y (db-setup))

;;form: (get-config y section option)
;;returns corresonding for the option value

(get-config y "log" "file")
;; => "/var/log/couchdb/couch.log"

(get-config y "httpd" "port")
;; => "5984"
```
To set a configuration:
```clojure
;;form: (set-config y section option value)

(set-config y "couch_httpd_auth" "timeout" "6000")
;;true

(get-config y "couch_httpd_auth" "timeout")
;; => "6000"
```
To delete a configuration:
```clojure
;;form: (delete-config y section option)

(delete-config y "foo" "bar")
;; => true
```

## License

Copyright © 2014 FIXME

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
