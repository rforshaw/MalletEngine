package com.linxonline.mallet.renderer ;

import java.util.List ;
import java.util.ArrayList ;

import com.linxonline.mallet.util.MalletList ;

public class DrawBuffer extends ABuffer
{
	private final ArrayList<GeometryBuffer> buffers = new ArrayList<GeometryBuffer>() ;
	private final Program program ;
	private final Shape.Attribute[] swivel ;
	private final Shape.Style style ;
	private final boolean ui ;
	private final int order ;

	public DrawBuffer( final Program _program,
					   final Shape.Attribute[] _swivel,
					   final Shape.Style _style,
					   final boolean _ui,
					   final int _order )
	{
		program = _program ;
		swivel = _swivel ;
		style = _style ;
		ui = _ui ;
		order = _order ;
	}

	public void addBuffers( final GeometryBuffer ... _buffers )
	{
		buffers.ensureCapacity( buffers.size() + _buffers.length ) ;
		for( final GeometryBuffer buffer : _buffers )
		{
			if( isCompatible( this, buffer ) == false )
			{
				throw new RuntimeException( "Attempting to add GeometryBuffer that is not compatible with DrawBuffer." ) ;
			}
		
			buffers.add( buffer ) ;
		}
	}

	public void removeBuffers( final GeometryBuffer ... _buffers )
	{
		for( final GeometryBuffer buffer : _buffers )
		{
			buffers.remove( buffer ) ;
		}
	}

	public GeometryBuffer getBuffer( final int _index )
	{
		return buffers.get( _index ) ;
	}

	public List<GeometryBuffer> getBuffers()
	{
		return buffers ;
	}

	public Program getProgram()
	{
		return program ;
	}

	public Shape.Attribute[] getAttribute()
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

	public static boolean isCompatible( final DrawBuffer _lhs, final DrawBuffer _rhs )
	{
		if( _lhs.isUI() != _rhs.isUI() )
		{
			return false ;
		}

		if( _lhs.getOrder() != _rhs.getOrder() )
		{
			return false ;
		}

		// Lets check the cheapest value first
		if( _lhs.getStyle().equals( _rhs.getStyle() ) == false )
		{
			return false ;
		}

		if( isCompatibleAttribute( _lhs.getAttribute(), _rhs.getAttribute() ) == false )
		{
			return false ;
		}

		if( _lhs.getProgram().equals( _rhs.getProgram() ) == false )
		{
			return false ;
		}
		
		return true ;
	}

	/**
		Ensure the GeometryBuffer is compatible with the DrawBuffer.
	*/
	public static boolean isCompatible( final DrawBuffer _lhs, final GeometryBuffer _rhs )
	{
		if( _lhs.isUI() != _rhs.isUI() )
		{
			return false ;
		}

		// Lets check the cheapest value first
		if( _lhs.getStyle().equals( _rhs.getStyle() ) == false )
		{
			return false ;
		}

		if( isCompatibleAttribute( _lhs.getAttribute(), _rhs.getAttribute() ) == false )
		{
			return false ;
		}

		return true ;
	}

	private static boolean isCompatibleAttribute( final IShape.Attribute[] _a, final IShape.Attribute[] _b )
	{
		if( _a.length != _b.length )
		{
			return false ;
		}

		for( int i = 0; i < _a.length; ++i )
		{
			if( _a[i] != _b[i] )
			{
				return false ;
			}
		}

		return true ;
	}

	@Override
	public BufferType getBufferType()
	{
		return BufferType.DRAW_BUFFER ;
	}

	@Override
	public int getOrder()
	{
		return order ;
	}
}
