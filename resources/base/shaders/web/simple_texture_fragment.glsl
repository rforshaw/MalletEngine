precision mediump float ;

uniform sampler2D inTex0 ;
uniform sampler2D inTex1 ;

varying vec2 outTexCoord0 ;
varying vec4 outColour ;

void main()
{
	vec4 t0 = texture2D( inTex0, outTexCoord0 ) ;
	//vec4 t1 = texture2D( inTex1, outTexCoord0 ) ;
	gl_FragColor = t0 * outColour ;//mix( t0, t1, t1.a ) * outColour ;
}
