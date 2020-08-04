#version 300 es

uniform mat4 inMVPMatrix ;

in vec4 inVertex ;
in vec4 inColour ;
in vec2 inTexCoord0 ;
in vec4 inNormal ;

out vec2 outTexCoord0 ;
out vec4 outColour ;

void main()
{
	gl_Position = inMVPMatrix * inVertex ;
	outTexCoord0 = inTexCoord0 ;
	outColour = inColour ;
}
