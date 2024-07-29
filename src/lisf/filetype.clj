(ns lisf.filetype
  (:require [flatland.ordered.map :refer [ordered-map]]))

(def types
 "Defines the file types and its data"
 (ordered-map
  ;; Directories
  :gitfolder {:icon "\ue5fb"
              :name #"\.git/"} 
  :dir {:icon "\uf4d3"
        :name #".*/"}
  
  ;; Specific files
  :license {:icon "\udb80\udd24"
            :name #"(LICENSE|license).*"}
  :readme {:icon "\udb80\udefc"
           :name #"(README|readme).*"}
  ;; By prefix
  ;; By extension
  :txt {:icon "\uf15c"
        :name #".*\.txt"}
  :md {:icon "\ue73e"
       :name #".*\.md"}
  :clj {:icon "\ue768"
        :name #".*\.clj"}
  :java {:icon "\ue738"
         :name #".*\.java"}
  :gitfile {:icon "\ue702"
            :name #".*\.git(ignore|config)"}
  :image {:icon "\udb80\udee9"
          :name #".*\.(jpeg|jpg|png|gif|webp)"}
  ;; Catch all
  :other {:icon "\uea7b"
          :name #".*"}))

(defn data-of 
  "Gets the data for a given filetype
   If the filetype isn't known defaults to :other"
  [type] 
  (get types type (get types :other)))

(defn icon-of 
  "Gets the icon for a given filetype"
  [type]
  (get (data-of type) :icon))

(defn name-pattern
  "Gets the regex for a given filetype name"
  [type]
  (get (data-of type) :name))

 (defn type-of 
   "Gets the filetype from the filename"
   [filename]
   (some #(when (re-matches (name-pattern %) filename) %) 
         (keys types)))