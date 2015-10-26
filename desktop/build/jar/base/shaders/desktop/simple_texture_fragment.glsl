#version 120

uniform sampler2D tex ;

varying vec2 outTexCoord0 ;
varying vec4 outColour ;

void main()
{
	gl_FragColor = texture2D( tex, outTexCoord0 ) * outColour ;
}