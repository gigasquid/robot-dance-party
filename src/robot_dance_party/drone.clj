(ns robot-dance-party.drone
  (:require [clj-drone.core :refer :all]
            [overtone.live :as ov :only [at apply-at]]))

(def drone-moves (atom []))
(defn change-drone-moves [move-list] (reset! drone-moves move-list))

(defn drone-get-ready []
  (drone-initialize)
  (drone :emergency)
  (drone :init-targeting)
  (drone :target-roundel-v)
  (drone :hover-on-roundel))

(defn drone-follow-roundel []
  (drone :init-targeting)
  (drone :target-roundel-v)
  (drone :hover-on-roundel))

(defn drone-loop-player
  [beat movelist metro]
  (let [next-beat (+ 48 beat)
        move (first movelist)
        next-moves (or (next movelist) @drone-moves)]
    (ov/at (metro beat)
           (when move
             (println (str "move " move))
             (drone move)))
    (ov/apply-at (metro next-beat) #'drone-loop-player [next-beat next-moves metro])))

(comment 
  (drone-initialize)
  (drone :emergency)
  (drone :init-targeting)
  (drone :target-roundel-v)
  (drone :hover-on-roundel)

                                        ;Use ip and port for non-standard drone ip/port
                                        ;(initialize ip port)
  (drone :take-off)


  (drone :anim-double-phi-theta-mixed)
  (drone :anim-wave)
  (drone :anim-turnaround)
  (drone :anim-yaw-shake)
  (drone :anim-flip-right)

  (drone-do-for 2 :up 0.3)
  (drone-do-for 3.75 :fly 0.2 0 0 0.5) ; sprial
  (drone :hover)
  (drone :land))


