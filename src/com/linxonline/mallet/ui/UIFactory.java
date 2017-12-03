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
import com.linxonline.mallet.renderer.World ;

import com.linxonline.mallet.renderer.ProgramAssist ;
import com.linxonline.mallet.renderer.Program ;

import com.linxonline.mallet.maths.Vector3 ;
import com.linxonline.mallet.maths.Vector2 ;

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
	public static UILayout constructWindowLayout( final UILayout.Type _type )
	{
		return constructWindowLayout( _type, CameraAssist.getDefaultCamera() ) ;
	}

	/**
		Construct a UILayout that uses the RenderWidth and RenderHeight
		of the window as the width and height of the UILayout.
	*/
	public static UILayout constructWindowLayout( final UILayout.Type _type, final Camera _camera )
	{
		final int width = GlobalConfig.getInteger( "RENDERWIDTH", 640 ) ;
		final int height = GlobalConfig.getInteger( "RENDERHEIGHT", 480 ) ;

		final Vector3 dimension = new Vector3( width, height, 0.0f ) ;
		final UILayout layout = new UILayout( _type, new Vector3(), new Vector3(), dimension ) ;

		layout.addListener( new ABase<UILayout>()
		{
			private final Notification.Notify<String> widthNotify = new Notification.Notify<String>()
			{
				public void inform( final String _data )
				{
					final UIRatio ratio = layout.getRatio() ;
					dimension.x = GlobalConfig.getInteger( "RENDERWIDTH", 640 ) ;

					layout.setLength( ratio.toUnitX( dimension.x ),
									  ratio.toUnitY( dimension.y ),
									  ratio.toUnitZ( dimension.z ) ) ;
					layout.makeDirty() ;

					CameraAssist.amendOrthographic( _camera, 0.0f, dimension.y, 0.0f, dimension.x, -1000.0f, 1000.0f ) ;
					CameraAssist.amendScreenResolution( _camera, ( int )dimension.x, ( int )dimension.y ) ;
				}
			} ;

			private final Notification.Notify<String> heightNotify = new Notification.Notify<String>()
			{
				public void inform( final String _data )
				{
					final UIRatio ratio = layout.getRatio() ;
					dimension.y = GlobalConfig.getInteger( "RENDERHEIGHT", 640 ) ;

					layout.setLength( ratio.toUnitX( dimension.x ),
									  ratio.toUnitY( dimension.y ),
									  ratio.toUnitZ( dimension.z ) ) ;
					layout.makeDirty() ;

					CameraAssist.amendOrthographic( _camera, 0.0f, dimension.y, 0.0f, dimension.x, -1000.0f, 1000.0f ) ;
					CameraAssist.amendScreenResolution( _camera, ( int )dimension.x, ( int )dimension.y ) ;
				}
			} ;

			@Override
			public void setParent( final UILayout _parent )
			{
				super.setParent( _parent ) ;
				GlobalConfig.addNotify( "RENDERWIDTH", widthNotify ) ;
				GlobalConfig.addNotify( "RENDERHEIGHT", heightNotify ) ;
			}

			public void refresh() {}

			public void shutdown()
			{
				GlobalConfig.removeNotify( "RENDERWIDTH", widthNotify ) ;
				GlobalConfig.removeNotify( "RENDERHEIGHT", heightNotify ) ;
			}
		} ) ;

		return layout ;
	}
}
