#version 150

varying vec4 outColour ;

void main()
{
	gl_FragColor = vec4( outColour.rgb, 1 ) ;
}
