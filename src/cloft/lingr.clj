(ns cloft.lingr
  (:require clj-http.client))

(defn lingr [room msg]
  (future
    (clj-http.client/post
      "http://lingr.com/api/room/say"
      {:form-params
       {:room room
        :bot 'cloft
        :text (str msg)
        :bot_verifier BOT-VERIFIER}})))


