MalletEngine
============

An Entity-Component based game engine written in Java. Supports Linux, Windows, Mac, Android, &amp; iOS.

Written by Ross Forshaw. All rights reserved.

This is the initial commit of Mallet Engine 2.0. I wrote this engine while developing some 
games in my spare time.

COMPILING

ant clean
ant -f Build.xml

or on Linux a bash script is provided:

./compile.sh

The compiled files are located in: ./build/jar/

RUNNING

To run the Mallet Engine you will need to copy the jar folder, located in ./lib to ./build/jar/ resulting 
in a final directory structure: ./build/jar/jar/

Linux &amp; Mac

./run

Windows

I have yet to write a concise script for Windows based machines.


Android &amp; iOS support?

I have yet to add the Android &amp; iOS source files to the repository. This will be done at a later date.

Note:
The Mallet Engine contains a pre-made entry point located at: ./src/com/linxonline/mallet/main/Main.java