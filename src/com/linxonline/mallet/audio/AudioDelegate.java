package com.linxonline.mallet.audio ;

public interface AudioDelegate
{
	public void addAudio( final Audio _audio ) ;
	public void removeAudio( final Audio _audio ) ;

	public void start() ;
	public void shutdown() ;
}