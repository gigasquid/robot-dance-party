(ns robot-dance-party.core
  (:use [overtone.live]
        [overtone.inst.sampled-piano]
        [overtone.inst.synth]
        [robot-dance-party.music]
        [robot-dance-party.sphero]
        [robot-dance-party.roomba]
        [robot-dance-party.drone])
  (:require
            [ellipso.core :as core]
            [ellipso.commands :as commands]
            [clj-drone.core :refer :all])
  (:import roombacomm.RoombaCommSerial))

(declare roomba)
(declare sphero)

(comment
  ;; Comm check for everyone!!
  ;; init the roomba
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
  (comment (.disconnect roomba))

  ;; init the sphero
  (def sphero (core/connect "/dev/rfcomm1"))

  (comment  (core/disconnect sphero))

  (commands/execute sphero (commands/colour 0xFF0000)) ;;red
  (commands/execute sphero (commands/colour 0xFF8000))
  (commands/execute sphero (commands/roll 0 0))
  (commands/execute sphero (commands/roll 0x4B 0))
  (commands/execute sphero (commands/heading 60))
  
  (commands/execute sphero (commands/stabilization true))

  ;;yellow

  ;;;; drone
  (drone-get-ready)
  (drone :take-off)
  (drone :anim-flip-right)
  (drone :land)

  ;; music
  (piano (note :E4))

  )


(defn go-play [beat song]
  (let [m (metro)]
    (reset! next-melody song)
    (bass-loop-player m)
    (robot-loop-player m)
    (roomba-loop-player m @roomba-moves roomba metro)
    (sphero-loop-player m @sphero-moves sphero metro)
    (drone-loop-player m @drone-moves metro)
    (melody-loop-player m song @next-melody)))


(defn all-stop []
  (stop)
  (drone :land)
  (.stop roomba)
  (commands/execute sphero (commands/roll 0 180)))


(change-melody-sounds [])
(change-bass-sounds [tick])
(change-robot-sounds [])
(change-sphero-moves [])
(change-roomba-moves [])
(change-drone-moves [])
(change-sphero-beat 16)
(change-roomba-beat 16)

(comment

  ;; Introduction - Music in Overtone

  (go-play (metro) repetition-a)
  (change-melody-sounds [saw2])

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

  ;; Part 1: Dance with Sphero
  (change-melody-sounds [])
  (change-robot-sounds [robot-ready])
  (change-sphero-moves (map commands/colour [RED YELLOW BLUE PURPLE]))
  (change-robot-sounds [])
  (change-melody theme)
  (change-melody-sounds [saw2])
  (change-sphero-moves [(commands/roll 0x4B 0) (commands/roll 0x4B 180)])
  (change-sphero-moves [(commands/roll 0x4B 90) (commands/roll 0x4B 270)])
  (change-sphero-beat 8)
  (change-sphero-moves [])
  (sphero-stop sphero)
  (change-melody-sounds [])

  ;; Part 2: Dance with Roomba

  (change-bass-sounds [daft-kick])
  (change-robot-sounds [robot-ready])
  (change-roomba-moves [ #(.goForward %) #(.goBackward %)])
  (change-robot-sounds [])
  (change-melody-sounds [ping])

  ;; Roomba & sphero
  (change-sphero-beat 16)
  (change-sphero-moves [(commands/roll 0x4B 0) (commands/roll 0x4B 180)])
  (change-sphero-moves [(commands/roll 0x4B 90) (commands/roll 0x4B 270)])

  (change-sphero-beat 8)
  (change-roomba-beat 8)
  (change-roomba-moves [])
  (change-sphero-moves [])
  (roomba-stop roomba)
  (sphero-stop sphero)


  ;; Drone
  (change-melody-sounds [])
  (change-bass-sounds [dirty-kick])
  (change-robot-sounds [robot-ready])
  (drone-get-ready)
  (drone :take-off)
  (change-robot-sounds [])
  (change-melody-sounds [ping])
  (change-drone-moves [:anim-double-phi-theta-mixed])

  ;; Drone  + Roomba
  (change-drone-moves [:hover])
  (change-roomba-beat 16)
  (change-roomba-moves [ #(.goForward %) #(.goBackward %)])
  (change-roomba-moves [ #(.spinRight %) #(.spinLeft %)])
  (change-bass-sounds [dirty-kick daft-kick])
  (change-melody-sounds [])


  ;; Drone + Roomba + Sphero
  (change-robot-sounds [robot-ready])
  (change-sphero-beat 16)
  (change-sphero-moves [(commands/roll 0x4B 0) (commands/roll 0x4B 180)])
  (change-sphero-moves [(commands/roll 0x4B 90) (commands/roll 0x4B 270)])
  (change-robot-sounds [])
  (change-melody-sounds [piano])
  (change-drone-moves [:anim-double-phi-theta-mixed])
  (change-drone-moves [:anim-double-phi-theta-mixed :hover :anim-wave :hover])

  (change-drone-moves [])
  (drone-do-for 2 :up 0.3)
  (drone :anim-flip-right)
  (drone :hover)
  (drone :land)
  (all-stop))


