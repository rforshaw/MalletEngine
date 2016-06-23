package com.linxonline.mallet.io.formats.sgeom ;

import com.linxonline.mallet.io.filesystem.GlobalFileSystem ;
import com.linxonline.mallet.io.filesystem.FileStream ;
import com.linxonline.mallet.io.formats.json.JSONObject ;
import com.linxonline.mallet.io.formats.json.JSONArray ;

import com.linxonline.mallet.util.logger.Logger ;

import com.linxonline.mallet.renderer.MalletColour ;
import com.linxonline.mallet.renderer.Shape ;

import com.linxonline.mallet.maths.Vector2 ;
import com.linxonline.mallet.maths.Vector3 ;

public class SGeom
{
	public static Shape load( final String _file )
	{
		final FileStream stream = GlobalFileSystem.getFile( _file ) ;
		if( stream.exists() == false )
		{
			return null ;
		}

		final JSONObject json = JSONObject.construct( stream ) ;

		final Shape.Style style = Shape.Style.getStyleByString( json.optString( "style", null ) ) ;

		final Shape.Swivel[] swivel = SGeom.constructSwivelOrder( json.optJSONArray( "swivel" ) ) ;
		if( swivel == null )
		{
			Logger.println( "No swivel order defined.", Logger.Verbosity.NORMAL ) ;
			return null ;
		}

		final JSONArray vertices = json.optJSONArray( "vertices" ) ;
		if( vertices == null )
		{
			Logger.println( "No vertices defined.", Logger.Verbosity.NORMAL ) ;
			return null ;
		}

		final JSONArray indices = json.optJSONArray( "indices" ) ;
		if( indices == null )
		{
			Logger.println( "No indices defined.", Logger.Verbosity.NORMAL ) ;
			return null ;
		}

		final int iSize = indices.length() ;
		final int vSize = vertices.length() ;
		final Shape shape = new Shape( style, swivel, iSize, vSize / swivel.length ) ;

		for( int i = 0; i < iSize; i++ )
		{
			// Copy indicies straight into Shape index buffer.
			shape.addIndex( indices.optInt( i ) ) ;
		}

		int swivelIndex = 0 ;
		Object[] vertex = null ;

		for( int i = 0; i < vSize; i++ )
		{
			// SGeom format does not group a specific vertex data together
			// What is associated to a specific vertex is defined by 
			// the swivel order and order of the vertices array.
			// swivel: ["POINT", "COLOUR"]
			// vertices: [{POINT},{COLOUR},  {POINT},{COLOUR}]
			// The first POINT and COLOUR represent vertex 1, the second 
			// POINT and COLOUR represent vertex 2.
			swivelIndex = i % swivel.length ;
			if( swivelIndex == 0 )
			{
				vertex = new Object[swivel.length] ;
			}

			final JSONObject jVertex = vertices.optJSONObject( i ) ;
			switch( swivel[swivelIndex] )
			{
				case POINT  :
				{
					final float x = ( float )jVertex.optDouble( "x" ) ;
					final float y = ( float )jVertex.optDouble( "y" ) ;
					final float z = ( float )jVertex.optDouble( "z" ) ;
					vertex[swivelIndex] = new Vector3( x, y, z ) ;
					//System.out.println( "Adding Point" + vertex[swivelIndex] ) ;
					break ;
				}
				case COLOUR :
				{
					final int r = jVertex.optInt( "r" ) ;
					final int g = jVertex.optInt( "g" ) ;
					final int b = jVertex.optInt( "b" ) ;
					vertex[swivelIndex] = new MalletColour( r, g, b ) ;
					//System.out.println( "Adding Colour" + vertex[swivelIndex] ) ;
					break ;
				}
				case NORMAL :
				{
					final float x = ( float )jVertex.optDouble( "x" ) ;
					final float y = ( float )jVertex.optDouble( "y" ) ;
					final float z = ( float )jVertex.optDouble( "z" ) ;
					vertex[swivelIndex] = new Vector3( x, y, z ) ;
					//System.out.println( "Adding Normal" + vertex[swivelIndex] ) ;
					break ;
				}
				case UV     :
				{
					final float u = ( float )jVertex.optDouble( "u" ) ;
					final float v = ( float )jVertex.optDouble( "v" ) ;
					vertex[swivelIndex] = new Vector2( u, v ) ;
					//System.out.println( "Adding UV: " + vertex[swivelIndex] ) ;
					break ;
				}
			}

			if( vertex != null && swivelIndex == ( swivel.length - 1 ) )
			{
				/*if( Shape.isCorrectSwivel( swivel, vertex ) == false )
				{
					Logger.println( "Swivel and vertex are out of sync. Unable to build shape.", Logger.Verbosity.NORMAL ) ;
					return null ;
				}*/

				shape.addVertex( vertex ) ;
			}
		}

		return shape ;
	}

	private static Shape.Swivel[] constructSwivelOrder( final JSONArray _swivel )
	{
		if( _swivel == null )
		{
			return null ;
		}

		final int length = _swivel.length() ;
		final Shape.Swivel[] swivel = new Shape.Swivel[length] ;

		for( int i = 0; i < length; i++ )
		{
			swivel[i] = Shape.Swivel.getSwivelByString( _swivel.optString( i ) ) ;
		}

		return swivel ;
	}
}
