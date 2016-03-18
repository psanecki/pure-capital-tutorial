(ns util.date
  (:require [clojure.string :as str])
  (:import
   java.util.TimeZone
   java.text.SimpleDateFormat))


(defn rcf3339
  "java.util.Date & String -> String
  ex. (rcf3339 (new java.util.Date)) \"GMT-8\" )"
  [date & [timezone]]
  (let [timezone (or timezone  (.getID (TimeZone/getDefault)))
        sdf (new SimpleDateFormat "yyyy-MM-dd'T'HH:mm:ss.SSSSSSXXX")
        gmt (TimeZone/getTimeZone timezone)]
    (.setTimeZone sdf gmt)
    (.format
     sdf
     date))
  )

(defn rcf3339-url
  "java.util.Date & String -> String
  ex. (rcf3339-url (new java.util.Date)) \"GMT-8\" )"
  [date & [timezone]]
  (let [rcf (rcf3339 date timezone)]
    (-> (str/replace rcf  #"(:)(\d{2}$)" "")
     (str/replace #":" "%3A")
     (str/replace #".\d{6}" ""))))
