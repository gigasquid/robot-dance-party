(ns robot-dance-party.sphero
  (require
   [ellipso.core :as core]
   [ellipso.commands :as commands]))


(def sphero (core/connect "/dev/rfcomm0"))

(core/disconnect sphero)

(def rainbow
  (map commands/colour [0xFF0000 0xFF8000 0xFFFF00 0x00FF00 0x0000FF 0x8000FF 0xFF00FF]))

(def spin
  (let [speeds (range 0x30 0xFF 0x30)
        cycle  (concat speeds (reverse speeds))]
    (map (partial commands/spin commands/CLOCKWISE) cycle)))

(reduce commands/execute
        sphero
        (flatten (interpose (commands/pause 1000) (map list rainbow))))

(commands/execute sphero (commands/stop))

(commands/execute sphero (commands/colour 0xFF0000)) ;;red
(commands/execute sphero (commands/colour 0xFF8000)) ;;yellow
(commands/execute sphero (commands/colour 0x0000FF)) ;;blue
(commands/execute sphero (commands/colour 0xFF00FF)) ;;purple

)
