#version 430 core

uniform mat4 inMVPMatrix ;

in vec4 inVertex ;
in vec4 inColour ;
in vec2 inTexCoord0 ;
in vec3 inNormal ;

layout( std140, binding = 0 ) buffer TestBlock1 {
	int test ;
} Test1 ;

layout( std140, binding = 1 ) buffer TestBlock2 {
	int test ;
} Test2 ;

out vec2 outTexCoord0 ;
out vec4 outColour ;

void main()
{
	int test1 = Test1.test ;
	int test2 = Test2.test ;

	gl_Position = inMVPMatrix * inVertex ;
	outTexCoord0 = inTexCoord0 ;

	outColour = inColour ;
	outColour.r = test1 ;
	outColour.g = test2 ;
	outColour.b = test2 ;
}
