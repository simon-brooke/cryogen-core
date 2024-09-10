(ns cryogen-core.console-message
  "Semantic wrapper for printing coloured messages to the console."
  (:require [clojure.pprint :as pprint]
            [clojure.string :as s]
            [clojure.stacktrace :refer [print-cause-trace print-stack-trace]]
            [text-decoration.core :refer [blue cyan green red yellow]])
  (:import [clojure.lang IExceptionInfo]))

(defn error
  "Print this error `message` to the console."
  [message]
  (binding [*print-readably* nil]
    (print (red "Error: ")
           (s/replace (yellow
                       (cond (instance? IExceptionInfo message)
                             (with-out-str (pprint/pprint (ex-data message)))
                             (instance? Throwable message)
                             {:message (.getMessage message)
                              :stack-trace (with-out-str
                                             (print-stack-trace message 10))
                              :causes (with-out-str
                                        (print-cause-trace message))}
                             :else message))
                      "\n" "\n\t"))))

(defmacro warn
  "Print this warning `message` to the console."
  [message]
  `(println (yellow ~message)))

(defmacro info
  "Print this informational `message` to the console."
  [message]
  `(println (blue ~message)))

(defmacro result
  "Print this result `value` to the console."
  [value]
  `(println "-->" (cyan ~value)))

(defmacro notice
  "Print this notice `message` to the console."
  [message]
  `(println (green ~message)))
