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

; left path room
(def left-path {
	:type		:room
	:key		:left-path
	:desc		"You enter a long winding path. At the end, you see a lake to the south. Continue south or go back (east) to the front of the cave?"
	:title		"You're back at the long winding path with a lake to the south. Travel to the lake or go east to the front of the cave?"
	:dir		{:east		:temp	;front of cave
				 :south		:lake}	;Lake	
	:contents	#{}
})

; lake path room
(def lake {
	:type		:room
	:key		:lake
	;;
	:desc		"You've reached the lake. The moonlight glisten off the murky waters. Cross the lake and go east or go back (north) on the path you came?"
	:title		"You're back at the lake. The wind blows eastward. Follow the wind eastward or go back north on the path you came from?"
	:dir		{:east		:lower-level
				 :north		:left-path}	 
	:contents	#{}
})

; lower level of cave
(def lower-level {
	:type		:room
	:key		:lower-level
	;;
	:desc		"You've reached the lower level of the cave. In the corner you see a chest. Do you open it? Or perhaps you'd like to go east across the lake or north to the main cave?"
	:title		"Once again, the darkness of the cave's lower level engulf you. You see the glint of a chest in the corner. Open it? Go east across the lake? Go north back to the main cave? Hmmm, 
				decisions..."	
	:dir		{:west		:lake
				 :north		:main-cave}
	:contents	#{:chest}
})

; treasure item
(def chest {
	:type		:item
	:key		:chest
	;;
	:desc		"Wow, so much gold! And diamonds! Looks like this is the treasure you were looking for."
	:name		"treasure"
})

; banana item
(def banana {
	:type		:item
	:key 		:banana
	;;
	:desc		"You peel the banana and gobble it down. You feel replenished with exactly 10 more HP than you had before."
	:name		"banana"
})

; adventurer (player)
(def init-adventurer {
	:type		:life
	:location	:lower-level
	:inventory	#{:banana}
	:tick		0
	:seen		#{}
	:hp			100
})

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

