
mat4 create_transformation( Transformation _trans )
{
	mat4 position = mat4( 1, 0, 0, _trans.pos.x,
						  0, 1, 0, _trans.pos.y,
						  0, 0, 1, _trans.pos.z,
						  0, 0, 0, 1 ) ;

	mat4 rotX = mat4( 1, 0, 0, 0,
					  0, cos( _trans.rot.x ), -sin( _trans.rot.x ), 0,
					  0, sin( _trans.rot.x ), cos( _trans.rot.x ), 0,
					  0, 0, 0, 1 ) ;

	mat4 rotY = mat4( cos( _trans.rot.y ), 0, sin( _trans.rot.y ), 0,
					  0, 1, 0, 0,
					  -sin( _trans.rot.y ), 0, cos( _trans.rot.y ), 0,
					  0, 0, 0, 1 ) ;

	mat4 rotZ = mat4( cos( _trans.rot.z ), -sin( _trans.rot.z ), 0, 0,
					  sin( _trans.rot.z ), cos( _trans.rot.z ), 0, 0,
					  0, 0, 1, 0,
					  0, 0, 0, 1 ) ;

	mat4 scale = mat4( _trans.scale.x, 0, 0, 0,
					   0, _trans.scale.y, 0, 0,
					   0, 0, _trans.scale.z, 0,
					   0, 0, 0, 1 ) ;

	mat4 off = mat4( 1, 0, 0, _trans.off.x,
					 0, 1, 0, _trans.off.y,
					 0, 0, 1, _trans.off.z,
					 0, 0, 0, 1 ) ;

	return transpose( scale * off * rotX * rotY * rotZ * position ) ;
}
