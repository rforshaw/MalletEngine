
uniform Transformation inTransformation ;
uniform mat4 inMVPMatrix ;

in vec4 inVertex ;
in vec4 inColour ;
in vec2 inTexCoord0 ;
in vec3 inNormal ;

out vec2 outTexCoord0 ;
out vec4 outColour ;

void main()
{
	mat4 inModelMatrix = create_transformation( inTransformation ) ;

	gl_Position = inMVPMatrix * inModelMatrix * inVertex ;
	outTexCoord0 = inTexCoord0 ;
	outColour = inColour ;
}
