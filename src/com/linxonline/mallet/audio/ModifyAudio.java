package com.linxonline.mallet.audio ;

/**
	Defines what a Developer can do to modify the AudioSource.
	Further features like GAIN, PITCH, POSITION, will be added soon.
**/
public enum ModifyAudio
{
	PLAY,
	STOP,
	PAUSE,
	LOOP_CONTINUOSLY,
	LOOP_SET,

	ADD_CALLBACK,
	REMOVE_CALLBACK
}