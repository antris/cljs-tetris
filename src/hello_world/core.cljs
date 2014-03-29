(ns hello-world.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require
            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [goog.events :as events]
            [cljs.core.async :refer [<! put! chan]]
            [clojure.string :as string]))

;(use '[clojure.string :only (join split)])

;#{:i, :o, :t, :s, :z, :j, :l}


(def tetrominoes [:i :o :t :s :z :j :l])

(def tetromino-shapes {
    :i '("0000" "1111")
    :o '("0110" "0110")
    :t '("0010" "1110")
    :s '("0110" "1100")
    :z '("1100" "0110")
    :j '("1000" "1110")
    :l '("0010" "1110")
  })

(defn empty-cell-as-html [] (dom/td #js {:className "cell empty"}))
(defn block-as-html [] (dom/td #js {:className "cell block"}))
(defn cell-as-html [cell] (if (= cell "1") (block-as-html) (empty-cell-as-html)))
(defn row-as-html [row] (apply dom/tr nil (map cell-as-html row)))

(defn print-tetromino [tetromino]
  (string/join (tetromino tetromino-shapes)))

(defn tetromino-elem [app]
  (reify
    om/IRender
    (render [this]
        (apply dom/table #js {:className "tetromino"} (map row-as-html ((:tetromino app) tetromino-shapes))))))

(def app-state (atom {:tetromino (rand-nth tetrominoes)}))

(om/root
  tetromino-elem
  app-state
  {:target (. js/document (getElementById "tetris"))})


