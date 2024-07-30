(ns lisf.listing 
  (:require [lisf.fs :as fs]
            [lisf.filetype :as ft]
            [lisf.util :as util]
            [clojure.string :as string]))

(def fields [:icon :name :size :date :user :group :permissions :dir-flag])
(def sort-fields #{:name :size :date :user :group})
(def header-fields {:name "Name" 
                    :date "Date Modified" 
                    :user "User"
                    :group "Group"
                    :size "Size"
                    :permissions "Permissions"})

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
    
    ;; Include the group
    (:group entry)
    (str (util/align-left (:group entry) (:group lengths)) "  ")

    ;; Include the owner
    (:user entry)
    (str (util/align-left (:user entry) (:user lengths)) "  ")

    ;; Include the file size
    (:size entry)
    (str (util/align-right (:size entry) (:size lengths)) "  ")

    ;; Include the permissions
    (:permissions entry)
    (str (:permissions entry) "  ")

    ;; Include the directory/link flag
    (:dir-flag entry)
    (str (:dir-flag entry))))

(defn fmt-short-entry
  [entry]
  (cond->> (:name entry)
    (:icon entry)
    (str (:icon entry) " ")))

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
         :user (fs/get-owner (:file entry))
         :permissions (fs/get-file-permissions (:file entry))
         :dir-flag (fs/get-dir-flag (:file entry))))

(defn sort-entries
  [criteria entries]
  (let [sortfn (case criteria 
                 :name #(compare (.toLowerCase (:name %1)) (.toLowerCase (:name %2))) 
                 :size #(compare (util/parse-size (:size %1)) (util/parse-size (:size %2)))  
                 :date #(compare (util/parse-date (:date %2)) (util/parse-date (:date %1)))  
                 :user #(compare (.toLowerCase (:user %1)) (.toLowerCase (:user %2)))
                 :group #(compare (.toLowerCase (:group %1)) (.toLowerCase (:group %2))))]
    (sort sortfn entries)))

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

    ;; If --long is used
    (:long opts)
    (map long-fields)

    ;; Show the group
    (:group opts)
    (map #(assoc % :group (fs/get-owning-group (:file %))))

    ;; Sort the output
    true 
    (sort-entries (:sort opts))

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
    (map #(assoc % :icon (ft/icon-of (:type %))))))

(defn field-max-length
  "Obtains the maximum length of a field in the entry list"
  [entries field]
  (util/max-length (map #(get % field) entries)))

;; (defn fmt-header
;;   "Formats the header of the list"
;;   [lengths] 
;;   (->> header-fields
;;        (filter (fn [[k v]] (contains? lengths k)))
;;        (map (fn [[k v]] (util/align-left v (get lengths k))))
;;        (string/join "  ")))

(defn display-list
  "Displays the entries in a list"
  [opts entries]
  (let [lengths (zipmap fields (map #(field-max-length entries %) fields))
        fmt (->> entries
                 (map #(fmt-entry % lengths))
                 (string/join \newline))] 
    fmt))

(defn fmt-entry-list
  "Format the entry list"
  [opts entries]
  (display-list opts entries))