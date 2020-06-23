package com.linxonline.mallet.audio ;

import java.lang.ref.Reference ;
import java.lang.ref.WeakReference ;

import com.linxonline.mallet.util.SourceCallback ;
import com.linxonline.mallet.util.caches.Cacheable ;

public class AudioData<T extends AudioData> implements Audio<T>, Cacheable
{
	private final static SourceCallback DEFAULT_CALLBACK = new SourceCallback()
	{
		public void callbackRemoved() {}

		public void start() {}
		public void pause() {}
		public void stop() {}

		public void tick( final float _dt ) {}
		public void finished() {}
	} ;

	private Category category ;
	private AudioSource source ;

	private String file ;
	private StreamType type ;
	private SourceCallback callback = DEFAULT_CALLBACK ;

	public boolean play = false ;
	public boolean stop = false ;
	public boolean dirty = false ;

	public AudioData( final String _file, final StreamType _type, final Category _cat )
	{
		set( _file, _type ) ;
		category = _cat ;
	}

	public void setSource( final AudioSource _source )
	{
		source = _source ;
	}

	public void set( final String _file, final StreamType _type )
	{
		file = _file ;
		type = _type ;
	}

	public void amendCallback( final SourceCallback _callback )
	{
		callback = _callback ;
		if( callback == null )
		{
			callback = DEFAULT_CALLBACK ;
		}
	}

	public String getFilePath()
	{
		return file ;
	}

	public StreamType getStreamType()
	{
		return type ;
	}

	public SourceCallback getCallback()
	{
		return callback ;
	}

	public AudioSource getSource()
	{
		return source ;
	}

	public Category getCategory()
	{
		return category ;
	}

	@Override
	public void reset()
	{
		setSource( null ) ;
		set( null, null ) ;
		amendCallback( DEFAULT_CALLBACK ) ;

		play = false ;
		stop = false ;
	}
}
