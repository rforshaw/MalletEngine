#version 120

uniform mat4 inMVPMatrix ;
uniform mat4 inPositionMatrix ;

attribute vec4 inVertex ;
attribute vec4 inColour ;
attribute vec2 inTexCoord0 ;
attribute vec4 inNormal ;

varying vec2 outTexCoord0 ;
varying vec4 outColour ;

void main()
{
	gl_Position = inMVPMatrix * inPositionMatrix * inVertex ;

	outTexCoord0 = inTexCoord0 ;
	outColour = inColour ;
}