(ns robot-dance-party.roomba
  (require
   [overtone.live :as ov :only [at apply-at]]))

(def roomba-moves (atom []))
(defn change-roomba-moves [move-list] (reset! roomba-moves move-list))
 
(def roomba-beat (atom 16))
(defn change-roomba-beat [num] (reset! roomba-beat num))

(defn roomba-loop-player
  [beat movelist roomba metro]
  (let [next-beat (+ @roomba-beat beat)
        move (first movelist)
        next-moves (or (next movelist) @roomba-moves)]
    (ov/at (metro beat)
        (when move
          (move roomba)))
    (ov/apply-at (metro next-beat) #'roomba-loop-player [next-beat next-moves roomba metro])))

(defn roomba-stop [roomba]
  (.stop roomba))
