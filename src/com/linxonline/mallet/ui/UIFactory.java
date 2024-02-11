package com.linxonline.mallet.ui ;

import java.util.List ;

import com.linxonline.mallet.core.GlobalConfig ;
import com.linxonline.mallet.util.notification.Notification ;

import com.linxonline.mallet.renderer.CameraAssist ;
import com.linxonline.mallet.renderer.Camera ;

import com.linxonline.mallet.renderer.WorldAssist ;
import com.linxonline.mallet.renderer.World ;

import com.linxonline.mallet.maths.Vector3 ;
import com.linxonline.mallet.maths.Vector2 ;
import com.linxonline.mallet.maths.IntVector2 ;

import com.linxonline.mallet.input.* ;

import com.linxonline.mallet.ui.gui.* ;

/**
	Contains helper functions for the construction of 
	commonly used UI elements and their supporting classes.
*/
public final class UIFactory
{
	private UIFactory() {}

	/**
		Construct a UILayout that uses the RenderWidth and RenderHeight
		of the window as the width and height of the UILayout.
		Will use the default camera.
	*/
	public static UILayout constructWindowLayout( final ILayout.Type _type )
	{
		return constructWindowLayout( _type, CameraAssist.getDefault() ) ;
	}

	/**
		Construct a UILayout that uses the RenderWidth and RenderHeight
		of the window as the width and height of the UILayout.
	*/
	public static UILayout constructWindowLayout( final ILayout.Type _type, final Camera _camera )
	{
		final World base = WorldAssist.getDefault() ;
		final IntVector2 dim = base.getRenderDimensions( new IntVector2() ) ;

		final Vector3 dimension = new Vector3( dim.x, dim.y, 0.0f ) ;

		final UILayout layout = new UILayout( _type ) ;
		final UIRatio ratio = layout.getRatio() ;

		layout.setLength( ratio.toUnitX( dimension.x ),
						  ratio.toUnitY( dimension.y ),
						  ratio.toUnitZ( dimension.z ) ) ;

		_camera.setOrthographic( Camera.Mode.HUD, 0.0f, dimension.y, 0.0f, dimension.x, -1000.0f, 1000.0f ) ;
		_camera.setScreenResolution( ( int )dimension.x, ( int )dimension.y ) ;

		new WindowListener( layout, _camera, dimension ) ;

		return layout ;
	}

	public static UIElement.MetaComponent createMeta( final String _type )
	{
		return new UIElement.MetaComponent()
		{
			@Override
			public String getType()
			{
				return _type ;
			}
		} ;
	}

	private static class WindowListener extends UIElement.Component
	{
		private World world ;
		private final Notification.Notify<World> renderNotify ;

		public WindowListener( final UILayout _parent,
							   final Camera _camera,
							   final Vector3 _dimension )
		{
			this( new UIElement.MetaComponent()
			{
				@Override
				public String getType()
				{
					return "WINDOW_LISTENER" ;
				}
			}, _parent, _camera, _dimension ) ;
		}

		public WindowListener( final UIElement.MetaComponent _meta,
							   final UILayout _parent,
							   final Camera _camera,
							   final Vector3 _dimension )
		{
			_parent.super( _meta ) ;
			world = WorldAssist.getDefault() ;

			renderNotify = world.attachRenderNotify( new Notification.Notify<World>()
			{
				private final IntVector2 dim = new IntVector2() ;

				@Override
				public void inform( final World _world )
				{
					_world.getRenderDimensions( dim ) ;
					_dimension.x = dim.x ;
					_dimension.y = dim.y ;

					final UIRatio ratio = _parent.getRatio() ;
					_parent.setLength( ratio.toUnitX( _dimension.x ),
										ratio.toUnitY( _dimension.y ),
										ratio.toUnitZ( _dimension.z ) ) ;

					_camera.setOrthographic( Camera.Mode.HUD, 0.0f, _dimension.y, 0.0f, _dimension.x, -1000.0f, 1000.0f ) ;
					_camera.setScreenResolution( ( int )_dimension.x, ( int )_dimension.y ) ;
				}
			} ) ;
		}

		@Override
		public void refresh() {}

		@Override
		public void shutdown()
		{
			world.dettachRenderNotify( renderNotify ) ;
		}
	}
}
