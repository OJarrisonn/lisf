(ns lisf.cli
  (:require [clojure.tools.cli :refer [parse-opts]]
            [lisf.fs :as fs]
            [lisf.listing :as listing]
            [lisf.config :refer [config-load]])
  (:require [clojure.string :as string]))

(def options 
 [[nil "--help" "Shows this help page"]
  ["-a" "--all" "Shows hidden files"]
  ["-d" "--only-dirs" "Shows only directories"]
  ["-f" "--only-files" "Shows only files"]
  ["-i" "--icons" "Shows icons (require nerd fonts)"]
  [nil "--no-quote" "Does not quote file names with spaces"]
  ["-l" "--long" "Shows long listing"]
  ["-g" "--group" "Shows the group of each file"]
  [nil, "--group-directories-first" "Shows directories first"]
  ["-s" "--sort FIELD" "Sorts the output"
   :default :name
   :parse-fn keyword
   :validate [#(contains? listing/sort-fields %) "Invalid sort field"]]
  ["-r" "--reverse" "Reverses the output"]])

(def usage 
  (->> ["lisf"
       "Simple ls implementation in clojure"] 
      (string/join \newline)))

(defn eval-args
 "Validates the cli args
   Returns {:output :status}"
 [args]
 (let [{:keys [options arguments errors summary]} (parse-opts args options)
       configs (config-load) 
       options (merge configs options)]
   (cond
     ;; Shows the help page no matter what
     (:help options) 
     {:output (str usage "\n" summary) 
      :status :ok}
     
     ;; Shows the errors
     errors 
     {:output (str errors "\n see --help for more information") 
      :status :err}
     
     ;; Cannot use both --only-dirs and --only-files
     (and (:only-dirs options) (:only-files options)) 
     {:output "Cannot use both --only-dirs and --only-files" 
      :status :err}
     
     ;; Only one path is allowed
     (> (count arguments) 1) 
     {:output "Too many arguments" 
      :status :err}
     
     ;; Path does not exist
     (not (fs/exists? (or (first arguments) "."))) 
     {:output (str "Directory " (first arguments) " does not exist") 
      :status :err}
     
     ;; Everything is fine
     :else 
     {:output 
      (->> (or (first arguments) ".") 
           (listing/build-entry-list options)
           (listing/fmt-entry-list options))
      :status :ok})))