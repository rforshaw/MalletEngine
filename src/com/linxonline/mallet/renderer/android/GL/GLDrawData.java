package com.linxonline.mallet.renderer.android.GL ;

import java.util.ArrayList ;

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
	private final ArrayList<Texture<GLImage>> textures = new ArrayList<Texture<GLImage>>() ;

	private Matrix4 drawMatrix    = new Matrix4() ;
	private Shape drawShape       = null ;

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

	@Override
	public void setFont( final MalletFont _font )
	{
		clearTextures() ;
		super.setFont( _font ) ;
	}

	public void addTexture( final MalletTexture _texture )
	{
		super.addTexture( _texture ) ;
	}

	public void removeTexture( final MalletTexture _texture )
	{
		super.removeTexture( _texture ) ;
	}

	public void clearTextures()
	{
		final int size = textures.size() ;
		for( int i = 0; i < size; i++ )
		{
			final Texture<GLImage> texture = textures.get( i ) ;
			texture.unregister() ;
		}
		textures.clear() ;
		getMalletTextures().clear() ;
	}

	public void setDrawShape( final Shape _shape )
	{
		drawShape = _shape ;
	}

	public Shape getDrawShape()
	{
		return drawShape ;
	}

	public ArrayList<Texture<GLImage>> getGLTextures()
	{
		return textures ;
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

	@Override
	public void unregister()
	{
		for( final Texture<GLImage> texture : textures )
		{
			texture.unregister() ;
		}
		textures.clear() ;
	}

	@Override
	public void reset()
	{
		super.reset() ;
		drawMatrix  = null ;
		drawShape   = null ;

		clipMatrix   = null ;
		clipPosition = null ;
		clipOffset   = null ;
		clipProgram  = null ;
		clipShape    = null ;
	}
}
