#version 100

uniform mat4 inMVPMatrix ;

attribute vec4 inVertex ;
attribute vec4 inColour ;
attribute vec2 inTexCoord ;

varying vec2 outTexCoord ;
varying vec4 outColour ;

void main()
{
	gl_Position = inMVPMatrix * inVertex ;
	outTexCoord = inTexCoord ;
	outColour = inColour ;
}