package com.linxonline.mallet.renderer.web.gl ;

import java.util.ArrayList ;
import java.util.HashMap ;
import java.util.Iterator ;
import java.awt.* ;
import java.awt.image.* ;
import java.awt.color.ColorSpace ;
import javax.imageio.* ;
import java.io.* ;
import javax.imageio.stream.* ;

import java.nio.* ;

import org.teavm.jso.webgl.WebGLRenderingContext ;
import org.teavm.jso.webgl.WebGLTexture ;
import org.teavm.jso.dom.html.HTMLImageElement ;

import com.linxonline.mallet.system.GlobalConfig ;
import com.linxonline.mallet.io.filesystem.* ;
import com.linxonline.mallet.io.filesystem.web.* ;
import com.linxonline.mallet.util.settings.Settings ;
import com.linxonline.mallet.util.logger.Logger ;
import com.linxonline.mallet.util.Tuple ;

import com.linxonline.mallet.resources.texture.* ;

import com.linxonline.mallet.renderer.* ;
import com.linxonline.mallet.resources.* ;

public class GLTextureManager extends AbstractManager<Texture>
{
	private final MetaGenerator metaGenerator = new MetaGenerator() ;

	/**
		Currently two OpenGL image formats are supported: RGBA and ABGR_EXT.
		It's set to RGBA by default due to the extension potentially not 
		being available, though unlikely. BufferedImage by default orders the channels ABGR.
	*/
	protected int imageFormat = GL3.RGBA ;

	public GLTextureManager()
	{
		final ResourceLoader<Texture> loader = getResourceLoader() ;
		loader.add( new ResourceDelegate<Texture>()
		{
			public boolean isLoadable( final String _file )
			{
				return true ;
			}

			public Texture load( final String _file, final Settings _settings )
			{
				final WebFile file = ( WebFile )GlobalFileSystem.getFile( _file ) ;
				if( file.exists() == false )
				{
					Logger.println( "Failed to create Texture: " + _file, Logger.Verbosity.NORMAL ) ;
					return null ;
				}

				return bind( file.getHTMLImage() ) ;
			}
		} ) ;
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

	public Texture bind( final HTMLImageElement _image )
	{
		return bind( _image, InternalFormat.COMPRESSED ) ;
	}

	/**
		Binds the BufferedImage byte-stream into video memory.
		BufferedImage must be in 4BYTE_ABGR.
		4BYTE_ABGR removes endinese problems.
	*/
	public Texture bind( final HTMLImageElement _image, final InternalFormat _format )
	{
		final WebGLRenderingContext gl = GLRenderer.getContext() ;
		if( gl == null )
		{
			System.out.println( "GL context doesn't exist" ) ;
			return null ;
		}

		//gl.enable( GL3.TEXTURE_2D ) ;

		final WebGLTexture textureID = glGenTextures( gl ) ;
		gl.bindTexture( GL3.TEXTURE_2D, textureID ) ;

		gl.texParameteri( GL3.TEXTURE_2D, GL3.TEXTURE_WRAP_S, GL3.REPEAT ) ;
		gl.texParameteri( GL3.TEXTURE_2D, GL3.TEXTURE_WRAP_T, GL3.REPEAT ) ;
		gl.texParameteri( GL3.TEXTURE_2D, GL3.TEXTURE_MAG_FILTER, GL3.LINEAR ) ;
		gl.texParameteri( GL3.TEXTURE_2D, GL3.TEXTURE_MIN_FILTER, GL3.LINEAR_MIPMAP_LINEAR ) ;

		final int width = _image.getWidth() ;
		final int height = _image.getHeight() ;
		//final int channels = _image.getSampleModel().getNumBands() ;
		int internalFormat = GL3.RGB ;

		imageFormat = GL3.RGBA ;

		//gl.pixelStorei( GL3.UNPACK_ALIGNMENT, 1 ) ;
		gl.texImage2D( GL3.TEXTURE_2D, 
						 0, 
						 GL3.RGBA, 
						 GL3.RGBA, 
						 GL3.UNSIGNED_BYTE, 
						 _image ) ;

		//gl.generateMipmap( GL3.TEXTURE_2D ) ;
		gl.bindTexture( GL3.TEXTURE_2D, null ) ;			// Reset to default texture

		return new Texture( new GLImage( textureID, width, height ) ) ;
	}

	/**
		Returns a ByteBuffer of BufferedImage data.
		Ensure BufferedImage is of 4BYTE_ABGR type.
		If imageFormat is set to RGBA, byte stream will be converted.
	*/
	private ByteBuffer getByteBuffer( final BufferedImage _image )
	{
		final DataBuffer buffer = _image.getRaster().getDataBuffer() ;
		final int type = buffer.getDataType() ;

		if( type == DataBuffer.TYPE_BYTE )
		{
			final byte[] data = ( (  DataBufferByte )  buffer).getData() ;
			if( imageFormat == GL3.RGBA )
			{
				convertABGRtoRGBA( data ) ;
			}

			return ByteBuffer.wrap( data ) ;
		}

		System.out.println( "Failed to determine DataBuffer type." ) ;
		return null ;
	}

	private byte[] convertABGRtoRGBA( final byte[] _data )
	{
		byte alpha, red, green, blue ;
		final int size = _data.length ;
		for( int i = 0; i < size; i += 4 )
		{
			red   = _data[i + 3] ;
			green = _data[i + 2] ;
			blue  = _data[i + 1] ;
			alpha = _data[i] ;

			_data[i]     = red ;
			_data[i + 1] = green ;
			_data[i + 2] = blue ;
			_data[i + 3] = alpha ;
		}

		return _data ;
	}

	private WebGLTexture glGenTextures( final WebGLRenderingContext _gl )
	{
		return _gl.createTexture() ;
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
		private final HashMap<String, MalletTexture.Meta> imageMetas = new HashMap<String, MalletTexture.Meta>() ;

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
				MalletTexture.Meta meta = imageMetas.get( _path ) ;
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
