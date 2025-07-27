package com.linxonline.mallet.renderer ;

public final class Depth extends ABuffer
{
	private final int order ;

	private final Operation op ;
	private final boolean mask ;

	private boolean clear = false ;
	private boolean enable = true ;

	/**
		It's likely that whatever is being rendered as the depth
		we don't actually want to render to the colour buffer.
		Assume by default this is the case, and prevent rendering
		to it.
	*/
	private final boolean[] colourMask = new boolean[] { false, false, false, false } ;

	public Depth( final int _order, final Operation _op, final boolean _mask )
	{
		order = _order ;

		op = _op ;
		mask = _mask ;
	}

	/**
		Determine whether the depth buffer should cleared
		before enacting the specified depth operations.
	*/
	public void setClear( final boolean _clear )
	{
		clear = _clear ;
	}

	/**
		Determine whether depth testing should be enabled.
		If set to true, this will trigger enabling the depth tests.
		If set to false, this will disable the depth tests.
		NOTE: Depth tests will be disabled by the World at the end
		of the draw call we do not want depth operations from one world
		impacting another.
	*/
	public void setEnable( final boolean _enable )
	{
		enable = _enable ;
	}

	public void setColourMask( final boolean _red, final boolean _green, final boolean _blue, final boolean _alpha )
	{
		colourMask[0] = _red ;
		colourMask[1] = _green ;
		colourMask[2] = _blue ;
		colourMask[3] = _alpha ;
	}

	public boolean isEnabled()
	{
		return enable ;
	}

	public boolean shouldClear()
	{
		return clear ;
	}

	public Operation getOperation()
	{
		return op ;
	}

	public boolean getMask()
	{
		return mask ;
	}

	public boolean[] getColourMask()
	{
		return colourMask ;
	}

	@Override
	public int getOrder()
	{
		return order ;
	}

	@Override
	public void requestUpdate()
	{
		DrawAssist.update( this ) ;
	}
}
