(ns robot-dance-party.music
  (:use [overtone.live]
        [overtone.inst.sampled-piano]
        [overtone.inst.synth]))

(comment (ping (note :E4))
         (bass (note :C4))
         (ks1 (note :C4))
         (stop))
(def piano sampled-piano)

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


(def melody-sounds (atom []))
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
