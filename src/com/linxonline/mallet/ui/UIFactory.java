package com.linxonline.mallet.ui ;

import com.linxonline.mallet.input.* ;
import com.linxonline.mallet.event.* ;
import com.linxonline.mallet.maths.* ;

import com.linxonline.mallet.renderer.MalletTexture ;
import com.linxonline.mallet.renderer.DrawDelegate ;
import com.linxonline.mallet.renderer.DrawAssist ;
import com.linxonline.mallet.renderer.Draw ;
import com.linxonline.mallet.renderer.Shape ;

/**
	Contains helper functions for the construction of 
	commonly used UI elements and their supporting classes.
*/
public class UIFactory
{
	public static UIButton.Listener constructButtonListener( final MalletTexture _neutral,
															 final MalletTexture _rollover,
															 final MalletTexture _clicked )
	{
		return new UIButton.Listener()
		{
			private Draw button = null ;

			@Override
			public void init( final DrawDelegate _delegate )
			{
				final UIElement parent = getParent() ;
				button = DrawAssist.createDraw( parent.getPosition(),
												parent.getOffset(),
												new Vector3(),
												new Vector3( 1, 1, 1 ), 10 ) ;
				DrawAssist.amendUI( button, true ) ;
				DrawAssist.amendTexture( button, _neutral ) ;
				DrawAssist.amendShape( button, Shape.constructPlane( parent.getLength(), new Vector2(), new Vector2( 1, 1 ) ) ) ;
				DrawAssist.attachProgram( button, "SIMPLE_TEXTURE" ) ;

				_delegate.addBasicDraw( button ) ;
			}

			@Override
			public void clicked( final InputEvent _event )
			{
				if( button != null )
				{
					DrawAssist.clearTextures( button ) ;
					DrawAssist.amendTexture( button, _clicked ) ;
					DrawAssist.forceUpdate( button ) ;
				}
			}

			@Override
			public void rollover( final InputEvent _event )
			{
				if( button != null )
				{
					DrawAssist.clearTextures( button ) ;
					DrawAssist.amendTexture( button, _rollover ) ;
					DrawAssist.forceUpdate( button ) ;
				}
			}

			@Override
			public void neutral( final InputEvent _event )
			{
				if( button != null )
				{
					DrawAssist.clearTextures( button ) ;
					DrawAssist.amendTexture( button, _neutral ) ;
					DrawAssist.forceUpdate( button ) ;
				}
			}
		} ;
	}
}