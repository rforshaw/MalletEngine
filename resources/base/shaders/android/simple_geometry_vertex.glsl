
uniform Transformation inTransformation ;
uniform mat4 inMVPMatrix ;

in vec4 inVertex ;
in vec4 inColour ;

out vec4 outColour ;

void main()
{
	mat4 inModelMatrix = create_transformation( inTransformation ) ;

	gl_Position = inMVPMatrix * inModelMatrix * inVertex ;
	outColour = inColour ;
}
