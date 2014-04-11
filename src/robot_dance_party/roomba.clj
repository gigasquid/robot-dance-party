(ns robot-dance-party.roomba
  (require
   [overtone.live :as ov :only [at apply-at]]))

(def roomba-moves (atom []))
(defn change-roomba-moves [move-list] (reset! roomba-moves move-list))

(defn roomba-loop-player
  [beat movelist roomba metro]
  (let [next-beat (+ 8 beat)
        move (first movelist)
        next-moves (or (next movelist) @roomba-moves)]
    (ov/at (metro beat)
        (when move
          (move roomba)))
    (ov/apply-at (metro next-beat) #'roomba-loop-player [next-beat next-moves roomba metro])))
