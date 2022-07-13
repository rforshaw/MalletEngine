package com.linxonline.mallet.renderer ;

import java.util.List ;
import java.util.ArrayList ;

import com.linxonline.mallet.util.MalletList ;

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
public class DrawInstancedBuffer extends DrawBuffer
{
	private final IShape shape ;
	private final String storageName ;

	public DrawInstancedBuffer( final Program _program,
								final IShape _shape,
								final boolean _ui,
								final int _order )
	{
		this( _program, _shape, _ui, _order, "Instances" ) ;
	}

	public DrawInstancedBuffer( final Program _program,
								final IShape _shape,
								final boolean _ui,
								final int _order,
								final String _storageName )
	{
		super( _program, _shape.getAttribute(), _shape.getStyle(), _ui, _order ) ;
		shape = _shape ;
		storageName = _storageName ;
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
	public BufferType getBufferType()
	{
		return BufferType.DRAW_INSTANCED_BUFFER ;
	}
}
