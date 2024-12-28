package com.linxonline.mallet.animation ;

import java.util.List ;
import com.linxonline.mallet.util.MalletList ;
import com.linxonline.mallet.util.Logger ;

import com.linxonline.mallet.io.filesystem.GlobalFileSystem ;
import com.linxonline.mallet.io.filesystem.FileStream ;
import com.linxonline.mallet.io.formats.json.JObject ;
import com.linxonline.mallet.io.formats.json.JArray ;

public final class AnimatorGenerator
{
	private final static JArray EMPTY_ARRAY = JArray.construct() ; 

	public AnimatorGenerator() {}

	public static <T extends Animation.Frame> Animation<T> load( final String _path, final FrameGenerator<T> _generator )
	{
		final FileStream file = GlobalFileSystem.getFile( _path ) ;
		if( file.exists() == false )
		{
			Logger.println( "No Animation found: " + _path, Logger.Verbosity.NORMAL ) ;
			return null ;
		}

		final JObject jAnimation = JObject.construct( file ) ;
		final int framerate = jAnimation.optInt( "framerate", 30 ) ;

		final JArray jResources = jAnimation.optJArray( "resources", EMPTY_ARRAY ) ;
		final JArray jFrames = jAnimation.optJArray( "frames", EMPTY_ARRAY ) ;
		final JArray jFrameOrder = jAnimation.optJArray( "frame_order", EMPTY_ARRAY ) ;

		final int[] frameOrder = generateFrameOrder( jFrames, jFrameOrder ) ;
		final var frames = generateFrames( _path, jResources, jFrames, _generator ) ;

		return new Animation<T>( _path, framerate, frameOrder, frames ) ;
	}

	private static int[] generateFrameOrder( final JArray _frames, final JArray _order )
	{
		final int orderLength = _order.length() ;
		if( orderLength <= 0 )
		{
			final int framesLength = _frames.length() ;
			final int[] order = new int[framesLength] ;

			for( int i = 0; i < framesLength; ++i )
			{
				order[i] = i ;
			}

			return order ;
		}

		final int[] order = new int[orderLength] ;
		for( int i = 0; i < orderLength; ++i )
		{
			order[i] = _order.getInt( i ) ;
		}

		return order ;
	}

	private static <T extends Animation.Frame> T[] generateFrames( final String _path, final JArray _resources, final JArray _frames, final FrameGenerator<T> _generator )
	{
		final int frameLength = _frames.length() ;

		final T[] frames = ( T[] )new Animation.Frame[frameLength] ;
		final List<JObject> resources = MalletList.<JObject>newList() ; 

		for( int i = 0; i < frameLength; ++i )
		{
			final JArray requiredResources = _frames.getJArray( i ) ;

			// Grab the resources required for the frame.
			resources.clear() ;
			final int requiredResourcesLength = requiredResources.length() ;
			for( int j = 0; j < requiredResourcesLength; ++j )
			{
				final int index = requiredResources.getInt( j ) ;
				resources.add( _resources.getJObject( index ) ) ;
			}

			frames[i] = _generator.create( _path, resources ) ;
		}

		return frames ;
	}

	public interface FrameGenerator<T>
	{
		public T create( final String _animPath, final List<JObject> _resources ) ;
	}
}
