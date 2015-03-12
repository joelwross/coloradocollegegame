#Notes from the January 31st meeting

# Presentations #
Jan 31

Agenda:

Dan - Google Code
  * Dan presented Google Code, with SVN browsing, integrated wiki, and issue tracking.
  * He recommended that windows users use Tortoise SVN.  Others can use CLI svn, or the graphical SVN client of their choice.
Guillermo - PB Wiki
  * simple wiki with PB Wiki
  * decided that Google Code wiki would be easier to deal with
Omer & Benji -
  * clients connects to server the server then responds to the client with pertinent information
  * Client talks to server through the clientThread
  * Server holds the world
  * would like it so that the client gets info specific to itself not everything

> Client:
    * Starts objects
    * ClientIO communicates between the user and the server
    * canvas does the rendering and painting
    * has a world that is localized - checks for things with validity
  * world - keeps track of info (users, objects, elements, etc) two worlds client and server
  * io - just talking between things
  * canvas - rendering and drawing
  * element - sprites that can draw themselves

Secretarial Duties:
  * Guillermo Jan - 31
  * Adam C. Feb - 7

Possible Projects
  * Conflict Management (Consistency) - Benji & Omer
  * Compartmentalize World - Adam C. & Guillermo
  * Multiple Servers - Omer & Benji
  * Resolver (simple) - Adam L. & Leon & Benji
  * Performance Issues & Testing (scripts) - Dan & Guillermo
  * JavaDoc (scripts) - Dan
  * Serializable -
  * User Representation - Joel & Benji
    * Sounds - Leon (later)