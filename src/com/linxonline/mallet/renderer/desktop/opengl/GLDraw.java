package com.linxonline.mallet.renderer.desktop.opengl ;

import java.util.Set ;

import com.linxonline.mallet.renderer.ProgramMap ;
import com.linxonline.mallet.renderer.Draw ;
import com.linxonline.mallet.renderer.BasicDraw ;
import com.linxonline.mallet.renderer.TextData ;
import com.linxonline.mallet.renderer.Shape ;

import com.linxonline.mallet.util.caches.Cacheable ;

public abstract class GLDraw implements Draw, Cacheable
{
	private final Mode mode ;

	private com.linxonline.mallet.renderer.opengl.Location<GLGeometryUploader.BufferObject, GLDraw> newLocation ;
	private Shape drawShape = null ;

	public GLDraw( final Mode _mode )
	{
		mode = ( _mode != null ) ? _mode : Mode.BASIC ;
	}

	public Mode getMode()
	{
		return mode ;
	}

	@Override
	public Shape setShape( final Shape _shape )
	{
		drawShape = _shape ;
		return _shape ;
	}

	@Override
	public Shape getShape()
	{
		return drawShape ;
	}

	public abstract BasicDraw<GLProgram> getBasicData() ;
	public abstract TextData getTextData() ;

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
		final ProgramMap<GLProgram> map = getBasicData().getProgram() ;
		if( map != null )
		{
			final GLProgram program = map.getProgram() ;
			if( program != null )
			{
				program.getUsedResources( _activeKeys, map ) ;
			}
		}
	}

	public enum Mode
	{
		BASIC,
		TEXT,
		STENCIL,
		DEPTH ;
	}
}
