package com.linxonline.mallet.audio ;

public interface AudioDelegate
{
	public Emitter add( final Emitter _emitter ) ;
	public Emitter remove( final Emitter _emitter ) ;

	public Emitter play( final Emitter _emitter ) ;
	public Emitter stop( final Emitter _emitter ) ;
	public Emitter pause( final Emitter _emitter ) ;

	public Emitter update( final Emitter _emitter ) ;

	/**
		Inform the Audio-system to stop accepting requests 
		from delegate and remove all previous requests
		from being updated.
	*/
	public void shutdown() ;
}
