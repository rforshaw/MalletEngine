package com.linxonline.mallet.util.capture ;

import java.awt.*;
import java.awt.peer.*;
import sun.awt.*;
import java.net.*;
import java.io.*;
import java.util.*;
import java.awt.image.* ;

public class DesktopCaptureScreen
{
	private final static Rectangle bounds = new Rectangle() ;
	private final static RobotPeer peer = retrieveRobotPeer( GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice() ) ;

	public DesktopCaptureScreen() {}

	private static RobotPeer retrieveRobotPeer( final GraphicsDevice _device )
	{
		try
		{
			final Toolkit toolkit = Toolkit.getDefaultToolkit();
			return ( ( ComponentFactory ) toolkit ).createRobot( null, _device ) ;
		}
		catch( AWTException ex ) {}

		return null ;
	}

	public static void fillBufferedImage( final BufferedImage _image, final Rectangle _bounds )
	{
		final int[] screenArray = captureScreen( _bounds.x, _bounds.y, _bounds.width, _bounds.height ) ;
		final int[] biArray = ( ( DataBufferInt ) _image.getRaster().getDataBuffer() ).getData() ;
		System.arraycopy( screenArray, 0, biArray, 0, screenArray.length ) ;
	}

	public static int[] captureScreen( final int _x, final int _y, final int _width, final int _height )
	{
		bounds.setBounds( _x, _y, _width, _height ) ;
		return peer.getRGBPixels( bounds ) ;
	}
}