package com.linxonline.mallet.ui ;

import com.linxonline.mallet.renderer.WorldAssist ;
import com.linxonline.mallet.renderer.DrawDelegate ;
import com.linxonline.mallet.renderer.Draw ;
import com.linxonline.mallet.renderer.World ;
import com.linxonline.mallet.audio.AudioDelegate ;

import com.linxonline.mallet.event.* ;
import com.linxonline.mallet.input.* ;
import com.linxonline.mallet.maths.* ;

public class UIList extends UILayout
{
	public UIList( final Type _type, final float _length )
	{
		super( _type ) ;
		switch( _type )
		{
			case HORIZONTAL :
			{
				setMaximumLength( 0.0f, _length, 0.0f ) ;
				break ;
			}
			case VERTICAL :
			default       :
			{
				setMaximumLength( _length, 0.0f, 0.0f ) ;
				break ;
			}
		}

		addListener( new StencilListener() ) ;
	}

	private static class StencilListener extends UIListener<UIList>
	{
		private Draw draw = null ;

		public void constructDraws()
		{
			final UIList parent = getParent() ;
			final int layer = parent.getLayer() ;

			//draw = DrawAssist.createClipDraw(  )
		}

		public void addDraws( final DrawDelegate<World, Draw> _delegate )
		{
		
		}

		public void removeDraws( final DrawDelegate<World, Draw> _delegate )
		{
		
		}
	}
}
