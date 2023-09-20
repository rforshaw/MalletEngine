package com.linxonline.mallet.renderer ;

import com.linxonline.mallet.maths.AABB ;
import com.linxonline.mallet.maths.Vector2 ;
import com.linxonline.mallet.maths.Vector3 ;
import com.linxonline.mallet.util.Interpolate ;
import com.linxonline.mallet.util.buffers.FloatBuffer ;

import com.linxonline.mallet.renderer.MalletColour ;

public final class TextDraw implements IUpdate
{
	private final Draw draw = new Draw() ;
	private final Vector2 length = new Vector2() ;

	private StringBuilder text ;
	private int startIndex = 0 ;
	private int endIndex = 0 ;

	private MalletColour colour = null ;

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
		draw.setHidden( _hide ) ;
	}

	public boolean isHidden()
	{
		return draw.isHidden() ;
	}

	public MalletColour setColour( final MalletColour _colour )
	{
		colour = _colour ;
		return colour ;
	}

	public MalletColour getColour()
	{
		return colour ;
	}

	public void setPositionInstant( final float _x, final float _y, final float _z )
	{
		draw.setPositionInstant( _x, _y, _z ) ;
	}

	public void setPosition( final float _x, final float _y, final float _z )
	{
		draw.setPosition( _x, _y, _z ) ;
	}

	public Vector3 getPosition( final Vector3 _fill )
	{
		return draw.getPosition( _fill ) ;
	}

	public void setOffsetInstant( final float _x, final float _y, final float _z )
	{
		draw.setOffsetInstant( _x, _y, _z ) ;
	}

	public void setOffset( final float _x, final float _y, final float _z )
	{
		draw.setOffset( _x, _y, _z ) ;
	}

	public Vector3 getOffset( final Vector3 _fill )
	{
		return draw.getOffset( _fill ) ;
	}

	public void setRotation( final float _x, final float _y, final float _z )
	{
		draw.setRotation( _x, _y, _z ) ;
	}

	public Vector3 getRotation( final Vector3 _fill )
	{
		return draw.getRotation( _fill ) ;
	}

	public void setScaleInstant( final float _x, final float _y, final float _z )
	{
		draw.setScaleInstant( _x, _y, _z ) ;
	}

	public void setScale( final float _x, final float _y, final float _z )
	{
		draw.setScale( _x, _y, _z ) ;
	}

	public Vector3 getScale( final Vector3 _fill )
	{
		return draw.getScale( _fill ) ;
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
		final Vector2 temp = new Vector2() ;
		draw.getPosition( temp ) ;

		float minX = temp.x ;
		float minY = temp.y ;

		draw.getOffset( temp ) ;
		minX += temp.x ;
		minY += temp.y ;

		final float maxX = minX + length.x ;
		final float maxY = minY + length.y ;

		_fill.set( minX, minY, maxX, maxY ) ;
		return _fill ;
	}

	@Override
	public boolean update( Interpolation _mode, final int _diff, final int _iteration )
	{
		return draw.update( _mode, _diff, _iteration ) ;
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
