(ns cryogen-core.test-utils
  "Utility functions used in the test suite."
  (:require [clojure.java.io :as io]
            [cryogen-core.console-message :refer [info]]
            [cryogen-core.markup :as m]
            [me.raynes.fs :as fs])
  (:import [java.io File]))


(defn markdown []
  (reify m/Markup
    (dir [this] "md")
    (exts [this] #{".md"})))

(defn asciidoc []
  (reify m/Markup
    (dir [this] "asc")
    (exts [this] #{".asc"})))

(defn create-entry [dir file]
  (fs/mkdirs (File. dir))
  (fs/create (File. (str dir File/separator file))))

(defn reset-resources []
  (doseq [dir ["public" "content"]]
    (fs/delete-dir dir)
    (create-entry dir ".gitkeep")))

(defmacro with-markup [mu & body]
  `(do
     (m/clear-registry) ;; to be sure, to be sure, to be certain
     (m/register-markup ~mu)
     (try
       ~@body
       (finally
         (m/clear-registry)))))

;; to set this up for testing:
;; (require '[clojure.java.io :as io :refer [file resource]])
;; (require '[me.raynes.fs :as fs])
;; (require '[cryogen-core.console-message :refer [info]])

(defn copy-resource!
  "Copy a file from `test/resources` to this `target-path`, first ensuring 
   that `target-path` exists."
  [resource-filename target-path]
  (let [resource-file (io/resource resource-filename)
        target-file (io/file target-path resource-filename)]
    (info
     (format "\tcopying resource `%s` to directory `%s`..."
             resource-filename
             target-path))
    (fs/mkdirs target-path)
    (with-open [in (io/input-stream resource-file)] (io/copy in target-file))))

(defmacro with-resources!
  "Copy each of the `src` filenames in these `src-target-pairs` from my
   resources to its respective `target` directory in the current file
   system, execute this `body`, and then clean up. 
   
   Return the value of the last expression in `body`."
  [src-target-pairs & body]
  `(do
     (reset-resources)
     (info (format "\tSetting up resources %s" ~src-target-pairs))
     (try
       (doall ;; where lazy evaluation bites you in the arse...
        (map copy-resource!
             (map first ~src-target-pairs)
             (map #(nth % 1) ~src-target-pairs)))
       (do ~@body)
       (finally
         (info "\tcleaning up!")
         (reset-resources)))))