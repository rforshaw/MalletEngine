package com.linxonline.mallet.renderer.android.opengl ;

import java.util.Set ;
import java.util.List ;

import com.linxonline.mallet.renderer.ProgramMap ;
import com.linxonline.mallet.renderer.Shape ;
import com.linxonline.mallet.renderer.Draw ;
import com.linxonline.mallet.renderer.BasicDraw ;
import com.linxonline.mallet.renderer.TextDraw ;
import com.linxonline.mallet.renderer.UpdateType ;
import com.linxonline.mallet.renderer.Interpolation ;

import com.linxonline.mallet.maths.Matrix4 ;
import com.linxonline.mallet.maths.Vector3 ;

import com.linxonline.mallet.util.caches.Cacheable ;

public class GLDraw implements Draw<GLDraw>, Cacheable
{
	private final BasicDraw<GLProgram> basic ;
	private TextDraw text ;

	private com.linxonline.mallet.renderer.opengl.Location<GLGeometryUploader.BufferObject, GLDraw> newLocation ;

	private Mode mode = Mode.BASIC ;
	private int endOrder = 0 ;

	private Shape drawShape    = null ;

	public GLDraw()
	{
		this( Mode.BASIC,
			  UpdateType.ON_DEMAND,
			  Interpolation.LINEAR,
			  new Vector3(),
			  new Vector3(),
			  new Vector3(),
			  new Vector3( 1, 1, 1 ), 0 ) ;
	}

	public GLDraw( final Mode _mode,
				   final UpdateType _type,
				   final Interpolation _interpolation,
				   final Vector3 _position,
				   final Vector3 _offset,
				   final Vector3 _rotation,
				   final Vector3 _scale,
				   final int _order )
	{
		basic = new BasicDraw( _type, _interpolation, _position, _offset, _rotation, _scale, _order ) ;
		setMode( _mode ) ;
	}

	public void setMode( final Mode _mode )
	{
		mode = ( _mode != null ) ? _mode : Mode.BASIC ;
		switch( mode )
		{
			case TEXT  :
			{
				text = new TextDraw() ;
				break ;
			}
			case BASIC :
			{
				text = null ;
				break ;
			}
		}
	}

	public Mode getMode()
	{
		return mode ;
	}

	public BasicDraw<GLProgram> getBasicDraw()
	{
		return basic ;
	}

	public TextDraw getTextDraw()
	{
		return text ;
	}

	public void setDrawShape( final Shape _shape )
	{
		drawShape = _shape ;
	}

	public Shape getDrawShape()
	{
		return drawShape ;
	}

	public void setEndOrder( final int _order )
	{
		endOrder = _order ;
	}
	
	public int getEndOrder()
	{
		return endOrder ;
	}

	public void setNewLocation( com.linxonline.mallet.renderer.opengl.Location<GLGeometryUploader.BufferObject, GLDraw> _location )
	{
		newLocation = _location ;
	}

	public com.linxonline.mallet.renderer.opengl.Location<GLGeometryUploader.BufferObject, GLDraw> getNewLocation()
	{
		return newLocation ;
	}

	public void getUsedResources( final Set<String> _activeKeys )
	{
		final ProgramMap<GLProgram> map = basic.getProgram() ;
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
		basic.reset() ;
		if( text != null )
		{
			text.reset() ;
		}

		setMode( Mode.BASIC ) ;
		setNewLocation( null ) ;
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
