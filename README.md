# little-couch

Clojure Interface to CouchDB, I'm using this to learn Clojure. A clojure re-write of [Ruby's Leanback gem](https://github.com/obi-a/leanback).

## Usage

##Specification
Create design documents

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

###views
```clojure
(view x design-doc-name, view-name, & options)
```


## License

Copyright © 2014 FIXME

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
