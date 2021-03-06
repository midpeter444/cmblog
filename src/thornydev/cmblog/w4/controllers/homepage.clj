(ns thornydev.cmblog.w4.controllers.homepage
  (:require [clojure.string :refer [join]]
            [net.cgrand.enlive-html :as h]
            [me.raynes.fs :refer [base-name]]
            [thornydev.cmblog.w4.util :refer [escape]]            
            [thornydev.cmblog.w4.dao.session-dao :as sessiondao]
            [thornydev.cmblog.w4.dao.blogpost-dao :as postdao]))


;; ---[ config settings ]--- ;;

(def ^:const max-posts-to-display 10)

(def homepage-html-path "resources/blog-template.html")

(h/deftemplate homepage-template (base-name homepage-html-path) [username posts]
  ;; page header section
  [:div#usercontrols] (h/set-attr :class (if (seq username) "visible" "invisible"))
  [:span#username]    (h/content (escape username))

  ;; body of last 10 posts
  [:div.top-posts]
  (h/do->
   (h/add-class (if (seq posts) "visible" "invisible"))
   (h/clone-for [p posts]
                [:h2 :a] (h/do->
                          (h/set-attr :href (str "/post/" (:permalink p)))
                          (h/content (:title p)))
                [:span.post-date] (h/content (str "Posted " (:date p)))
                [:span.post-author] (h/content (str "By " (:author p)))
                [:a.num-comments] (h/do->
                                   (h/set-attr :href (str "/post/" (:permalink p)))
                                   (h/content (str (count (:comments p)))))
                [:span.post-body] (h/html-content (:body p))
                [:span.post-tags] (->> p :tags
                                       (map #(vector :a {:href (str "/tag/" %)} %))
                                       (apply h/html)
                                       (interpose " ")
                                       h/content))))

;; ---[ compojure handler fn ]--- ;;

(defn- show-posts [session-id posts]
  (let [username (-> session-id
                     sessiondao/find-username-by-session-id)])
  (apply str (homepage-template username posts))
  )


(defn show-home-page [session-id]
  (show-posts session-id
              (postdao/find-by-date-descending max-posts-to-display)))


(defn show-posts-by-tag [session-id tag]
  (show-posts session-id
              (postdao/find-by-tag-date-descending tag max-posts-to-display)))


