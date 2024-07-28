(ns lisf.cli
  (:require [clojure.tools.cli :refer [parse-opts]]
            [lisf.fs :as fs]
            [lisf.filetype :as file]
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
  ["-h" "--header" "Shows a header at the top of the list"]
  [nil, "--group-directories-first" "Shows directories first"]
  ["-s" "--sort" "Sorts the output"
   :default "name"
   :parse-fn keyword]
  ["-r" "--reverse" "Reverses the output"]])

(def usage 
  (->> ["lisf"
       "Simple ls implementation in clojure"] 
      (string/join \newline)))

(defn fmt-entry
  "Format a given entry for printing"
  [entry]
  (str (:dir-flag entry) 
       (:permissions entry) "  " 
       (:owner entry) "  " 
       (:name entry)))

(defn build-list
  "Builds the list of entries of a path based on the options"
  [opts path]
  (as-> (fs/lisf path) entries
    ;; Build the entry list 
    (map (fn [file]
           (let [name (fs/get-name file)]
             {:file file
              :name name
              :type (file/type-of name)}))
         entries)
    ;; If not all filter hidden files
    (if (not (:all opts))
      (filter #(fs/not-hidden (:file %)) entries)
      entries)
    ;; Sort the output
    (sort-by (fn [entry] (.toLowerCase (:name entry)))
             entries)
    ;; Group directories first
    (if (:group-directories-first opts)
      (sort-by (fn [entry] (if (fs/is-dir (:file entry)) 0 1))
               entries)
      entries)
    ;; Reverse 
    (if (:reverse opts)
      (reverse entries)
      entries)
    ;; Quote the file names
    (if (not (:no-quote opts))
      (map #(if (string/includes? (:name %) " ")
              (assoc % :name (str "\"" (:name %) "\"")) 
              %) 
           entries)
      entries)
    ;; Use icons
    (if (:icons opts)
      (map #(assoc % :name (str (file/icon (:type %)) "  " (:name %))) entries)
      entries)
    ;; If --long is used
    (if (:long opts)
      (map #(assoc % 
                   :owner (fs/get-owner (:file %)) 
                   :permissions (fs/get-file-permissions (:file %))
                   :dir-flag (fs/get-dir-flag (:file %))) 
           entries)
      entries)
    ;;Build the output
    (map fmt-entry entries)
    (string/join \newline entries)))

(defn eval-args
 "Validates the cli args
   Returns {:output :status}"
 [args]
 (let [configs (config-load)
       {:keys [options arguments errors summary]} (parse-opts args options)]
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
     :else {:output (build-list (merge configs options) (or (first arguments) "."))
            :status :ok})))