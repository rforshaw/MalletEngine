package com.linxonline.mallet.ui.gui ;

import java.util.Map ;

import com.linxonline.mallet.ui.IBase ;

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
			public GUIBase create( final GUIDraw.Meta _meta )
			{
				return new GUIDraw( _meta ) ;
			}
		} ) ;

		creators.put( "UIELEMENT_GUIDRAWEDGE", new Generator<GUIDrawEdge.Meta>()
		{
			@Override
			public GUIBase create( final GUIDrawEdge.Meta _meta )
			{
				return new GUIDrawEdge( _meta ) ;
			}
		} ) ;

		creators.put( "UITEXTFIELD_GUIEDITTEXT", new Generator<GUIEditText.Meta>()
		{
			@Override
			public GUIBase create( final GUIEditText.Meta _meta )
			{
				return new GUIEditText( _meta ) ;
			}
		} ) ;

		creators.put( "UIELEMENT_GUIPANELDRAW", new Generator<GUIPanelDraw.Meta>()
		{
			@Override
			public GUIBase create( final GUIPanelDraw.Meta _meta )
			{
				return new GUIPanelDraw( _meta ) ;
			}
		} ) ;

		creators.put( "UIELEMENT_GUIPANELEDGE", new Generator<GUIPanelEdge.Meta>()
		{
			@Override
			public GUIBase create( final GUIPanelEdge.Meta _meta )
			{
				return new GUIPanelEdge( _meta ) ;
			}
		} ) ;

		creators.put( "UILIST_GUISCROLLBAR", new Generator<GUIScrollbar.Meta>()
		{
			@Override
			public GUIBase create( final GUIScrollbar.Meta _meta )
			{
				return null ;
			}
		} ) ;

		creators.put( "UIELEMENT_GUITEXT", new Generator<GUIText.Meta>()
		{
			@Override
			public GUIBase create( final GUIText.Meta _meta )
			{
				return new GUIText( _meta ) ;
			}
		} ) ;

		creators.put( "UICHECKBOX_GUITICK", new Generator<GUITick.Meta>()
		{
			@Override
			public GUIBase create( final GUITick.Meta _meta )
			{
				return new GUITick( _meta ) ;
			}
		} ) ;
	}

	public static void addGenerator( final String _id, final Generator _generator )
	{
		creators.put( _id, _generator ) ;
	}

	public static <E extends IBase> E create( final IBase.Meta _meta )
	{
		final String type = _meta.getType() ;
		final Generator generator = creators.get( type ) ;
		if( generator == null )
		{
			Logger.println( type + " GUIGenerator doesn't exist.", Logger.Verbosity.MAJOR ) ;
			return null ;
		}

		return ( E )generator.create( _meta ) ;
	}

	public interface Generator<M extends IBase.Meta>
	{
		public GUIBase create( final M _meta ) ;
	}
}
