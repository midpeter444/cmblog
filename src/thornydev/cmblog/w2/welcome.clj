(ns thornydev.cmblog.w2.welcome
  (:require [net.cgrand.enlive-html :as h]
            [me.raynes.fs :refer [base-name]]
            [thornydev.cmblog.w2.session-dao :as sessiondao])
  (:import (org.apache.commons.lang3 StringEscapeUtils)))


;; ---[ config settings ]--- ;;

(def welcome-html-path "resources/welcome.html")
(def redirect-route "/signup")


;; ---[ helper fns ]--- ;;

(h/deftemplate welcome-template (base-name welcome-html-path) [username]
  [:span#username] (h/content (StringEscapeUtils/escapeHtml4 username)))


;; ---[ compojure handler fn ]--- ;;

(defn show-welcome-page [cookies]
  (if-let [username (-> cookies
                        (get "session")
                        :value
                        sessiondao/find-username-by-session-id)]
    (apply str (welcome-template username))
    (do (println "welcome() can't identify the user, redirecting to signup")
        (ring.util.response/redirect redirect-route))))
