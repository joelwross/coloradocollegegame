Hi, all!

Benjamin and I just uploaded the code to Google Code. To access it, go to
http://code.google.com/p/coloradocollegegame/source
and follow the directions there.

You'll need a password, which is in your profile. Click on "My Profile" (top-right of the page), then "Settings" (top-left of the page), and your password should be there.

We haven't commented anything yet, but poke around anyway - we challenge you to comprehend our convolusion.

Also: you probably won't get it to run because we just now realized that the clients are hard-coded to connect to Mathserver. Oops! If you want to change that, it's in ClientIO.java line 34. If you don't, we'll fix it soon.

- Omer.

P.S. The version of svn on Mathserver (and subsequently on the computers in the Linux lab) is too old to correctly handle a secure connection (https). We've gotten it to work with svn 1.3, but the current version is 1.4.

