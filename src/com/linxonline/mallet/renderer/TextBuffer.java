package com.linxonline.mallet.renderer ;

import java.util.List ;
import java.util.ArrayList ;

public final class TextBuffer extends ABuffer
{
	private final Program program ;
	private final boolean ui ;
	private final int order ;
	private final ArrayList<TextDraw> draws = new ArrayList<TextDraw>() ;

	public TextBuffer( final Program _program,
					   final boolean _ui,
					   final int _order )
	{
		program = _program ;
		ui = _ui ;
		order = _order ;
	}

	public void addDraw( final TextDraw _draw )
	{
		draws.add( _draw ) ;
	}

	public void addDraws( final TextDraw ... _draws )
	{
		final int size = _draws.length ;
		for( int i = 0; i < size; ++i )
		{
			addDraw( _draws[i] ) ;
		}
	}

	public void removeDraw( final TextDraw _draw )
	{
		draws.remove( _draw ) ;
	}

	public void removeDraws( final TextDraw ... _draws )
	{
		final int size = _draws.length ;
		for( int i = 0; i < size; ++i )
		{
			removeDraw( _draws[i] ) ;
		}
	}

	public Program getProgram()
	{
		return program ;
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

	public List<TextDraw> getTextDraws()
	{
		return draws ;
	}

	@Override
	public void requestUpdate()
	{
		DrawAssist.update( this ) ;
	}
}
