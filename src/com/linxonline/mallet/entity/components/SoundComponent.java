package com.linxonline.mallet.entity.components ;

/**
	Sound Component should be implemented to allow an Entity
	to quickly issue Sound Events commands.

	Whether that be manipulating, removing, or creating a sound.
**/
public class SoundComponent extends Component
{
	public SoundComponent()
	{
		super( "SOUND", "SOUNDCOMPONENT" ) ;
	}
}