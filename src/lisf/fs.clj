(ns lisf.fs 
  (:require [clojure.java.io :as io]))

(defn exists?
  "Check if the given path exists"
  [path]
  (.exists (io/file path)))

(defn lisf
  "List all files in a given directory
   dir - directory to list files
   If the directory is a file, it will return the file itself"
  [dir]
  (let [directory (io/file dir)]
    (if (.isDirectory directory)
      (.listFiles directory) 
      [directory])))

(defn get-name
  "Gets the name of a given file"
  [file]
  (str
   (.getName file)
   (if (.isDirectory file)
     "/"
     "")))