package com.linxonline.mallet.renderer ;

import java.util.List ;
import java.util.ArrayList ;
import java.util.Iterator ;
import java.lang.ref.WeakReference ;

import com.linxonline.mallet.util.Logger ;

public class TextUpdaterPool
{
	private final List<WeakReference<TextUpdater>> pool = new ArrayList<WeakReference<TextUpdater>>() ;

	/**
		Remove TextUpdaters and associated resources that are
		used by the passed in World.
	*/
	public void clean( final World _world )
	{
		synchronized( pool )
		{
			final List<ABuffer> worldBuffers = _world.getBuffers() ;

			for( final Iterator<WeakReference<TextUpdater>> iterator = pool.iterator(); iterator.hasNext(); )
			{
				final WeakReference<TextUpdater> weak = iterator.next() ;

				final TextUpdater updater = weak.get() ;
				if( updater == null )
				{
					Logger.println( "TextUpdater no longer exists, but has not been removed from the pool first.", Logger.Verbosity.MAJOR ) ;
					iterator.remove() ;
					continue ;
				}

				boolean removeUpdater = true ;
				for( final TextBuffer buffer : updater.getBuffers() )
				{
					if( worldBuffers.contains( buffer ) == false )
					{
						removeUpdater = false ;
						continue ;
					}

					DrawAssist.remove( buffer ) ;
					_world.removeBuffers( buffer ) ;
				}

				WorldAssist.update( _world ) ;

				if( removeUpdater )
				{
					iterator.remove() ;
					DrawAssist.remove( updater ) ;
				}
			}
		}
	}

	/**
		Remove TextUpdaters and associated resources that are used
		by the passed in program and world.
	*/
	public void clean( final World _world, final Program _program )
	{
		synchronized( pool )
		{
			final List<ABuffer> worldBuffers = _world.getBuffers() ;

			for( final Iterator<WeakReference<TextUpdater>> iterator = pool.iterator(); iterator.hasNext(); )
			{
				final WeakReference<TextUpdater> weak = iterator.next() ;

				final TextUpdater updater = weak.get() ;
				if( updater == null )
				{
					Logger.println( "TextUpdater no longer exists, but has not been removed from the pool first.", Logger.Verbosity.MAJOR ) ;
					iterator.remove() ;
					continue ;
				}

				boolean removeUpdater = true ;
				for( final TextBuffer buffer : updater.getBuffers() )
				{
					if( _program.equals( buffer.getProgram() ) == false )
					{
						removeUpdater = false ;
						continue ;
					}

					if( worldBuffers.contains( buffer ) == false )
					{
						removeUpdater = false ;
						continue ;
					}

					DrawAssist.remove( buffer ) ;
					_world.removeBuffers( buffer ) ;
				}

				WorldAssist.update( _world ) ;

				if( removeUpdater )
				{
					iterator.remove() ;
					DrawAssist.remove( updater ) ;
				}
			}
		}
	}

	/**
		Find if a GeometryBuffer has the same signature as passed in.
		If no buffer exists create a new buffer and add it to 
		the passed in world.
	*/
	public TextUpdater getOrCreate( final World _world,
									final Program _program,
									final boolean _ui,
									final int _order )
	{
		TextUpdater updater = get( _world, _program, _ui, _order ) ;
		if( updater == null )
		{
			final TextBuffer buffer = DrawAssist.add( new TextBuffer( _program, _ui, _order ) ) ;
			updater = DrawAssist.add( new TextUpdater( buffer ) ) ;

			updater.addBuffers( buffer ) ;

			_world.addBuffers( buffer ) ;
			WorldAssist.update( _world ) ;

			synchronized( pool )
			{
				pool.add( new WeakReference( updater ) ) ;
			}
		}

		return updater ;
	}

	/**
		When a TextUpdater is created it is added to the global 
		pool of available DrawBuffers.

		This allows other areas of the system to use existing 
		buffers rather than create their own.

		You should only create a new buffer if a buffer does 
		not yet exist for the content you want to render.
	*/
	public TextUpdater get( final World _world,
							final Program _program,
							final boolean _ui,
							final int _order )
	{
		synchronized( pool )
		{
			final List<ABuffer> worldBuffers = _world.getBuffers() ;

			for( final WeakReference<TextUpdater> weak : pool )
			{
				final TextUpdater updater = weak.get() ;
				if( updater == null )
				{
					continue ;
				}

				final List<TextBuffer> buffers = updater.getBuffers() ;
				final TextBuffer buffer = buffers.get( 0 ) ;
				if( buffer == null )
				{
					// Will need to remove buffer from global pool.
					continue ;
				}

				if( buffer.isUI() != _ui )
				{
					continue ;
				}

				if( buffer.getOrder() != _order )
				{
					continue ;
				}

				if( _program.equals( buffer.getProgram() ) == false )
				{
					continue ;
				}

				if( worldBuffers.contains( buffer ) == false )
				{
					continue ;
				}

				return updater ;
			}
		}

		return null ;
	}
}
