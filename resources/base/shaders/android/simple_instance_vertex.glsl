#version 300 es

uniform mat4 inMVPMatrix ;

in vec4 inVertex ;
in vec4 inColour ;
in vec2 inTexCoord0 ;
in vec3 inNormal ;

layout( std140, binding = 0 ) buffer Instances
{
	mat4 inModelMatrix[] ;
} ;

out vec2 outTexCoord0 ;
out vec4 outColour ;

void main()
{
	gl_Position = inMVPMatrix * inModelMatrix[gl_InstanceID] * inVertex ;

	outTexCoord0 = inTexCoord0 ;
	outColour = inColour ;
}
