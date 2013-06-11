package com.linxonline.mallet.renderer ;

import java.util.ArrayList ;

import com.linxonline.mallet.util.settings.* ;
import com.linxonline.mallet.resources.* ;
import com.linxonline.mallet.maths.* ;

public final class RenderContainer
{
	public static final int MODEL_TYPE = 1 ;
	public static final int TEXT_TYPE = 2 ;
	public static final int GEOMETRIC_TYPE = 3 ;

	public final ArrayList<Integer> enabledTypes = new ArrayList<Integer>() ;
	public final ArrayList<Settings> furtherSettings = new ArrayList<Settings>() ;
	public final Settings settings = new Settings() ;
	public Vector3 position = null ;

	public RenderContainer() {}

	public void addFurtherSettings( final Settings _settings )
	{
		furtherSettings.add( _settings ) ;
	}

	public void removeFurtherSettings( final Settings _settings )
	{
		furtherSettings.remove( _settings ) ;
	}

	public void enableType( int _type )
	{
		if( enabledTypes.contains( _type ) == false )
		{
			enabledTypes.add( _type ) ;
		}
	}

	public void disableType( int _type )
	{
		if( enabledTypes.contains( _type ) == true )
		{
			enabledTypes.remove( _type ) ;
		}
	}
	
	public void clearResources()
	{
		clearSetting( settings ) ;
		for( final Settings set : furtherSettings )
		{
			clearSetting( set ) ;
		}
	}
	
	private void clearSetting( final Settings _settings )
	{
		Resource resource = ( Resource )_settings.getObject( "MODEL", null ) ;
		if( resource != null )
		{
			resource.unregister() ;
		}

		resource = ( Resource )_settings.getObject( "TEXTURE", null ) ;
		if( resource != null )
		{
			resource.unregister() ;
		}
	}
}