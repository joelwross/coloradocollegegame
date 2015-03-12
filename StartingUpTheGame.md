# Introduction #

The game has come quite aways from the beginning. Now we are almost at a point to double click on the executable to get it going.


# Details #


To get into the game you will still need to go to the command line. (At least until we can implement the executable JAR). Get into your favorite command prompt, get into the working directory of all the game files and do the following
```
javac *.java   // to make sure the files are all compiled correctly
java StartUp   // this will put you right into the GUI to start either a client or server
```

Here it gets much easier. The GUI does all of the hard work for you in terms of getting either the client or server configured and connected. By default it will put you on the Client tab to connect to a known server, the 2nd tab is designed for Server startup.

## Connecting as a Client to a Server ##

For connecting to a locally hosted server with default options

  1. Select the CLIENT tab.
  1. Push the "**Start Client**" button

For more options:

  1. Select the CLIENT tab.
  1. Check the appropriate check boxes for what options you would like
    * Connect to Server IP - use this to connect to a known server
    * Server Port # - use this if the port has been changed from the default
    * Client Verbose Mode - use this if you wish to listen to all output given to the client. (Please note that this will slow down the game a little bit to print out all the messages.)
  1. Push the "**Start Client**" button when all options have been selected.

## To Host a Server for Clients to Connect ##

For a default server

  1. Select the SERVER tab.
  1. Push the "**Start Server**" button

For more options:

  1. Select the SERVER tab.
  1. Check the appropriate check boxes for what options you would like
    * Server Port # - use this if the port has been changed from the default to a defined port
    * Directory - use this to change the directory of known files
    * Element list files with extension - use this if the Element list files extension has been changed.
    * World files with extension - use this if the World files extension has been changed.
    * Elements in File - use this if you wish to load a different set of Element files
    * World in File - use this if you wish to load a different set of World files
    * Server Verbose Mode - use this if you wish to listen to all output given by the server. (Please note that this will slow down the game a little bit to print out all the messages.)
  1. Push the "**Start Server**" button when all options have been selected.