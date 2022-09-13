package com.linxonline.mallet.renderer ;

public final class RenderPools
{
	private static final DrawUpdaterPool drawPool = new DrawUpdaterPool() ;
	private static final DrawInstancedUpdaterPool drawInstancedPool = new DrawInstancedUpdaterPool() ;
	private static final TextUpdaterPool textPool = new TextUpdaterPool() ;

	public static DrawUpdaterPool getDrawUpdaterPool()
	{
		return drawPool ;
	}

	public static DrawInstancedUpdaterPool getDrawInstancedUpdaterPool()
	{
		return drawInstancedPool ;
	}

	public static TextUpdaterPool getTextUpdaterPool()
	{
		return textPool ;
	}
}
