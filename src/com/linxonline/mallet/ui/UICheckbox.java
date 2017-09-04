package com.linxonline.mallet.ui ;

import java.util.List ;

import com.linxonline.mallet.renderer.* ;
import com.linxonline.mallet.input.* ;
import com.linxonline.mallet.event.* ;
import com.linxonline.mallet.maths.* ;

public class UICheckbox extends UIElement
{
	private boolean checked = false ;

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
		checked = _checked ;
	}

	public boolean isChecked()
	{
		return checked ;
	}

	public static GUIBasic createGUIBasic( final MalletTexture _sheet,
											final UIButton.UV _neutralBox,
											final UIButton.UV _rolloverBox,
											final UIButton.UV _tick )
	{
		return new GUIBasic( _sheet, _neutralBox, _rolloverBox, _tick ) ;
	}

	public static class GUIBasic extends UIFactory.GUIBasic<UICheckbox>
	{
		private final UICheckbox.UV neutralBox ;
		private final UICheckbox.UV rolloverBox ;
		private final UICheckbox.UV tick ;

		private Draw drawTick = null ;

		public GUIBasic( final MalletTexture _sheet,
						 final UICheckbox.UV _neutralBox,
						 final UICheckbox.UV _rolloverBox,
						 final UICheckbox.UV _tick )
		{
			super( _sheet, _neutralBox ) ;
			neutralBox  = _neutralBox ;
			rolloverBox = _rolloverBox ;
			tick        = _tick ;
		}

		@Override
		public void constructDraws()
		{
			super.constructDraws() ;

			final MalletTexture sheet = getTexture() ;
			if( sheet != null && tick != null )
			{
				final UICheckbox parent = getParent() ;
				drawTick = DrawAssist.createDraw( parent.getPosition(),
												  getOffset(),
												  new Vector3(),
												  new Vector3( 1, 1, 1 ), parent.getLayer() + 1 ) ;
				DrawAssist.amendUI( drawTick, true ) ;
				DrawAssist.amendShape( drawTick, Shape.constructPlane( getLength(), tick.min, tick.max ) ) ;

				final Program program = ProgramAssist.create( "SIMPLE_TEXTURE" ) ;
				ProgramAssist.map( program, "inTex0", sheet ) ;

				DrawAssist.attachProgram( drawTick, program ) ;
			}
		}

		/**
			Called when listener receives a valid DrawDelegate
			and when the parent UIElement is flagged as visible.
		*/
		@Override
		public void addDraws( final DrawDelegate<World, Draw> _delegate, final World _world )
		{
			super.addDraws( _delegate, _world ) ;

			final UICheckbox parent = getParent() ;
			if( drawTick != null && parent.isChecked() )
			{
				_delegate.addBasicDraw( drawTick, _world ) ;
			}
		}

		/**
			Only called if there is a valid DrawDelegate and 
			when the parent UIElement is flagged as invisible.
		*/
		@Override
		public void removeDraws( final DrawDelegate<World, Draw> _delegate )
		{
			super.removeDraws( _delegate ) ;
			_delegate.removeDraw( drawTick ) ;
		}

		@Override
		public void refresh()
		{
			super.refresh() ;
			if( drawTick != null )
			{
				Shape.updatePlaneGeometry( DrawAssist.getDrawShape( drawTick ), getLength() ) ;
				DrawAssist.amendOrder( drawTick, getParent().getLayer() + 1 ) ;
				DrawAssist.forceUpdate( drawTick ) ;
			}
		}

		@Override
		public InputEvent.Action mouseReleased( final InputEvent _input )
		{
			final UICheckbox parent = getParent() ;

			if( parent.isEngaged() == true )
			{
				parent.setChecked( !parent.isChecked() ) ;

				final DrawDelegate<World, Draw> delegate = getDrawDelegate() ;
				if( delegate != null )
				{
					if( parent.isChecked() == true )
					{
						DrawAssist.forceUpdate( drawTick ) ;
						delegate.addBasicDraw( drawTick, getWorld() ) ;
					}
					else
					{
						delegate.removeDraw( drawTick ) ;
					}
				}
			}

			return InputEvent.Action.PROPAGATE ;
		}

		@Override
		public InputEvent.Action touchReleased( final InputEvent _input )
		{
			return mouseReleased( _input ) ;
		}

		@Override
		public void engage()
		{
			Shape.updatePlaneUV( DrawAssist.getDrawShape( draw ), rolloverBox.min, rolloverBox.max ) ;
			DrawAssist.forceUpdate( draw ) ;
		}

		@Override
		public void disengage()
		{
			Shape.updatePlaneUV( DrawAssist.getDrawShape( draw ), neutralBox.min, neutralBox.max ) ;
			DrawAssist.forceUpdate( draw ) ;
		}
	}
}
