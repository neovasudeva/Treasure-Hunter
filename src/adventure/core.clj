(ns adventure.core)
(require '[clojure.string :as str])

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;
; Initialize adventure
;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

; adventurer (player)
(def init-adventurer {
	:type		:life
	:location	:lower-level
	:inventory	#{:banana}
	:tick		0
	:seen		#{}
	:hp			100
})

;; self
;; 
;; PURPOSE
;; returns map of adventure stats
(defn self
	"Return all info of the adventurer"
	[state]
	(:adventurer state))

;; status
;;
;; PURPOSE
;; function responsible for printing out the desc/title of room every time it is entered
;; returns vector of [state, description of room]
(defn status
	"Get the status of the adventurer"
	[state]
	(let [loc (get-in state [:adventurer :location])
		  curr-room (get-in state [:map loc])]
		(if (contains? (get-in state [:adventurer :seen]) loc)
			[state (:title curr-room)]
			[(update-in state [:adventurer :seen] #(conj % loc)) (:desc curr-room)]
		)
	)
)

;; decrease-hp
;;
;; PURPOSE
;; decreases the hp of the player every move that is made
;; once hp goes to 0, game ends
;; serves as a helper to the move function
(defn decrease-hp
	"Decrease the hp of the player every move that is made"
	[state]
	(update-in state [:adventurer :hp] - 2)
)

;; increase-hp
;;
;; PURPOSE
;; increases hp of player by 10 (once banana is eaten)
;; returns new state
(defn increase-hp
	"Increase hp of player by 10 once banana is eaten"
	[state]
	(update-in state [:adventurer :hp] + 10)
)

;; move
;;
;; PURPOSE
;; tells if you can move to a certain place
;; returns [state, string of whether or not you can move there]
(defn move
	"Check the accessibility from one room to another"
	[state dir]
	(let [from (get-in state [:adventurer :location])
		  to (dir (:dir (get-in state [:map from])))
			inventory (get-in state [:adventurer :inventory])]
		(if (nil? to) 
			[state "Are you trying to fall off the map? Enter a valid state!"]
			(cond
				(= to :lake)		(if (contains? inventory :boat)	
													[(decrease-hp (first (status (assoc-in state [:adventurer :location] to)))) (last (status (assoc-in state [:adventurer :location] to)))]
													[state "The waters are too frigid to swim in. Looks like you would need a boat to get across the lake ..."]
												)
				(= to :palace)	(if (contains? inventory :treasure)
													[(decrease-hp (first (status (assoc-in state [:adventurer :location] to)))) "Congrats adventurer! You beat the game!"]
													[(decrease-hp (first (status (assoc-in state [:adventurer :location] to)))) (last (status (assoc-in state [:adventurer :location] to)))]
												)
				:else						[(decrease-hp (first (status (assoc-in state [:adventurer :location] to)))) (last (status (assoc-in state [:adventurer :location] to)))]
			)
		)
	)
)

;; describe
;;
;; PURPOSE
;; return a string of what to examine
;; it's actually a helper for examine
(defn describe
	"Describe something you are paying attention to."
	[state target]
	(let [self (:adventurer state)
		  loc (get-in state [:adventurer :location])
		  loc-actual (get-in state [:map loc])
		  inv (get-in state [:adventurer :inventory])]
		(case target
			:location			(str "You are currently in the " (name loc) ".")
			:inventory		(if (empty? inv)	
											(str "Sadly, there is nothing in your inventory.")
											(str "You are carrying: " (clojure.string/join ", " (map #(name %) inv)) ".")
										)
			:directions		(str "You can go: " (clojure.string/join ", " (map #(name %) (keys (:dir loc-actual)))) ".")
			:room					(if (empty? (:contents loc-actual))
											(str	"There appears to be nothing of significance in this room.")
											(str "The room holds: " (clojure.string/join ", "(map #(name %) (:contents loc-actual))) ".") 
										)
			:hp						(str "You have " (get-in state [:adventurer :hp]) ".") 
			"Nani?"
		)
	)
)

;; examine
;;
;; PURPOSE
;; Examines various things, specifically the inventory of the adventurer, the current location,
;; and valid directions from the current room the player can take
;; returns [state, string saying of examined thing]
(defn examine
	"Examine something. Could be a place, an item, or the adverturer yourself."
	[state target]
	(case target
		:inventory	[state (describe state :inventory)]
		:location	[state (describe state :location)]
		:directions	[state (describe state :directions)]
		:room		[state (describe state :room)]
		:hp			[state (describe state :hp)]
		[state "Silly, you can't examine that! Try something else."]
	)
)

;; take
;;
;; PURPOSE
;; allows the player to take an item from the room
;; returns [new state, string indicating player took object]
(defn take
	"Take an item from a place"
	[state item]
	(let [room-key (get-in state [:adventurer :location])
		  room (get-in state [:map room-key])]
		(if (contains? (:contents room) item)
			(let [mid-state (update-in state [:map room-key :contents] disj item)]
				[(update-in mid-state [:adventurer :inventory] conj item)
					(str "You took the " (name item) ".")]
			)
			[state "That doesn't exist in this room."]	
		)
	)
)

;; HELPERS
(defn curr-room
	"Return the keyword of current room."
	[state]
	(get-in state [:adventurer :location]))

(defn has-item
	"Check whether the adventurer owns the item."
	[state item]
	(if (contains? (get-in state [:adventurer :inventory]) item)
		true
		false))

(defn there-is-item
	"Check whether there is an item right in the room."
	[state item]
	(if (contains? (get-in state [:map (curr-room state) :contents]) item)
		true
		false))

(defn valid-item
	"Check whether the item exists in the game."
	[state item]
	(if (contains? (:items state) item)
		true
		false))

(defn add-to-inventory
	[state item]
	(if (valid-item state item)
		(update-in state [:adventurer :inventory] #(merge % item))
		(vector state "You can't add a nonexistent item!")))			
		;; This is expected to never happen.

(defn remove-from-inventory
	[state item]
	(if (has-item state item)
		(update-in state [:adventurer :inventory] disj item)
		(vector state "You don't have the item!")))						
		;; This is expected to never happen.

(defn add-to-inventory 
	[state item]
	(update-in state [:adventurer :inventory] conj item)
)

(defn drop-to-room
	[state item]
	(let [curr (curr-room state)]
		(if (has-item state item)
			(update-in state [:map curr :contents] #(merge % item))
			(vector state "You can't drop an item you don't own."))))	
			;; This is expected to never happen.

(defn pick-from-room
	[state item]
	(let [curr (curr-room state)]
		(if (there-is-item state item)
			(update-in state [:map curr :contents] disj item))
			(vector state "You can't pick up an item that is not in the room!")))

;; drop 
;; 
;; PURPOSE
;; allow player to drop item into a room
;; returns [new state, string indicating the drop]
(defn drop
	"Drop an item from your inventory. Maybe you need it no more or it's too heavy to carry"
	[state item]
	[(remove-from-inventory (drop-to-room state item) item) (str "You dropped the " (name item) ".")]
)

;; use
;;
;; PURPOSE
;; allows the player to "use" an item in their inventory
;; returns [new state, string indicating successful use]
(defn use
	"Use an item from inventory"
	[state item]
	(let [inventory (get-in state [:adventurer :inventory])
				room (get-in state [:adventurer :location])]
		(case item
			:banana	[(remove-from-inventory (increase-hp state) :banana) "You ate the banana and feel the HP surge through your body."]
			:chest	[state "You try to force the chest open to no avail. It has a keyhole, though. Perhaps use a key instead."]
			:key		(cond
								(contains? inventory :chest)
									[(add-to-inventory (remove-from-inventory state :chest) :treasure) "You opened the chest and found the treasure!"]
								(contains? inventory :treasure)
									[state "Looks like you already have the treasure ... Hurry and make your way to the PALACE!"]
								:else [state "Hmmm ... you have the key but what does it open?"]
							)
			:wood		(if (= room :workshop)
								[(add-to-inventory (remove-from-inventory state :wood) :boat) "Your wood was transformed into a boat! You can now travel over bodies of water!"]
								[state "You pull out the wood but can't seem to find a use for it ..."]
							)
			; default
			[state "You don't have that your inventory."]
		)
	)
)
				
		
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;
; ROOMS 
;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(def town {
	:type		:room
	:key		:town
	:name		"Town"
	:desc		"If this prints, you broke the game. Good job, pal."
	:title	"Welcome back to the town! The smell of street food and chitter chatter relaxes you. You see the palace to the east,\nthe front of the cave to the south, and the workshop to the east."
	:dir 		{:south :front-of-cave
					 :east	:palace
					 :west	:workshop}
	:contents	#{:wood}
})

(def workshop {
	:type 		:room
	:key			:workshop
	:name 		"Workshop"
	:desc			"This is the workshop! Here, we turn wood into boats! Do you have wood? Go east to go back to the town."
	:title 		"Welcome back to the workshop! We can turn any wood into a boat! If you wanna go back to the town go east."
	:dir			{:east  :town}
	:contents	#{}
})

(def palace {
	:type 		:room
	:key			:palace
	:name 		"Palace"
	:desc 		"Welcome to the palace adventurer! Please, have you seen the treasure? We can't find it anywhere. Go west back to\nthe town to start your search!"
	:title 		"Ah adventurer, have you found the treasure? The PALACE needs it for ... financial reasons. Anyways, go back to the\ntown to keep searching!"
	:dir 		{:west 	:town}
	:contents 	#{}
})

(def front-of-cave {
	:type 		:room
	:key			:front-of-cave
	:name 		"Front of Cave"
	:desc			"Bats fly out from what appears to be a dark cave. Looking around though, you do have several options: go south into the\nmain cave, go east to the left path, go west to the right path, or go north back to the town."
	:title 		"You are again at the front of the main cave. A wind blows towards you and whispers 'well hello there' into your ear.\nLooking around though, you do have several options: go south into the main cave, go east to the left path, go west to a slightly to the right path\n, or go north back to the town."
	:dir 		{:east 	:right-path
					 :west 	:left-path
			  	 :north	:town
					 :south :main-cave}
	:contents	#{}
})

(def right-path {
	:type 		:room
	:key			:right-path
	:name 		"Right Path"
	:desc			"You go down the right path and see mountains ahead. Do you choose to persevere southward to the mountains or go back to\nthe front of the cave?"
	:title 		"You once again travel down the right path and see the beautiful mountains head of you. Go on to the mountains or get scared\nand run back to the front of the cave?"
	:dir 		{:south		:mountains
					 :west		:front-of-cave}
	:contents	#{}
})

(def mountains {
	:type		:room
	:key 		:mountains
	:name 	"Mountains"
	:desc		"After trudging through the steep slopes of the mountain, you see a well to the south. What a random place for a well. Go south and\nvisit the well or roll northwards down the mountain to the right path?"
	:title 		"This time, the steep slopes of the mountain are easy to climb. You see the familiar well to the south. Check out the well or go\nnorth back down the mountain?"
	:dir 		{:south		:well
					 :north		:right-path}
	:contents	#{}
})

(def well {
	:type		:room
	:key 		:well
	:name 	"Well"
	:desc		"You peek into the well and there appears to be a key floating in the musty water. Should you pick it up or just leave that rusty old\nthing and head back north to the peak of the mountain?"
	:title 	"You peek down the well and see the sunlight shine on top of the water. Something glistens in the reflections. Is it just the sunlight\nor perhaps an object? Go north to head back to the mountian peak."
	:dir 		{:north		:mountains}
	:contents	#{}
})

(def main-cave {
	:type		:room
	:key		:main-cave
	:name		"Main Cave"
	:desc 	"The cave is dark and grimy. Bats and stalagmites line the ceiling, but soon you reach a fork in the path. Go left to the cave's upper\nlevel or south deeper into the cave's lower level?"
	:title 	"The bats and stalagmites of the main cave are all too familiar to you. Go left to the cave's upper level or south deeper into the cave's lower level?"
	:dir 		{:west 	:upper-level
					 :south :lower-level
				   :north :front-of-cave}
	:contents	#{}
})

(def upper-level {
	:type 		:room
	:key			:upper-level
	:name 		"Upper Level"
	:desc 		"After a tough climb, you see that the upper levelsurprisingly has several trees. There seems to be some spare wood lying by the trunk\nof a tree. Will you examine the upper level, or perhaps go back to the main cave?"
	:title 		"You are getting tired of climbing to the upper level and wonder how trees of all things managed to grow down here. Looking around, you\nsee an abundance of wood. Go east to go to the main cave."
	:dir 		{:east 	:main-cave}
	:contents	#{:wood}
})

(def lake {
	:type		:room
	:key		:lake
	:name		"Lake"
	:desc		"You are paddling across the lake, wishing you had a sweet motor boat instead of this dingy raft keeping you afloat. The moonlight glistens off the\nmurky waters. Cross the lake and go east or go north to the left path?"
	:title	"You're back on the lake. The wind blows eastward and whispers in your ear 'General Kenobi, what a pleasure'. Cross the lake eastward or go north\n to the left path?"
	:dir		{:north		:workshop
					 :east		:lower-level}	 
	:contents	#{}
})

(def left-path {
	:type		:room
	:key		:left-path
	:name		"Left Path"
	:desc		"You enter a long winding path. At the end, you see a lake to the south. Continue south or go east to the front of the cave?"
	:title	"You're back at the left path with a lake to the south. You notice Travel to the lake or go east to the front of the cave?"
	:dir		{:east		:front-of-cave	
				 :south		:lake}		
	:contents	#{}
})



;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;
; REPL Stuff
;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn match [pattern input]
  (loop [pattern pattern
		input input
		vars '()]
	(cond (and (empty? pattern) (empty? input)) (reverse vars)
		  (or (empty? pattern) (empty? input)) nil
		  (= (first pattern) "@")
			(recur (rest pattern)
			  (rest input)
			  (cons (first input) vars))
		  (= (first pattern) (first input))
			  (recur (rest pattern)
			    (rest input)
			    vars)
		  :fine-be-that-way nil
		  )
	)
)

(def initial-env [
	[:examine "@"] examine
	[:move "@"] move
	[:take "@"] take
	[:drop "@"] drop
	[:use "@"] use
])

(defn react
	[state input-vector]
	(loop [idx 0]
		(if (>= idx (count initial-env))
			[state "INVALID"]
			(if-let [vars (match (initial-env idx) input-vector)]
				(apply (initial-env (inc idx)) state vars)
				(recur (+ idx 2))))))

(def init-state {
	:adventurer {
		:type				:life
		:location		:town
		:inventory	#{:banana}
		:seen				#{:town}
		:hp					100
	}
	:map {
		:town					town
		:workshop			workshop
		:lower-level	lower-level
		:well					well
		:lake					lake 
		:palace				palace
	}
	:items #{
		:banana
		:wood
		:key
		:chest
	}
})

; (canonicalize "go north")
; => ["go" "north"]
(defn canonicalize
  "Given an input string, strip out whitespaces, lowercase all words, and convert to a vector of keywords."
  [input]
	(into [] (map #(keyword %) (str/split (str/replace (str/lower-case input) #"[?.!]" "") #" +")))
)

(defn repl
	"Start a REPL"
	[env]
	(do (print "\nWelcome to Treasure Hunter! You are a brave adventurer in search of a treasure. Not far to the\neast is the palace. Your job is to find the treasure and deliver it to the palace. Be weary\nadventurer, you only have limited energy. You start with 100 HP but each move to another room\nwill cost you 2 HP. Of course, you are carrying a banana that you can eat for an additional\n10 HP. That being said, search for the treasure in the surrounding rooms before you run out of\nHP. For a more detailed description of valid instructions, take a peek at the README.md file. Now, where will you go\nfirst? East to the palace? South to the front of the cave? Perhaps west to the workshop?\n> ") (flush)
		(loop [state init-state]
			(let [curr-loc (get-in state [:adventurer :location])
				  items (get-in state [:adventurer :inventory])]
				(if (and (= curr-loc :palace) (contains? items :treasure)) 
						nil
				   	(let [canon-vec (canonicalize (read-line))]
							(if (= (canon-vec 0) :quit) 
								(println "Come back soon!")
								(do
									(doseq [item (filter string? (react state canon-vec))]
										(println item))
									(flush)
									(print "\n> ")
									(flush)
									(recur (first (react state canon-vec)))
								)	
							)
			))))))

(defn main
	"Main entry"
	[]
	(repl initial-env))
