MalletEngine
============

An entity-component based game engine written in Java, supports Linux, Windows, Mac, and Android.

Can be cross-compiled to support iOS and Web based platforms too, however these are not extensively tested.

Features:
  Flexible 2D/3D rendering pipeline.
  Collision Detection system.
  Entity-component model.
  UI framework.
  Audio framework.
  Animation framework.
  File management framework.
  Event messaging system.

COMPILING

The majority of development is centred around Linux and Android. We aim to keep 3rd party dependencies to a minimum, currently only requiring JOGL and JSON for desktop. Android provides OpenGL and JSON as part of its platform.

Requires Java 8 and Gradle.

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

RUNNING

To run the base Mallet-Engine go to './desktop/target'.

    On Linux call: ./run
    
This will run the Test program located at: './src/com/linxonline/mallet/core/desktop/DesktopTestMain' and will ensure that the engine successfully compiled.


Windows OpenAL support:

You will need to make sure you have openAL installed. You can get it from : http://connect.creativelabs.com/openal/Downloads/oalinst.zip
