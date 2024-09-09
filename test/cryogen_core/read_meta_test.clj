(ns cryogen-core.read-meta-test
  (:require [clojure.java.io :refer [file resource]]
            [clojure.test :refer :all]
            [cryogen-core.read-meta :refer [read-page-meta]]))


(defn- reader-string [s]
  (java.io.PushbackReader. (java.io.StringReader. s)))

(deftest test-metadata-parsing
  (testing "Parsing page/post configuration"
    (let [valid-metadata   (reader-string "{:layout :post :title \"Hello World\"}")
          invalid-metadata (reader-string "{:layout \"post\" :title \"Hello World\"}")]
      (is (read-page-meta nil valid-metadata))
      (is (thrown? Exception (read-page-meta nil invalid-metadata))))))

(deftest read-meta-test
  (let [common-metadata {:author "Simon Brooke",
                         :date "2024-09-05"}]
    (testing "Reading metadata in EDN format"
      (let [expected (merge common-metadata
                            {:metadata-format :edn,
                             :description "Test document with EDN metadata",
                             :tags ["Test" "EDN"],
                             :title "EDN Metadata Test"})
            actual (read-page-meta (resource "with-edn-metadata.md"))]
        (is (= actual expected))))(testing "Reading metadata in JSON format"
      (let [expected (merge common-metadata
                            {:metadata-format :json,
                             :description "Test document with JSON metadata",
                             :tags ["Test" "JSON"],
                             :title "JSON Metadata Test"})
            actual (read-page-meta (resource "with-json-metadata.md"))]
        (is (= actual expected))))
    (testing "reading metadata in YAML format"
      (let [expected (merge common-metadata
                            {:metadata-format :yaml,
                             :description "Test document with YAML metadata",
                             :tags ["Test" "YAML"],
                             :title "YAML Metadata Test"})
            actual (read-page-meta (resource "with-yaml-metadata.md"))]
        (is (= actual expected))))))

