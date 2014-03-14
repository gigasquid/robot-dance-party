(ns robot-dance-party.core
  (:use [overtone.live]
        [overtone.inst.sampled-piano]
        [robot-dance-party.music])
  (:require
            [ellipso.core :as core]
            [ellipso.commands :as commands]))

(comment
  (def sphero (core/connect "/dev/rfcomm0"))

  (core/disconnect sphero)

  (commands/execute sphero (commands/colour 0xFF0000)) ;;red
  (commands/execute sphero (commands/colour 0xFF8000)) ;;yellow
  (commands/execute sphero (commands/colour 0x0000FF)) ;;blue
  (commands/execute sphero (commands/colour 0xFF00FF))) ;;purple


(def rainbow
  (map commands/colour [0xFF0000 0xFF8000 0xFFFF00 0x00FF00 0x0000FF 0x8000FF 0xFF00FF]))
(def sphero-moves
  [(commands/roll 0x4B 0) (commands/roll 0x4B 180)])

(defn sphero-loop-player
  [beat colors angle]
  (let [next-beat (+ 4 beat)]
    (at (metro beat)
        (commands/execute sphero (first colors))
        (commands/execute sphero (commands/roll 0x4B (mod angle 360)))
        ;(commands/execute sphero (first moves))
        )
    (apply-at (metro next-beat) #'sphero-loop-player [next-beat (rest colors) (+ angle 30)])))



(defn go-play [beat song]
  (let [m (metro)]
    (reset! next-melody song)
    (bass-loop-player m)
    (robot-loop-player m)
    (sphero-loop-player m (cycle rainbow) 0)
    (melody-loop-player m song @next-melody)))


(defn all-stop []
  (stop)
  (commands/execute sphero (commands/roll 0x00 180)))

(comment  (all-stop)

          (commands/execute sphero (commands/roll 0x30 0))
          (commands/execute sphero (commands/roll 0x00 0))
          (commands/execute sphero (commands/roll 0x30 180))
          (commands/execute sphero (commands/roll 0 180))

          (commands/execute sphero (commands/roll 0x64 0))
          (commands/execute sphero (commands/roll 0x64 180))

          (commands/execute sphero (commands/roll 0x00 180))

          (commands/execute sphero (commands/roll 0x30 0))
          (commands/execute sphero (commands/roll 0x30 45))
          (commands/execute sphero (commands/roll 0x30 90))
          (commands/execute sphero (commands/roll 0x30 135))
          (commands/execute sphero (commands/roll 0x30 180))
          (commands/execute sphero (commands/roll 0x30 225))
          (commands/execute sphero (commands/roll 0x30 270))
          (commands/execute sphero (commands/roll 0x30 315))
          (commands/execute sphero (commands/roll 0x30 0))

          )



(comment

  (change-melody-sounds [])
  (change-bass-sounds [tick])
  (change-robot-sounds [])
  (go-play (metro) repetition-a)
  (change-robot-sounds [robot-ready])
  (change-melody-sounds [saw2])
  (change-robot-sounds [])
  (change-melody (transpose -5 repetition-a))
  (change-melody (transpose -10 repetition-a))
  (change-melody repetition-a)
  (change-melody repetition-b)
  (change-melody (transpose 2 repetition-b))
  (change-melody repetition-b)
  (change-melody (transpose -2 repetition-b))
  (change-melody repetition-b3)
  (change-melody (transpose -2 repetition-b3))
  (change-melody repetition-b)
  (change-melody repetition-a)


  (change-bass-sounds [daft-kick])
  (change-melody-sounds [])

  (change-melody-sounds [piano])
  (change-bass-sounds [])
  (change-melody theme)
  (change-melody-sounds [])

  (change-bass-sounds [dirty-kick daft-kick])
  (change-melody-sounds [plucked-string])

  (all-stop))


