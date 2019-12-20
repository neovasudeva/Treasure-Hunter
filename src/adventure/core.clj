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
;;
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
			[(first (status (assoc-in state [:adventurer :location] to)))
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

(defn tick
	"Add the tick by 1"
	[state]
	(update-in state [:adventurer :tick] inc))
		
(def town {
	:type		:room
	:key		:town

	:desc		"This is the town, where adventurer spawn. Your task is going to find the 
				 treasure in 30 moves, go now ! Explore all the dircetions except the north, lol"
	:title 		"Welcome back to the town."
	:dir 		{:west	:workshop}
	:contents	#{:money}
})

(def workshop
	{:type 		:room
	:key 		:workshop

	:desc		"This is the workshop, go back here when you gather the woods and hammer, if you want to go back to the town, please go east"

	:title 		"Here is the workshop."
	:dir 		{:east :town}
	:contents	#{}
})



;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; REPL Stuff
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
		:tick		0
		:seen		#{:town}
		:hp			100
	}
	:map {
		:town 			town
		:workshop		workshop
	}
	:items #{
		:banana
		:woods
		:key
		:chest
		:air
		:less-air
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
