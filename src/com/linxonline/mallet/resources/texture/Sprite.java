package com.linxonline.mallet.resources.texture ;

import java.util.ArrayList ;

import com.linxonline.mallet.resources.Resource ;

public final class Sprite extends Resource
{
	private static final String type = "SPRITE" ;
	public int framerate = 30 ;
	public final ArrayList<String> textures = new ArrayList<String>() ;

	public Sprite() {}

	public void addTexture( String _texture )
	{
		if( _texture != null )
		{
			textures.add( _texture ) ;
		}
	}

	public final int size()
	{
		return textures.size() ;
	}

	public final String getTexture( final int _i )
	{
		return textures.get( _i ) ;
	}

	@Override
	public String type()
	{
		return type ;
	}
}