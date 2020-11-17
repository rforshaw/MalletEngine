package com.linxonline.mallet.renderer ;

import com.linxonline.mallet.maths.Vector3 ;
import com.linxonline.mallet.util.Interpolate ;
import com.linxonline.mallet.util.buffers.FloatBuffer ;

import com.linxonline.mallet.renderer.MalletColour ;

public class TextDraw implements IUpdate
{
	private Draw draw = new Draw() ;
	private StringBuilder text ;
	private int startIndex = 0 ;
	private int endIndex = 0 ;

	private boolean dirty = false ;
	private boolean hidden = false ;

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
		return draw.setColour( _colour ) ;
	}

	public MalletColour getColour()
	{
		return draw.getColour() ;
	}

	public void setPosition( final float _x, final float _y, final float _z )
	{
		draw.setPosition( _x, _y, _z ) ;
	}

	public Vector3 getPosition( final Vector3 _fill )
	{
		return draw.getPosition( _fill ) ;
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

	public void setScale( final float _x, final float _y, final float _z )
	{
		draw.setScale( _x, _y, _z ) ;
	}

	public Vector3 getScale( final Vector3 _fill )
	{
		return draw.getScale( _fill ) ;
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

	/**
		Force the draw objects rendering state to be updated,
		irrspective of whether the update() thinks the state 
		needs to be updated.
	*/
	public void makeDirty()
	{
		draw.makeDirty() ;
	}
}
