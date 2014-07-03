# little-couch

Clojure Interface to CouchDB, I'm using this to learn Clojure. A clojure re-write of [Ruby's Leanback gem](https://github.com/obi-a/leanback).

## Usage

##Specification
Create design document

```clojure
(def x (db-setup {:database "my_database"}))

(create-doc x "_design/my_doc"
{
 :language "javascript",
 :views {
   :get_emails {
     :map "function(doc){ if(doc.firstname && doc.email) emit(doc.id,{Name: doc.firstname, Email: doc.email}); }"
   }
 }
})
;;{:ok true, :id "_design/my_doc", :rev "1-271b38abf2ac551c5263be4ba9ab56df"}

(get-doc x "_design/my_doc")
;;{:_id "_design/my_doc", :_rev "1-271b38abf2ac551c5263be4ba9ab56df", :language "javascript", :views {:get_emails {:map "function(doc){ if(doc.firstname && doc.email) emit(doc.id,{Name: doc.firstname, Email: doc.email}); }"}}}
```

Query the view

```clojure
;;form: (view x design-doc-name view-name options-map)

(view x "_design/my_doc" "get_emails")
;;{:total_rows 2, :offset 0, :rows [{:id "linda", :key nil, :value {:Name "linda", :Email "linda@southmunn.com"}} {:id "sam", :key nil, :value {:Name "sam", :Email "obi@cc.com"}}]}


(view x "_design/my_doc" "get_emails" {:limit 1, :descending true, :include_docs true})
;;{:total_rows 2, :offset 0, :rows [{:id "sam", :key nil, :value {:Name "sam", :Email "obi@cc.com"}, :doc {:_id "sam", :_rev "6-4aad2696c425c3782d6dc9d18c596564", :nice "watch", :email "obi@cc.com", :firstname "sam"}}]}

```


## License

Copyright © 2014 FIXME

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
