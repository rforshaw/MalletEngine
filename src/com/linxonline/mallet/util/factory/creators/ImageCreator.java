package com.linxonline.mallet.util.factory.creators ;

import com.linxonline.mallet.renderer.MalletTexture ;
import com.linxonline.mallet.renderer.Interpolation ;
import com.linxonline.mallet.renderer.UpdateType ;
import com.linxonline.mallet.renderer.DrawAssist ;
import com.linxonline.mallet.renderer.Draw ;
import com.linxonline.mallet.renderer.Shape ;

import com.linxonline.mallet.renderer.ProgramAssist ;
import com.linxonline.mallet.renderer.Program ;

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

		final Vector2 dim = _image.<Vector2>getObject( "DIM", null ) ;
		final Shape plane = Shape.constructPlane( new Vector3( dim.x, dim.y, 0.0f ), new Vector2(), new Vector2( 1, 1 ) ) ;

		final Draw draw = DrawAssist.createDraw( entity.position,
												 _image.<Vector3>getObject( "OFFSET", null ),
												 new Vector3(),
												 new Vector3( 1, 1, 1 ),
												 _image.getInteger( "LAYER", 0 ) ) ;

		DrawAssist.amendShape( draw, plane ) ;

		final Program program = ProgramAssist.createProgram( "SIMPLE_TEXTURE" ) ;
		ProgramAssist.map( program, "inTex0", new MalletTexture( _image.getString( "IMAGE", "" ) ) ) ;

		DrawAssist.attachProgram( draw, program ) ;

		//DrawAssist.amendTexture( draw, new MalletTexture( _image.getString( "IMAGE", "" ) ) ) ;
		//DrawAssist.attachProgram( draw, ProgramAssist.createProgram( "SIMPLE_TEXTURE" ) ) ;
		DrawAssist.amendUpdateType( draw, UpdateType.ON_DEMAND ) ;
		DrawAssist.amendInterpolation( draw, Interpolation.LINEAR ) ;

		final RenderComponent render = new RenderComponent() ;
		render.addBasicDraw( draw ) ;
		
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
