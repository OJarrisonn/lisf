(ns lisf.listing 
  (:require [lisf.fs :as fs]
            [lisf.filetype :as ft]
            [lisf.util :as util]
            [clojure.string :as string]))

(def fields [:icon :name :size :date :owner :permissions :dir-flag])

(defn fmt-entry
  "Format a given entry for printing aligned"
  [entry lengths]
  (cond->> (:name entry)
    ;; Include the link target
    (:link-target entry)
    (util/swap->> str " -> " (:link-target entry))

    ;; Include the icon
    (:icon entry)
    (str (:icon entry) " ")

    ;; Include the date
    (:date entry)
    (str (util/align-left (:date entry) (:date lengths)) "  ")

    ;; Include the owner
    (:owner entry)
    (str (util/align-left (:owner entry) (:owner lengths)) "  ")

    ;; Include the file size
    (:size entry)
    (str (util/align-right (:size entry) (:size lengths)) "  ")

    ;; Include the permissions
    (:permissions entry)
    (str (:permissions entry) "  ")

    ;; Include the directory/link flag
    (:dir-flag entry)
    (str (:dir-flag entry))))

(defn into-entry
  "Converts a file into an entry"
  [file]
  {:file file
   :name (fs/get-name file)
   :type (ft/type-of (fs/get-name file))})

(defn quote-name
  "Quotes the name of a file if it has spaces"
  [entry]
  (if (string/includes? (:name entry) " ")
      (assoc entry :name (str "\"" (:name entry) "\""))
      entry))

(defn long-fields
  "Associates the long fields to the entry"
  [entry]
  (assoc entry
         :link-target (if (fs/link? (:file entry)) 
                        (fs/get-link-target (:file entry)) 
                        nil)
         :size (util/fmt-size (fs/get-size (:file entry)) true)
         :date (util/fmt-date (fs/get-date (:file entry)))
         :owner (fs/get-owner (:file entry))
         :permissions (fs/get-file-permissions (:file entry))
         :dir-flag (fs/get-dir-flag (:file entry))))

(defn build-entry-list
  "Builds the list of entries of a path based on the options"
  [opts path]
  (cond->> (fs/list-files path)
    ;; Build the entry list 
    true 
    (map into-entry)

    ;; If not all filter hidden files
    (not (:all opts)) 
    (filter #(not (fs/hidden? (:file %))))

    ;; Sort the output
    true 
    (sort-by #(.toLowerCase (:name %)))

    ;; Group directories first
    (:group-directories-first opts)
    (sort-by #(if (fs/dir? (:file %)) 0 1))

    ;; Reverse 
    (:reverse opts) 
    reverse

    ;; Quote the file names
    (not (:no-quote opts))
    (map quote-name)

    ;; Use icons
    (:icons opts)
    (map #(assoc % :icon (ft/icon-of (:type %))))
    ;; If --long is used
    (:long opts)
    (map long-fields)))

(defn field-max-length
  "Obtains the maximum length of a field in the entry list"
  [entries field]
  (util/max-length (map #(get % field) entries)))

(defn fmt-entry-list
  "Format the entry list"
  [entries]
  (let [lengths (zipmap fields (map #(field-max-length entries %) fields))]
    (->> entries
         (map #(fmt-entry % lengths))
         (string/join \newline))))