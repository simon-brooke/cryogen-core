(ns cryogen-core.infer-meta-test
  (:require [clojure.java.io :refer [file resource]]
            [clojure.test :refer :all]
            [cryogen-core.infer-meta :refer [clean infer-image-data
                                             infer-title main-title
                                             main-title?
                                             using-inferred-metadata]]
            [cryogen-core.markup :as m]
            [cryogen-core.test-utils :refer [copy-resource! reset-resources
                                             with-markup with-resources!]]
            [cryogen-markdown.core :refer [markdown]]))

(deftest infer-title-test
  (testing "infer-title from H1"
    (let [dom '({:tag :h1, :attrs {:id "this-is-a-test-h1"}, :content ("This is a test H1")}
                {:tag :p,
                 :attrs nil,
                 :content (list "Testing new post metadata inference.")})
          page (file "../content/md/posts/2023-06-06-this-is-a-test-filename.md")
          config {:post-date-format "yyyy-mm-dd"}
          expected "This is a test H1"
          actual (infer-title page config dom)]
      (is (= actual expected))))

  (testing "infer-title from filename"
    (let [dom (list {:tag :h2, :attrs {:id "this-is-a-test-h2"}, :content (list "This is a test H2")}
                    {:tag :p,
                     :attrs nil,
                     :content (list "Testing new post metadata inference.")})
          page (file "../content/md/posts/2023-06-06-this-is-a-test-filename.md")
          config {:post-date-format "yyyy-mm-dd"}
          expected "This is a test filename"
          actual (infer-title page config dom)]
      (is (= actual expected)))))

;; Something in the test suite clears the `content` directory at the end of 
;; the run. If we want to test inference about actual images, we need to set
;; it up, since `cryogen-core.io/get-resource` expects to get files from the
;; actual file system rather than from resources

(deftest infer-image-data-test
  (testing "extracting properties from an existing image file"
    (reset-resources)
    (copy-resource! "Test-Logo.png" "content/img")
    (let [dom (list {:tag :h1, :attrs {:id "this-is-a-test"}, :content (list "This is a test")}
                    {:tag :p,
                     :attrs nil,
                     :content
                     (list {:tag :img,
                            :attrs
                            {:src "/img/Test-Logo.png",
                             :alt "This is an image"},
                            :content nil})}
                    {:tag :p,
                     :attrs nil,
                     :content (list "Testing new post metadata inference.")})
          expected {:path "/img/Test-Logo.png", :alt "This is an image", :width 64, :height 30, :type "image/png"}
          actual (infer-image-data dom {:blog-prefix ""})]
      (is (= actual expected) "Image file does exist, so we extract properties from it"))
    (reset-resources))
  (testing "returning path only when the file does not exist"
    (let [dom (list {:tag :h1, :attrs {:id "this-is-a-test"}, :content (list "This is a test")}
                    {:tag :p,
                     :attrs nil,
                     :content
                     (list {:tag :img,
                            :attrs
                            {:src "/img/Missing-File.png",
                             :alt "This is an image"},
                            :content nil})}
                    {:tag :p,
                     :attrs nil,
                     :content (list "Testing new post metadata inference.")})
          expected "/img/Missing-File.png"
          actual (infer-image-data dom {:blog-prefix ""})]
      (is (= actual expected) "Image file does not exist, so return path only"))))

(deftest main-title-test
  (let [dom '({:tag :h1, :attrs {:id "this-is-a-test-h1"}, :content ("This is a test H1")}
              {:tag :p,
               :attrs nil,
               :content (list "Testing new post metadata inference.")}
              {:tag :p,
               :attrs nil,
               :content
               ({:tag :strong, :attrs nil, :content ("Tags:")}
                " Climate, Living Spaces")}
              {:tag :p,
               :attrs nil,
               :content ({:tag :strong, :attrs nil, :content ("Tags:")} " Ecocide")})]
    (let [expected :h1
          actual (:tag (main-title dom))]
      (is (= actual expected) "The main title should be `This is a test H1`."))
    (let [expected true
          actual (main-title? (first dom) dom)]
      (is (= actual expected) "The main title should be the main title."))))

(deftest clean-test
  (let [original '({:tag :h1, :attrs {:id "this-is-a-test-h1"}, :content ("This is a test H1")}
                   {:tag :p,
                    :attrs nil,
                    :content (list "Testing new post metadata inference.")}
                   {:tag :p,
                    :attrs nil,
                    :content
                    ({:tag :strong, :attrs nil, :content ("Tags:")}
                     " Climate, Living Spaces")}
                   {:tag :p,
                    :attrs nil,
                    :content ({:tag :strong, :attrs nil, :content ("Tags:")} " Ecocide")})
        expected '({:tag :p,
                    :attrs nil,
                    :content (list "Testing new post metadata inference.")})
        actual (clean original)]
    (is (= actual expected))))

(deftest integration-test
  (testing "exercise the whole thing"
    (with-resources! [["inferring-metadata.md" "content"] ["Test-Logo.png" "content/img"]]
      (with-markup (markdown)
        (let [expected (read-string (slurp (resource "sample-dom.edn")))
              actual (using-inferred-metadata (file "content/inferring-metadata.md") (first (m/markups))
                                              (read-string (slurp (resource "config.edn"))))]
          (is (= actual expected)))))))
