#version 100

varying vec2 outTexCoord ;
varying vec4 outColour ;

void main()
{
	gl_FragColor = outColour ;//vec4( outColour.r, outColour.g, outColour.b, 1.0 ) ;
}