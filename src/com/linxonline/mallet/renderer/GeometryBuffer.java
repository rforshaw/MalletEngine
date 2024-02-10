package com.linxonline.mallet.renderer ;

import java.util.List ;
import java.util.ArrayList ;

import com.linxonline.mallet.util.MalletList ;

public class GeometryBuffer extends ABuffer
{
	private final Shape.Attribute[] swivel ;
	private final Shape.Style style ;
	private final ArrayList<Draw> draws = new ArrayList<Draw>() ;

	public GeometryBuffer( final Shape.Attribute[] _swivel,
						   final Shape.Style _style )
	{
		swivel = _swivel ;
		style = _style ;
	}

	public void addDraws( final Draw ... _draws )
	{
		final int size = _draws.length ;
		for( int i = 0; i < size; ++i )
		{
			final Draw draw = _draws[i] ;
			draws.add( draw ) ;
		}
	}

	public void removeDraws( final Draw ... _draws )
	{
		final int size = _draws.length ;
		for( int i = 0; i < size; ++i )
		{
			final Draw draw = _draws[i] ;
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
	public Shape.Attribute[] getAttribute()
	{
		return swivel ;
	}

	public Shape.Style getStyle()
	{
		return style ;
	}

	@Override
	public int getOrder()
	{
		throw new UnsupportedOperationException( "getOrder() is not intended to be used within GeometryBuffer." ) ;
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
