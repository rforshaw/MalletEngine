#version 300 es

uniform mat4 inModelMatrix ;
uniform mat4 inMVPMatrix ;

in vec4 inVertex ;
in vec4 inColour ;
in vec2 inTexCoord0 ;
in vec3 inNormal ;

struct Test
{
	int test ;
} ;

layout( std140, binding = 0 ) buffer TestBlock1 {
	Test test1 ;
} ;

layout( std140, binding = 1 ) buffer TestBlock2 {
	Test test2 ;
} ;

out vec2 outTexCoord0 ;
out vec4 outColour ;

void main()
{
	gl_Position = inMVPMatrix * inModelMatrix * inVertex ;
	outTexCoord0 = inTexCoord0 ;

	outColour = inColour ;
	outColour.r = float( test1.test ) ;
	outColour.g = float( test2.test ) ;
	outColour.b = float( test2.test ) ;
}
