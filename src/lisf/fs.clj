(ns lisf.fs 
  (:require [clojure.java.io :as io])
  (:import [java.io File]
           [java.nio.file Files LinkOption]
           [java.nio.file.attribute PosixFilePermissions PosixFileAttributes]))

(defn exists?
  "Check if the given path exists"
  [path]
  (.exists (io/file path)))

(def hidden?
  "Check if the file is hidden"
  #(.isHidden %))

(def dir?
  "Check if the file is a directory"
  #(.isDirectory %))

(defn link?
  "Check if the file is a symbolic link"
  [file]
  (-> file .toPath Files/isSymbolicLink))

(defn get-link-target
  "Obtains the target of a symbolic link"
  [file]
  (-> file .toPath Files/readSymbolicLink))

(defn list-files
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

(defn get-owning-group
  "Obtains the owning group of a file"
  [^File file]
  (let [path (.toPath file)
        attrs (Files/readAttributes path PosixFileAttributes (into-array LinkOption [LinkOption/NOFOLLOW_LINKS]))]
    (.getName (.group attrs))))

(defn get-file-permissions 
  "Obtains the permissions of a file as a string"
  [file]
  (let [path (.toPath file)
        posix-permissions (Files/getPosixFilePermissions 
                           path 
                           (into-array LinkOption [LinkOption/NOFOLLOW_LINKS]))]
    (PosixFilePermissions/toString posix-permissions)))

(defn get-owner
  "Obtains the owner name of a file"
  [file]
  (let [path (.toPath file)
        owner (Files/getOwner path (into-array LinkOption [LinkOption/NOFOLLOW_LINKS]))]
    (.getName owner)))

(defn get-size
  "Obtains the size of a file"
  [file]
  (if (or (dir? file) (link? file))
    0
    (.length file)))

(defn get-date
  "Obtains the last modified date of a file"
  [file]
  (let [date (.lastModified file)]
    (java.util.Date. date)))

(defn get-dir-flag
  "Obtains the directory flag"
  [file]
  (cond 
    (link? file) "l"
    (dir? file) "d"
    :else "."))