(ns lisf.util
  (:import [java.time Instant]
           [java.time.temporal ChronoUnit]
           [java.util Date]) 
  (:require [clojure.string :as string]))

(defmacro swap->> [& forms]
  "Takes the last form and places it as the second form"
  (let [last (last forms)
        forms (butlast forms)
        first (first forms)
        rest (next forms)]
    `(~first ~last ~@rest)))

(defn fmt-recent-date [date] 
  (let [formatter (java.text.SimpleDateFormat. "dd MMM HH:mm")] 
    (.format formatter date)))

(defn fmt-old-date [date] 
  (let [formatter (java.text.SimpleDateFormat. "dd MMM yyyy")] 
    (.format formatter date)))

(defn fmt-date
  "Formats the date of a file based on how recent it is"
  [^Date date]
  (let [current-date (Instant/now)
        one-year-ago (.minus current-date 180 ChronoUnit/DAYS)]
    (if (.isAfter (.toInstant date) one-year-ago)
      (fmt-recent-date date)
      (fmt-old-date date))))

(defn fmt-size 
  "Formats the size of a file
   size - size in bytes
   binary - if the size should use binary or decimal prefixes"
  [size binary?]
  (let [unit (if binary? 1024 1000)
        units (if binary? ["" "Ki" "Mi" "Gi" "Ti"] ["" "K" "M" "G" "T"])
        idx (->> (iterate #(/ % unit) size)
                 (take-while #(> % unit))
                 count)]
    (if (zero? idx)
      "-"
      (str (format "%.1f" (/ size (Math/pow unit idx))) (nth units idx)))))

(defn size-unit-to-int
  "Converts a size unit to an integer"
  [unit]
  (case unit
    nil 1
    :byte 1
    :Ki 1024
    :K 1000
    :Mi 1048576
    :M 1000000
    :Gi 1073741824
    :G 1000000000
    :Ti 1099511627776
    :T 1000000000000))

(defn parse-size
  "Gets a string representing a fmt-size and returns the size in bytes"
  [size]
  (let [[-all number unit] (re-find #"(\d+\.?\d*)(\w*)" size)]
    (if (nil? -all) 
      0
      (* (Float/parseFloat number) 
         (size-unit-to-int (keyword unit))))))

(defn parse-date
  "Parses a date dd MMM HH:mm or dd MMM yyyy to a Date object"
  [date]
  (let [formatter (if (string/includes? date ":") 
                    (java.text.SimpleDateFormat. "dd MMM HH:mm")
                    (java.text.SimpleDateFormat. "dd MMM yyyy"))]
    (.parse formatter date)))

(defn max-length 
  "Obtains the maximum length of a list of strings"
  [strings]
  (reduce max (map #(count (str %)) strings)))

(defn align-right
  "Aligns the string to the right"
  [string length]
  (str (apply str (repeat (- length (count string)) " ")) string))

(defn align-left
  "Aligns the string to the left"
  [string length]
  (str string (apply str (repeat (- length (count string)) " "))))