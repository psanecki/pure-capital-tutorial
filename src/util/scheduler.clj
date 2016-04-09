(ns util.scheduler)

(defn every-nmsec
  [millis f]
  (let [scheduler (java.util.concurrent.Executors/newScheduledThreadPool 1)]
    (.scheduleAtFixedRate
     scheduler f
     1 millis
     java.util.concurrent.TimeUnit/MILLISECONDS)
    scheduler))

(defn shutdown
  [scheduler]
  (.shutdown scheduler))
