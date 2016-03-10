package com.linxonline.mallet.entity.components ;

import com.linxonline.mallet.renderer.DrawFactory ;
import com.linxonline.mallet.util.settings.Settings ;
import com.linxonline.mallet.event.EventController ;
import com.linxonline.mallet.event.EventProcessor ;
import com.linxonline.mallet.event.Event ;

/**
	UIRenderComponent is used in conjunction with UIComponent.
	Manages all Draw events used by the UI system.
*/
public class UIRenderComponent extends RenderComponent
{
	public UIRenderComponent()
	{
		this( "UIRENDER" ) ;
	}

	public UIRenderComponent( final String _name )
	{
		super( _name ) ;
		initEventProcessor() ;
	}

	public UIRenderComponent( final String _name, final String _group )
	{
		super( _name, _group ) ;
		initEventProcessor() ;
	}

	private void initEventProcessor()
	{
		final EventController controller = getComponentEventController() ;
		controller.addEventProcessor( new EventProcessor<Event<Settings>>( "ADD_UI_DRAW", "ADD_UI_DRAW" )
		{
			@Override
			public void processEvent( final Event<Event<Settings>> _event )
			{
				final Event<Settings> draw = _event.getVariable() ;
				DrawFactory.insertIDCallback( draw, UIRenderComponent.this ) ;
				add( _event.getVariable() ) ;
			}
		} ) ;

		controller.addEventProcessor( new EventProcessor<Event<Settings>>( "REMOVE_UI_DRAW", "REMOVE_UI_DRAW" )
		{
			@Override
			public void processEvent( final Event<Event<Settings>> _event )
			{
				remove( _event.getVariable() ) ;
			}
		} ) ;
	}
}