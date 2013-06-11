package com.linxonline.mallet.entity ;

import java.util.HashMap ;

import com.linxonline.mallet.resources.texture.Sprite ;
import com.linxonline.mallet.renderer.* ;

/*==============================================================*/
// SpriteComponent - Enables the management of multiple			// 
// animations and their timing.					 			    //
/*==============================================================*/

public class SpriteComponent extends RenderComponent
{
	public Sprite sprite = null ;
	private HashMap<String, Sprite> sprites = new HashMap<String, Sprite>() ;
	private float elapsedTime = 0.0f ;
	private float frameDelta = 0.0f ;
	private int frame = 0 ;
	private int length = 0 ;

	public SpriteComponent()
	{
		super( "SPRITE", "RENDERCOMPONENT" ) ;
	}

	public final void addSprite( final Sprite _sprite, final String _name )
	{
		if( _sprite == null )
		{
			System.out.println( "Failed to add Sprite: " + _name ) ;
			return ;
		}

		sprites.put( _name, _sprite ) ;
	}

	public final void setSpriteByName( final String _name )
	{
		if( sprites.containsKey( _name ) == true )
		{
			setSprite( sprites.get( _name ) ) ;
		}
	}

	protected final void setSprite( final Sprite _sprite )
	{
		frame = 0 ;
		sprite = _sprite ;
		frameDelta = 1.0f / sprite.framerate ;

		getDrawAt( 0 ).addString( "FILE", sprite.textures.get( frame ) ) ;
		getDrawAt( 0 ).addObject( "TEXTURE", null ) ;

		length = sprite.textures.size() ;
	}

	@Override
	public void update( final float _dt )
	{
		elapsedTime += _dt ;
		if( elapsedTime >= frameDelta )
		{
			getDrawAt( 0 ).addString( "FILE", sprite.textures.get( frame ) ) ;
			getDrawAt( 0 ).addObject( "TEXTURE", null ) ;
			elapsedTime = 0.0f ;
			frame = ++frame % length ;	// Increment Frame, reset to 0 if reachs length.
		}
	}
}