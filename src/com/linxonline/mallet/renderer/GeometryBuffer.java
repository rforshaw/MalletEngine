package com.linxonline.mallet.renderer ;

import java.util.List ;
import java.util.ArrayList ;

import com.linxonline.mallet.util.MalletList ;

public class GeometryBuffer extends ABuffer
{
	private final Shape.Swivel[] swivel ;
	private final Shape.Style style ;
	private final boolean ui ;
	private final int order ;
	private final ArrayList<Draw> draws = new ArrayList<Draw>() ;

	public GeometryBuffer( final Shape.Swivel[] _swivel,
						   final Shape.Style _style,
						   final boolean _ui,
						   final int _order )
	{
		swivel = _swivel ;
		style = _style ;
		ui = _ui ;
		order = _order ;
	}

	public void addDraws( final Draw ... _draws )
	{
		for( final Draw draw : _draws )
		{
			draws.add( draw ) ;
		}
	}

	public void removeDraws( final Draw ... _draws )
	{
		for( final Draw draw : _draws )
		{
			draws.remove( draw ) ;
		}
	}

	/**
		Return the swivel that this geometry class expects 
		all Draw objects to adhere to.
		If the draw object's shape does not have an identical 
		swivel then it will most likely not work. Or simply 
		work by sheer fluke.
	*/
	public Shape.Swivel[] getSwivel()
	{
		return swivel ;
	}

	public Shape.Style getStyle()
	{
		return style ;
	}

	public boolean isUI()
	{
		return ui ;
	}

	@Override
	public int getOrder()
	{
		return order ;
	}

	public List<Draw> getDraws()
	{
		return draws ;
	}

	@Override
	public BufferType getBufferType()
	{
		return BufferType.GEOMETRY_BUFFER ;
	}
}
