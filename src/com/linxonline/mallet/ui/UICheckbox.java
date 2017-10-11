package com.linxonline.mallet.ui ;

import java.util.List ;

import com.linxonline.mallet.renderer.* ;
import com.linxonline.mallet.input.* ;
import com.linxonline.mallet.event.* ;
import com.linxonline.mallet.maths.* ;

public class UICheckbox extends UIElement
{
	private boolean checked = false ;

	private final Connect.Signal checkChanged = new Connect.Signal() ;

	/**
		If the UICheckbox is being added to a UILayout
		then you don't have to define the position, 
		offset, or length.
	*/
	public UICheckbox()
	{
		this( new Vector3(), new Vector3(), new Vector3(), null ) ;
	}

	public UICheckbox( final Vector3 _length )
	{
		this( new Vector3(), new Vector3(), _length, null ) ;
	}

	public UICheckbox( final Vector3 _offset,
					   final Vector3 _length )
	{
		this( new Vector3(), _offset, _length, null ) ;
	}

	public UICheckbox( final Vector3 _position,
					   final Vector3 _offset,
					   final Vector3 _length )
	{
		this( _position, _offset, _length, null ) ;
	}

	public UICheckbox( final Vector3 _position,
					   final Vector3 _offset,
					   final Vector3 _length,
					   final ABase<UIButton> _listener )
	{
		super( _position, _offset, _length ) ;
		addListener( _listener ) ;
	}

	@Override
	public InputEvent.Action passInputEvent( final InputEvent _event )
	{
		if( isIntersectInput( _event ) == true )
		{
			processInputEvent( _event ) ;
			switch( _event.getInputType() )
			{
				case MOUSE_MOVED :
				case TOUCH_MOVE  : return InputEvent.Action.PROPAGATE ;
				default          : return InputEvent.Action.CONSUME ;
			}
		}

		return InputEvent.Action.PROPAGATE ;
	}

	public void setChecked( final boolean _checked )
	{
		if( checked != _checked )
		{
			checked = _checked ;
			makeDirty() ;
			UIElement.signal( this, checkChanged() ) ;
		}
	}

	public boolean isChecked()
	{
		return checked ;
	}

	public Connect.Signal checkChanged()
	{
		return checkChanged ;
	}

	public static class GUITick extends UIFactory.GUIDraw<UICheckbox>
	{
		private boolean checked = false ;

		public GUITick( final MalletTexture _sheet, final UIElement.UV _uv )
		{
			super( _sheet, _uv ) ;
		}

		@Override
		public void setParent( final UICheckbox _parent )
		{
			super.setParent( _parent ) ;
			checked = _parent.isChecked() ;
		}

		/**
			Called when listener receives a valid DrawDelegate
			and when the parent UIElement is flagged as visible.
		*/
		@Override
		public void addDraws( final DrawDelegate<World, Draw> _delegate, final World _world )
		{
			final UICheckbox parent = getParent() ;
			if( parent.isChecked() )
			{
				super.addDraws( _delegate, _world ) ;
			}
		}

		@Override
		public void refresh()
		{
			super.refresh() ;
			final UICheckbox parent = getParent() ;
			if( checked != parent.isChecked() )
			{
				checked = !checked ;
				final DrawDelegate<World, Draw> delegate = getDrawDelegate() ;
				if( delegate != null )
				{
					if( parent.isChecked() == true )
					{
						DrawAssist.forceUpdate( getDraw() ) ;
						delegate.addBasicDraw( getDraw(), getWorld() ) ;
					}
					else
					{
						delegate.removeDraw( getDraw() ) ;
					}
				}
			}
		}

		@Override
		public InputEvent.Action mouseReleased( final InputEvent _input )
		{
			final UICheckbox parent = getParent() ;
			if( parent.isEngaged() == true )
			{
				parent.setChecked( !parent.isChecked() ) ;
				parent.makeDirty() ;
			}

			return InputEvent.Action.PROPAGATE ;
		}

		@Override
		public InputEvent.Action touchReleased( final InputEvent _input )
		{
			return mouseReleased( _input ) ;
		}
	}
}
