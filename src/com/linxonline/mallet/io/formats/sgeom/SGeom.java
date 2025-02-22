package com.linxonline.mallet.io.formats.sgeom ;

import com.linxonline.mallet.io.filesystem.GlobalFileSystem ;
import com.linxonline.mallet.io.filesystem.FileStream ;
import com.linxonline.mallet.io.formats.json.JObject ;
import com.linxonline.mallet.io.formats.json.JArray ;

import com.linxonline.mallet.util.Logger ;

import com.linxonline.mallet.renderer.Colour ;
import com.linxonline.mallet.renderer.Shape ;

import com.linxonline.mallet.maths.Vector2 ;
import com.linxonline.mallet.maths.Vector3 ;

public class SGeom
{
	private SGeom() {}

	public static Shape load( final String _file )
	{
		final FileStream stream = GlobalFileSystem.getFile( _file ) ;
		if( stream.exists() == false )
		{
			return null ;
		}

		final JObject json = JObject.construct( stream ) ;

		final Shape.Style style = Shape.Style.getStyleByString( json.optString( "style", null ) ) ;

		final Shape.Attribute[] swivel = SGeom.constructAttributeOrder( json.getJArray( "swivel" ) ) ;
		if( swivel == null )
		{
			Logger.println( "No swivel order defined.", Logger.Verbosity.NORMAL ) ;
			return null ;
		}

		final JArray vertices = json.getJArray( "vertices" ) ;
		if( vertices == null )
		{
			Logger.println( "No vertices defined.", Logger.Verbosity.NORMAL ) ;
			return null ;
		}

		final JArray indices = json.getJArray( "indices" ) ;
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
			shape.addIndex( indices.getInt( i ) ) ;
		}

		int swivelIndex = 0 ;
		Object[] vertex = null ;

		for( int i = 0; i < vSize; i++ )
		{
			// SGeom format does not group a specific vertex data together
			// What is associated to a specific vertex is defined by 
			// the swivel order and order of the vertices array.
			// swivel: ["VEC3", "FLOAT"]
			// vertices: [{VEC3},{FLOAT},  {VEC3},{FLOAT}]
			// The first VEC3 and FLOAT represent vertex 1, the second 
			// VEC3 and FLOAT represent vertex 2.
			swivelIndex = i % swivel.length ;
			if( swivelIndex == 0 )
			{
				vertex = new Object[swivel.length] ;
			}

			final JObject jVertex = vertices.getJObject( i ) ;
			switch( swivel[swivelIndex] )
			{
				case VEC3  :
				{
					final float x = ( float )jVertex.getDouble( "x" ) ;
					final float y = ( float )jVertex.getDouble( "y" ) ;
					final float z = ( float )jVertex.getDouble( "z" ) ;
					vertex[swivelIndex] = new Vector3( x, y, z ) ;
					//System.out.println( "Adding Point" + vertex[swivelIndex] ) ;
					break ;
				}
				case FLOAT :
				{
					final int r = jVertex.getInt( "r" ) ;
					final int g = jVertex.getInt( "g" ) ;
					final int b = jVertex.getInt( "b" ) ;
					vertex[swivelIndex] = new Colour( r, g, b ) ;
					//System.out.println( "Adding Colour" + vertex[swivelIndex] ) ;
					break ;
				}
				case VEC2     :
				{
					final float u = ( float )jVertex.getDouble( "u" ) ;
					final float v = ( float )jVertex.getDouble( "v" ) ;
					vertex[swivelIndex] = new Vector2( u, v ) ;
					//System.out.println( "Adding VEC2: " + vertex[swivelIndex] ) ;
					break ;
				}
			}

			if( vertex != null && swivelIndex == ( swivel.length - 1 ) )
			{
				/*if( Shape.isCorrectAttribute( swivel, vertex ) == false )
				{
					Logger.println( "Attribute and vertex are out of sync. Unable to build shape.", Logger.Verbosity.NORMAL ) ;
					return null ;
				}*/

				shape.copyVertex( vertex ) ;
			}
		}

		return shape ;
	}

	private static Shape.Attribute[] constructAttributeOrder( final JArray _swivel )
	{
		if( _swivel == null )
		{
			return null ;
		}

		final int length = _swivel.length() ;
		final Shape.Attribute[] swivel = new Shape.Attribute[length] ;

		for( int i = 0; i < length; i++ )
		{
			swivel[i] = Shape.Attribute.getAttributeByString( _swivel.getString( i ) ) ;
		}

		return swivel ;
	}
}
