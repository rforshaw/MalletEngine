#version 300 es

uniform mat4 inMVPMatrix ;

in vec4 inVertex ;
in vec4 inColour ;

out vec4 outColour ;

void main()
{
	gl_Position = inMVPMatrix * inVertex ;

	outColour = inColour ;
}
