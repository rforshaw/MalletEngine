package com.linxonline.malleteditor.system ;

import com.linxonline.mallet.system.GlobalConfig ;

import com.linxonline.mallet.ui.* ;
import com.linxonline.mallet.event.* ;
import com.linxonline.mallet.input.* ;
import com.linxonline.mallet.audio.* ;
import com.linxonline.mallet.maths.* ;
import com.linxonline.mallet.renderer.* ;
import com.linxonline.mallet.main.game.* ;
import com.linxonline.mallet.animation.* ;
import com.linxonline.mallet.util.sort.* ;
import com.linxonline.mallet.io.filesystem.* ;

import com.linxonline.mallet.entity.* ;
import com.linxonline.mallet.entity.components.* ;

import com.linxonline.mallet.util.settings.Settings ;

public class UIEditorState extends GameState
{
	public UIEditorState( final String _name )
	{
		super( _name ) ;
	}

	@Override
	public void initGame()
	{
		final JUI jui = JUI.create( "base/ui/uieditor/main.jui" ) ;

		final Entity entity = new Entity( "UI" ) ;
		final UIComponent component = new UIComponent() ;
		component.addElement( jui.getParent() ) ;

		entity.addComponent( component ) ;
		addEntity( entity ) ;
	}
}
