(ns lisf.core
  (:gen-class)
  (:require [lisf.filetype :as file])
  (:require [lisf.fs :as fs])
  (:require [lisf.cli :as cli]))

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
  
  (let [{:keys [output status]} (cli/eval-args args)]
    (println output)
    (System/exit (if (= status :ok) 0 1))))