#version 120

uniform sampler2D tex ;

varying vec2 outTexCoord ;
varying vec4 outColour ;

void main()
{
	gl_FragColor = vec4( outColour.r, outColour.g, outColour.b, texture2D( tex, outTexCoord ).a ) ;
}