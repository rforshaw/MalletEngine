package com.linxonline.mallet.entity.components ;

import java.util.Map ;
import java.util.List ;

import com.linxonline.mallet.entity.Entity ;

import com.linxonline.mallet.util.MalletMap ;
import com.linxonline.mallet.util.MalletList ;
import com.linxonline.mallet.input.InputEvent ;
import com.linxonline.mallet.input.KeyInputListener ;
import com.linxonline.mallet.input.KeyCode ;

/**
	Provides a convient method to process particular logic based an a key value.
	Decouples the key from the logic.
**/
public class KeyInputComponent extends InputComponent
{
	private final Map<KeyCode, Key> keys = MalletMap.<KeyCode, Key>newMap() ;

	public KeyInputComponent( final Entity _parent )
	{
		this( _parent, Entity.AllowEvents.YES ) ;
	}

	public KeyInputComponent( final Entity _parent, Entity.AllowEvents _allow )
	{
		this( _parent, _allow, InputMode.WORLD ) ;
	}

	public KeyInputComponent( final Entity _parent, Entity.AllowEvents _allow, final InputMode _mode )
	{
		super( _parent, _allow, _mode ) ;
		init() ;
	}

	/**
		Add custom initialisation logic here.
	*/
	public void init() {}

	public void registerKeys( final KeyCode[] _keys, final KeyInputListener _listener )
	{
		for( final KeyCode key : _keys )
		{
			registerKey( key, _listener ) ;
		}
	}

	public void registerKey( final KeyCode _key, final KeyInputListener _listener )
	{
		final Key key = keys.get( _key ) ;
		if( key != null )
		{
			key.add( _listener ) ;
		}
		else
		{
			keys.put( _key, new Key( _listener ) ) ;
		}
	}

	public void unregisterKey( final KeyCode _key )
	{
		if( keys.containsKey( _key ) == true )
		{
			keys.remove( _key ) ;
		}
	}

	@Override
	protected void processInputEvent( final InputEvent _input )
	{
		switch( _input.inputType )
		{
			case KEYBOARD_PRESSED :
			{
				final Key key = keys.get( _input.keycode ) ;
				if( key != null )
				{
					key.callPressed( _input ) ;
				}
				break ;
			}
			case KEYBOARD_RELEASED :
			{
				final Key key = keys.get( _input.keycode ) ;
				if( key != null )
				{
					key.callReleased( _input ) ;
				}
				break ;
			}
		}
	}

	private static class Key
	{
		public final List<KeyInputListener> listeners = MalletList.<KeyInputListener>newList() ;

		public Key( final KeyInputListener _listener )
		{
			listeners.add( _listener ) ;
		}

		public void add( final KeyInputListener _listener )
		{
			listeners.add( _listener ) ;
		}

		public void callPressed( final InputEvent _input )
		{
			final int size = listeners.size() ;
			for( int i = 0; i < size; i++ )
			{
				listeners.get( i ).pressed( _input ) ;
			}
		}

		public void callReleased( final InputEvent _input )
		{
			final int size = listeners.size() ;
			for( int i = 0; i < size; i++ )
			{
				listeners.get( i ).released( _input ) ;
			}
		}
	}
}
