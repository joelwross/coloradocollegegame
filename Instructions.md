Here is how to get it to work **(updated to work on any server Feb 06, 2007)**:

Checkout the code.

http://code.google.com/p/coloradocollegegame/source

Compile the code.

```
javac *.java
```

~~Run the Server on matheserver.~~

Run the Server anywhere you like.

```
java Server
```

Run the Client User Interface(s) ((such as ~~Representation2D~~ Representation3D)) **connecting to domain-name**

&lt;domain&gt;

 (e.g., localhost)**!**

~~java Representation2D -s~~

&lt;domain&gt;

~~```
java Representation3D -s <domain>
```~~

Move around!

**CONTROLS**
```
W - Forward
S - Backward
A - Turn Left
D - Turn Right

UP - Up
DOWN - Down
LEFT - Strafe Left
RIGHT - Strafe Right

Number Pad
1 - Not Implemented
2 - Pitch Back
3 - Not Implemented
4 - Turn Left
5 - Not Implemented
6 - Turn Right
7 - Roll Left
8 - Roll Right
9 - Pitch Right
0 - Not Implemented
```

Note: To make either the Server or the Client display testing messages (i.e. make it verbose)  add a -v:

~~java Representation2D -v -s~~

&lt;domain&gt;

~~```
java Representation3D -v -s <domain>
```~~

or

```
java Server -v
```

And have fun!

