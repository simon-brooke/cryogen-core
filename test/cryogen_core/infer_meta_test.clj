(ns cryogen-core.infer-meta-test
  (:require [clojure.java.io :refer [file]]
            [clojure.test :refer :all]
            [cryogen-core.infer-meta :refer [clean infer-description
                                             infer-title infer-toc-status main-title
                                             main-title?]]
            [cryogen-core.markup :refer [Markup]]))

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

;; Not wanting to pollute the repository by adding an image just for testing.
;; (deftest infer-image-data-test
;;   (let [dom (list {:tag :h1, :attrs {:id "this-is-a-test"}, :content (list "This is a test")}
;;              {:tag :p,
;;               :attrs nil,
;;               :content
;;               (list {:tag :img,
;;                 :attrs
;;                 {:src "/blog/img/uploads/refugeeswelcome.png",
;;                  :alt "This is an image"},
;;                 :content nil})}
;;              {:tag :p,
;;               :attrs nil,
;;               :content (list "Testing new post metadata inference.")})
;;         expected "/img/uploads/refugeeswelcome.png"
;;         actual (infer-image-data dom {:blog-prefix "/blog"})]
;;     (is (= actual expected))))

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

(deftest infer-toc-status-test
  (let [dom '({:tag :h1, 
               :attrs {:id "test-document-with-six-subheads"},
               :content ("Test document with six subheads")}
              {:tag :h2
               :content ("Part one")}
              {:tag :p,
               :attrs nil,
               :content ("Lorem ipsum dolor sit amet, consectetur adipiscing elit. Pellentesque malesuada et ante sit amet scelerisque. Nam a velit justo. Duis maximus quis quam non dapibus. Aenean auctor eros vel lacus dapibus imperdiet. Aliquam eu nibh pharetra, euismod arcu eget, ullamcorper lorem. Nulla at dictum lacus, a pretium velit. Ut ac est nec arcu dictum dapibus. ")}
              {:tag :h2
               :content ("Part two")}
              {:tag :p,
               :attrs nil,
               :content ("Phasellus mi est, blandit at sodales a, commodo et nisl. Pellentesque euismod urna id nibh laoreet aliquet. Sed eros ante, varius ut nunc nec, varius pulvinar ex. Nam vitae dui non tortor rhoncus cursus non id elit. Quisque ullamcorper leo neque, eget ornare sem dictum ac. In consectetur, lacus at varius consectetur, sem elit dapibus risus, sit amet dapibus elit metus imperdiet orci. Maecenas sagittis nisi tellus, nec vestibulum lectus mollis ac. Praesent in mauris quis leo mattis laoreet dictum eget arcu.")}
              {:tag :h2
               :content ("Part three")}
              {:tag :p,
               :attrs nil,
               :content ("Donec viverra, enim quis scelerisque egestas, enim lectus sodales diam, sit amet lobortis nisl dui et magna. Nulla vitae tempor nunc. Mauris eleifend lacus in justo tincidunt suscipit. Pellentesque nisl risus, elementum eget lobortis non, malesuada sit amet dolor. Nulla facilisi. Nulla non massa fermentum, suscipit arcu et, tristique turpis. Vivamus fermentum lorem et ligula vestibulum, id tincidunt erat pretium. ")}
              {:tag :h3
               :content ("Section 3.1")}
              {:tag :p,
               :attrs nil,
               :content ("Morbi gravida elit sed erat vehicula viverra. Aenean sollicitudin vulputate tincidunt. Phasellus luctus viverra magna. Cras maximus, est ac scelerisque tristique, felis libero condimentum urna, at sollicitudin velit sapien sit amet nulla. Nullam non arcu ut orci dictum malesuada vel sed orci. Aliquam egestas lacinia leo, vel congue nunc. Nam pellentesque lectus est, eu tincidunt dolor ornare quis. ")}
              {:tag :h3
               :content ("Section 3.2")}
              {:tag :p,
               :attrs nil,
               :content ("Mauris rutrum nec leo id commodo. Proin lobortis fringilla augue, fermentum fermentum mi rutrum id. Nam dignissim aliquet magna. Quisque malesuada ante in vestibulum volutpat. Vivamus et odio rutrum metus ullamcorper efficitur. Mauris quis gravida ligula. Praesent tempus orci eget iaculis rhoncus.")}
              {:tag :h2
               :content ("Part four")}
              {:tag :p,
               :attrs nil,
               :content ("Donec gravida diam eu pretium sagittis. Proin ac nibh est. Mauris efficitur ex sed iaculis sagittis. Ut ac velit tortor. Nam leo enim, convallis ac blandit vitae, iaculis vitae nisi. Integer ut est a risus luctus vehicula ut at est. Praesent volutpat diam sit amet ante volutpat finibus. Phasellus finibus faucibus lobortis. Vestibulum euismod vitae nibh eu feugiat. Ut tristique ut ex sit amet semper. Suspendisse ac dictum odio, quis gravida ipsum. Praesent a massa in lectus congue gravida nec faucibus lacus. Nullam lacinia pretium pulvinar.")}
              {:tag :p,
               :attrs nil,
               :content
               ({:tag :strong, :attrs nil, :content ("Tags:")}
                " Test, Lorem Ipsum")}
              {:tag :p,
               :attrs nil,
               :content ({:tag :strong, :attrs nil, :content ("Tags:")} " Pig-latin")})]
    (let [config {}
          expected true
          actual (infer-toc-status dom config)]
      (is (= actual expected)
          "No value in config, therefore default to four, therefore true"))
    (let [config {:toc-min-subheads 0}
          expected false
          actual (infer-toc-status dom config)]
      (is (= actual expected)
          "Zero in config, therefore disable function, therefore false"))
    (let [config {:toc-min-subheads :invalid}
          expected false
          actual (infer-toc-status dom config)]
      (is (= actual expected)
          "Invalid config, therefore disable function, therefore false"))
    (let [config {:toc-min-subheads 3}
          expected true
          actual (infer-toc-status dom config)]
      (is (= actual expected)
          "Three in config, there are (more than) three subheads, therefore true"))
    (let [config {:toc-min-subheads 6}
          expected true
          actual (infer-toc-status dom config)]
      (is (= actual expected)
          "Six in config, there are (exactly) six subheads, therefore true"))
    (let [config {:toc-min-subheads 7}
          expected false
          actual (infer-toc-status dom config)]
      (is (= actual expected)
          "Seven in config, there are fewer than seven subheads, therefore false"))))

(deftest infer-description-test
    (let [first-p-content "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Pellentesque malesuada et ante sit amet scelerisque. Nam a velit justo. Duis maximus quis quam non dapibus. Aenean auctor eros vel lacus dapibus imperdiet. Aliquam eu nibh pharetra, euismod arcu eget, ullamcorper lorem. Nulla at dictum lacus, a pretium velit. Ut ac est nec arcu dictum dapibus."
          dom '({:tag :h1, 
               :attrs {:id "test-document-with-subheads"},
               :content ("Test document with subheads")}
              {:tag :h2
               :content ("Part one")}
              {:tag :p,
               :attrs nil,
               :content (first-p-content)}
              {:tag :h2
               :content ("Part two")}
              {:tag :p,
               :attrs nil,
               :content ("Phasellus mi est, blandit at sodales a, commodo et nisl. Pellentesque euismod urna id nibh laoreet aliquet. Sed eros ante, varius ut nunc nec, varius pulvinar ex. Nam vitae dui non tortor rhoncus cursus non id elit. Quisque ullamcorper leo neque, eget ornare sem dictum ac. In consectetur, lacus at varius consectetur, sem elit dapibus risus, sit amet dapibus elit metus imperdiet orci. Maecenas sagittis nisi tellus, nec vestibulum lectus mollis ac. Praesent in mauris quis leo mattis laoreet dictum eget arcu.")})
          expected first-p-content
          actual (infer-description nil {} dom)]
      (is (= actual expected))))

(defn- markdown []
  (reify Markup
    (dir [this] "md")
    (exts [this] #{".md"})))

;; (deftest infer-klipse-status-test
;;   (let [text "So, the core of the idea is that you pay a small amount on your first hectare of land, a little bit more on your next hectare, a little bit more on your next, and so on. There are two key numbers in this idea: the constant, which is the amount of money you pay on the first hectare, and the exponent, which is the power the number of hectares we've counted so far is raised to to calculate the little bit more. So what you pay in tax is Σ1..n(cn)e, where n is the number of hectares you own.
 
;;  Expressed as a clojure function, that's:
 
;;  ``` klipse-cljs
;;   (defn expt [x n] (reduce * (repeat n x)))

;;  (defn summed-exponential-series 
;;   \"Sum an exponential series from 1 to limit.
   
;;    `constant`: the constant by which integers in the range are multiplied;
;;    `exponent`: the exponent to which they are raised;
;;    `limit`: the limit of the range to be summed.\"
;;   [constant exponent limit]
;;   (reduce 
;;     + 
;;     (map 
;;       #(expt 
;;          (* constant %) 
;;          exponent) 
;;       (range 1 limit))))
;;  ```
;;  "
;;  dom (trimmed-html-snippet 
;;       ((render-fn (markdown)) (java.io.PushbackReader. (StringReader. text ))))
;;  expected true
;;  actual (infer-klipse-status dom)]
;;  (is (= actual expected)
;;      "Source tagged as `klipse-cljs`, therefore klipse, therefore true"))
;;    (let [text "So, the core of the idea is that you pay a small amount on your first hectare of land, a little bit more on your next hectare, a little bit more on your next, and so on. There are two key numbers in this idea: the constant, which is the amount of money you pay on the first hectare, and the exponent, which is the power the number of hectares we've counted so far is raised to to calculate the little bit more. So what you pay in tax is Σ1..n(cn)e, where n is the number of hectares you own.
 
;;  Expressed as a clojure function, that's:
 
;;  ``` clojure
;;   (defn expt [x n] (reduce * (repeat n x)))

;;  (defn summed-exponential-series 
;;   \"Sum an exponential series from 1 to limit.
   
;;    `constant`: the constant by which integers in the range are multiplied;
;;    `exponent`: the exponent to which they are raised;
;;    `limit`: the limit of the range to be summed.\"
;;   [constant exponent limit]
;;   (reduce 
;;     + 
;;     (map 
;;       #(expt 
;;          (* constant %) 
;;          exponent) 
;;       (range 1 limit))))
;;  ```
;;  "
;;          dom (trimmed-html-snippet
;;               ((render-fn (markdown)) (java.io.PushbackReader. (StringReader. text))))
;;          expected false
;;          actual (infer-klipse-status dom)]
;;      (is (= actual expected) 
;;          "Source tagged as `clojure`, therefore not klipse, therefore false.")))