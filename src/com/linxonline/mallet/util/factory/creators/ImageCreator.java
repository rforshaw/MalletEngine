package com.linxonline.mallet.util.factory.creators ;

import com.linxonline.mallet.renderer.DrawFactory ;
import com.linxonline.mallet.util.settings.Settings ;
import com.linxonline.mallet.util.factory.Creator ;
import com.linxonline.mallet.entity.Entity ;
import com.linxonline.mallet.entity.components.* ;
import com.linxonline.mallet.maths.* ;

public class ImageCreator extends Creator<Entity, Settings>
{
	private static final Vector2 OFFSET_ZERO = new Vector2() ;

	public ImageCreator()
	{
		setType( "IMAGE" ) ;
	}

	@Override
	public Entity create( final Settings _image )
	{
		parseImage( _image ) ;

		final String name = _image.getString( "NAME", "IMAGE" ) ;
		final Vector2 position = _image.getObject( "POS", OFFSET_ZERO ) ;

		final Entity entity = new Entity( name ) ;
		entity.position = new Vector3( position ) ;

		final RenderComponent render = new RenderComponent() ;
		render.add( DrawFactory.createTexture( _image.getString( "IMAGE", "" ),
												entity.position,
											   _image.<Vector2>getObject( "OFFSET", null ),
											   _image.<Vector2>getObject( "DIM", null ),
											   _image.<Vector2>getObject( "FILL", null ), 
											   new Vector2(),		// Clip View 
											   new Vector2( 1, 1 ),		// Clip Offset
											   _image.getInteger( "LAYER", 0 ), render ) ) ;

		entity.addComponent( render ) ;
		return entity ;
	}

	/**
		Reuse the same Settings object, reduce the amount of
		temporary objects.
	**/
	private void parseImage( final Settings _image )
	{
		_image.addInteger( "LAYER", Integer.parseInt( _image.getString( "LAYER", "0" ) ) ) ;
		
		final String fill = _image.getString( "FILL", null ) ;
		if( fill != null )
		{
			_image.addObject( "FILL", Vector2.parseVector2( fill ) ) ;
		}

		CommonTypes.setDimension( _image, _image ) ;
		CommonTypes.setPercentagePosition( _image, _image ) ;
		CommonTypes.setOffset( _image, _image ) ;
	}
}
