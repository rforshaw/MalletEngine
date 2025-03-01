package com.linxonline.mallet.renderer ;

/**
	Draw Instanced Buffer ignores the Draw objects 
	own shape and renders its shape using the 
	position, rotation, scale of the draw object.

	Draw object transformations require a Storage 
	object to store a Matrix4 per draw. By default 
	this is expected to be called 'Instances' within 
	the shader program.

	The Storage object is created behind the scenes 
	and managed internally by the renderer, you do 
	not have to specify a Storage object. 
*/
public final class DrawInstancedBuffer extends DrawBuffer
{
	private final IShape shape ;
	private final String storageName ;
	private final boolean isStatic ;

	public DrawInstancedBuffer( final Program _program,
								final IShape _shape,
								final boolean _ui,
								final int _order )
	{
		this( _program, _shape, _ui, _order, false, "Instances" ) ;
	}

	public DrawInstancedBuffer( final Program _program,
								final IShape _shape,
								final boolean _ui,
								final int _order,
								final boolean _static )
	{
		this( _program, _shape, _ui, _order, _static, "Instances" ) ;
	}

	public DrawInstancedBuffer( final Program _program,
								final IShape _shape,
								final boolean _ui,
								final int _order,
								final boolean _static,
								final String _storageName )
	{
		super( _program, _ui, _order ) ;
		shape = _shape ;
		storageName = _storageName ;
		isStatic = _static ;
	}

	public boolean isStatic()
	{
		return isStatic ;
	}

	public IShape getShape()
	{
		return shape ;
	}

	public String getStorageName()
	{
		return storageName ;
	}

	@Override
	public void requestUpdate()
	{
		DrawAssist.update( this ) ;
	}
}
