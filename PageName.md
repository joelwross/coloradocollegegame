#February 7th notes


# Game Programming Wed Feb 07 #


Discussions:

-Benjy and Omer demoed what we have

-we talked about seperating elements from their graphical representations.
having different things (wall, inanimate element, etc) that extend the element class and then have URLs to various types of representations for that element (textual, 2d, etc) depending on how the game is being rendered.

-Also talked about how perhaps a canvas will extend the world to allow canvas easier access to elements (this may change from above discussion).

-Joel explains 3d graphics, demonstrates Java3d
-Java3d uses opengl or the windows graphics driver.

-Benjy raised the issue of the cost incurred by having both a "world" coordinate system and a coordinate system for 3d rendering.

-Resolver: what will it be like??probably on client and server side, perhaps independent from the world.

To Do:

-There needs to be coordination between people who want to make worlds and those who are dealing with the central engine code (Server, theWorld, elements).

-Use text, xml to represent these things????