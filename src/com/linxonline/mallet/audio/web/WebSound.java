package com.linxonline.mallet.audio.web ;

import org.teavm.jso.dom.html.HTMLSourceElement ;

import com.linxonline.mallet.audio.SoundInterface ;

/**
	Stores the central audio data.
	Can be used by multiple WebAudioSource's.
*/
public class WebSound implements SoundInterface
{
	private HTMLSourceElement source ;
	private final int consumption ;		// Buffer size

	public WebSound( final HTMLSourceElement _source, final int _consumption )
	{
		source = _source ;
		consumption = _consumption ;
	}

	/**
		Return the audio buffer size in bytes.
	*/
	@Override
	public long getMemoryConsumption()
	{
		return consumption ;
	}

	public void destroy() {}
}
