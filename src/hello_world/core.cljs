(ns hello-world.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require
            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [goog.events :as events]
            [cljs.core.async :refer [<! put! chan]]
            [clojure.string :as string]))

(def tetrominoes [:i :o :t :s :z :j :l])

(def tetromino-shapes {
    :i [
         "    "
         "iiii"]
    :o [
         " oo "
         " oo "]
    :t [
         "  t "
         "ttt "]
    :s [
         " ss "
         "ss  "]
    :z [
         "zz  "
         " zz "]
    :j [
         "j   "
         "jjj "]
    :l [
         "  l "
         "lll "]
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
(defn row-as-html [row] (apply dom/tr nil (mapv cell-as-html row)))

(defn print-tetromino [tetromino]
  (string/join (tetromino tetromino-shapes)))

(defn tetromino-elem [tetromino]
  (apply dom/table #js {:className "tetromino"}
    (mapv row-as-html (tetromino tetromino-shapes))))

(def FIELD_HEIGHT 20)
(def FIELD_WIDTH 10)

(def empty-play-field (repeat FIELD_HEIGHT (apply str (repeat FIELD_WIDTH " "))))

(defn put-piece-to-row [cells piece-cells x]
  (concat (take x cells) piece-cells (drop (+ x (count piece-cells)) cells)))
(defn put-piece-to-rows [rows piece-rows x]
  (mapv put-piece-to-row rows piece-rows (repeat (count rows) x)))
(defn put-piece-into-field [rows piece x y]
  (def piece-rows (piece tetromino-shapes))
  (def piece-height (count piece-rows))
  (concat (take y rows) (put-piece-to-rows (take piece-height (drop y rows)) piece-rows x) (drop (+ y piece-height) rows)))
(defn play-field-elem [app]
  (def play-field (:play-field app))
  (def current-piece (:current-piece app))
  (def current-piece-x (:current-piece-x app))
  (def current-piece-y (:current-piece-y app))
  (apply dom/table #js {:className "playField"}
         (mapv row-as-html
           (put-piece-into-field play-field current-piece current-piece-x current-piece-y))))

(def app-state (atom {
                 :play-field empty-play-field
                 :next-piece (rand-nth tetrominoes)
                 :current-piece (rand-nth tetrominoes)
                 :current-piece-x 4
                 :current-piece-y 0
               }))

(defn next-piece-elem [next-piece]
  (dom/div #js {:className "nextPiece"} (tetromino-elem next-piece)))

(defn app [state]
  (reify
    om/IRender
    (render [this]
      (dom/div nil
        (play-field-elem state)
        (next-piece-elem (:next-piece state))))))

(defn get-cells-in-row [index, row]
  (mapv #(vector % index) (mapv first (remove #(= (second %) " ") (map-indexed vector row)))))
(defn piece-cells [piece] (apply concat (map-indexed get-cells-in-row (piece tetromino-shapes))))

(defn within-play-field [x y]
  (and
   (and (>= x 0) (< x FIELD_WIDTH))
   (and (>= y 0) (< y FIELD_HEIGHT)))
  )

(defn move-piece-if-legal [piece x y direction-x direction-y]
  (let [
        cells (piece-cells piece)
        moved-cells (mapv (fn [v] [(+ (first v) x direction-x) (+ (second v) y direction-y)]) cells)
        all-within-play-field (every? #(apply within-play-field %) moved-cells)
        ]
     (if all-within-play-field
       [piece (+ x direction-x) (+ y direction-y)]
       [piece x y])))

(defn make-piece-fall [state]
  (def current-y (:current-piece-y state))
  (def new-y (if (> current-y (- FIELD_HEIGHT 3))
               current-y
               (+ 1 current-y)
               ))
  (swap! app-state assoc :current-piece-y new-y))

(events/listen js/document.body goog.events.EventType.KEYDOWN
    (fn [e] (let [
                  x (:current-piece-x (deref app-state))
                  y (:current-piece-x (deref app-state))
                  piece (:current-piece (deref app-state))
                  ]
      (if (= (.-keyCode e) 40) (make-piece-fall (deref app-state)))
      (swap! app-state assoc :current-piece-x
        (case (.-keyCode e)
          37 (nth (move-piece-if-legal piece x y -1 0) 1)
          39 (nth (move-piece-if-legal piece x y 1 0) 1)
          x)))))

(defn update-state [] (make-piece-fall (deref app-state)))

(declare game-loop)
(defn game-loop []
    (js/setTimeout
      (fn [] (update-state) (game-loop))
      200))

(game-loop)

(om/root
  app
  app-state
  {:target (. js/document (getElementById "tetris"))})


