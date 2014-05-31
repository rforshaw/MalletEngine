MalletEngine
============

An Entity-Component based game engine written in Java. Supports Linux, Windows, Mac, Android, &amp; iOS.

This is the initial commit of Mallet Engine 2.0. I wrote this engine while developing some 
games in my spare time.

COMPILING

Requires Java 6 JDK & Ant

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

You will need to make sure you have openAL installed. You can get it from : http://connect.creativelabs.com/openal/Downloads/oalinst.zip


run.bat


Android &amp; iOS support?

Android support has been integrated into the Mallet Engine. Android specific code is located within the android folders, and desktop specific code is located within desktop folders. The desktop build ignores Android sources and vice versa. To compile the Android Mallet Engine you will need the Android SDK, compiling Desktop Mallet Engine does not require the Android SDK.

I've yet to decide whether to integrate iOS code into the Mallet Engine codebase or keep it as a separate codebase.

Note:
The Mallet Engine contains a pre-made entry point located at: ./src/com/linxonline/mallet/main/_desktop/DesktopTestMain.java
