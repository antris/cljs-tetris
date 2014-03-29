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
    :i '(
         "    "
         "iiii")
    :o '(
         " oo "
         " oo ")
    :t '(
         "  t "
         "ttt ")
    :s '(
         " ss "
         "ss  ")
    :z '(
         "zz  "
         " zz ")
    :j '(
         "j   "
         "jjj ")
    :l '(
         "  l "
         "lll ")
  })
(defn cell-as-html [cell]
    (case cell
      " " (dom/td #js {:className "cell cell-empty"})
      "i" (dom/td #js {:className "cell cell-i"})
      "o" (dom/td #js {:className "cell cell-o"})
      "t" (dom/td #js {:className "cell cell-t"})
      "s" (dom/td #js {:className "cell cell-s"})
      "z" (dom/td #js {:className "cell cell-z"})
      "j" (dom/td #js {:className "cell cell-j"})
      "l" (dom/td #js {:className "cell cell-l"})))
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


