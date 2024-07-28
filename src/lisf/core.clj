(ns lisf.core
  (:gen-class)
  (:require [lisf.filetype :as file])
  (:require [lisf.fs :as fs]))

(defn fmt-entry 
  "Format the file entry"
  [file]
  (let [name (fs/get-name file)
        ftype (file/type-of name)
        ficon (file/icon ftype)] 
    (str ficon "  " name)))

; Entry point for lisf
(defn -main
  "lisf is ls implementation in clojure"
  [& args]
  (let [dir (if (empty? args) "." (first args))
        files (fs/lisf dir)]
    (doseq [file files]
      (println (fmt-entry file)))))
