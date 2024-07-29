(ns lisf.util
  (:import [java.time Instant]
           [java.time.temporal ChronoUnit]
           [java.util Date]))

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