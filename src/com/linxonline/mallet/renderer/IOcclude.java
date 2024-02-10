package com.linxonline.mallet.renderer ;

/**
	Determine whether the draw object should be drawn.
	It could be argued that this should be set on the GeometryBuffer,
	but to ensure the GeometryBuffer can be used by other DrawBuffers
	that may have different occluder requirements we specify it on a
	per DrawBuffer basis instead.
*/
public interface IOcclude
{
	/**
		Return true if the draw object should not be rendered.
		We also pass in the camera, as this will help to determine
		if the draw object is likely occluded.
	*/
	public boolean occlude( final Camera _camera, final Draw _draw ) ;
}
