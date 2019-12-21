# Final Project - CS 296 - Treasure Hunter
Hello! This repo contains our CS296 final project. It is a text-based game called "Treasure Hunter." More details are below.

### Group Members
neov2, chenfei5, yimingy5 

### Instructions
**Goal of the Game**<br>
You are an adventurer who needs to find the treasure to save the kingdom. Embark on your journey around the map in search for treasure,
which is hidden in a chest, and return it to the palace. Be wary though, you can only make 50 moves before you perish. You can, however,
extend your life by eating food. 

**Legend of Valid Commands**<br>

        GENERAL:
        To check the room, type in: examine room
        To check the items with you, type in: examine inventory
        To check the location, type in: examine location
        To check your health, type in: examine hp
        To check the way you can go, type in : examine directions

        ITEM：
        There are some items distributed in different places, you can go and find them to achieve different goals.
        To pick item, type in: take + {name_of_item} Eg: take wood
        To use item, type in: use + {name_of_item"} Eg: use wood
        HINT: To open the chest, you have to pick up both chest and the key, then type in: use key //
        HINT: To transform wood into boat, you have to go to the workshop
        To drop item, type in: drop + {name_of_item} Eg: drop wood // the item you drop will be left in the room, you can come back and pick it up any time.

        MOVEMENT:
        To move, type in: move + {direction} Eg: move north
        NOTICE: not every room have full accesses to each directions,
                to know which way you can go, you can type in: examine directions

        QUIT:
        To quit game, just type in: quit


        MAP:
                                   (Start point)
        WORKSHOP --------------------- TOWN --------------------------- PALACE                                  
                                        |   
                                        |
                                        |
                                        |
                                        |
        LEFT_PATH-----------------FRONT_OF_CAVE--------------------RIGHT_PATH
            ~                           |                               |
            ~                           |                               |
            ~                           |                               |
            ~                           |                               |
            ~    UPPER_LEVEL---------MAIN_CAVE                      MOUNTAINS
            ~        (WOOD)             |                               |
            ~                           |                               |
            ~                           |                               |
            ~                           |                               |
           LAKE~~~~~~Water~Road~~~~~LOWER_LEVEL                       WEllS
           (NEED BOATS to enter and leave)                                                          

**For those who are speed-running the game** <br>
For those that wish to simply see the game in action, run the following commands in REPL to beat the game. To win, you must find the chest and key, take them into your inventory
and use the key to open the chest. Then, once the treasure is secure in your inventory, run to the palace to win. The following is a set of instructions to enter in order to beat the
game swiftly. In this speed-run, we use wood we found to create a boat and cross the lake, but that step is not necessary to beat the game.

           For test:
           Here is the steps how you can easily win:
                1.move south
                2.move east
                3.move south
                4.move south
                5.examine room
                6.take key
                7.move north
                8.move north
                9.move west
                10.move south
                11.move west
                12.examine room
                13.take wood
                14.move east
                15.move north
                16.move north
                17.move west
                18.use wood
                19.examine inventory
                20.move east
                21.move south
                22.move west
                23.move south
                24.move east
                26.examine room
                27.take chest
                28.use key
                29.move north
                30.move north
                31.move north
                32.move east

                BOOM! You beat the game!!!
