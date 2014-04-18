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

(def sphero-beat (atom 16))
(defn change-sphero-beat [num] (reset! sphero-beat num))

(defn sphero-loop-player
  [beat movelist sphero metro]
  (let [next-beat (+ @sphero-beat beat)
        moves (first movelist)
        next-moves (or (next movelist) @sphero-moves)]
    (ov/at (metro beat)
        (when moves
          (if (seq? moves)
           (doseq [move moves]
             (commands/execute sphero move))
           (commands/execute sphero moves))))
    (ov/apply-at (metro next-beat) #'sphero-loop-player [next-beat next-moves sphero metro])))

(defn sphero-stop [sphero]
  (commands/execute sphero (commands/roll 0x00 180)))
