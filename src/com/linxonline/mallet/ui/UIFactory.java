package com.linxonline.mallet.ui ;

import java.util.List ;

import com.linxonline.mallet.core.GlobalConfig ;
import com.linxonline.mallet.util.notification.Notification ;

import com.linxonline.mallet.renderer.DrawDelegateCallback ;
import com.linxonline.mallet.renderer.DrawDelegate ;
import com.linxonline.mallet.renderer.UpdateType ;

import com.linxonline.mallet.renderer.CameraAssist ;
import com.linxonline.mallet.renderer.Camera ;

import com.linxonline.mallet.renderer.MalletFont ;
import com.linxonline.mallet.renderer.MalletColour ;
import com.linxonline.mallet.renderer.MalletTexture ;
import com.linxonline.mallet.renderer.DrawAssist ;
import com.linxonline.mallet.renderer.Draw ;
import com.linxonline.mallet.renderer.Shape ;

import com.linxonline.mallet.renderer.WorldAssist ;
import com.linxonline.mallet.renderer.World ;

import com.linxonline.mallet.renderer.ProgramAssist ;
import com.linxonline.mallet.renderer.Program ;

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
		return constructWindowLayout( _type, CameraAssist.getDefaultCamera() ) ;
	}

	/**
		Construct a UILayout that uses the RenderWidth and RenderHeight
		of the window as the width and height of the UILayout.
	*/
	public static UILayout constructWindowLayout( final ILayout.Type _type, final Camera _camera )
	{
		final World base = WorldAssist.getDefaultWorld() ;
		final IntVector2 dim = WorldAssist.getRenderDimensions( base ) ;

		final Vector3 dimension = new Vector3( dim.x, dim.y, 0.0f ) ;

		final UILayout layout = new UILayout( _type ) ;
		final UIRatio ratio = layout.getRatio() ;

		layout.setLength( ratio.toUnitX( dimension.x ),
						  ratio.toUnitY( dimension.y ),
						  ratio.toUnitZ( dimension.z ) ) ;

		CameraAssist.amendOrthographic( _camera, 0.0f, dimension.y, 0.0f, dimension.x, -1000.0f, 1000.0f ) ;
		CameraAssist.amendScreenResolution( _camera, ( int )dimension.x, ( int )dimension.y ) ;

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
				public String getType() { return "WINDOW_LISTENER" ; }
			}, _parent, _camera, _dimension ) ;
		}

		public WindowListener( final UIElement.MetaComponent _meta,
							   final UILayout _parent,
							   final Camera _camera,
							   final Vector3 _dimension )
		{
			_parent.super( _meta ) ;
			world = WorldAssist.getDefaultWorld() ;

			renderNotify = WorldAssist.attachRenderNotify( world, new Notification.Notify<World>()
			{
				public void inform( final World _world )
				{
					final IntVector2 dim = WorldAssist.getRenderDimensions( _world ) ;
					_dimension.x = dim.x ;
					_dimension.y = dim.y ;

					final UIRatio ratio = _parent.getRatio() ;
					_parent.setLength( ratio.toUnitX( _dimension.x ),
										ratio.toUnitY( _dimension.y ),
										ratio.toUnitZ( _dimension.z ) ) ;

					CameraAssist.amendOrthographic( _camera, 0.0f, _dimension.y, 0.0f, _dimension.x, -1000.0f, 1000.0f ) ;
					CameraAssist.amendScreenResolution( _camera, ( int )_dimension.x, ( int )_dimension.y ) ;
				}
			} ) ;
		}

		public void refresh() {}

		public void shutdown()
		{
			WorldAssist.dettachRenderNotify( world, renderNotify ) ;
		}
	}
}
