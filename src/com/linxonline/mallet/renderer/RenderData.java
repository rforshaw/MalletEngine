package com.linxonline.mallet.renderer ;

import com.linxonline.mallet.util.settings.Settings ;
import com.linxonline.mallet.maths.* ;

import com.linxonline.mallet.util.caches.Cacheable ;
import com.linxonline.mallet.util.sort.SortInterface ;

public class RenderData implements SortInterface, Cacheable
{
	public int id ;
	public int type ;
	public int layer ;
	public Vector3 position ;
	public Settings drawData ;

	public DrawInterface drawCall = null ;

	public RenderData()
	{
		set( -1, -1, null, new Vector3(), -1 ) ;
	}

	public RenderData( final int _id,
						final int _type,
						final Settings _draw,
						final Vector3 _position,
						final int _layer )
	{
		set( _id, _type, _draw, _position, _layer ) ;
	}

	public void set( final int _id,
						final int _type,
						final Settings _draw,
						final Vector3 _position,
						final int _layer )
	{
		id = _id ;
		type = _type ;
		layer = _layer ;
		drawData = _draw ;
		position = _position ;
		
		if( drawData != null )
		{
			drawData.addInteger( "ID", _id ) ;
		}
	}

	public void copy( final RenderData _data )
	{
		id = _data.id ;
		type = _data.type ;
		layer = _data.layer ;

		position.setXYZ( _data.position ) ;

		drawData = _data.drawData ;
		drawCall = _data.drawCall ;
	}

	public int sortValue()
	{
		return layer ;
	}

	public void unregisterResources() {}

	public void reset()
	{
		int id = -1 ;
		int type = -1 ;
		int layer = -1 ;
		Vector3 position = null ;
		drawData = null ;
		drawCall = null ; 
	}
}