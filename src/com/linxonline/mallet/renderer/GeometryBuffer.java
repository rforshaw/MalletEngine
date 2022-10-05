package com.linxonline.mallet.renderer ;

import java.util.List ;
import java.util.ArrayList ;

import com.linxonline.mallet.util.MalletList ;

public class GeometryBuffer extends ABuffer
{
	public static final IOcclude OCCLUDER_FALLBACK = new IOcclude()
	{
		@Override
		public boolean occlude( final Draw _draw )
		{
			return false ;
		}
	} ;

	private final Shape.Attribute[] swivel ;
	private final Shape.Style style ;
	private final ArrayList<Draw> draws = new ArrayList<Draw>() ;

	private IOcclude occluder = OCCLUDER_FALLBACK ;

	public GeometryBuffer( final Shape.Attribute[] _swivel,
						   final Shape.Style _style,
						   final boolean _ui )
	{
		swivel = _swivel ;
		style = _style ;
	}

	/**
		Allow the developer to specify their own occluder mechanisim.
		Before the buffer is drawn determine whether the specific
		draw object should actually be rendered.
	*/
	public void setOccluder( final IOcclude _occluder )
	{
		occluder = ( _occluder != null ) ? _occluder : OCCLUDER_FALLBACK ;
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

	public IOcclude getOccluder()
	{
		return occluder ;
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

	/**
		Determine whether the draw object should be drawn.
	*/
	public interface IOcclude
	{
		public boolean occlude( final Draw _draw ) ;
	}
}
