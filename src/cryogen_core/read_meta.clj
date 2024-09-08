(ns cryogen-core.read-meta
  "Read embedded metadata as frontmatter from a file. This version by Simon 
   Brooke, extending Dmitri Sotnikov's version which read EDN only."
  (:require [clj-yaml.core :as yaml]
            [clojure.data.json :as json]
            [clojure.java.io :as io]
            [clojure.string :refer [join]]
            [cryogen-core.console-message :refer [error info warn]]
            [cryogen-core.schemas :as schemas]
            [schema.core :as s])
  (:import (java.io File PushbackReader)
           (java.util.regex Pattern)))

(declare delimiters)

(defn extract-frontmatter
  "Extract, and return as a map, front matter from the text of these `lines`, 
   expected to be in this `format` (if supplied), or to have these delimiters
   (if supplied). If the first line does not match the expected `start-delimiter`,
   return `nil` immediately without throwing an exception."
  ([lines ^Pattern start-delimiter ^Pattern end-delimiter]
   (when (re-matches start-delimiter (first lines))
     (let [text (join "\n" (take-while #(not (re-matches end-delimiter %)) (rest lines)))]
       {:text text
        :metadata-count (+ (count text) 8)})))
  ([lines ^clojure.lang.Keyword format]
   (if (delimiters format)
     (let [fm (extract-frontmatter lines
                          (-> delimiters format :start :re)
                          (-> delimiters format :end :re))]
      ;;  (info (format "Read `%s` as frontmatter" fm))
       (assoc fm :metadata-format format))
     (throw (Exception. (format "Unknown frontmatter format '%s'" format))))))

(defn parse-yaml
  "Attempt to parse frontmatter from these `lines` as YAML."
  [lines]
  (let [frontmatter (extract-frontmatter lines :yaml)]
    (dissoc 
     (merge frontmatter (yaml/parse-string (:text frontmatter)))
     :text)))

(defn parse-json
  "Attempt to parse frontmatter from these `lines` as JSON."
  [lines] 
  (let [frontmatter (extract-frontmatter lines :json)]
            (dissoc 
             (merge frontmatter (json/read-str (:text frontmatter) :key-fn keyword)) 
             :text)))

(def ^:const delimiters {:json  {:end {:string "```" :re #"```"}
                                 :parse-fn parse-json
                                 :start {:string "```json" :re #"^``` *json$"}}
                         :yaml {:end {:string "..." :re #"^[\.-]{3}$"}
                                :parse-fn parse-yaml
                                :start {:string "---" :re #"^---$"}}})

(defn read-page-meta
  "Returns the metadata encoded as frontmatter of file `f`, considered as a
   markdown page/post. `rdr`. if passed, is expected to be a reader open on 
   that file."
  ;; I'm really sceptical of whether what we want is a pushback reader at all, 
  ;; unless we can get character by character parsing of alternative formats.
  ;; In any case, if reading EDN fails, the pushback reader is going to be in
  ;; the wrong position.
  ([^File f ^PushbackReader rdr]
   (let [edn (try (read rdr) (catch Exception not-edn
                               (warn (format "Not EDN: %s" (.getMessage not-edn)))))
         metadata (if (map? edn) (assoc edn :metadata-format :edn)
                          (let [lines (remove empty? (line-seq (io/reader f)))]
                            (first
                             (remove empty?
                                     (map #(try
                                             ((-> delimiters % :parse-fn) lines)
                                             (catch Throwable any (error (.getMessage any))))
                                          (keys delimiters))))))
         to-skip (or (:metadata-count metadata) 0)]
     (do (info (format "Skipping %d characters of metadata" to-skip))
         (take to-skip rdr)
         (s/validate schemas/MetaData metadata))))
  ([^File f] 
     (read-page-meta f (java.io.PushbackReader. (io/reader f)))))