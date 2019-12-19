(ns adventure.core)

(def sample-adventurer-state {
	:type		:life
	;;
	:location	:the-lawn
	:inventory	#{:knife :pan :map}
	:tick		23
	:seen		#{:bedroom :livingroom :corridor :the-lawn}
	;;
	:hp			99
	:lives		3
	:npc-met	#{:mom :dad :little-brother :puppy}
	:skills		#{:hide :jump}
	})

(def sample-room-state {
	:type		:room
	:key		:sample-room
	;;
	:desc		"This is a small room."
	:name		"Sample Room"
	:title		"in the room"
	:dir		{:east	:right-room
				 :north	:up-room
				 :west	:left-room
				 :south	:down-room}
	:contents	#{:bottle-water :piece-of-cake}
	;;
	})

(def sample-item {
	:type		:item
	:key		:raw-egg
	:order		0
	;;
	:desc		"This is a raw egg. You probably want to eat it or throw it to somebody you don't like."
	:name		"a raw egg"
	;;
})

(def init-adventurer {
	:type		:life
	:location	:forgotten-land
	:inventory	#{:raw-egg}
	:tick		0
	:seen		#{}
	:hp			100
	:lives		3
	:npc-met	#{}
	:skills		#{}})

;; (status state)
;; => You are in the room. This is a small room.
;;

(comment
(defn status
	"Get the status of the adventurer"
	[state]
	(let [loc (get-in [:adventurer :location] state)
		  map (:map state)]
		(print (str "You are " (-> map loc :title) ". "))
		(when-not ((get-in [:adventurer :seen] state) loc)
			(print (-> map loc :desc)))
		(update-in state [:adventurer :seen] #(conj % loc))))

(defn examine
	"Examine something. Could be a place, an item, or the adverturer yourself."
	[state target]
	(let [type (:type target)]
		(case type
			:life	(let [self (get state :adventurer)
						 loc (get self :location)
						 inv (get self :inventory)
						 health [(get self :hp) (get self :lives)]]
						(str "You carefully examined yourself." (describe loc) (describe inv) (describe health)))
			:room 	(let [room (get-in [:map target] state)
						  name (get room :name)
						  dir (get room :dir)
						  items (get room :contents)]
						(str "You are currently in " name "." (describe dir) (describe items)))
						  
			:item	(let [item (get-in [])])
			"Want to find something?")))	
)

(defn main
	"lol"
	[]
	(println "lol"))

