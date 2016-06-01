package com.linxonline.mallet.ui ;

import java.util.ArrayList ;

import com.linxonline.mallet.renderer.DrawDelegate ;
import com.linxonline.mallet.audio.AudioDelegate ;

import com.linxonline.mallet.event.* ;
import com.linxonline.mallet.input.* ;
import com.linxonline.mallet.maths.* ;

public class UIMenu extends UILayout
{
	public UIMenu( final Type _type, final float _length )
	{
		super( _type ) ;
		switch( _type )
		{
			case HORIZONTAL : setMaximumLength( 0.0f, _length, 0.0f ) ; break ;
			case VERTICAL   : setMaximumLength( _length, 0.0f, 0.0f ) ; break ;
		}
	}

	public static class Item extends UIButton
	{
		public Item()
		{
			super() ;
		}
	}
}