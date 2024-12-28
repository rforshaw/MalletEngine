#version 150

uniform sampler2DArray inTex0 ;

varying vec2 outTexCoord0 ;
varying vec4 outColour ;

void main()
{
	vec4 t0 = texture( inTex0, vec3( outTexCoord0, 0 ) ) ;
	vec4 t1 = texture( inTex0, vec3( outTexCoord0, 1 ) ) ;

	gl_FragColor = mix( t0, t1, t0.a ) * outColour ;
}
