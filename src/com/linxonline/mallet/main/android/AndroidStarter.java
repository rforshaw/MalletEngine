package com.linxonline.mallet.main.android ;

import android.util.DisplayMetrics ;

import com.linxonline.mallet.maths.* ;

import com.linxonline.mallet.main.AbstractStarter ;
import com.linxonline.mallet.main.game.GameSystem ;
import com.linxonline.mallet.main.game.GameLoader ;
import com.linxonline.mallet.main.game.GameSettings ;

import com.linxonline.mallet.main.game.test.GameTestLoader ;

import com.linxonline.mallet.system.SystemInterface ;
import com.linxonline.mallet.system.SystemInterface.ShutdownDelegate ;
import com.linxonline.mallet.system.GlobalConfig ;

import com.linxonline.mallet.renderer.RenderInterface ;
import com.linxonline.mallet.renderer.RenderInfo ;

import com.linxonline.mallet.io.filesystem.FileSystem ;
import com.linxonline.mallet.io.filesystem.GlobalHome ;
import com.linxonline.mallet.io.filesystem.GlobalFileSystem ;

import com.linxonline.mallet.io.reader.config.ConfigParser ;
import com.linxonline.mallet.io.reader.config.ConfigReader ;
import com.linxonline.mallet.io.writer.config.ConfigWriter ;

import com.linxonline.mallet.util.notification.Notification ;
import com.linxonline.mallet.util.settings.Settings ;
import com.linxonline.mallet.util.logger.Logger ;
import com.linxonline.mallet.util.Tuple ;

import com.linxonline.mallet.io.filesystem.android.* ;
import com.linxonline.mallet.system.android.gl.GLAndroidSystem ;
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
		final SystemInterface main = getMainSystem() ;
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
	public void setRenderSettings( final SystemInterface _system )
	{
		final AndroidActivity activity = ( ( GLAndroidSystem )_system ).activity ;
		final DisplayMetrics metrics = new DisplayMetrics() ;
		activity.getWindowManager().getDefaultDisplay().getMetrics( metrics ) ;

		final RenderInterface render = _system.getRenderer() ;

		final RenderInfo info = render.getRenderInfo() ;
		info.setKeepRenderRatio( GlobalConfig.getBoolean( "KEEPRATIO", true ) ) ;

		final UI.Unit unit = GlobalConfig.<UI.Unit>getObject( "UI_UNIT", UI.Unit.CENTIMETRE ) ;

		final int xdpu = unit.convert( GlobalConfig.getInteger( "DPIX", ( int )metrics.xdpi ) ) ;
		final int ydpu = unit.convert( GlobalConfig.getInteger( "DPIY", ( int )metrics.ydpi ) ) ;
		UIRatio.setGlobalUIRatio( xdpu, ydpu ) ;
	}
}
