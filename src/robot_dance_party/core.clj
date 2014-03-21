(ns robot-dance-party.core
  (:use [overtone.live]
        [overtone.inst.sampled-piano]
        [robot-dance-party.music])
  (:require
            [ellipso.core :as core]
            [ellipso.commands :as commands])
  (:import roombacomm.RoombaCommSerial))

(comment
(def roomba (RoombaCommSerial. ))

;;Find your port for your Roomba
(map println (.listPorts roomba))

(def portname "/dev/rfcomm0")
(.connect roomba portname)
(.startup roomba)  ;;puts Roomba in safe Mode
;; What mode is Roomba in?
(.modeAsString roomba)
(.control roomba)
(.updateSensors roomba) ; returns true if you are connected
(.playNote roomba 72 40)
(.disconnect roomba)

  
  (def sphero (core/connect "/dev/rfcomm1"))

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
  [beat colors angle count angle-incr]
  (let [next-beat (+ 4 beat)
        new-angle (if (< angle 360) (+ angle 360) angle)
        new-angle-incr (if (zero? (mod count 12)) (* angle-incr -1) angle-incr)
        good-angle (if (zero? (mod count 12)) (* new-angle -1) new-angle)]
    (at (metro beat)
        (commands/execute sphero (first colors))
        (commands/execute sphero (commands/roll 0x4B (mod good-angle 360)))
        ;(commands/execute sphero (first moves))
        )
    (apply-at (metro next-beat) #'sphero-loop-player [next-beat (rest colors) (+ good-angle new-angle-incr) (inc count) new-angle-incr])))

(defn roomba-loop-player
  [beat turn]
  (let [next-beat (+ 8 beat)]
    (at (metro beat)
        (if turn
          (.spinLeft roomba)
          (.spinRight roomba))
        )
    (.playNote roomba 72 40)
    (apply-at (metro next-beat) #'roomba-loop-player [next-beat (not turn)])))


(defn go-play [beat song]
  (let [m (metro)]
    (reset! next-melody song)
    (bass-loop-player m)
    (robot-loop-player m)
    (roomba-loop-player m true)
    (sphero-loop-player m (cycle rainbow) 0 0 30)
    (melody-loop-player m song @next-melody)))


(defn all-stop []
  (stop)
  (.stop roomba)
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


