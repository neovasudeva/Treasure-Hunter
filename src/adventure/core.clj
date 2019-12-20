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
			(do 
				; updates seen rooms in adventure
				[(update-in state [:adventurer :seen] #(conj % loc)) (:desc curr-room)]
			)
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

;; move
;;
;; PURPOSE
;; tells if you can move to a certain place
;; returns [state, string of whether or not you can move there]
(defn move
	"Check the accessibility from one room to another"
	[state dir]
	(let [from (get-in state [:adventurer :location])
		  to (dir (:dir (get-in state [:map from])))]
		(if (nil? to) 
			[state "Are you trying to fall off the map? Enter a valid state!"]
			[(decrease-hp (first (status (assoc-in state [:adventurer :location] to))))
			(last (status (assoc-in state [:adventurer :location] to)))]
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
			:location	(str "You are currently in the " (name loc) ".")
			:inventory	(str "You are carrying: " (clojure.string/join ", " 
							(map #(name %) inv)) ".")
			:directions	(str "You can go: " (clojure.string/join ", " 
							(map #(name %) (keys (:dir loc-actual)))) ".") 
			:room		(str "The room holds: " (clojure.string/join ", "
							(map #(name %) (:contents loc-actual))) ".") 
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
	(remove-from-inventory (drop-to-room state item) item)
)
		
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;
; ROOMS 
;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(def town {
	:type		:room
	:key		:town
	:name 		"Town"

	:desc		"Welcome to Treasure Hunter! You are a brave adventurer in search of a treasure. Not far to the 
				 east is the PALACE. Your job is to find the treasure and deliver it to the PALACE. Be weary 
				 adventurer, you only have limited ENERGY. You start with 100 HP but each move to another room 
				 will cost you 2 HP. Of course, you are carrying a BANANA that you can eat for an additional 
				 10 HP. That being said, search for the treasure in the surrounding rooms before you run out of 
				 HP. For a more detailed description of valid instructions, type 'help'. Now, where will you go
				 first? East to the PALACE? South to the FRONT OF THE CAVE? Perhaps west to the WORKSHOP?"
	:title 		"Welcome back to the town. The aroma of the street shops make you hungry, but you can't stop
				 to eat until the treasure is found. Will you go east to the PALACE? South to the
				 FRONT OF THE CAVE? West to the WORKSHOP?"
	:dir 		{:east 	:palace
			 	 :south :front-of-cave
			 	 :west	:workshop
		 		}
	:contents	#{:money}
})

(def workshop {
	:type 		:room
	:key 		:workshop
	:name 		"Workshop"

	:desc		"This is the WORKSHOP! Here, we turn wood into boats! Do you have wood?"
	:title 		"Welcome back to the WORKSHOP! We can turn any wood into a boat! If you wanna go back to the TOWN
				 go east."
	:dir 		{:east :town}
	:contents	#{}
})

(def palace {
	:type 		:room
	:key 		:palace
	:name 		"Palace"
	:desc 		"Welcome to the PALACE adventurer! Please, have you seen the treasure? We can't find it anywhere.
				 Go west back to the TOWN to start your search!"
	:title 		"Ah adventurer, have you found the treasure? The PALACE needs it for ... financial reasons.
				 Anyways, go back to the TOWN to keep searching!"
	:dir 		{:west 	:town}
	:contents 	#{}
})

(def front-of-cave {
	:type 		:room
	:key 		:front-of-cave
	:name 		"From of Cave"
	:desc		"Bats fly out from what appears to be a dark cave. Looking around though, you do have several 
				 options: go south into the MAIN CAVE, go east to the LEFT PATH, go west to a 
				 slightly to the RIGHT PATH, or go north back to the TOWN."
	:title 		"You are again at the front of the MAIN CAVE. Looking around though, you do have several
				 options: go south into the MAIN CAVE, go east to the LEFT PATH, go west to a 
				 slightly to the RIGHT PATH, or go north back to the TOWN."
	:dir 		{:east 	:right-path
				 :west 	:left-path
			  	 :north	:town
				 :south :main-cave}
	:contents	#{}
})

(def right-path {
	:type 		:room
	:key 		:right-path
	:name 		"Right Path"
	:desc		"You go down the RIGHT PATH and see MOUNTAINS ahead. Do you choose to persevere southward to the 
				 MOUNTAINS or go back to the FRONT OF THE CAVE?"
	:title 		"You once again travel down the RIGHT PATH and see the beautiful MOUNTAINS ahead of you. Go 
				 on to the MOUNTAINS or go back to the FRONT OF THE CAVE?"
	:dir 		{:south		:mountains
				 :west		:front-of-cave}
	:contents	#{}
})

(def mountains {
	:type		:room
	:key 		:mountains
	:name 		"Mountains"
	:desc		"After trudging through the steep slopes of the MOUNTAIN, you see a WELL to the south. 
				 What a random place for a well. Go north and peer into the WELL or grab a sled
				 and sled north down the MOUNTAIN to the RIGHT PATH?"
	:title 		"You once again travel down the RIGHT PATH and see the beautiful MOUNTAINS ahead of you. Go 
				 on to a WELL to the south or sled northward down the mountain to the RIGHT PATH?"
	:dir 		{:south		:well
				 :north		:right-path}
	:contents	#{}
})

(def well {
	:type		:room
	:key 		:well
	:name 		"Well"
	:desc		"You peek into the WELL and there appears to be a key. Should you pick it up or just leave it and head back north to the MOUNTAIN path?"
	:title 		"You peek down the WELL AND OMG FIX MY ASS"
	:dir 		{:south		:well
				 :north		:right-path}
	:contents	#{}
})

(def main-cave {
	:type		:room
	:key		:main-cave
	:name		"Main Cave"
	:desc 		"The cave is dark and grimy. Bats and stalagmites line the ceiling, but soon you reach a fork in the path. Go left to the cave's UPPER LEVEL or south deeper into the cave's LOWER LEVEL?"
	:title 		"The bats and stalagmites of the MAIN CAVE are all too familiar to you. 
				 Go left to the cave's UPPER LEVEL or south deeper into the cave's LOWER LEVEL?"
	:dir 		{:west 	:upper-level
				 :south :lower-level
				 :north :front-of-cave}
	:contents	#{}
})

(def upper-level {
	:type 		:room
	:key		:upper-level
	:name 		"Upper Level"
	:desc 		"After a tough climb, the UPPER LEVEL's surprisingly has several trees. There seems to be some 
				 spare wood lying by the trunk of a tree. Will you pick up the wood, or perhaps go back to the 
				 MAIN CAVE"
	:title 		"You are getting tired of climbing to the UPPER LEVEL. fix me idk what to do if the wood has 
				 already been taken."
	:dir 		{:east 	:main-cave}
	:contents	#{:wood}
})

(def left-path {
	:type		:room
	:key		:left-path
	:name		"Left Path"
	:desc		"You enter a long winding path. At the end, you see a LAKE to the south. 
				 Continue south or go back (east) to the front of the cave?"
	:title		"You're back at the LEFT PATH with a lake to the south. Travel to the LAKE or 
				 go east to the FRONT OF THE CAVE?"
	:dir		{:east		:front-of-cave	
				 :south		:lake}		
	:contents	#{}
})

(def lake {
	:type		:room
	:key		:lake
	:name		"Lake"
	;;
	:desc		"You've reached the LAKE. The moonlight glistens off the murky waters. Cross the LAKE
				  and go east or go back (north) on the LEFT PATH?"
	:title		"You're back at the LAKE. The wind blows eastward. Follow the wind eastward or go 
				 back north on the LEFT PATH?"
	:dir		{:east		:lower-level
				 :north		:left-path}	 
	:contents	#{}
})

; lower level of cave
(def lower-level {
	:type		:room
	:key		:lower-level
	:name		"Lower Level"
	;;
	:desc		"You've reached the LOWER LEVEL of the cave. In the corner you see a chest. Do you open it? 
				 Or perhaps you'd like to go east across the LAKE or north to the MAIN CAVE?"
	:title		"Once again, the darkness of the cave's LOWER LEVEL engulf you. You see the 
				 glint of a chest in the corner. Open it? Go east across the LAKE? Go north back to the MAIN CAVE? 
				 Hmmm, decisions..."	
	:dir		{:west		:lake
				 :north		:main-cave}
	:contents	#{:chest}
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
		:type		:life
		:location	:town
		:inventory	#{:banana}
		:seen		#{:town}
		:hp			100
	}
	:map {
		:town 			town
		:workshop		workshop
		:palace			palace
		:front-of-cave	front-of-cave
		:main-cave		main-cave
		:upper-level	upper-level
		:left-path		left-path
		:lake			lake
		:lower-level	lower-level
		:right-path		right-path
		:mountains		mountains
		:well			well
	}
	:items #{
		:banana
		:woods
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
	(do (print "Welcome to TreasureHunter.\n> ") (flush)
		(loop [state init-state]
			(let [curr-loc (get-in state [:adventurer :location])
				  items (get-in state [:adventurer :items])
				  continue (nil? (find items :chest))]
				(if (and (= curr-loc :palace) (= continue false)) nil
				   	(let [canon-vec (canonicalize (read-line))]
						(if (= (canon-vec 0) :bye) 
							nil
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
