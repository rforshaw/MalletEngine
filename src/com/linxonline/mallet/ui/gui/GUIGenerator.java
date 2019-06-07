package com.linxonline.mallet.ui.gui ;

import java.util.Map ;

import com.linxonline.mallet.ui.UIElement ;
import com.linxonline.mallet.ui.UITextField ;
import com.linxonline.mallet.ui.UICheckbox ;

import com.linxonline.mallet.util.Logger ;
import com.linxonline.mallet.util.MalletMap ;

public class GUIGenerator
{
	private final static Map<String, Generator> creators = MalletMap.<String, Generator>newMap() ;
	static
	{
		creators.put( "UIELEMENT_GUIDRAW", new Generator<GUIDraw.Meta>()
		{
			@Override
			public GUIComponent create( final GUIDraw.Meta _meta, final UIElement _parent )
			{
				return new GUIDraw( _meta, _parent ) ;
			}
		} ) ;

		creators.put( "UIELEMENT_GUIDRAWEDGE", new Generator<GUIDrawEdge.Meta>()
		{
			@Override
			public GUIComponent create( final GUIDrawEdge.Meta _meta, final UIElement _parent )
			{
				return new GUIDrawEdge( _meta, _parent ) ;
			}
		} ) ;

		creators.put( "UITEXTFIELD_GUIEDITTEXT", new Generator<GUIEditText.Meta>()
		{
			@Override
			public GUIComponent create( final GUIEditText.Meta _meta, final UIElement _parent )
			{
				return new GUIEditText( _meta, ( UITextField )_parent ) ;
			}
		} ) ;

		creators.put( "UIELEMENT_GUIPANELDRAW", new Generator<GUIPanelDraw.Meta>()
		{
			@Override
			public GUIComponent create( final GUIPanelDraw.Meta _meta, final UIElement _parent )
			{
				return new GUIPanelDraw( _meta, _parent ) ;
			}
		} ) ;

		creators.put( "UIELEMENT_GUIPANELEDGE", new Generator<GUIPanelEdge.Meta>()
		{
			@Override
			public GUIComponent create( final GUIPanelEdge.Meta _meta, final UIElement _parent )
			{
				return new GUIPanelEdge( _meta, _parent ) ;
			}
		} ) ;

		creators.put( "UILIST_GUISCROLLBAR", new Generator<GUIScrollbar.Meta>()
		{
			@Override
			public GUIComponent create( final GUIScrollbar.Meta _meta, final UIElement _parent )
			{
				return null ;
			}
		} ) ;

		creators.put( "UIELEMENT_GUITEXT", new Generator<GUIText.Meta>()
		{
			@Override
			public GUIComponent create( final GUIText.Meta _meta, final UIElement _parent )
			{
				return new GUIText( _meta, _parent ) ;
			}
		} ) ;

		creators.put( "UICHECKBOX_GUITICK", new Generator<GUITick.Meta>()
		{
			@Override
			public GUIComponent create( final GUITick.Meta _meta, final UIElement _parent )
			{
				return new GUITick( _meta, ( UICheckbox )_parent ) ;
			}
		} ) ;
	}

	public static <M extends UIElement.MetaComponent> void addGenerator( final String _id, final Generator<M> _generator )
	{
		creators.put( _id, _generator ) ;
	}

	public static <E extends UIElement.Component> E create( final UIElement.MetaComponent _meta, final UIElement _parent )
	{
		final String type = _meta.getType() ;
		final Generator<UIElement.MetaComponent> generator = creators.get( type ) ;
		if( generator == null )
		{
			Logger.println( type + " GUIGenerator doesn't exist.", Logger.Verbosity.MAJOR ) ;
			return null ;
		}

		return ( E )generator.create( _meta, _parent ) ;
	}

	public interface Generator<M extends UIElement.MetaComponent>
	{
		public GUIComponent create( final M _meta, final UIElement _parent ) ;
	}
}
