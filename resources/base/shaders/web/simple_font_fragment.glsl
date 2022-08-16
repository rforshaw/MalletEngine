precision highp float ;

uniform sampler2D inTex0 ;

varying vec2 outTexCoord0 ;
varying vec4 outColour ;

void main()
{
	float mask = texture2D( inTex0, outTexCoord0 ).r ;
	//mask = smoothstep( 0.1, 0.65, mask ) ;

	if( mask < 0.4 )
	{
		gl_FragColor = vec4( 0 ) ;
	}
	else
	{
		gl_FragColor = vec4( outColour.r, outColour.g, outColour.b, 1 ) ;
	}
}
