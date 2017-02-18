package com.linxonline.mallet.renderer.web.gl ;

import java.util.List ;

import com.linxonline.mallet.util.MalletList ;

import com.linxonline.mallet.renderer.Shape ;
import com.linxonline.mallet.renderer.DrawData ;
import com.linxonline.mallet.renderer.UpdateType ;
import com.linxonline.mallet.renderer.Interpolation ;
import com.linxonline.mallet.renderer.MalletTexture ;
import com.linxonline.mallet.renderer.MalletFont ;
import com.linxonline.mallet.renderer.texture.* ;

import com.linxonline.mallet.maths.Matrix4 ;
import com.linxonline.mallet.maths.Vector3 ;

public class GLDrawData extends DrawData
{
	private GLGeometryUploader.GLBuffer buffer   = null ;
	private GLGeometryUploader.Location location = null ;

	private Matrix4 drawMatrix = new Matrix4() ;
	private Shape drawShape    = null ;

	private Matrix4 clipMatrix    = null ;
	private Vector3 clipPosition  = null ;
	private Vector3 clipOffset    = null ;
	private GLProgram clipProgram = null ;
	private Shape clipShape       = null ;

	public GLDrawData() {}

	public GLDrawData( final UpdateType _type,
					   final Interpolation _mode,
					   final Vector3 _position,
					   final Vector3 _offset,
					   final Vector3 _rotation,
					   final Vector3 _scale,
					   final int _order )
	{
		super( _type, _mode, _position, _offset, _rotation, _scale, _order ) ;
	}

	public void setDrawShape( final Shape _shape )
	{
		drawShape = _shape ;
	}

	public Shape getDrawShape()
	{
		return drawShape ;
	}

	public void setClipProgram( final GLProgram _program )
	{
		clipProgram = _program ;
	}

	public GLProgram getClipProgram()
	{
		return clipProgram ;
	}

	public void setClipShape( final Shape _shape )
	{
		clipShape = _shape ;
	}

	public Shape getClipShape()
	{
		return clipShape ;
	}

	public void setClipMatrix( final Matrix4 _matrix )
	{
		clipMatrix = _matrix ;
	}

	public Matrix4 getClipMatrix()
	{
		return clipMatrix ;
	}

	public void setClipPosition( final Vector3 _position )
	{
		clipPosition = _position ;
	}

	public Vector3 getClipPosition()
	{
		return clipPosition ;
	}

	public void setClipOffset( final Vector3 _offset )
	{
		clipOffset = _offset ;
	}

	public Vector3 getClipOffset()
	{
		return clipOffset ;
	}

	public void setDrawMatrix( final Matrix4 _matrix )
	{
		drawMatrix = _matrix ;
	}
	
	public Matrix4 getDrawMatrix()
	{
		return drawMatrix ;
	}

	public void setGLBuffer( final GLGeometryUploader.GLBuffer _buffer )
	{
		buffer = _buffer ;
	}

	public GLGeometryUploader.GLBuffer getGLBuffer()
	{
		return buffer ;
	}

	public void setLocation( final GLGeometryUploader.Location _location )
	{
		location = _location ;
	}

	public GLGeometryUploader.Location getLocation()
	{
		return location ;
	}

	@Override
	public void unregister() {}

	@Override
	public void reset()
	{
		super.reset() ;
		buffer      = null ;
		drawMatrix  = null ;
		drawShape   = null ;

		clipMatrix   = null ;
		clipPosition = null ;
		clipOffset   = null ;
		clipProgram  = null ;
		clipShape    = null ;
	}
}
