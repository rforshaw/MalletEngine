#version 430 core

uniform mat4 inViewMatrix ;
uniform mat4 inProjectionMatrix ;

in vec4 inVertex ;
in vec4 inColour ;
in vec2 inTexCoord0 ;
in vec3 inNormal ;

out vec2 outTexCoord0 ;
out vec4 outColour ;

void main()
{
	gl_Position = inProjectionMatrix * inViewMatrix * floor( inVertex ) ;
	outTexCoord0 = inTexCoord0 ;
	outColour = inColour ;
}
