package com.linxonline.mallet.renderer ;

import java.util.List ;
import java.util.ArrayList ;
import java.lang.ref.WeakReference ;

import com.linxonline.mallet.util.MalletList ;

public class TextBuffer extends ABuffer
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

	public void addDraws( final TextDraw ... _draws )
	{
		for( final TextDraw draw : _draws )
		{
			draws.add( draw ) ;
		}
	}

	public void removeDraws( final TextDraw ... _draws )
	{
		for( final TextDraw draw : _draws )
		{
			draws.remove( draw ) ;
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
	public BufferType getBufferType()
	{
		return BufferType.TEXT_BUFFER ;
	}
}
