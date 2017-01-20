package com.linxonline.mallet.ui ;

import com.linxonline.mallet.renderer.DrawDelegateCallback ;
import com.linxonline.mallet.renderer.DrawDelegate ;
import com.linxonline.mallet.renderer.UpdateType ;

import com.linxonline.mallet.renderer.MalletFont ;

import com.linxonline.mallet.renderer.MalletTexture ;
import com.linxonline.mallet.renderer.DrawAssist ;
import com.linxonline.mallet.renderer.Draw ;
import com.linxonline.mallet.renderer.Shape ;

import com.linxonline.mallet.renderer.ProgramAssist ;
import com.linxonline.mallet.renderer.Program ;

import com.linxonline.mallet.audio.AudioDelegate ;

import com.linxonline.mallet.input.* ;
import com.linxonline.mallet.event.* ;
import com.linxonline.mallet.maths.* ;

/**
	Used to determine if the user has acted within a UI area.

	When the user has clicked, rolled over, or the location 
	is reset to a neutral position an event defined by the 
	developer is sent through the entity's event-system.

	The event can then be picked up by other components such as a 
	render-component to modify the visual element of the entity.
*/
public class UIButton extends UIElement
{
	private final Vector2 mouse = new Vector2() ;
	private State current = State.NEUTRAL ;

	private enum State
	{
		NEUTRAL,
		ROLLOVER,
		CLICKED
	}

	/**
		If the UIButton is being added to a UILayout
		then you don't have to define the position, 
		offset, or length.
	*/
	public UIButton()
	{
		this( new Vector3(), new Vector3(), new Vector3(), null ) ;
	}

	public UIButton( final Vector3 _length )
	{
		this( new Vector3(), new Vector3(), _length, null ) ;
	}

	public UIButton( final Vector3 _offset,
					 final Vector3 _length )
	{
		this( new Vector3(), _offset, _length, null ) ;
	}

	public UIButton( final Vector3 _position,
					 final Vector3 _offset,
					 final Vector3 _length )
	{
		this( _position, _offset, _length, null ) ;
	}

	public UIButton( final Vector3 _position,
					 final Vector3 _offset,
					 final Vector3 _length,
					 final BaseListener _listener )
	{
		super( _position, _offset, _length ) ;
		addListener( _listener ) ;
	}

	@Override
	public InputEvent.Action passInputEvent( final InputEvent _event )
	{
		if( isIntersectInput( _event ) == true )
		{
			return super.passInputEvent( _event ) ;
		}

		return InputEvent.Action.PROPAGATE ;
	}

	public static UIListener constructUIListener( final MalletTexture _sheet,
												  final UIButton.UV _neutral,
												  final UIButton.UV _rollover,
												  final UIButton.UV _clicked )
	{
		return new UIListener( "", null, _sheet, _neutral, _rollover, _clicked ) ;
	}

	public static UIListener constructUIListener( final String _text,
												  final MalletFont _font,
												  final MalletTexture _sheet,
												  final UIButton.UV _neutral,
												  final UIButton.UV _rollover,
												  final UIButton.UV _clicked,
												  final boolean _forceRatio )
	{
		return new UIListener( _text, _font, _sheet, _neutral, _rollover, _clicked, _forceRatio ) ;
	}

	public static class UIListener extends BaseListener
	{
		private final Vector3 aspectRatio = new Vector3() ;		// Visual elements aspect ratio
		private final Vector3 length = new Vector3() ;			// Actual length of the visual element
		private final Vector3 offset = new Vector3() ;			// Offset within the UIElement

		private final StringBuilder text = new StringBuilder() ;
		private MalletFont font ;

		private final MalletTexture sheet ;
		private final UIButton.UV neutral ;
		private final UIButton.UV rollover ;
		private final UIButton.UV clicked ;
		private UIButton.UV active = null ;

		private final boolean retainRatio ;

		private DrawDelegate delegate = null ;
		private Draw draw = null ;
		private Draw drawText = null ;

		private UI.Alignment drawAlignmentX = UI.Alignment.Left ;
		private UI.Alignment drawAlignmentY = UI.Alignment.Left ;

		private UI.Alignment drawTextAlignmentX = UI.Alignment.Centre ;
		private UI.Alignment drawTextAlignmentY = UI.Alignment.Centre ;

		public UIListener( final String _text,
						   final MalletFont _font,
						   final MalletTexture _sheet,
						   final UIButton.UV _neutral,
						   final UIButton.UV _rollover,
						   final UIButton.UV _clicked )
		{
			this( _text, _font, _sheet, _neutral, _rollover, _clicked, false ) ;
		}

		public UIListener( final String _text,
						   final MalletFont _font,
						   final MalletTexture _sheet,
						   final UIButton.UV _neutral,
						   final UIButton.UV _rollover,
						   final UIButton.UV _clicked,
						   final boolean _retainRatio )
		{
			text.append( _text ) ;
			font = _font ;

			sheet = _sheet ;
			neutral = _neutral ;
			rollover = _rollover ;
			clicked = _clicked ;
			active = neutral ;

			retainRatio = _retainRatio ;
		}

		public void setAlignment( final UI.Alignment _x, final UI.Alignment _y )
		{
			drawAlignmentX = ( _x == null ) ? UI.Alignment.Left : _x ;
			drawAlignmentY = ( _y == null ) ? UI.Alignment.Left : _y ;
		}

		public void setTextAlignment( final UI.Alignment _x, final UI.Alignment _y )
		{
			drawTextAlignmentX = ( _x == null ) ? UI.Alignment.Centre : _x ;
			drawTextAlignmentY = ( _y == null ) ? UI.Alignment.Centre : _y ;
		}

		public StringBuilder getText()
		{
			return text ;
		}

		@Override
		public void setParent( final UIElement _parent )
		{
			super.setParent( _parent ) ;
			_parent.addEvent( DrawAssist.constructDrawDelegate( new DrawDelegateCallback()
			{
				public void callback( final DrawDelegate _delegate )
				{
					delegate = _delegate ;
					if( draw != null )
					{
						delegate.addBasicDraw( draw ) ;
					}

					if( drawText != null )
					{
						delegate.addTextDraw( drawText ) ;
					}
				}
			} ) ) ;

			updateLength( _parent.getLength() ) ;
			updateOffset( _parent.getOffset() ) ;

			draw = DrawAssist.createDraw( _parent.getPosition(),
										  offset,
										  new Vector3(),
										  new Vector3( 1, 1, 1 ), _parent.getLayer() ) ;
			DrawAssist.amendUI( draw, true ) ;
			DrawAssist.amendShape( draw, Shape.constructPlane( length, neutral.min, neutral.max ) ) ;

			final Program program = ProgramAssist.createProgram( "SIMPLE_TEXTURE" ) ;
			ProgramAssist.map( program, "inTex0", sheet ) ;

			DrawAssist.attachProgram( draw, program ) ;

			final Vector3 textOffset = new Vector3( _parent.getOffset() ) ;
			textOffset.add( length.x / 2, length.y / 2, 0.0f ) ;

			drawText = DrawAssist.createTextDraw( text,
												  font,
												  _parent.getPosition(),
												  textOffset,
												  new Vector3(),
												  new Vector3( 1, 1, 1 ), _parent.getLayer() + 1 ) ;
			DrawAssist.amendUI( drawText, true ) ;
			DrawAssist.attachProgram( drawText, ProgramAssist.createProgram( "SIMPLE_FONT" ) ) ;
		}

		@Override
		public InputEvent.Action mousePressed( final InputEvent _input )
		{
			if( getParent().isEngaged() == true )
			{
				Shape.updatePlaneUV( DrawAssist.getDrawShape( draw ), clicked.min, clicked.max ) ;
				DrawAssist.forceUpdate( draw ) ;
			}

			return InputEvent.Action.PROPAGATE ;
		}

		@Override
		public InputEvent.Action touchPressed( final InputEvent _input )
		{
			return mousePressed( _input ) ;
		}

		@Override
		public void engage()
		{
			Shape.updatePlaneUV( DrawAssist.getDrawShape( draw ), rollover.min, rollover.max ) ;
			DrawAssist.forceUpdate( draw ) ;
		}

		@Override
		public void disengage()
		{
			Shape.updatePlaneUV( DrawAssist.getDrawShape( draw ), neutral.min, neutral.max ) ;
			DrawAssist.forceUpdate( draw ) ;
		}

		@Override
		public void refresh()
		{
			updateLength( getParent().getLength() ) ;
			updateOffset( getParent().getOffset() ) ;

			Shape.updatePlaneGeometry( DrawAssist.getDrawShape( draw ), length ) ;
			DrawAssist.forceUpdate( draw ) ;

			if( font != null )
			{
				final Vector3 textOffset = DrawAssist.getOffset( drawText ) ;
				textOffset.setXYZ( offset ) ;

				final float x = UI.align( drawTextAlignmentX, font.stringWidth( text ), length.x ) ;
				final float y = UI.align( drawTextAlignmentY, font.getHeight(), length.y ) ;

				textOffset.add( x, y, 0.0f ) ;
				DrawAssist.forceUpdate( drawText ) ;
			}
		}

		private void updateLength( final Vector3 _length )
		{
			if( active == null || retainRatio == false )
			{
				length.setXYZ( _length ) ;
				return ;
			}

			UI.calcSubDimension( aspectRatio, sheet, active ) ;
			UI.fill( UI.Modifier.RetainAspectRatio, length, aspectRatio, _length ) ;
		}

		private void updateOffset( final Vector3 _offset )
		{
			UI.align( drawAlignmentX, drawAlignmentY, offset, length, getParent().getLength() ) ;
			offset.add( _offset ) ;
		}

		@Override
		public void shutdown()
		{
			if( delegate != null )
			{
				delegate.shutdown() ;
			}
		}
	}
}
