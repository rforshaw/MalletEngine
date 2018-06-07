package com.linxonline.mallet.renderer.web.gl ;

import java.util.List ;
import java.util.Map ;
import java.util.Iterator ;

import java.nio.* ;

import org.teavm.jso.webgl.WebGLRenderingContext ;
import org.teavm.jso.webgl.WebGLTexture ;
import org.teavm.jso.browser.Window ;
import org.teavm.jso.dom.html.HTMLDocument ;
import org.teavm.jso.dom.html.HTMLImageElement ;
import org.teavm.jso.dom.html.HTMLCanvasElement ;
import org.teavm.jso.dom.events.* ;
import org.teavm.jso.canvas.ImageData ;

import com.linxonline.mallet.core.GlobalConfig ;
import com.linxonline.mallet.io.filesystem.* ;
import com.linxonline.mallet.io.filesystem.web.* ;
import com.linxonline.mallet.io.AbstractManager ;
import com.linxonline.mallet.util.settings.Settings ;
import com.linxonline.mallet.util.Logger ;
import com.linxonline.mallet.util.Tuple ;
import com.linxonline.mallet.util.MalletList ;
import com.linxonline.mallet.util.MalletMap ;

import com.linxonline.mallet.renderer.* ;

public class GLTextureManager extends AbstractManager<GLImage>
{
	private static final Window window = Window.current() ;
	private static final HTMLDocument document = window.getDocument() ;

	/**
		When loading a texture the TextureManager will stream the 
		image content a-synchronously.
		To ensure the textures are added safely to resources we 
		temporarily store the images in a queue.
		The BufferedImages are then binded to OpenGL and added 
		to resources in order. If we don't do this images may be 
		binded to OpenGL out of order causing significant performance 
		degradation.
	*/
	private final List<Tuple<String, HTMLImageElement>> toBind = MalletList.<Tuple<String, HTMLImageElement>>newList() ;
	private final MetaGenerator metaGenerator = new MetaGenerator() ;

	/**
		Currently two OpenGL image formats are supported: RGBA and ABGR_EXT.
		It's set to RGBA by default due to the extension potentially not 
		being available, though unlikely. BufferedImage by default orders the channels ABGR.
	*/
	protected int imageFormat = GL3.RGBA ;
	protected GLWorldState worldState ;

	public GLTextureManager()
	{
		final ResourceLoader<GLImage> loader = getResourceLoader() ;
		loader.add( new ResourceDelegate<GLImage>()
		{
			public boolean isLoadable( final String _file )
			{
				return true ;
			}

			public GLImage load( final String _file )
			{
				final WebFile file = ( WebFile )GlobalFileSystem.getFile( _file ) ;
				if( file.exists() == false )
				{
					Logger.println( "Failed to create Texture: " + _file, Logger.Verbosity.NORMAL ) ;
					return null ;
				}

				final HTMLImageElement img = file.getHTMLImage() ;
				window.getDocument().getBody().appendChild( img ) ;

				img.getStyle().setProperty( "display", "none" ) ;
				img.setSrc( _file ) ;
				img.addEventListener( "load", new EventListener()
				{
					@Override
					public void handleEvent( final Event _event )
					{
						synchronized( toBind )
						{
							// We don't want to bind the BufferedImage now
							// as that will take control of the OpenGL context.
							toBind.add( new Tuple<String, HTMLImageElement>( _file, img ) ) ;
						}
					}
				} ) ;

				put( _file, null ) ;
				return null ; 
			}
		} ) ;
	}

	public void setWorldState( final GLWorldState _worldState )
	{
		worldState = _worldState ;
	}

	@Override
	public GLImage get( final String _file )
	{
		synchronized( toBind )
		{
			// GLRenderer will continuosly call get() until it 
			// recieves a Texture, so we only need to bind 
			// textures that are waiting for the OpenGL context 
			// when the render requests it.
			if( toBind.isEmpty() == false )
			{
				for( final Tuple<String, HTMLImageElement> tuple : toBind )
				{
					put( tuple.getLeft(), bind( tuple.getRight() ) ) ;
				}
				toBind.clear() ;
			}
		}

		final GLImage image = super.get( _file ) ;
		if( image != null )
		{
			//System.out.println( "GLImage: " + _file ) ;
			return image ;
		}

		final GLWorld world = worldState.getWorld( _file ) ;
		if( world != null )
		{
			return world.getImage() ;
		}

		return null ;
	}

	/**
		Return the meta information associated with an image
		defined by _path.
		If the meta data has yet to be generated, create it 
		and store the meta data in imageMetas. This hashmap 
		is persistant across the runtime of the renderer.
		If the meta data changes from one call to the next, 
		the meta data stored is NOT updated.
		FileStream would need to be updated to support 
		file modification timestamps.
	*/
	public MalletTexture.Meta getMeta( final String _path )
	{
		return metaGenerator.getMeta( _path ) ;
	}

	/**
		Change the image format used to bind textures.
		RGBA and ABGR_EXT are supported.
	*/
	public void setImageFormat( final int _format )
	{
		imageFormat = _format ;
	}

	public GLImage bind( final HTMLImageElement _image )
	{
		return bind( _image, InternalFormat.COMPRESSED ) ;
	}

	public GLImage bind( final HTMLCanvasElement _image )
	{
		return bind( _image, InternalFormat.COMPRESSED ) ;
	}

	/**
		Binds the BufferedImage byte-stream into video memory.
		BufferedImage must be in 4BYTE_ABGR.
		4BYTE_ABGR removes endinese problems.
	*/
	public GLImage bind( final HTMLCanvasElement _image, final InternalFormat _format )
	{
		final WebGLTexture textureID = glGenTextures() ;
		MGL.bindTexture( GL3.TEXTURE_2D, textureID ) ;

		MGL.texParameteri( GL3.TEXTURE_2D, GL3.TEXTURE_MIN_FILTER, GL3.LINEAR ) ;
		MGL.texParameteri( GL3.TEXTURE_2D, GL3.TEXTURE_WRAP_S, GL3.CLAMP_TO_EDGE ) ;
		MGL.texParameteri( GL3.TEXTURE_2D, GL3.TEXTURE_WRAP_T, GL3.CLAMP_TO_EDGE ) ;

		MGL.texImage2D( GL3.TEXTURE_2D, 
						 0, 
						 GL3.RGBA,
						 GL3.RGBA, 
						 GL3.UNSIGNED_BYTE, 
						 _image ) ;

		MGL.bindTexture( GL3.TEXTURE_2D, null ) ;			// Reset to default texture
		//final long estimatedConsumption = width * height * ( channels * 8 ) ;
		return new GLImage( textureID, 0L/*estimatedConsumption*/ ) ;
	}

	/**
		Binds the BufferedImage byte-stream into video memory.
		BufferedImage must be in 4BYTE_ABGR.
		4BYTE_ABGR removes endinese problems.
	*/
	public GLImage bind( final HTMLImageElement _image, final InternalFormat _format )
	{
		final WebGLTexture textureID = glGenTextures() ;
		MGL.bindTexture( GL3.TEXTURE_2D, textureID ) ;

		MGL.texParameteri( GL3.TEXTURE_2D, GL3.TEXTURE_WRAP_S, GL3.REPEAT ) ;
		MGL.texParameteri( GL3.TEXTURE_2D, GL3.TEXTURE_WRAP_T, GL3.REPEAT ) ;
		MGL.texParameteri( GL3.TEXTURE_2D, GL3.TEXTURE_MAG_FILTER, GL3.LINEAR ) ;
		MGL.texParameteri( GL3.TEXTURE_2D, GL3.TEXTURE_MIN_FILTER, GL3.LINEAR_MIPMAP_NEAREST ) ;

		//gl.pixelStorei( GL3.UNPACK_ALIGNMENT, 1 ) ;
		MGL.texImage2D( GL3.TEXTURE_2D, 
						 0, 
						 GL3.RGBA, 
						 GL3.RGBA, 
						 GL3.UNSIGNED_BYTE, 
						 _image ) ;

		MGL.generateMipmap( GL3.TEXTURE_2D ) ;
		MGL.bindTexture( GL3.TEXTURE_2D, null ) ;			// Reset to default texture

		//final long estimatedConsumption = width * height * ( channels * 8 ) ;
		return new GLImage( textureID, 0L/*estimatedConsumption*/ ) ;
	}

	private WebGLTexture glGenTextures()
	{
		return MGL.createTexture() ;
	}

	public enum InternalFormat
	{
		COMPRESSED,
		UNCOMPRESSED
	}

	/**
		Retains meta information about textures.
		A texture can be loaded and used by the renderer,
		without storing the meta data.
	*/
	protected static class MetaGenerator
	{
		private final Map<String, MalletTexture.Meta> imageMetas = MalletMap.<String, MalletTexture.Meta>newMap() ;

		/**
			Return the meta information associated with an image
			defined by _path.
			If the meta data has yet to be generated, create it 
			and store the meta data in imageMetas. This hashmap 
			is persistant across the runtime of the renderer.
			If the meta data changes from one call to the next, 
			the meta data stored is NOT updated.
			FileStream would need to be updated to support 
			file modification timestamps.
		*/
		public MalletTexture.Meta getMeta( final String _path )
		{
			synchronized( imageMetas )
			{
				final MalletTexture.Meta meta = imageMetas.get( _path ) ;
				if( meta != null)
				{
					return meta ;
				}

				final WebFile file = ( WebFile )GlobalFileSystem.getFile( _path ) ;
				if( file.exists() == false )
				{
					Logger.println( "No Texture found to create Meta: " + _path, Logger.Verbosity.NORMAL ) ;
					return new MalletTexture.Meta( _path, 0, 0 ) ;
				}

				final HTMLImageElement img = file.getHTMLImage() ;
				return addMeta( _path, new MalletTexture.Meta( _path, img.getWidth(), img.getHeight() ) ) ; 
			}
		}

		private MalletTexture.Meta addMeta( final String _path, final MalletTexture.Meta _meta )
		{
			if( _meta != null )
			{
				imageMetas.put( _path, _meta ) ;
				return _meta ;
			}

			Logger.println( "Failed to create Texture Meta: " + _path, Logger.Verbosity.NORMAL ) ;
			return new MalletTexture.Meta( _path, 0, 0 ) ;
		}
	}
}
