(ns lisf.cli
  (:require [clojure.tools.cli :refer [parse-opts]]
            [lisf.fs :as fs]
            [lisf.filetype :as file])
  (:require [clojure.string :as string]))

(def options 
 [[nil "--help" "Shows this help page"]
  ["-a" "--all" "Shows hidden files"]
  ["-d" "--only-dirs" "Shows only directories"]
  ["-f" "--only-files" "Shows only files"]
  ["-i" "--icon" "Shows icons"]
  ["-l" "--long" "Shows long listing"]
  ["-h" "--human" "Shows human readable sizes"]
  ["-s" "--sort" "Sorts the output"
   :default "name"
   :parse-fn keyword]
  ["-r" "--reverse" "Reverses the output"]])

(def usage 
  (->> ["lisf"
       "Simple ls implementation in clojure"] 
      (string/join \newline)))

(defn build-list
  "Builds the list of entries of a path based on the options"
  [opts path]
  (->> (fs/lisf path) 
       ;; Build the entry list
       (map (fn [file] (let [name (fs/get-name file)] 
                         {:file file 
                          :name name 
                          :type (file/type-of name)}))) 
       ;; If not all filter hidden files
       (filter #(or (:all opts) 
                    (fs/not-hidden (:file %))))
       ;; If only-dirs filter only directories 
       (filter #(or (not (:only-dirs opts)) 
                    (fs/is-dir (:file %))))
       ;; If only-files filter only files  
       (filter #(or (not (:only-files opts)) 
                    (not (fs/is-dir (:file %)))))
       ;; Sort the output
       (sort-by (fn [entry] (.toLowerCase (:name entry))))
       ;; Reverse 
       ((if (:reverse opts) reverse identity))
       ;;Build the output
       (map #(:name %))
       (string/join \newline)))

(defn eval-args
 "Validates the cli args
   Returns {:output :status}"
 [args]
 (let [{:keys [options arguments errors summary]} (parse-opts args options)]
   (cond
     ;; Shows the help page no matter what
     (:help options) {:output (str usage "\n" summary)
                      :status :ok}
     ;; Shows the errors
     errors {:output (str errors "\n see --help for more information")
             :status :err}
     ;; Cannot use both --only-dirs and --only-files
     (and (:only-dirs options) (:only-files options)) {:output "Cannot use both --only-dirs and --only-files"
                                                       :status :err}
     ;; Only one path is allowed
     (> (count arguments) 1) {:output "Too many arguments"
                              :status :err}
     ;; Path does not exist
     (not (fs/exists? (or (first arguments) "."))) {:output (str "Directory " (first arguments) " does not exist")
                                                    :status :err}
     ;; Everything is fine
     :else {:output (build-list options (or (first arguments) "."))
            :status :ok})))