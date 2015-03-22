package com.linxonline.mallet.input ;

public interface XInputDevice
{
	public void setXInputListener( final XInputListener _listener ) ;

	public static enum Code
	{
		A, B, X, Y,
		R1, R2, L1, L2, 
		START, ACTION, SELECT, 
		JOYSTICK_1, JOYSTICK_2,
		JOYSTICK_1_X, JOYSTICK_1_Y, 
		JOYSTICK_2_X, JOYSTICK_2_Y,
		DPAD_X, DPAD_Y
	}

	/**
		XInputDevice is passed to XInputListener.
	*/
	public static class Event
	{
		public long timestamp ;
		public Code code ;
		public int value ;

		public Event( final long _timestamp, final int _code )
		{
			timestamp = _timestamp ;
			code = Code.A ;
			value = 0 ;
		}

		public Event( final long _timestamp, final XInputDevice.Code _code )
		{
			timestamp = _timestamp ;
			code = _code ;
			value = 0 ;
		}

		public Event( final long _timestamp, final XInputDevice.Code _code, final int _value )
		{
			timestamp = _timestamp ;
			code = _code ;
			value = _value ;
		}
		
		public String toString()
		{
			final StringBuffer buffer = new StringBuffer() ;
			buffer.append( "TimeStamp: " + timestamp ) ;
			buffer.append( " Code: " + code ) ;
			buffer.append( " Value: " + value ) ;

			return buffer.toString() ;
		}
	}
}