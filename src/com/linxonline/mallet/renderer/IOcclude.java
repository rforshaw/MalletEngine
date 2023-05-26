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
	public boolean occlude( final Draw _draw ) ;
}
