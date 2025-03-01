package com.linxonline.mallet.renderer ;

import java.util.List ;
import java.util.ArrayList ;

public final class GeometryBuffer extends ABuffer
{
	private final Shape.Attribute[] attributes ;
	private final ArrayList<Draw> draws = new ArrayList<Draw>() ;

	public GeometryBuffer( final Shape.Attribute[] _attributes )
	{
		attributes = _attributes ;
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
		Return the attributes that this geometry class expects 
		all Draw objects to adhere to.
		If the draw object's shape does not have an identical 
		attributes then it will most likely not work. Or simply 
		work by sheer fluke.
	*/
	public Shape.Attribute[] getAttribute()
	{
		return attributes ;
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

	/**
		Determine if the draw object geometry adheres
		to the same attribute structure as our buffer.

		This can be an expensive operation to do when
		calling addDraw(), so we'll allow the developer
		to validate if they are not confidant it's correct.

		Returns true if all geometry adheres to the same
		attribute structure as our geometry.
	*/
	public boolean validate()
	{
		final int size = draws.size() ;
		for( int i = 0; i < size; ++i )
		{
			final Draw draw = draws.get( i ) ;
			final IShape[] shapes = draw.getShapes() ;
			
			for( int j = 0; j < shapes.length; ++j )
			{
				final IShape shape = shapes[j] ;
				if( IShape.Attribute.isCompatible( attributes, shape.getAttribute() ) == false )
				{
					return false ;
				}
			}
		}

		return true ;
	}

	@Override
	public void requestUpdate()
	{
		DrawAssist.update( this ) ;
	}
}
