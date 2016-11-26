package com.linxonline.mallet.audio ;

public interface AudioDelegate
{
	public void addAudio( final Audio _audio ) ;
	public void removeAudio( final Audio _audio ) ;

	/**
		Inform the Audio-system to stop accepting requests 
		from delegate and remove all previous requests
		from being updated.
	*/
	public void shutdown() ;
}
