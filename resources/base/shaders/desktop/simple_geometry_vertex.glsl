#version 150

uniform mat4 inModelMatrix ;
uniform mat4 inMVPMatrix ;

attribute vec4 inVertex ;
attribute vec4 inColour ;

varying vec4 outColour ;

void main()
{
	gl_Position = inMVPMatrix * inModelMatrix * inVertex ;
	outColour = inColour ;
}
