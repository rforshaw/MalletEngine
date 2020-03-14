package com.linxonline.mallet.renderer.android.opengl ;

import java.util.Set ;
import java.util.List ;

import com.linxonline.mallet.renderer.ProgramMap ;
import com.linxonline.mallet.renderer.Shape ;
import com.linxonline.mallet.renderer.DrawData ;
import com.linxonline.mallet.renderer.UpdateType ;
import com.linxonline.mallet.renderer.Interpolation ;

import com.linxonline.mallet.maths.Matrix4 ;
import com.linxonline.mallet.maths.Vector3 ;

public class GLDrawData extends DrawData
{
	private com.linxonline.mallet.renderer.opengl.Location<GLGeometryUploader.BufferObject, GLDrawData> newLocation ;

	private Mode mode = Mode.BASIC ;
	private int endOrder = 0 ;

	private Matrix4 drawMatrix = new Matrix4() ;
	private Shape drawShape    = null ;

	public GLDrawData()
	{
		super() ;
	}

	public GLDrawData( final Mode _mode,
					   final UpdateType _type,
					   final Interpolation _interpolation,
					   final Vector3 _position,
					   final Vector3 _offset,
					   final Vector3 _rotation,
					   final Vector3 _scale,
					   final int _order )
	{
		super( _type, _interpolation, _position, _offset, _rotation, _scale, _order ) ;
		setMode( _mode ) ;
	}

	public void setMode( final Mode _mode )
	{
		mode = ( _mode != null ) ? _mode : Mode.BASIC ;
	}

	public Mode getMode()
	{
		return mode ;
	}

	public void setDrawShape( final Shape _shape )
	{
		drawShape = _shape ;
	}

	public Shape getDrawShape()
	{
		return drawShape ;
	}

	public void setDrawMatrix( final Matrix4 _matrix )
	{
		drawMatrix = _matrix ;
	}

	public Matrix4 getDrawMatrix()
	{
		return drawMatrix ;
	}

	public void setEndOrder( final int _order )
	{
		endOrder = _order ;
	}

	public int getEndOrder()
	{
		return endOrder ;
	}

	public void setNewLocation( com.linxonline.mallet.renderer.opengl.Location<GLGeometryUploader.BufferObject, GLDrawData> _location )
	{
		newLocation = _location ;
	}

	public com.linxonline.mallet.renderer.opengl.Location<GLGeometryUploader.BufferObject, GLDrawData> getNewLocation()
	{
		return newLocation ;
	}

	public void getUsedResources( final Set<String> _activeKeys )
	{
		final ProgramMap<GLProgram> map = ( ProgramMap<GLProgram> )getProgram() ;
		if( map != null )
		{
			final GLProgram program = map.getProgram() ;
			if( program != null )
			{
				program.getUsedResources( _activeKeys, map ) ;
			}
		}
	}

	@Override
	public void reset()
	{
		super.reset() ;
		setMode( Mode.BASIC ) ;
		setNewLocation( null ) ;
		setDrawMatrix( null ) ;
		setDrawShape( null ) ;
	}

	public enum Mode
	{
		BASIC,
		TEXT,
		STENCIL,
		DEPTH ;
	}
}
