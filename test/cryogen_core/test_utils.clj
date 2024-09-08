(ns cryogen-core.test-utils
  "Utility functions used in the test suite."
  (:require [clojure.java.io :as io]
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

(defn copy-resource! 
  "Copy a file from `test/resources` to this `target-path`, first ensuring 
   that `target-path` exists."
  [resource-filename target-path]
  (let [resource-file (io/resource resource-filename)
        target-file (io/file target-path resource-filename)]
    (fs/mkdirs target-path)
    (with-open [in (io/input-stream resource-file)] (io/copy in target-file))))