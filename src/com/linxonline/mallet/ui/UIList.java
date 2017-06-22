package com.linxonline.mallet.ui ;

import java.util.UUID ;
import java.util.List ;

import com.linxonline.mallet.util.MalletList ;

import com.linxonline.mallet.renderer.* ;
import com.linxonline.mallet.event.* ;
import com.linxonline.mallet.input.* ;
import com.linxonline.mallet.maths.* ;

public class UIList extends UILayout
{
	private final World world ;
	private final Draw pane ;

	private final List<Draw> interceptToAddBasic = MalletList.<Draw>newList() ;
	private final List<Draw> interceptToAddText = MalletList.<Draw>newList() ;
	private final List<Draw> interceptToRemove = MalletList.<Draw>newList() ;
	private DrawDelegate<World, Draw> delegate = null ;

	public UIList( final Type _type, final float _length )
	{
		super( _type ) ;
		final UUID uid = UUID.randomUUID() ;
		world = WorldAssist.constructWorld( uid.toString(), 0 ) ;
		pane = UIList.createPane( world, this ) ;

		addEvent( DrawAssist.constructDrawDelegate( new DrawDelegateCallback()
		{
			public void callback( final DrawDelegate<World, Draw> _delegate )
			{
				if( delegate != null )
				{
					// Don't call shutdown(), we don't want to 
					// clean anything except an existing DrawDelegate.
					delegate.shutdown() ;
				}

				delegate = _delegate ;
			}
		} ) ) ;

		/*switch( _type )
		{
			case HORIZONTAL :
			{
				setMaximumLength( 0.0f, _length, 0.0f ) ;
				break ;
			}
			case VERTICAL :
			default       :
			{
				setMaximumLength( _length, 0.0f, 0.0f ) ;
				break ;
			}
		}*/
	}

	@Override
	public void passDrawDelegate( final DrawDelegate<World, Draw> _delegate, final World _world )
	{
		super.passDrawDelegate( delegate, world ) ;
		_delegate.addBasicDraw( pane, _world ) ;
	}

	@Override
	public void shutdown()
	{
		super.shutdown() ;
		if( world != WorldAssist.getDefaultWorld() )
		{
			WorldAssist.destroyWorld( world ) ;
		}
	}

	private static Draw createPane( final World _world, final UIElement _parent )
	{
		final Vector3 length = _parent.getLength() ;
		final Draw pane = DrawAssist.createDraw( _parent.getPosition(),
												 _parent.getOffset(),
												 new Vector3(),
												 new Vector3( 1, 1, 1 ), _parent.getLayer() + 1 ) ;

		DrawAssist.amendUI( pane, true ) ;
		DrawAssist.amendShape( pane, Shape.constructPlane( new Vector3( length.x, length.y, 0 ),
															new Vector2( 0, 1 ),
															new Vector2( 1, 0 ) ) ) ;

		final Program program = ProgramAssist.create( "SIMPLE_TEXTURE" ) ;
		ProgramAssist.map( program, "inTex0", new MalletTexture( _world ) ) ;
		DrawAssist.attachProgram( pane, program ) ;
		return pane ;
	}
}
