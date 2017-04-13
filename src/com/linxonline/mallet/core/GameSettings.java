package com.linxonline.mallet.core ;

import com.linxonline.mallet.maths.Vector2 ;

public class GameSettings
{
	private final String name ;			// Application name
	private final String config ;		// Config location

	private final Vector2 window ;
	private final Vector2 render ;

	public GameSettings( final String _name )
	{
		this( _name, "base/config.cfg" ) ;
	}

	public GameSettings( final String _name, final String _config )
	{
		this( _name,
			  _config,
			  new Vector2( 1280, 720 ),
			  new Vector2( 1280, 720 ) ) ;
	}

	public GameSettings( final String _name,
						 final String _config,
						 final Vector2 _window,
						 final Vector2 _render )
	{
		name = _name ;
		config = _config ;

		window = new Vector2( _window ) ;
		render = new Vector2( _render ) ;
	}

	public String getApplicationName() { return name ; }
	public String getConfigLocation() { return config ; }

	public int getWindowWidth() { return ( int )window.x ; }
	public int getWindowHeight() { return ( int )window.y ; }

	public int getRenderWidth() { return ( int )render.x ; }
	public int getRenderHeight() { return ( int )render.y ; }
}
