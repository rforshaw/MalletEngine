package com.linxonline.mallet.ui ;

import com.linxonline.mallet.input.* ;
import com.linxonline.mallet.event.* ;
import com.linxonline.mallet.maths.* ;

import com.linxonline.mallet.renderer.DrawDelegateCallback ;
import com.linxonline.mallet.renderer.DrawDelegate ;
import com.linxonline.mallet.renderer.UpdateType ;

import com.linxonline.mallet.renderer.MalletTexture ;
import com.linxonline.mallet.renderer.DrawAssist ;
import com.linxonline.mallet.renderer.Draw ;
import com.linxonline.mallet.renderer.Shape ;

/**
	Contains helper functions for the construction of 
	commonly used UI elements and their supporting classes.
*/
public class UIFactory
{
	public static UIButton.Listener constructButtonListener( final MalletTexture _sheet,
															 final UIButton.UV _neutral,
															 final UIButton.UV _rollover,
															 final UIButton.UV _clicked )
	{
		return new UIButton.Listener()
		{
			private DrawDelegate delegate = null ;
			private Draw button = null ;

			@Override
			public void setParent( final UIElement _parent )
			{
				super.setParent( _parent ) ;
				_parent.addEvent( DrawAssist.constructDrawDelegate( new DrawDelegateCallback()
				{
					public void callback( DrawDelegate _delegate )
					{
						delegate = _delegate ;
						delegate.addBasicDraw( button ) ;
					}
				} ) ) ;

				button = DrawAssist.createDraw( _parent.getPosition(),
												_parent.getOffset(),
												new Vector3(),
												new Vector3( 1, 1, 1 ), 10 ) ;
				DrawAssist.amendUI( button, true ) ;
				DrawAssist.amendTexture( button, _sheet ) ;
				DrawAssist.amendShape( button, Shape.constructPlane( _parent.getLength(), _neutral.min, _neutral.max ) ) ;
				DrawAssist.attachProgram( button, "SIMPLE_TEXTURE" ) ;
			}

			@Override
			public void clicked( final InputEvent _event )
			{
				Shape.updatePlaneUV( DrawAssist.getDrawShape( button ), _clicked.min, _clicked.max ) ;
				DrawAssist.forceUpdate( button ) ;
			}

			@Override
			public void rollover( final InputEvent _event )
			{
				Shape.updatePlaneUV( DrawAssist.getDrawShape( button ), _rollover.min, _rollover.max ) ;
				DrawAssist.forceUpdate( button ) ;
			}

			@Override
			public void neutral( final InputEvent _event )
			{
				Shape.updatePlaneUV( DrawAssist.getDrawShape( button ), _neutral.min, _neutral.max ) ;
				DrawAssist.forceUpdate( button ) ;
			}

			@Override
			public void refresh()
			{
				Shape.updatePlaneGeometry( DrawAssist.getDrawShape( button ), getParent().getLength() ) ;
				DrawAssist.forceUpdate( button ) ;
			}

			@Override
			public void shutdown()
			{
				if( delegate != null )
				{
					delegate.shutdown() ;
				}
			}
		} ;
	}
}