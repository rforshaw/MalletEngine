precision mediump float ;

uniform sampler2D tex ;

varying vec2 outTexCoord0 ;
varying vec4 outColour ;

void main()
{
	float mask = texture2D( tex, outTexCoord0 ).r ;
	mask = smoothstep( 0.1, 0.75, mask ) ;

	gl_FragColor = vec4( outColour.r, outColour.g, outColour.b, mask ) ;
}