package com.linxonline.mallet.resources ;

import java.io.* ;
import java.nio.* ;
import com.jogamp.openal.* ;
import com.jogamp.openal.util.* ;

import com.linxonline.mallet.maths.* ;
import com.linxonline.mallet.resources.sound.* ;

public class SoundManager extends AbstractManager
{
	private AL openAL = null ;
	private Vector3 position = new Vector3() ;
	private Vector3 velocity = new Vector3() ;
	private float[] orientation = { position.x, position.y, position.z - 1.0f, 0.0f, 1.0f, 0.0f } ;

	public SoundManager()
	{
		initSoundBackend() ;
	}

	@Override
	protected Sound createResource( final String _file )
	{
		final ALSASound sound = new ALSASound() ;
		final int[] buffer = initBuffer( _file ) ;
		if( buffer != null )
		{
			sound.source = initSource( buffer ) ;
			sound.buffer = buffer ;
			sound.setOpenAL( openAL ) ;

			return new Sound( sound ) ;
		}

		return null ;
	}

	private int[] initBuffer( final String _file )
	{
		int[] format = new int[1] ;
		ByteBuffer[] data = new ByteBuffer[1] ;
		int[] size = new int[1] ;
		int[] freq = new int[1] ;
		int[] loop = new int[1] ;
		
		try
		{
			ALut.alutLoadWAVFile( _file, format, data, size, freq, loop ) ;
		}
		catch( ALException _ex )
		{
			System.out.println( "Error loading wav file: " + _file ) ;
			return null ;
		}
		
		int[] buffer = new int[1] ;
		openAL.alGenBuffers( 1, buffer, 0 ) ;
		if( openAL.alGetError() != AL.AL_NO_ERROR )
		{
			System.out.println( "Failed to Generate Buffer" ) ;
			return null ;
		}
		
		openAL.alBufferData( buffer[0], format[0], data[0], size[0], freq[0] ) ;
		return buffer ;
	}

	private int[] initSource( final int[] _buffer )
	{
		int[] source = new int[1] ;
		openAL.alGenSources( 1, source, 0 ) ;
		if( openAL.alGetError() != AL.AL_NO_ERROR )
		{
			System.out.println( "Failed to Generate Source" ) ;
			return null ;
		}

		openAL.alSourcei( source[0], AL.AL_BUFFER, _buffer[0] ) ; // Bind Buffer
		openAL.alSourcef( source[0], AL.AL_PITCH, 1.0f ) ;
		openAL.alSourcef( source[0], AL.AL_GAIN, 1.0f ) ;
		openAL.alSource3f( source[0], AL.AL_POSITION, 0.0f, 0.0f, 0.0f ) ;

		openAL.alSourcei( source[0], AL.AL_LOOPING, AL.AL_FALSE ) ;

		if( openAL.alGetError() != AL.AL_NO_ERROR )
		{
			System.out.println( "Failed to Configure Source" ) ;
			return null ;
		}

		return source ;
	}
	
	private void initSoundBackend()
	{
		try
		{
			ALut.alutInit() ;
			openAL = ALFactory.getAL() ;
			openAL.alGetError() ;
		}
		catch( ALException _ex )
		{
			_ex.printStackTrace() ;
		}
		
		openAL.alListener3f( AL.AL_POSITION, position.x, position.y, position.z ) ;
		openAL.alListener3f( AL.AL_VELOCITY, velocity.x, velocity.y, velocity.z ) ;
		openAL.alListenerfv( AL.AL_ORIENTATION, orientation, 0 ) ;
	}

	public void shutdown()
	{
		//AL.destroy() ;
	}
}
