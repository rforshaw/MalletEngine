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

	public WebSound( final HTMLSourceElement _source )
	{
		source = _source ;
	}

	public void destroy() {}
}