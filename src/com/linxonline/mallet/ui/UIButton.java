package com.linxonline.mallet.ui ;

import java.util.ArrayList ;

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
												  final UIButton.UV _clicked )
	{
		return new UIListener( _text, _font, _sheet, _neutral, _rollover, _clicked ) ;
	}

	public static class UV
	{
		public final Vector2 min ;
		public final Vector2 max ;

		public UV( final Vector2 _min, final Vector2 _max )
		{
			min = _min ;
			max = _max ;
		}
	}

	public static class UIListener extends BaseListener
	{
		private final StringBuilder text = new StringBuilder() ;
		private MalletFont font ;

		private final MalletTexture sheet ;
		private final UIButton.UV neutral ;
		private final UIButton.UV rollover ;
		private final UIButton.UV clicked ;

		private DrawDelegate delegate = null ;
		private Draw draw = null ;
		private Draw drawText = null ;

		public UIListener( final String _text,
						   final MalletFont _font,
						   final MalletTexture _sheet,
						   final UIButton.UV _neutral,
						   final UIButton.UV _rollover,
						   final UIButton.UV _clicked )
		{
			text.append( _text ) ;
			font = _font ;

			sheet = _sheet ;
			neutral = _neutral ;
			rollover = _rollover ;
			clicked = _clicked ;
		}

		@Override
		public void setParent( final UIElement _parent )
		{
			super.setParent( _parent ) ;
			_parent.addEvent( DrawAssist.constructDrawDelegate( new DrawDelegateCallback()
			{
				public void callback( DrawDelegate _delegate )
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

			final Vector3 length = _parent.getLength() ;

			draw = DrawAssist.createDraw( _parent.getPosition(),
										  _parent.getOffset(),
										  new Vector3(),
										  new Vector3( 1, 1, 1 ), _parent.getLayer() ) ;
			DrawAssist.amendUI( draw, true ) ;
			//DrawAssist.amendTexture( draw, sheet ) ;
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
		public InputEvent.Action pressed( final InputEvent _input )
		{
			Shape.updatePlaneUV( DrawAssist.getDrawShape( draw ), clicked.min, clicked.max ) ;
			DrawAssist.forceUpdate( draw ) ;

			return InputEvent.Action.PROPAGATE ;
		}

		@Override
		public InputEvent.Action released( final InputEvent _input )
		{
			switch( getParent().getState() )
			{
				case ENGAGED : return move( _input ) ;
				case NEUTRAL : return exited( _input ) ;
				default      : return InputEvent.Action.PROPAGATE ;
			}
		}

		@Override
		public InputEvent.Action move( final InputEvent _input )
		{
			Shape.updatePlaneUV( DrawAssist.getDrawShape( draw ), rollover.min, rollover.max ) ;
			DrawAssist.forceUpdate( draw ) ;

			return InputEvent.Action.PROPAGATE ;
		}

		@Override
		public InputEvent.Action exited( final InputEvent _input )
		{
			Shape.updatePlaneUV( DrawAssist.getDrawShape( draw ), neutral.min, neutral.max ) ;
			DrawAssist.forceUpdate( draw ) ;

			return InputEvent.Action.PROPAGATE ;
		}

		@Override
		public void refresh()
		{
			final Vector3 length = getParent().getLength() ;
			final Vector3 offset = getParent().getOffset() ;

			Shape.updatePlaneGeometry( DrawAssist.getDrawShape( draw ), length ) ;
			DrawAssist.forceUpdate( draw ) ;

			if( font != null )
			{
				final Vector3 textOffset = DrawAssist.getOffset( drawText ) ;
				textOffset.setXYZ( offset ) ;
				textOffset.add( ( length.x / 2 ) - ( font.stringWidth( text ) / 2 ), ( length.y / 2 ) - ( font.getHeight() / 2 ), 0.0f ) ;
				DrawAssist.forceUpdate( drawText ) ;
			}
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
