
uniform mat4 inModelMatrix ;
uniform mat4 inViewMatrix ;
uniform mat4 inProjectionMatrix ;

in vec4 inVertex ;
in vec4 inColour ;

out vec4 outColour ;

void main()
{
	gl_Position = inProjectionMatrix * inViewMatrix * inModelMatrix * inVertex ;
	outColour = inColour ;
}
