precision mediump float ;

uniform sampler2D inTex0 ;

varying vec2 outTexCoord0 ;
varying vec4 outColour ;

void main()
{
	float mask = texture2D( inTex0, outTexCoord0 ).a ;
	mask = smoothstep( 0.1, 0.75, mask ) ;

	gl_FragColor = vec4( outColour.r, outColour.g, outColour.b, mask ) ;
}