package com.linxonline.mallet.entity.components ;

import java.util.Map ;
import java.util.List ;

import com.linxonline.mallet.entity.Entity ;

import com.linxonline.mallet.util.MalletMap ;
import com.linxonline.mallet.util.MalletList ;
import com.linxonline.mallet.input.InputEvent ;
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
	}

	public void registerKeys( final KeyCode[] _keys, final IAction _action )
	{
		for( int i = 0; i < _keys.length; ++i )
		{
			registerKey( _keys[i], _action ) ;
		}
	}

	public void registerKey( final KeyCode _key, final IAction _action )
	{
		final Key key = keys.get( _key ) ;
		if( key != null )
		{
			key.add( _action ) ;
		}
		else
		{
			keys.put( _key, new Key( _action ) ) ;
		}
	}

	public void unregisterKey( final KeyCode _key )
	{
		keys.remove( _key ) ;
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

	public interface IAction
	{
		public void pressed( final InputEvent _input ) ;
		public void released( final InputEvent _input ) ;
	}

	private static class Key
	{
		public final List<IAction> actions = MalletList.<IAction>newList() ;

		public Key( final IAction _action )
		{
			actions.add( _action ) ;
		}

		public void add( final IAction _action )
		{
			actions.add( _action ) ;
		}

		public void callPressed( final InputEvent _input )
		{
			final int size = actions.size() ;
			for( int i = 0; i < size; i++ )
			{
				actions.get( i ).pressed( _input ) ;
			}
		}

		public void callReleased( final InputEvent _input )
		{
			final int size = actions.size() ;
			for( int i = 0; i < size; i++ )
			{
				actions.get( i ).released( _input ) ;
			}
		}
	}
}
