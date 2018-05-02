package com.linxonline.mallet.ui ;

import java.util.Map ;
import java.util.List ;

import com.linxonline.mallet.ui.gui.GUIGenerator ;

import com.linxonline.mallet.util.Logger ;
import com.linxonline.mallet.util.MalletMap ;
import com.linxonline.mallet.util.MalletList ;

/**
	Using the passed in Meta generate the associated UI element.

	The goal of this is to ensure consistency between different 
	file formats and what gets displayed to the user. A file 
	format will populate the intended elements meta object and 
	can then call the UIGenerator to construct the actual element.
*/
public class UIGenerator
{
	private final static Map<String, Generator> creators = MalletMap.<String, Generator>newMap() ;
	static
	{
		creators.put( "UIELEMENT", new Generator<UIElement, UIElement.Meta>()
		{
			@Override
			public UIElement create( final UIElement.Meta _meta )
			{
				return addListeners( new UIElement(), _meta ) ;
			}

			@Override
			public UIElement apply( final UIElement _element, final UIElement.Meta _meta )
			{
				return UIElement.applyMeta( _meta, _element ) ;
			}
		} ) ;

		creators.put( "UILAYOUT", new Generator<UILayout, UILayout.Meta>()
		{
			@Override
			public UILayout create( final UILayout.Meta _meta )
			{
				return addListeners( new UILayout( _meta.getType() ), _meta ) ;
			}

			@Override
			public UILayout apply( final UILayout _element, final UILayout.Meta _meta )
			{
				return UILayout.applyMeta( _meta, _element ) ;
			}
		} ) ;

		creators.put( "UIWINDOW_LAYOUT", new Generator<UILayout, UILayout.Meta>()
		{
			@Override
			public UILayout create( final UILayout.Meta _meta )
			{
				return addListeners( UIFactory.constructWindowLayout( _meta.getType() ), _meta ) ;
			}

			@Override
			public UILayout apply( final UILayout _element, final UILayout.Meta _meta )
			{
				// The UIWINDOW_LAYOUT does not apply any UILayout or UIElement meta 
				// as it fills the entire window.
				return _element ;
			}
		} ) ;

		creators.put( "UITEXTFIELD", new Generator<UITextField, UITextField.Meta>()
		{
			@Override
			public UITextField create( final UITextField.Meta _meta )
			{
				return addListeners( new UITextField(), _meta ) ;
			}

			@Override
			public UITextField apply( final UITextField _element, final UITextField.Meta _meta )
			{
				return UITextField.applyMeta( _meta, _element ) ;
			}
		} ) ;

		creators.put( "UIBUTTON", new Generator<UIButton, UIButton.Meta>()
		{
			@Override
			public UIButton create( final UIButton.Meta _meta )
			{
				return addListeners( new UIButton(), _meta ) ;
			}

			@Override
			public UIButton apply( final UIButton _element, final UIButton.Meta _meta )
			{
				return UIButton.applyMeta( _meta, _element ) ;
			}
		} ) ;

		creators.put( "UIMENU", new Generator<UIMenu, UIMenu.Meta>()
		{
			@Override
			public UIMenu create( final UIMenu.Meta _meta )
			{
				return addListeners( new UIMenu( _meta.getType(), _meta.getThickness() ), _meta ) ;
			}

			@Override
			public UIMenu apply( final UIMenu _element, final UIMenu.Meta _meta )
			{
				return UIMenu.applyMeta( _meta, _element ) ;
			}
		} ) ;
		
		creators.put( "UISPACER", new Generator<UISpacer, UISpacer.Meta>()
		{
			@Override
			public UISpacer create( final UISpacer.Meta _meta )
			{
				return addListeners( new UISpacer( _meta.getAxis() ), _meta ) ;
			}

			@Override
			public UISpacer apply( final UISpacer _element, final UISpacer.Meta _meta )
			{
				return UISpacer.applyMeta( _meta, _element ) ;
			}
		} ) ;

		creators.put( "UICHECKBOX", new Generator<UICheckbox, UICheckbox.Meta>()
		{
			@Override
			public UICheckbox create( final UICheckbox.Meta _meta )
			{
				return addListeners( new UICheckbox(), _meta ) ;
			}

			@Override
			public UICheckbox apply( final UICheckbox _element, final UICheckbox.Meta _meta )
			{
				return UICheckbox.applyMeta( _meta, _element ) ;
			}
		} ) ;

		creators.put( "UILIST", new Generator<UIList, UIList.Meta>()
		{
			@Override
			public UIList create( final UIList.Meta _meta )
			{
				return addListeners( new UIList( _meta.getType() ), _meta ) ;
			}

			@Override
			public UIList apply( final UIList _element, final UIList.Meta _meta )
			{
				return UIList.applyMeta( _meta, _element ) ;
			}
		} ) ;

		creators.put( "UIABSTRACTVIEW", new Generator<UIAbstractView, UIAbstractView.Meta>()
		{
			@Override
			public UIAbstractView create( final UIAbstractView.Meta _meta )
			{
				return addListeners( new UIAbstractView(), _meta ) ;
			}

			@Override
			public UIAbstractView apply( final UIAbstractView _element, final UIAbstractView.Meta _meta )
			{
				return UIAbstractView.applyMeta( _meta, _element ) ;
			}
		} ) ;
	}

	public static <E extends UIElement, M extends E.Meta> E addListeners( final E _element, final M _meta )
	{
		final List<UIElement.MetaListener> listeners = _meta.getListeners( MalletList.<UIElement.MetaListener>newList() ) ;
		for( final UIElement.MetaListener meta : listeners )
		{
			_element.addListener( GUIGenerator.create( meta, _element ) ) ;
		}
		return _element ;
	}

	public static void addGenerator( final String _id, final Generator _generator )
	{
		creators.put( _id, _generator ) ;
	}

	public static <E extends UIElement> E create( final E.Meta _meta )
	{
		final String type = _meta.getElementType() ;
		final Generator<E, E.Meta> generator = creators.get( type ) ;
		if( generator == null )
		{
			Logger.println( type + " UIGenerator doesn't exist.", Logger.Verbosity.MAJOR ) ;
			return null ;
		}

		return apply( generator.create( _meta ), _meta, generator ) ;
	}

	public static <E extends UIElement, M extends E.Meta> E apply( final E _element, final M _meta )
	{
		final String type = _meta.getElementType() ;
		final Generator<E, E.Meta> generator = creators.get( type ) ;
		if( generator == null )
		{
			Logger.println( type + " UIGenerator doesn't exist.", Logger.Verbosity.MAJOR ) ;
			return null ;
		}

		return apply( _element, _meta, generator ) ;
	}

	private static <E extends UIElement, M extends E.Meta> E apply( final E _element, final M _meta, final Generator<E, M> _generator )
	{
		return _generator.apply( _element, _meta ) ;
	}

	public interface Generator<E extends UIElement, M extends E.Meta>
	{
		/**
			Create a base element using the meta information passed in.
		*/
		public E create( final M _meta ) ;

		/**
			Apply the meta information from _meta to _element.
		*/
		public E apply( final E _element, final M _meta ) ;
	}
}
