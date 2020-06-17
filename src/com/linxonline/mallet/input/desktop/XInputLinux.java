package com.linxonline.mallet.input.desktop ;

import com.linxonline.mallet.util.tools.ConvertBytes ;
import com.linxonline.mallet.io.filesystem.* ;
import com.linxonline.mallet.input.* ;

public class XInputLinux implements XInputDevice
{
	private static final int BUTTON      = 0x01 ;
	private static final int AXIS        = 0x02 ;
	private static final int INIT        = 0x80 ;
	private static final int INIT_BUTTON = 0x81 ;

	private static final int RELEASED = 0 ;
	private static final int PRESSED  = 1 ;
	
	private final String in ;
	private XInputListener listener ;

	private final ByteInCallback callback = new ByteInCallback()
	{
		public int readBytes( final byte[] _stream, final int _length )
		{
			switch( ( int )_stream[6] )
			{
				case AXIS        : processAxis( _stream ) ; break ;
				case BUTTON      : processButton( _stream ) ; break ;
				//case INIT        : System.out.println( "INIT" ) ; break ;
				//case INIT_BUTTON : System.out.println( "INIT_BUTTON" ) ; break ;
			}
			return 8 ;
		}

		private void processAxis( final byte[] _stream )
		{
			if( listener == null )
			{
				return ;
			}

			final long timestamp = ConvertBytes.toInt( ConvertBytes.flipEndian( _stream, 0, 4 ), 0 ) & 0xFFFFFFFFL ;
			final short value =  ConvertBytes.toShort( ConvertBytes.flipEndian( _stream, 4, 2 ), 4 ) ;

			XInputDevice.Code axis = null ;
			switch( _stream[7] )
			{
				case 0  : axis = Code.JOYSTICK_1_X ; break ;
				case 1  : axis = Code.JOYSTICK_1_Y ; break ;
				case 2  : axis = Code.L2 ;           break ;
				case 3  : axis = Code.JOYSTICK_2_X ; break ;
				case 4  : axis = Code.JOYSTICK_2_Y ; break ;
				case 5  : axis = Code.R2 ;           break ;
				case 6  : axis = Code.DPAD_X ;       break ;
				case 7  : axis = Code.DPAD_Y ;       break ;
				default : return ;
			}

			listener.analogue( new XInputDevice.Event( timestamp, axis, value ) ) ;
		}

		private void processButton( final byte[] _stream )
		{
			if( listener == null )
			{
				return ;
			}

			final long timestamp = ConvertBytes.toInt( ConvertBytes.flipEndian( _stream, 0, 4 ), 0 ) & 0xFFFFFFFFL ;
			final short value =  ConvertBytes.toShort( ConvertBytes.flipEndian( _stream, 4, 2 ), 4 ) ;

			XInputDevice.Code button = null ;
			switch( _stream[7] )
			{
				case 0  : button = Code.A ;          break ;
				case 1  : button = Code.B ;          break ;
				case 2  : button = Code.X ;          break ;
				case 3  : button = Code.Y ;          break ;
				case 4  : button = Code.L1 ;         break ;
				case 5  : button = Code.R1 ;         break ;
				case 6  : button = Code.SELECT ;     break ;
				case 7  : button = Code.START ;      break ;
				case 8  : button = Code.ACTION ;     break ;
				case 9  : button = Code.JOYSTICK_1 ; break ;
				case 10 : button = Code.JOYSTICK_2 ; break ;
				default : return ;
			}

			switch( value )
			{
				case PRESSED  : listener.keyPressed( new XInputDevice.Event( timestamp, button ) ) ; break ;
				case RELEASED : listener.keyReleased( new XInputDevice.Event( timestamp, button ) ) ; break ;
			}
		}

		public void start()
		{
			if( listener != null )
			{
				listener.start() ;
			}
		}

		public void end()
		{
			if( listener != null )
			{
				listener.end() ;
			}
		}
	} ;

	public XInputLinux( final String _file )
	{
		in = _file ;
		final FileStream stream = GlobalFileSystem.getFile( _file ) ;
		stream.getByteInCallback( callback, 8 ) ;
	}

	public void setXInputListener( final XInputListener _listener )
	{
		listener = _listener ;
	}
}
