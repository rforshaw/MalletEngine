package com.linxonline.mallet.renderer ;

import com.linxonline.mallet.maths.AABB ;
import com.linxonline.mallet.maths.Vector2 ;
import com.linxonline.mallet.maths.Vector3 ;

public final class TextDraw implements IUpdate
{
	private final Transformation trans = new Transformation() ;
	private final Vector2 length = new Vector2() ;

	private StringBuilder text ;
	private int startIndex = 0 ;
	private int endIndex = 0 ;

	private boolean hidden = false ;
	private Colour colour = null ;

	public TextDraw()
	{
		this( "" ) ;
	}

	public TextDraw( final String _text )
	{
		text = new StringBuilder() ;
		text.append( _text ) ;
		setRange( 0, _text.length() ) ;
	}

	public TextDraw( final StringBuilder _text )
	{
		text = _text ;
		setRange( 0, _text.length() ) ;
	}

	public void setHidden( final boolean _hide )
	{
		hidden = _hide ;
	}

	public boolean isHidden()
	{
		return hidden ;
	}

	public Colour setColour( final Colour _colour )
	{
		colour = _colour ;
		return colour ;
	}

	public Colour getColour()
	{
		return colour ;
	}

	public void setPositionInstant( final float _x, final float _y, final float _z )
	{
		trans.setPositionInstant( _x, _y, _z ) ;
	}

	public void setPosition( final float _x, final float _y, final float _z )
	{
		trans.setPosition( _x, _y, _z ) ;
	}

	public Vector3 getPosition( final Vector3 _fill )
	{
		return trans.getPosition( _fill ) ;
	}

	public void setOffsetInstant( final float _x, final float _y, final float _z )
	{
		trans.setOffsetInstant( _x, _y, _z ) ;
	}

	public void setOffset( final float _x, final float _y, final float _z )
	{
		trans.setOffset( _x, _y, _z ) ;
	}

	public Vector3 getOffset( final Vector3 _fill )
	{
		return trans.getOffset( _fill ) ;
	}

	public void setRotation( final float _x, final float _y, final float _z )
	{
		trans.setRotation( _x, _y, _z ) ;
	}

	public Vector3 getRotation( final Vector3 _fill )
	{
		return trans.getRotation( _fill ) ;
	}

	public void setScaleInstant( final float _x, final float _y, final float _z )
	{
		trans.setScaleInstant( _x, _y, _z ) ;
	}

	public void setScale( final float _x, final float _y, final float _z )
	{
		trans.setScale( _x, _y, _z ) ;
	}

	public Vector3 getScale( final Vector3 _fill )
	{
		return trans.getScale( _fill ) ;
	}

	/**
		Set the relative boundary dimensions that the text is
		expected to fill.

		NOTE: This will eventually be replaced with something
		that will support different boundary shapes. For now,
		we'll keep it square.
	*/
	public void setBoundary( final float _x, final float _y )
	{
		length.x = _x ;
		length.y = _y ;
	}

	public AABB getBoundary( final AABB _fill )
	{
		return getBoundary( _fill, new Vector2() ) ;
	}

	public AABB getBoundary( final AABB _fill, final Vector2 _temp )
	{
		trans.getPosition( _temp ) ;

		float minX = _temp.x ;
		float minY = _temp.y ;

		trans.getOffset( _temp ) ;
		minX += _temp.x ;
		minY += _temp.y ;

		final float maxX = minX + length.x ;
		final float maxY = minY + length.y ;

		_fill.set( minX, minY, maxX, maxY ) ;
		return _fill ;
	}

	@Override
	public boolean update( Interpolation _mode, final float _coefficient )
	{
		return trans.update( _mode, _coefficient ) ;
	}

	public void setRange( final int _startIndex, final int _endIndex )
	{
		startIndex = _startIndex ;
		endIndex = _endIndex ;
	}

	public StringBuilder getText()
	{
		return text ;
	}

	public int getStart()
	{
		return startIndex ;
	}

	public int getEnd()
	{
		return endIndex ;
	}
}
