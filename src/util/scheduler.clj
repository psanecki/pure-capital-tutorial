(ns util.scheduler)

(defn every-nmsec
  [f millis]
  (let [scheduler (java.util.concurrent.Executors/newScheduledThreadPool 1)]
    (.scheduleAtFixedRate
     scheduler f
     1 millis
     java.util.concurrent.TimeUnit/MILLISECONDS)
    scheduler))

(defn shutdown
  [scheduler]
  (.shutdown scheduler))
