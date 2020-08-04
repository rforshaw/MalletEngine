#version 300 es

uniform sampler2D inTex0 ;

in vec2 outTexCoord0 ;
in vec4 outColour ;

out vec4 fragColor ;

void main()
{
	float mask = texture( inTex0, outTexCoord0 ).a ;
	mask = smoothstep( 0.1, 0.75, mask ) ;

	fragColor = vec4( outColour.r, outColour.g, outColour.b, mask ) ;
}
