(ns cloft.lingr
  (:require clj-http.client))

(def BOT-VERIFIER
  (apply str (drop-last (try
                          (slurp "bot_verifier.txt")
                          (catch java.io.FileNotFoundException e "")))))

(defn lingr [room msg]
  (future
    (clj-http.client/post
      "http://lingr.com/api/room/say"
      {:form-params
       {:room room
        :bot 'cloft
        :text (str msg)
        :bot_verifier BOT-VERIFIER}})))

(defn lingr-mcujm [msg]
  (lingr "mcujm" msg))
