package com.linxonline.malleteditor.core ;

import java.util.Map ;

import com.linxonline.mallet.renderer.MalletColour ;
import com.linxonline.mallet.renderer.MalletFont ;
import com.linxonline.mallet.util.MalletMap ;
import com.linxonline.mallet.maths.* ;
import com.linxonline.mallet.ui.* ;

public abstract class UIPacket implements Connect.Connection
{
	private final String type ;

	private String name = "" ;
	private int layer = 0 ;
	private boolean visible = true ;
	private boolean disabled = false ;

	private final Vector3 position = new Vector3() ;
	private final Vector3 offset = new Vector3() ;
	private final Vector3 margin = new Vector3() ;

	private final Vector3 length = new Vector3() ;
	private final Vector3 minimumLength = new Vector3() ;
	private final Vector3 maximumLength = new Vector3() ;

	private final Connect.Signal nameChanged    = new Connect.Signal() ;
	private final Connect.Signal layerChanged   = new Connect.Signal() ;
	private final Connect.Signal disableChanged = new Connect.Signal() ;
	private final Connect.Signal visibleChanged = new Connect.Signal() ;

	private final Connect.Signal positionChanged = new Connect.Signal() ;
	private final Connect.Signal offsetChanged   = new Connect.Signal() ;
	private final Connect.Signal marginChanged   = new Connect.Signal() ;

	private final Connect.Signal lengthChanged = new Connect.Signal() ;
	private final Connect.Signal minimumLengthChanged = new Connect.Signal() ;
	private final Connect.Signal maximumLengthChanged = new Connect.Signal() ;

	private final Connect connect = new Connect() ;

	public UIPacket( final String _type )
	{
		type = _type ;
	}

	public abstract boolean supportsChildren() ;

	public abstract UIElement createElement() ;

	public void setName( final String _name )
	{
		if( name.equals( _name ) == false )
		{
			name = _name ;
			UIPacket.signal( this, nameChanged() ) ;
		}
	}

	public void setLayer( final int _layer )
	{
		if( layer != _layer )
		{
			layer = _layer ;
			UIPacket.signal( this, layerChanged() ) ;
		}
	}

	public void setDisableFlag( final boolean _flag )
	{
		if( disabled != _flag )
		{
			disabled = _flag ;
			UIPacket.signal( this, disableChanged() ) ;
		}
	}

	public void setVisibleFlag( final boolean _flag )
	{
		if( visible != _flag )
		{
			visible = _flag ;
			UIPacket.signal( this, visibleChanged() ) ;
		}
	}

	public String getType()
	{
		return type ;
	}

	public int getLayer()
	{
		return layer ;
	}

	public boolean getDisableFlag()
	{
		return disabled ;
	}

	public boolean getVisibleFlag()
	{
		return visible ;
	}

	public void setPosition( final float _x, final float _y, final float _z )
	{
		if( UI.applyVec3( position, _x, _y, _z ) == true )
		{
			UIPacket.signal( this, positionChanged() ) ;
		}
	}

	public void setOffset( final float _x, final float _y, final float _z )
	{
		if( UI.applyVec3( offset, _x, _y, _z ) == true )
		{
			UIPacket.signal( this, offsetChanged() ) ;
		}
	}

	public void setMargin( final float _x, final float _y, final float _z )
	{
		if( UI.applyVec3( margin, _x, _y, _z ) == true )
		{
			UIPacket.signal( this, marginChanged() ) ;
		}
	}

	public Vector3 getPosition( final Vector3 _populate )
	{
		_populate.setXYZ( position ) ;
		return _populate ;
	}

	public Vector3 getOffset( final Vector3 _populate )
	{
		_populate.setXYZ( offset ) ;
		return _populate ;
	}

	public Vector3 getMargin( final Vector3 _populate )
	{
		_populate.setXYZ( margin ) ;
		return _populate ;
	}

	public void setLength( final float _x, final float _y, final float _z )
	{
		if( UI.applyVec3( length, _x, _y, _z ) == true )
		{
			UIPacket.signal( this, lengthChanged() ) ;
		}
	}

	public void setMinimumLength( final float _x, final float _y, final float _z )
	{
		if( UI.applyVec3( minimumLength, _x, _y, _z ) == true )
		{
			UIPacket.signal( this, minimumLengthChanged() ) ;
		}
	}

	public void setMaximumLength( final float _x, final float _y, final float _z )
	{
		if( UI.applyVec3( maximumLength, _x, _y, _z ) == true )
		{
			UIPacket.signal( this, maximumLengthChanged() ) ;
		}
	}

	public Vector3 getLength( final Vector3 _populate )
	{
		_populate.setXYZ( length ) ;
		return _populate ;
	}

	public Vector3 getMinimumLength( final Vector3 _populate )
	{
		_populate.setXYZ( minimumLength ) ;
		return _populate ;
	}

	public Vector3 getMaximumLength( final Vector3 _populate )
	{
		_populate.setXYZ( maximumLength ) ;
		return _populate ;
	}

	/**
		Remove all connections made to this packet.
		Should only be called by the packets owner.
	*/
	public void shutdown()
	{
		UIPacket.disconnect( this ) ;
	}

	@Override
	public Connect getConnect()
	{
		return connect ;
	}

	public Connect.Signal nameChanged()
	{
		return nameChanged ;
	}

	public Connect.Signal layerChanged()
	{
		return layerChanged ;
	}

	public Connect.Signal disableChanged()
	{
		return disableChanged ;
	}

	public Connect.Signal visibleChanged()
	{
		return visibleChanged ;
	}

	public Connect.Signal positionChanged()
	{
		return positionChanged ;
	}

	public Connect.Signal offsetChanged()
	{
		return offsetChanged ;
	}

	public Connect.Signal marginChanged()
	{
		return marginChanged ;
	}

	public Connect.Signal lengthChanged()
	{
		return lengthChanged ;
	}

	public Connect.Signal minimumLengthChanged()
	{
		return minimumLengthChanged ;
	}

	public Connect.Signal maximumLengthChanged()
	{
		return maximumLengthChanged ;
	}

	/**
		Connect the slot to the signal, a signal may contain 
		multiple data-points if any of those data points change 
		the slot will be informed of the change.

		Signals or Variables can not be immutable - for example String.
	*/
	public static <T extends Connect.Connection> Connect.Slot<T> connect( final T _packet, final Connect.Signal _signal, final Connect.Slot<T> _slot )
	{
		final Connect connect = _packet.getConnect() ;
		connect.connect( _packet, _signal, _slot ) ;
		return _slot ;
	}

	/**
		Disconnect the specific slot from a particular signal
		and associated variable.

		Disconnect will not work if the signal or variable is 
		immutable.
	*/
	public static <T extends Connect.Connection> boolean disconnect( final T _packet, final Connect.Signal _signal, final Connect.Slot<T> _slot )
	{
		final Connect connect = _packet.getConnect() ;
		return connect.disconnect( _packet, _signal, _slot ) ;
	}

	/**
		Disconnect all slots from a signal.
	*/
	public static <T extends Connect.Connection> boolean disconnect( final T _packet )
	{
		final Connect connect = _packet.getConnect() ;
		return connect.disconnect( _packet ) ;
	}

	/**
		Inform all slots connected to this signal and associated
		to the variable that the signal's state has changed.
	*/
	public static <T extends Connect.Connection> void signal( final T _packet, final Connect.Signal _signal )
	{
		final Connect connect = _packet.getConnect() ;
		connect.signal( _packet, _signal ) ;
	}

	public class UIText
	{
		private String text ;
		private MalletFont font ;
		private UI.Alignment xAlign ;
		private UI.Alignment yAlign ;

		private final Connect.Signal textChanged   = new Connect.Signal() ;
		private final Connect.Signal fontChanged   = new Connect.Signal() ;
		private final Connect.Signal xAlignChanged = new Connect.Signal() ;
		private final Connect.Signal yAlignChanged = new Connect.Signal() ;
		
		public UIText()
		{
			this( null, null, null, null ) ;
		}

		public UIText( final String _text,
					   final MalletFont _font,
					   final UI.Alignment _x,
					   final UI.Alignment _y )
		{
			text   = ( _text != null ) ? _text : "" ;
			font   = ( _font != null ) ? _font : new MalletFont( "Arial" ) ;

			xAlign = ( _x != null ) ? _x : UI.Alignment.LEFT ;
			yAlign = ( _y != null ) ? _y : UI.Alignment.TOP ;
		}

		public void setText( final String _text )
		{
			if( _text != null )
			{
				text = _text ;
				UIPacket.signal( UIPacket.this, textChanged() ) ;
			}
		}

		public void setFont( final MalletFont _font )
		{
			if( _font != null )
			{
				font = _font ;
				UIPacket.signal( UIPacket.this, fontChanged() ) ;
			}
		}

		public void setAlignment( final UI.Alignment _x, final UI.Alignment _y )
		{
			if( _x != null && xAlign != _x )
			{
				xAlign = _x ;
				UIPacket.signal( UIPacket.this, xAlignChanged() ) ;
			}
			
			if( _y != null && yAlign != _y )
			{
				yAlign = _y ;
				UIPacket.signal( UIPacket.this, yAlignChanged() ) ;
			}
		}

		public String getText()
		{
			return text ;
		}

		public MalletFont getFont()
		{
			return font ;
		}

		public UI.Alignment getAlignmentX()
		{
			return xAlign ;
		}

		public UI.Alignment getAlignmentY()
		{
			return yAlign ;
		}
		
		public Connect.Signal textChanged()
		{
			return textChanged ;
		}

		public Connect.Signal fontChanged()
		{
			return fontChanged ;
		}

		public Connect.Signal xAlignChanged()
		{
			return xAlignChanged ;
		}

		public Connect.Signal yAlignChanged()
		{
			return yAlignChanged ;
		}
	}

	public class UIEdge
	{
		private String texture ;
		private float edge ;
		private MalletColour neutral ;
		private MalletColour rollover ;
		private MalletColour clicked ;

		private final Connect.Signal textureChanged  = new Connect.Signal() ;
		private final Connect.Signal edgeChanged     = new Connect.Signal() ;
		private final Connect.Signal neutralChanged  = new Connect.Signal() ;
		private final Connect.Signal rolloverChanged = new Connect.Signal() ;
		private final Connect.Signal clickedChanged  = new Connect.Signal() ;

		public UIEdge( final String _texture,
					   final float _edge,
					   final MalletColour _neutral,
					   final MalletColour _rollover,
					   final MalletColour _clicked )
		{
			texture  = ( _texture != null )  ? _texture : "" ;
			edge     = ( _edge > 0.0f )      ? _edge : 1.0f ;

			neutral  = ( _neutral != null )  ? _neutral : new MalletColour() ;
			rollover = ( _rollover != null ) ? _neutral : new MalletColour() ;
			clicked  = ( _clicked != null )  ? _neutral : new MalletColour() ;
		}

		public void setTexture( final String _texture )
		{
			if( _texture != null )
			{
				texture = _texture ;
				UIPacket.signal( UIPacket.this, textureChanged() ) ;
			}
		}

		public void setEdge( final float _edge )
		{
			if( Math.abs( edge - _edge ) > 0.001f )
			{
				edge = _edge ;
				UIPacket.signal( UIPacket.this, edgeChanged() ) ;
			}
		}

		public void setNeutral( final MalletColour _colour )
		{
			if( _colour != null && neutral.equals( _colour ) == false )
			{
				neutral = _colour ;
				UIPacket.signal( UIPacket.this, neutralChanged() ) ;
			}
		}

		public void setRollover( final MalletColour _colour )
		{
			if( _colour != null && rollover.equals( _colour ) == false )
			{
				rollover = _colour ;
				UIPacket.signal( UIPacket.this, rolloverChanged() ) ;
			}
		}

		public void setClicked( final MalletColour _colour )
		{
			if( _colour != null && clicked.equals( _colour ) == false )
			{
				clicked = _colour ;
				UIPacket.signal( UIPacket.this, clickedChanged() ) ;
			}
		}

		public String getTexture()
		{
			return texture ;
		}

		public float getEdge()
		{
			return edge ;
		}

		public MalletColour getNeutral()
		{
			return neutral ;
		}

		public MalletColour getRollover()
		{
			return rollover ;
		}

		public MalletColour getClicked()
		{
			return clicked ;
		}
		
		public Connect.Signal textureChanged()
		{
			return textureChanged ;
		}

		public Connect.Signal edgeChanged()
		{
			return edgeChanged ;
		}

		public Connect.Signal neutralChanged()
		{
			return neutralChanged ;
		}

		public Connect.Signal rolloverChanged()
		{
			return rolloverChanged ;
		}

		public Connect.Signal clickedChanged()
		{
			return clickedChanged ;
		}
	}
}
