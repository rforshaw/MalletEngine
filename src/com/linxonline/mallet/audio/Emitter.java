package com.linxonline.mallet.audio ;

import com.linxonline.mallet.maths.Vector3 ;

import com.linxonline.mallet.util.SourceCallback ;

public class Emitter
{
	private final static SourceCallback FALLBACK = new SourceCallback()
	{
		public void callbackRemoved() {}

		public void start() {}
		public void pause() {}
		public void stop() {}

		public void tick( final float _dt ) {}
		public void finished() {}
	} ;

	private final String file ;
	private final StreamType type ;
	private final Category category ;
	private final Vector3 position = new Vector3() ;
	private SourceCallback callback ;

	public Emitter( final String _file, final StreamType _type, final Category.Channel _channel )
	{
		this( _file, _type, _channel, FALLBACK ) ;
	}

	public Emitter( final String _file, final StreamType _type, final Category.Channel _channel, final SourceCallback _callback )
	{
		file = _file ;
		type = _type ;
		category = new Category( _channel ) ;
		callback = _callback ;
	}

	public void setPosition( final float _x, final float _y, final float _z )
	{
		position.setXYZ( _x, _y, _z ) ;
	}

	public Vector3 getPosition( final Vector3 _fill )
	{
		_fill.setXYZ( position ) ;
		return _fill ;
	}

	public String getFilepath()
	{
		return file ;
	}

	public StreamType getStreamType()
	{
		return type ;
	}

	public Category getCategory()
	{
		return category ;
	}

	public void setCallback( final SourceCallback _callback )
	{
		callback = ( _callback != null ) ? _callback : FALLBACK ;
	}
	
	public SourceCallback getCallback()
	{
		return callback ;
	}
}
