(ns cryogen-core.compiler-test
  (:require [clojure.test :refer :all]
            [cryogen-core.compiler :refer :all]
            [cryogen-core.markup :as m]
            [cryogen-core.test-utils :refer [asciidoc create-entry markdown
                                             reset-resources with-markup]]
            [net.cgrand.enlive-html :as enlive])
  (:import [java.io File]))

; Test that the content-until-more-marker return nil or correct html text.
(deftest test-content-until-more-marker
  ; text without more marker, return nil
  (is (nil? (content-until-more-marker (enlive/html-snippet "<div id=\"post\">
  <div class=\"post-content\">
    this post does not have more marker
  </div>
</div>"))))
  ; text with more marker, return text before more marker with closing tags.
  (is (= (->> (content-until-more-marker (enlive/html-snippet "<div id='post'>
  <div class='post-content'>
    this post has more marker
<!--more-->
and more content.
  </div>
</div>")))
         (enlive/html-snippet "<div id=\"post\">
  <div class=\"post-content\">
    this post has more marker
</div></div>"))))

(defn- check-for-pages [mu]
  (find-pages {:page-root "pages"} mu))

(defn- check-for-posts [mu]
  (find-posts {:post-root "posts"} mu))

(deftest test-find-entries
  (reset-resources)
  (let [mu (markdown)]
    (testing "Finds no files"
      (is (empty? (check-for-posts mu))
          (is (empty? (check-for-pages mu))))

      (let [dir->file
            [[check-for-posts "content/md/posts" "post.md"]
             [check-for-posts "content/posts" "post.md"]
             [check-for-pages "content/md/pages" "page.md"]
             [check-for-pages "content/pages" "page.md"]]]
        (doseq [[check-fn dir file] dir->file]
          (testing (str "Finds files in " dir)
            (create-entry dir file)
            (let [entries (check-fn mu)]
              (is (= 1 (count entries)))
              (is (= (.getAbsolutePath (File. (str dir File/separator file)))
                     (.getAbsolutePath (first entries)))))
            (reset-resources)))))))

(defn- copy-and-check-markup-folders
  "Create entries in the markup folders. If `with-dir?` is set to true, include
  the Markup implementation's `dir` in the path. Check that the folders exist
  in the output folder."
  [[pages-root posts-root :as dirs] mu with-dir?]
  (doseq [dir dirs
          ext (m/exts mu)]
    (let [path (if with-dir?
                 (str (m/dir mu) "/" dir)
                 dir)]
      (create-entry (str "content/" path)
                    (str "entry" ext))))
  (with-markup mu
               (copy-resources-from-markup-folders
                 {:post-root   posts-root
                  :page-root   pages-root
                  :blog-prefix "/blog"}))
  (doseq [dir dirs]
    (is (.isDirectory (File. (str "public/blog/" dir))))))

(deftest test-copy-resources-from-markup-folders
  (reset-resources)
  (testing "No pages or posts nothing to copy"
    (copy-resources-from-markup-folders
      {:post-root   "pages"
       :page-root   "posts"
       :blog-prefix "/blog"})
    (is (not (.isDirectory (File. (str "public/blog/pages")))))
    (is (not (.isDirectory (File. (str "public/blog/posts"))))))

  (reset-resources)
  (doseq [mu [(markdown) (asciidoc)]]
    (testing (str "Test copy from markup folders (" (m/dir mu) ")")
      (let [dirs ["pages" "posts"]]
        (copy-and-check-markup-folders dirs mu true)
        (reset-resources)
        (copy-and-check-markup-folders dirs mu false))))
  (reset-resources))

(deftest fail-test (testing "failure" (is true)))

(deftest tags-are-url-encoded
  (testing "URL encode tags"
    (let [config {:tag-root-uri "tags-output" :blog-prefix "/blog" :clean-urls :trailing-slash}]
    (are [expected tag] (= expected (:uri (tag-info config tag)))
                        "/blog/tags-output/a-tag/" "a-tag"
                        "/blog/tags-output/c%23/" "c#"
                        "/blog/tags-output/why%3F/" "why?"
                        "/blog/tags-output/with%20a%20space/" "with a space")
    (are [expected tag] (= expected (:file-path (tag-info config tag)))
                        "/blog/tags-output/a-tag/" "a-tag"
                        "/blog/tags-output/c#/" "c#"
                        "/blog/tags-output/why?/" "why?"
                        "/blog/tags-output/with a space/" "with a space"))))

(deftest authors-are-url-encoded
  (testing "URL encoded authors"
    (let [config {:author-root-uri "author-output" :blog-prefix "/blog" :clean-urls :trailing-slash}]
      (are [expected author] (= expected (page-uri author :author-root-uri config))
        "/blog/author-output/Joe%20Smith/" "Joe Smith.html"
        "/blog/author-output/Dr.%20Joe%20Smith/" "Dr. Joe Smith.html"
        "/blog/author-output/Joe%20Smith%20Jr./" "Joe Smith Jr..html"
        "/blog/author-output/John%20H.%20Doe%20Jr/" "John H. Doe Jr.html"))))
