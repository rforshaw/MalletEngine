#version 100

uniform mat4 inMVPMatrix ;

attribute vec4 inVertex ;
attribute vec4 inColour ;
attribute vec2 inTexCoord0 ;

varying vec2 outTexCoord0 ;
varying vec4 outColour ;

void main()
{
	gl_Position = inMVPMatrix * inVertex ;
	outTexCoord0 = inTexCoord0 ;
	outColour = inColour ;
}