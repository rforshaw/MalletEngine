MalletEngine
============

A flexible game-engine written in Java intended for Desktop (Linux and Windows), Mobile (Android), and Web.

**NOTE:** I have not kept the Mobile, or Web sides up to date with the developments on the Desktop side.

## Features:
 - Flexible 2D/3D rendering pipeline, using OpenGL 3.2.
 - Collision Detection system.
 - Entity/Component model - We provide two implementations of a Component based system.
 - UI framework.
 - Audio framework.
 - Animation framework.
 - File management framework.
 - Event messaging system.

# COMPILING

The majority of development is centred around Linux. We aim to keep 3rd party dependencies to a minimum.

Desktop dependencies: JOGL, JSON, Rhino.

Each supported platform has their own building scripts located within their own directory situated at the root.

    Android : ./android
    iOS: ./ios
    Desktop(Windows, Linux, Mac): ./desktop
    Web: ./web

The Desktop and Android platforms use gradle as their build-system. Make sure you have Gradle installed.

    On Linux call: ./compile.sh

This will clear the previous build and generate an output within './desktop/target'.

*Note: Building desktop will copy the Mallet-Engine and dependencies to './TemplateProject', this project is intended as the starting basis for your own game.*

The contents of './resources' is copied into each of the targetted platforms and provides the base data.

*Note: TemplateProject also contains a 'resource' directory store your games data within 'resource/base'*

# RUNNING

To run the base Mallet-Engine go to './desktop/target'.

    On Linux call: ./run
    
This will run the Test program located at: './src/com/linxonline/mallet/core/desktop/DesktopTestMain' and will ensure that the engine successfully compiled.


Windows OpenAL support:

You will need to make sure you have openAL installed. You can get it from : http://connect.creativelabs.com/openal/Downloads/oalinst.zip
