3D Graphics can, obviously, greatly enhance the appearance and feel of a game. 3D environments can be more interactive, and generally look more impressive than their 2D counterparts.


# Make It Pretty! (or "Creating 3D Graphics") #
At the most abstract level, there are three basic steps for creating three dimensional graphical effects:
  1. **Model the world in three dimensions**
  1. **Convert from three dimensions into two dimensions**
  1. **Draw the two dimensional model**
And that's really about it. The hard part is actually performing these steps.

## Model the world in three dimensions ##
Graphical worlds are treated as existing in R<sup>3</sup>. In general, 3D objects are defined as a collection of three-dimensional points {x,y,z}. These points usually represent the vertices (or corners) of a polyhedron. So your basic cube is defined by 8 points. These points can then be collected into polygons (faces), or more commonly into collections of triangles and quadrilaterals ("quads" for short). Keeping track of objects based on points, as well as where the faces exist, may not be a simple problem. We can also use all kinds of vector geometry to manipulate (transform) the object--move it, rotate it, blow it up, whatever.

## Convert from three dimensions into two dimensions ##
While we may be storing a 3D world inside the computer, the screen we want to display on only has two dimensions. So we need to convert our 3D computer model world into something that can be displayed. The simplest way to do this is to use a simple projection--project the 3D world onto the plane which is the computer screen.

## Draw the two dimensional model ##
Now that we've turned our 3D world into something that can be represented in the 2D world of the screen, we can go ahead and draw it. We can use premade methods like Java's DrawPolygon() method, or define our own ways of getting the pixels of the screen to light up the way we want them to. Note that when using **ray-tracing** (a method for determining how to _shade_ objects), we're deciding how to draw the 2D model based on whatever we had to do to convert it to 2D--the color of the pixel is determined by the length of the projection, for example.


# Reinventing The Wheel (or "Using Graphical APIs") #
There is a lot of work that gets done in storing a world in 3D, and then drawing that 3D world on the screen in a way that looks pretty. Somewhere or other, we're going to have let someone else do the work--we need to stop somewhere on our journey down the graphics pipeline. At the lowest level, this could be rasterization: we could fill a pixel-array that represents the screen with the colors that we want, and then let someone else (the machine) put that image on the display. At a higher level, we can use stuf like the DrawPolygon() method to let someone else figure out how to color the pixels. At a higher level, we can just define the polyhedra models ourselves and let someone else convert them to 2D and put them on the screen. And at the highest level, we can say that we want a blue cube, and let the machine do the rest.

We need to decide how much work we want to do. Do we want to spend more time on making something appear from (relative) scratch? Or do we want to spend more time on making something look really good? One produces work that is more original, one produces work of higher quality. And this decision will inform which API we decide we'd like to use.


# The Giving Tree (or "The Java3D API") #
The Java3D API is a project being developed by users on java.sun.com. It attempts to allow the user to create 3D graphics and environments for various programs in an efficient manner, while hiding a lot of the effort involved in actually putting the images on the screen.

Java3D's main feature is the tree structure it uses to organize and render environments. This tree is call the **Scene Graph**. In effect, each branch of a tree represents a different object in the world, and so Java renders the graph efficently by traversing the tree in some amazing way (I think anyway. I'm not sure exactly how this works, as documentation on the rending process has eluded me). Nodes in the tree are either Groups which link together objects and transformations of objects, or the leaf objects themselves. The structure is pretty easy to understand once you work with it a bit, and it becomes easy to extend or modify a world.

Java3D also includes predefined objects like Cubes and Spheres, as well as a well-defined methodology for including stuff like background or fog. Basically, you just add the element you want to the scene graph, and Java takes care of the rest for you. Java3D is full customizable, so you can model your own 3D objects with no problem. It also allows behavior definitions and the ability to load worlds from 3D modeling programs like Blender or Maya. It even has support for sound, though I haven't figured out the detail of this yet.

And best of all, Java3D is built in Java and designed to run in Java. Everything you've ever learned about Java programming applies, and makes it easy to implement and use. It has nice Java Documentation, so we can easily look up new classes we need to use. Indeed, the main advantage of Java3D is that so much work is done for us. We can find other classes to use, rather than have to write our own. We can get a good looking world quickly, without worrying about making it look quickly good.

As of this writing, I don't know how easy it would be to implement Java3D on top of our game engine. I believe that we could just pass around coordinates (the same as are passed around for 2D drawing) and then have those affect the transformations that locate the objects for Java3D to render. Basically we can get cheap and easy rendering with little overhead.

**Java3D Links**

[The Java3D Homepage](http://java.sun.com/products/java-media/3D/index.jsp)

[The Java3D API](http://download.java.net/media/java3d/javadoc/1.5.0/index.html)

[The Java3D Tutorial](http://java.sun.com/developer/onlineTraining/java3d/index.html)


# Go Go Gadget Graphics Card! (or "OpenGL through JOGL") #
Most modern games are designed using one of two graphical APIs: OpenGL or Direct3D. Direct3D (commonly known as DirectX) is Microsoft's graphical API. It's used for Windows and for XBox games. Unfortunately, it only runs on Windows, so those of us with Macs or Linux machines couldn't use it. The other major API is OpenGL. OpenGL runs on any machine with a supporting graphics card--which effectively means, in my limited experience, most every machine. Thus, we'd want to use OpenGL over Direct3D.

OpenGL does much of what the Java3D API does. Indeed, Java3D runs on top of either OpenGL or Direct3D, using those APIs to interface with the graphics hardware. OpenGL lets us model the world, and then renders that world so that we don't have to. However, modeling a world in OpenGL looks a little uglier than in Java3D. OpenGL does not have built-in 3D objects like cubes or spheres. Instead it has built-in polygons, triangles, and quads. You define the points of your triangle and then let the computer and the graphics card do the rendering. Thus code can get a little tedious, though it is possible to load and parse text files that store the objects of the world, thereby automating some of the content creation. OpenGL also includes methods for defining backgrounds and transparency, just like Java3D. OpenGL has been around longer than Java3D and seems to have a larger support community, so there may be more example code (though fewer pre-build classes that we can use without modification).

OpenGL does not run using native Java-style code. Indeed, I believe it was originally written to work in C. However, there are numerous "bindings" that allow us to write OpenGL code in Java, the most popular of which seems to be JOGL (Java OpenGL). In order to use JOGL, you simply implement an OpenGL interface, create an "OpenGL canvas"-type object, and then use that to call OpenGL commands as if you were writing in C. In the end it looks similar to all your g2d.drawPolygon() calls, but you're using the slightly thick looking OpenGL methods.

So again, integration with our game engine may or may not be a problem. It seems to be relatively easy to abstract OpenGL methods to other classes (so, for example, to create our own Cube class), which we can then move around just like with 2D objects. This effectively gives us some of the predefined classes of Java3D, while allowing us to still work directly with the graphics card. Of course, we do loose the optimizations that Java3D allows. On the other hand, OpenGL is more widely used than Java3D, and may be more helpful to know in the future.

I'll also add that I'm biased: I think programming in OpenGL is a lot more fun that programming in Java3D.

Edit: While OpenGL is awesome, JOGL is kind of a bitch. Pick a standard and stick with it you fools!

**OpenGL/JOGL Links**

[JOGL Homepage](https://jogl.dev.java.net/)

[JOGL API](http://download.java.net/media/jogl/builds/nightly/javadoc_public/index.html)

[JOGL "Getting Started" Instructions - NOTE: The original post is out of date. The 4th page of the thread has an example using the new standard](http://javagaming.org/forums/index.php?topic=1474.0)

[NeHe's OpenGL Tutorials (very good!)](http://nehe.gamedev.net/)


# In The Beginning, There Was The Point3D (or "Creating Our Own API") #
Building our own API, our own set of classes to do 3D graphics, is not impossible. I know I managed to build a working 3D renderer, though it doesn't have nearly the capabilities of Java3D or OpenGL. It also runs very slowly, though there are obvious places for improvement (indeed, I am working on writing a cleaner version in C++, though it could be done in Java).

My program design involves defining Point3D objects (points in three dimensional space), and then from them building Blocks, which are convex polyhedra in space. Rendering is done either through a simple projection algorithm and liberal use of the DrawPolygon() method, or by single-vector raytracing to each object. While texture-mapping is possible, really nice texturing would require more research and work.

It's doable, but do we really want to do it?


# So What Already? (or "Conclusion") #
3D graphics are of course a layer that is added onto the more important game engine--graphics are simply for display, the engine actually lets people play the game. There is also a definate learning curve for whatever method we decide to use to give the game 3D graphics. But they just look so good, I think we have to implement them in some manner.

I would suggest we use a premade API like Java3D or OpenGL rather than attempt to create our own. While having our own rendering would make the game more "original" (and could be fun to do in it's own right), I feel that rendering is not the point of the project. The point is to learn how to make a working game, and develop something that looks damn impressive so that other people will gawk at it and think we're awesome. We can get such an effect with much less effort if we don't try to create things from scratch.

Given the choice between Java3D and OpenGL, I believe that Java3D would be easier to use and would probably give us a better end result (or at least a more nicely integrated one). However, knowing OpenGL could be more helpful in the long run, and does allow us to do things at a lower level (and as such, with slightly more flexibility). I would personally push for OpenGL because I have a stronger interest in learning that, but I believe that Java3D would likely be a better option for the group.


### Don't Shoot the Messenger (or "Disclaimer") ###
All "facts" in this page are as I understand them from my brief (1-2 week) exploration of web resources. I do not know everything about graphics, Java3D or OpenGL. It is quite possible I understood something wrong, and as such reported false information. But if necessary, the page can be updated to include better information--that's the wonder of a wiki!

Oh, I also didn't cite my sources. So sue me.





