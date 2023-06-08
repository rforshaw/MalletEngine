#version 430 core

uniform mat4 inMVPMatrix ;

in vec4 inVertex ;
in vec4 inColour ;
in vec2 inTexCoord0 ;
in vec3 inNormal ;

struct Transformation
{
	vec4 pos ;
	vec4 off ;
	vec4 rot ;
	vec4 scale ;
} ;

layout( std140, binding = 0 ) buffer Instances
{
	Transformation transformations[] ;
} ;

out vec2 outTexCoord0 ;
out vec4 outColour ;

void main()
{
	Transformation trans = transformations[gl_InstanceID] ;

	mat4 position = mat4( 1, 0, 0, trans.pos.x,
						  0, 1, 0, trans.pos.y,
						  0, 0, 1, trans.pos.z,
						  0, 0, 0, 1 ) ;

	mat4 rotX = mat4( 1, 0, 0, 0,
					  0, cos( trans.rot.x ), -sin( trans.rot.x ), 0,
					  0, sin( trans.rot.x ), -cos( trans.rot.x ), 0,
					  0, 0, 0, 1 ) ;

	mat4 rotY = mat4( cos( trans.rot.y ), 0, sin( trans.rot.y ), 0,
					  0, 1, 0, 0,
					  -sin( trans.rot.y ), 0, cos( trans.rot.y ), 0,
					  0, 0, 0, 1 ) ;

	mat4 rotZ = mat4( cos( trans.rot.z ), -sin( trans.rot.z ), 0, 0,
					  sin( trans.rot.z ), cos( trans.rot.z ), 0, 0,
					  0, 0, 1, 0,
					  0, 0, 0, 1 ) ;

	mat4 scale = mat4( trans.scale.x, 0, 0, 0,
					   0, trans.scale.y, 0, 0,
					   0, 0, trans.scale.z, 0,
					   0, 0, 0, 1 ) ;

	mat4 off = mat4( 1, 0, 0, trans.off.x,
					 0, 1, 0, trans.off.y,
					 0, 0, 1, trans.off.z,
					 0, 0, 0, 1 ) ;

	mat4 inModelMatrix = transpose( position * rotX * rotY * rotZ * scale * off ) ;

	gl_Position = inMVPMatrix * inModelMatrix * inVertex ;

	outTexCoord0 = inTexCoord0 ;
	outColour = inColour ;
}
