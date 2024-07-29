(ns lisf.config 
  (:require [lisf.fs :as fs]))

(def config-path
  "The path to the configuration file"
  (if (System/getenv "XDG_CONFIG_HOME") 
    (str (System/getenv "XDG_CONFIG_HOME") "/lisf.config") 
    (str (System/getenv "HOME") "/.config/lisf.config")))

(defn config-load 
  "Loads the configuration file"
  []
  (if (fs/exists? config-path)
    (load-file config-path)
    {}))