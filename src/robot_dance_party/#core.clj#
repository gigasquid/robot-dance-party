(ns robot-dance-party.core
  (:use [overtone.live]
        [overtone.inst.sampled-piano]))

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
  (let [env  (env-gen (lin-env attack sustain release) 1 1 0 1 FREE)
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


(stop)

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


;;;;

(defsynth dubstep [bpm 113 wobble 0 note 40 snare-vol 1 kick-vol 1 v 1 out-bus 0]
 (let [trig (impulse:kr (/ bpm 113))
       freq (midicps note)
       swr (demand trig 0 (dseq [wobble] INF))
       sweep (lin-exp (lf-tri swr) -1 1 40 3000)
       wob (apply + (saw (* freq [0.99 1.01])))
       wob (lpf wob sweep)
       wob (* 0.8 (normalizer wob))
       wob (+ wob (bpf wob 1500 2))
       wob (+ wob (* 0.2 (g-verb wob 9 0.7 0.7)))

       kickenv (decay (t2a (demand (impulse:kr (/ bpm 113)) 0 (dseq [1 0 0 0 0 0 1 0 1 0 0 1 0 0 0 0] INF))) 0.7)
       kick (* (* kickenv 7) (sin-osc (+ 40 (* kickenv kickenv kickenv 200))))
       kick (clip2 kick 1)]

   (out out-bus    (* v (clip2 (+ wob) 1)))))

(comment
  ;;Control the dubstep synth with the following:
  (def d (dubstep))
  (ctl d :wobble 8)
  (ctl d :wobble 0)
  (ctl d :note 40)
  (ctl d :bpm 40)
  (ctl d :v 0.4)
  (kill d)
  (stop))

