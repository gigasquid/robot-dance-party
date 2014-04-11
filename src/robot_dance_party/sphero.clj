(ns robot-dance-party.sphero
  (require
   [ellipso.core :as core]
   [ellipso.commands :as commands]
   [overtone.live :as ov :only [at apply-at]]))

(def RED 0xFF0000)
(def YELLOW 0xFF8000)
(def BLUE 0x0000FF)
(def PURPLE 0xFF00FF)

(def sphero-moves (atom []))
(defn change-sphero-moves [moves] (reset! sphero-moves moves))

(defn sphero-loop-player
  [beat movelist sphero metro]
  (let [next-beat (+ 16 beat)
        moves (first movelist)
        next-moves (or (next movelist) @sphero-moves)]
    (ov/at (metro beat)
        (when moves
          (if (seq? moves)
           (doseq [move moves]
             (commands/execute sphero move))
           (commands/execute sphero moves))))
    (ov/apply-at (metro next-beat) #'sphero-loop-player [next-beat next-moves sphero metro])))

