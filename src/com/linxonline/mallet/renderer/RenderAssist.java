package com.linxonline.mallet.renderer ;

public final class RenderAssist
{
	private static RenderAssist.Assist assist ;

	private RenderAssist() {}

	/**
		This should be set by the Renderer.
	*/
	public static void setAssist( final RenderAssist.Assist _interface )
	{
		assist = _interface ;
	}

	public static void setDisplayDimensions( final int _width, final int _height )
	{
		assist.setDisplayDimensions( _width, _height ) ;
	}

	public static void setFullscreen( final boolean _set )
	{
		assist.setFullscreen( _set ) ;
	}

	public interface Assist
	{
		public void setDisplayDimensions( final int _width, final int _height ) ;
		public void setFullscreen( final boolean _set ) ;
	}
}
