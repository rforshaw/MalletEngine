package com.linxonline.mallet.core ;

import com.linxonline.mallet.maths.IntVector2 ;

public class GameSettings
{
	private final String name ;			// Application name
	private final String config ;		// Config location

	private final IntVector2 window ;
	private final IntVector2 render ;

	public GameSettings( final String _name )
	{
		this( _name, "base/config.cfg" ) ;
	}

	public GameSettings( final String _name, final String _config )
	{
		this( _name,
			  _config,
			  new IntVector2( 1280, 720 ),
			  new IntVector2( 1280, 720 ) ) ;
	}

	public GameSettings( final String _name,
						 final String _config,
						 final IntVector2 _window,
						 final IntVector2 _render )
	{
		name = _name ;
		config = _config ;

		window = new IntVector2( _window ) ;
		render = new IntVector2( _render ) ;
	}

	public String getApplicationName() { return name ; }
	public String getConfigLocation() { return config ; }

	public int getWindowWidth() { return window.x ; }
	public int getWindowHeight() { return window.y ; }

	public int getRenderWidth() { return render.x ; }
	public int getRenderHeight() { return render.y ; }
}
