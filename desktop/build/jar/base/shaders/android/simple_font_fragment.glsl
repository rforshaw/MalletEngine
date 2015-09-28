#version 100

uniform sampler2D tex ;

varying vec2 outTexCoord ;
varying vec4 outColour ;

void main()
{
	float mask = texture2D( tex, outTexCoord ).a ;
	mask = smoothstep( 0.1, 0.75, mask ) ;

	gl_FragColor = vec4( outColour.r, outColour.g, outColour.b, mask ) ;
}