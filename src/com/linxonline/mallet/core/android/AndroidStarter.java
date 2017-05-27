package com.linxonline.mallet.core.android ;

import android.util.DisplayMetrics ;

import com.linxonline.mallet.maths.* ;

import com.linxonline.mallet.core.AbstractStarter ;
import com.linxonline.mallet.core.GameSystem ;
import com.linxonline.mallet.core.GameLoader ;
import com.linxonline.mallet.core.GameSettings ;

import com.linxonline.mallet.core.test.GameTestLoader ;

import com.linxonline.mallet.core.ISystem ;
import com.linxonline.mallet.core.ISystem.ShutdownDelegate ;
import com.linxonline.mallet.core.GlobalConfig ;

import com.linxonline.mallet.renderer.IRender ;

import com.linxonline.mallet.io.filesystem.FileSystem ;
import com.linxonline.mallet.io.filesystem.GlobalHome ;
import com.linxonline.mallet.io.filesystem.GlobalFileSystem ;

import com.linxonline.mallet.io.reader.config.ConfigParser ;
import com.linxonline.mallet.io.reader.config.ConfigReader ;
import com.linxonline.mallet.io.writer.config.ConfigWriter ;

import com.linxonline.mallet.util.notification.Notification ;
import com.linxonline.mallet.util.settings.Settings ;
import com.linxonline.mallet.util.Logger ;
import com.linxonline.mallet.util.Tuple ;

import com.linxonline.mallet.io.filesystem.android.* ;
import com.linxonline.mallet.core.android.gl.GLAndroidSystem ;
import com.linxonline.mallet.renderer.android.GL.* ;

import com.linxonline.mallet.ui.UI ;
import com.linxonline.mallet.ui.UIRatio ;

public abstract class AndroidStarter extends AbstractStarter
{
	public AndroidStarter( final AndroidActivity _activity, final Notification.Notify _notify )
	{
		super( new GLAndroidSystem( _activity, _notify ) ) ;
	}

	public void run()
	{
		final ISystem main = getMainSystem() ;
		final GameSystem game = getGameSystem() ;

		setRenderSettings( main ) ;

		Logger.println( "Running...", Logger.Verbosity.MINOR ) ;
		game.runSystem() ;			// Begin running the game-loop
		Logger.println( "Stopped...", Logger.Verbosity.MINOR ) ;
	}

	public void stop()
	{
		getGameSystem().stopSystem() ;
		getMainSystem().stopSystem() ;
	}

	public void shutdown()
	{
		getMainSystem().shutdownSystem() ;
	}

	/**
		Set the Rendering Systems initial Display, Render Dimensions, & Camera position.
		Uses the configuration file loaded above to set the rendering system.
	*/
	@Override
	public void setRenderSettings( final ISystem _system )
	{
		final AndroidActivity activity = ( ( GLAndroidSystem )_system ).activity ;
		final DisplayMetrics metrics = new DisplayMetrics() ;
		activity.getWindowManager().getDefaultDisplay().getMetrics( metrics ) ;

		final IRender render = _system.getRenderer() ;

		//final RenderInfo info = render.getRenderInfo() ;
		//info.setKeepRenderRatio( GlobalConfig.getBoolean( "KEEPRATIO", true ) ) ;

		final UI.Unit unit = GlobalConfig.<UI.Unit>getObject( "UI_UNIT", UI.Unit.CENTIMETRE ) ;

		final int xdpu = unit.convert( GlobalConfig.getInteger( "DPIX", ( int )metrics.xdpi ) ) ;
		final int ydpu = unit.convert( GlobalConfig.getInteger( "DPIY", ( int )metrics.ydpi ) ) ;
		UIRatio.setGlobalUIRatio( xdpu, ydpu ) ;
	}
}
