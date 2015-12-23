#version 100

uniform sampler2D inTex0 ;

varying vec2 outTexCoord0 ;
varying vec4 outColour ;

void main()
{
	gl_FragColor = texture2D( inTex0, outTexCoord0 ) * outColour ;
}