#version 300 es

uniform sampler2D inTex0 ;

in vec2 outTexCoord0 ;
in vec4 outColour ;

out vec4 fragColor;

void main()
{
	fragColor = texture( inTex0, outTexCoord0 ) * outColour ;
}
