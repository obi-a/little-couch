# little-couch

Clojure Interface to CouchDB, I'm using this to learn Clojure. A clojure re-write of [Ruby's Leanback gem](https://github.com/obi-a/leanback).

## Usage

##Specification
Create design document

```clojure
(def x (db-setup {:database "my_database"}))

;;form: (create-doc x doc-id data)

(create-doc x
            "_design/my_doc"
            {:language "javascript",
             :views {
               :get_emails {
                             :map "function(doc){ if(doc.firstname && doc.email) emit(doc.id,{Name: doc.firstname, Email: doc.email}); }"
                           }}})
;;{:ok true, :id "_design/my_doc", :rev "1-271b38abf2ac551c5263be4ba9ab56df"}

(get-doc x "_design/my_doc")
;;{:_id "_design/my_doc", :_rev "1-271b38abf2ac551c5263be4ba9ab56df", :language "javascript", :views {:get_emails {:map "function(doc){ if(doc.firstname && doc.email) emit(doc.id,{Name: doc.firstname, Email: doc.email}); }"}}}
```

Query the view

```clojure
;;form: (view x design-doc-name view-name options-map)

(view x
      "_design/my_doc"
      "get_emails")
;;{:total_rows 2, :offset 0, :rows [{:id "linda", :key nil, :value {:Name "linda", :Email "linda@southmunn.com"}} {:id "sam", :key nil, :value {:Name "sam", :Email "obi@cc.com"}}]}


(view x
      "_design/my_doc"
      "get_emails"
      {:limit 1
       :descending true
       :include_docs true})
;;{:total_rows 2, :offset 0, :rows [{:id "sam", :key nil, :value {:Name "sam", :Email "obi@cc.com"}, :doc {:_id "sam", :_rev "6-4aad2696c425c3782d6dc9d18c596564", :nice "watch", :email "obi@cc.com", :firstname "sam"}}]}

```

Dynamic Queries
```clojure
;;form: where(x attributes-map options-map)
```

Sample usage
```clojure
(create-doc x
            "christina"
            {:firstname "christina"
             :state "new york"
             :gender "female"
             :city "bronx"
             :age 22})

(create-doc x
            "james"
            {:firstname "james"
             :state "new york"
             :gender "male"
             :city "manhattan"
             :age 23})

(create-doc x
            "kevin"
            {:firstname "kevin"
             :state "new york"
             :gender "male"
             :city "bronx"
             :age 37})

(create-doc x
            "lisa"
            {:firstname "lisa"
             :state "new york"
             :gender "female"
             :city "manhattan"
             :age 31})

(create-doc x
            "_design/gender_city"
            {:language "javascript",
             :views {
                       :people_by_gender_and_city {
                              :map "function(doc){ if(doc.gender && doc.city && doc.age) emit([doc.gender, doc.city, doc.age]);}"
                            }}})
(view x
      "_design/gender_city"
      "people_by_gender_and_city"
      {:startkey  "[\"female\", \"bronx\", 22]"
       :endkey  "[\"female\", \"bronx\", 22]"})
;;{:total_rows 4, :offset 0, :rows [{:id "christina", :key ["female" "bronx" 22], :value nil}]}

(where x {:city "bronx"})
;;"({:_id christina, :_rev 2-4ea790e405726bebe5967da666b98435, :age 22, :gender female, :state new york, :city bronx, :firstname christina, :email aol} {:_id kevin, :_rev 1-eed2e9e37bd289a4acbbc4fb9329cfe2, :age 37, :gender male, :state new york, :city bronx, :firstname kevin})"

(where x {:city "bronx"})
;;"({:_id christina, :_rev 2-4ea790e405726bebe5967da666b98435, :age 22, :gender female, :state new york, :city bronx, :firstname christina, :email aol} {:_id kevin, :_rev 1-eed2e9e37bd289a4acbbc4fb9329cfe2, :age 37, :gender male, :state new york, :city bronx, :firstname kevin})"

(where x {:city "bronx" :gender "male"})
;;"({:_id kevin, :_rev 1-eed2e9e37bd289a4acbbc4fb9329cfe2, :age 37, :gender male, :state new york, :city bronx, :firstname kevin})"

(where x {:gender "female" :age 22 :email "aol"})
;;"({:_id christina, :_rev 2-4ea790e405726bebe5967da666b98435, :age 22, :gender female, :state new york, :city bronx, :firstname christina, :email aol})"

```

Security object

```clojure
(set-security-object x {:admins {:names ["david"], :roles ["admin"]},
                         :readers {:names ["david"], :roles ["admin"]}})
;;{:ok true}

(get-security_object x)
;;{:readers {:roles ["admin"], :names ["david"]}, :admins {:roles ["admin"], :names ["david"]}}
```

CouchDB Configuration
```clojure
(def y (db-setup))

;;form: (get-config y section option)
;;returns corresonding for the option value

(get-config y "log" "file")
;;"/var/log/couchdb/couch.log"

(get-config y "httpd" "port")
;;"5984"


;;form: (set-config y section option value)

(set-config y "couch_httpd_auth" "timeout" "6000")
;;true

(get-config y "couch_httpd_auth" "timeout")
;;"6000"


;;form: (delete-config y section option)

(delete-config y "foo" "bar")
;;true
```


## License

Copyright © 2014 FIXME

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
