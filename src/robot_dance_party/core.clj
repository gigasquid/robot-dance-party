(ns robot-dance-party.core
  (:use [overtone.live]
        [overtone.inst.sampled-piano])
  (:require [ellipso.core :as core]
            [ellipso.commands :as commands]))

(def sphero (core/connect "/dev/rfcomm0"))

(core/disconnect sphero)

(commands/execute sphero (commands/colour 0xFF0000)) ;;red
(commands/execute sphero (commands/colour 0xFF8000)) ;;yellow
(commands/execute sphero (commands/colour 0x0000FF)) ;;blue
(commands/execute sphero (commands/colour 0xFF00FF)) ;;purple


(def piano sampled-piano)

(definst plucked-string [note 60 amp 0.8 dur 2 decay 30 coef 0.3 gate 1]
  (let [freq   (midicps note)
        noize  (* 0.8 (white-noise))
        dly    (/ 1.0 freq)
        plk    (pluck noize gate dly dly decay coef)
        dist   (distort plk)
        filt   (rlpf dist (* 12 freq) 0.6)
        clp    (clip2 filt 0.8)
        reverb (free-verb clp 0.4 0.8 0.2)]
    (* amp (env-gen (perc 0.0001 dur)) reverb)))

(defsynth saw-wave [freq 440 attack 0.01 sustain 0.03 release 0.1 amp 0.8 out-bus 0]
  (let [env  (env-gen (lin attack sustain release) 1 1 0 1 FREE)
        src  (mix (saw [freq (* 1.01 freq)]))
        src  (lpf src (mouse-y 100 2000))
        sin  (sin-osc (* 2 freq))
        sin2 (sin-osc freq)
        src  (mix [src sin sin2])]
    (out out-bus (* src env amp))))

(defn saw2 [music-note] (saw-wave (midi->hz music-note)))

(definst tick [freq 560 dur 0.1 width 0.5]
  (let [freq-env (* freq (env-gen (perc 0 (* 0.99 dur))))
        env      (env-gen (perc 0.01 dur) 1 1 0 1 FREE)
        sqr      (* (env-gen (perc 0 0.001)) (pulse (* 2 freq) width))
        src      (sin-osc freq-env)
        drum     (+ sqr (* env src))]
    (hpf (compander drum drum 0.2 0.3 0.01 0.1 0.01) (mouse-x 200 1000))))


(def repetition-sub-a (map note [:C5, :A3, :B4, :A3, :C5, :E5, :A3, :A4, :C5, :A3, :B4, :A3, :C5, :A4]))
(def repetition-a (concat (map note [:A4, :A3]) repetition-sub-a (map note [:A3, :A4]) repetition-sub-a))


(def repetition-b  (map note
                        (concat
                         [:F4, :F4, :A4, :F4, :G4, :F4, :A4, :C5, :F4, :F4, :A4, :F4, :G4, :F4,
                          :A4, :F4]
                         [:F4, :F4, :A4, :F4, :G4, :F4, :A4, :C5, :F4, :F4, :A4, :F4, :G4, :F4,
                          :A4, :F4])))


(def repetition-b3 (map note (concat
                              [:E4, :E4, :G4, :E4, :F#3, :E4, :G4, :B4, :E4, :E4, :G4, :E4, :F#3,
                               :E4, :G4, :E4]
                              [:E4, :E4, :G4, :E4, :F#3, :E4, :G4,
                               :B4, :E4, :E4, :G4, :E4, :F#3, :E4, :G4, :E4])))


(defn transpose [updown notes]
  (map #(+ updown %1) notes))

(def metro (metronome (* 4 113)))
(metro)


(def melody-sounds (atom [piano plucked-string]))
(def next-melody (atom repetition-a))

(defn melody-loop-player
  [beat notes song]
  (let [n     (first notes)
        notes (or (next notes)  @next-melody)
        next-beat (inc beat)]
    (when n
      (at (metro beat)
          (doseq [sound-fn @melody-sounds]
            (sound-fn n))))
    (apply-at  (metro next-beat) #'melody-loop-player [next-beat notes song])))

(defn change-melody-sounds [sounds]
  (reset! melody-sounds sounds))

(defn change-melody [song]
  (reset! next-melody song))


(def dirty-kick (sample (freesound-path 30669)))
(def daft-kick (sample (freesound-path 177908)))
(def robot (sample (freesound-path 131006)))
(def robot-ready (sample (freesound-path 187404)))
(def bass-sounds (atom [dirty-kick daft-kick]))
(def robot-sounds (atom [robot-ready]))

(defn bass-loop-player
  [beat]
  (let [ next-beat (+ 4 beat)
        f-bass (first @bass-sounds)
        l-bass (second @bass-sounds)]
    (when f-bass
      (at (metro beat) (f-bass))
      (when l-bass
        (at (metro (+ beat 2)) (l-bass))
        (at (metro (+ beat 3)) (l-bass))))
    (apply-at  (metro next-beat) #'bass-loop-player [next-beat])))

(defn robot-loop-player
  [beat]
  (let [ next-beat (+ 32 beat)
        robot-s (first @robot-sounds)]
    (when robot-s
      (at (metro beat) (robot-s)))
    (apply-at (metro next-beat) #'robot-loop-player [next-beat])))

(defn change-bass-sounds [sounds]
  (reset! bass-sounds sounds))

(defn change-robot-sounds [sounds]
  (reset! robot-sounds sounds))

(defn go-play [beat song]
  (let [m (metro)]
    (reset! next-melody song)
    (bass-loop-player m)
    (robot-loop-player m)
    (sphero-loop-player m (cycle rainbow) (cycle sphero-moves))
    (melody-loop-player m song @next-melody)))

(def theme  (concat
              repetition-a
              (transpose -5 repetition-a)
              repetition-a
              (transpose -5 repetition-a)
              repetition-b
              (transpose 2 repetition-b)
              (transpose -2 repetition-b3)
              repetition-b3
              repetition-b
              (transpose 2 repetition-b)
              repetition-b3
              repetition-b3))

(def rainbow
  (map commands/colour [0xFF0000 0xFF8000 0xFFFF00 0x00FF00 0x0000FF 0x8000FF 0xFF00FF]))
(def sphero-moves
  [(commands/roll 0x4B 0) (commands/roll 0x4B 180)])


(defn sphero-loop-player
  [beat colors moves]
  (let [ next-beat (+ 16 beat)]
    (at (metro beat)
        (commands/execute sphero (first colors))
        (commands/execute sphero (first moves)))
    (apply-at (metro next-beat) #'sphero-loop-player [next-beat (rest colors) (rest moves)])))


(defn all-stop []
  (stop)
  (commands/execute sphero (commands/roll 0x00 180)))

(all-stop)

(commands/execute sphero (commands/roll 0x30 0))
(commands/execute sphero (commands/roll 0x00 0))
(commands/execute sphero (commands/roll 0x30 180))
(commands/execute sphero (commands/roll 0 180))

(commands/execute sphero (commands/roll 0x64 0))
(commands/execute sphero (commands/roll 0x64 180))

(commands/execute sphero (commands/roll 0x00 180))




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

  (stop))


