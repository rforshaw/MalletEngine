package com.linxonline.mallet.renderer.web.gl ;

import org.teavm.jso.webgl.WebGLRenderingContext ;
import org.teavm.jso.webgl.WebGLUniformLocation ;
import org.teavm.jso.webgl.WebGLRenderbuffer ;
import org.teavm.jso.webgl.WebGLFramebuffer ;
import org.teavm.jso.webgl.WebGLProgram ;
import org.teavm.jso.webgl.WebGLTexture ;
import org.teavm.jso.webgl.WebGLBuffer ;
import org.teavm.jso.webgl.WebGLShader ;

import org.teavm.jso.typedarrays.ArrayBuffer ;
import org.teavm.jso.typedarrays.ArrayBufferView ;
import org.teavm.jso.typedarrays.Int16Array ;
import org.teavm.jso.typedarrays.Float32Array ;
import org.teavm.jso.typedarrays.Uint8Array ;

import org.teavm.jso.dom.html.HTMLImageElement ;
import org.teavm.jso.dom.html.HTMLCanvasElement ;

import com.linxonline.mallet.util.buffers.FloatBuffer ;

public final class MGL
{
	private static WebGLRenderingContext gl ;
	private final static float[] matrix = FloatBuffer.allocate( 16 ) ;

	public final static int GL_MAX_TEXTURE_SIZE = WebGLRenderingContext.MAX_TEXTURE_SIZE ;
	public final static int GL_TEXTURE_WRAP_S = WebGLRenderingContext.TEXTURE_WRAP_S ;
	public final static int GL_TEXTURE_WRAP_T = WebGLRenderingContext.TEXTURE_WRAP_T ;
	public final static int GL_TEXTURE_MAG_FILTER = WebGLRenderingContext.TEXTURE_MAG_FILTER ;
	public final static int GL_TEXTURE_MIN_FILTER = WebGLRenderingContext.TEXTURE_MIN_FILTER ;
	public final static int GL_CLAMP_TO_EDGE = WebGLRenderingContext.CLAMP_TO_EDGE ;
	public final static int GL_REPEAT = WebGLRenderingContext.REPEAT ;
	public final static int GL_LINEAR = WebGLRenderingContext.LINEAR ;
	public final static int GL_NEAREST = WebGLRenderingContext.NEAREST ;
	public final static int GL_NEAREST_MIPMAP_NEAREST = WebGLRenderingContext.NEAREST_MIPMAP_NEAREST ;
	public final static int GL_LINEAR_MIPMAP_LINEAR = WebGLRenderingContext.LINEAR_MIPMAP_LINEAR ;

	//public final static int GL_ABGR_EXT = WebGLRenderingContext.ABGR_EXT ;
	//public final static int GL_COMPRESSED_RGBA = WebGLRenderingContext.COMPRESSED_RGBA ;
	//public final static int GL_COMPRESSED_RGB = WebGLRenderingContext.COMPRESSED_RGB ;
	public final static int GL_RGBA = WebGLRenderingContext.RGBA ;
	public final static int GL_RGB = WebGLRenderingContext.RGB ;
	//public final static int GL_BGR = WebGLRenderingContext.BGR ;
	//public final static int GL_RED = WebGLRenderingContext.RED ;
	public final static int GL_DEPTH_COMPONENT = WebGLRenderingContext.DEPTH_COMPONENT ;
	public final static int GL_DEPTH_STENCIL = WebGLRenderingContext.DEPTH_STENCIL ;

	public final static int GL_UNPACK_ALIGNMENT = WebGLRenderingContext.UNPACK_ALIGNMENT ;

	public final static int GL_LINES = WebGLRenderingContext.LINES ;
	public final static int GL_LINE_STRIP = WebGLRenderingContext.LINE_STRIP ;
	public final static int GL_TRIANGLES = WebGLRenderingContext.TRIANGLES ;

	public final static int GL_TEXTURE_2D = WebGLRenderingContext.TEXTURE_2D ;
	//public final static int GL_PRIMITIVE_RESTART = WebGLRenderingContext.PRIMITIVE_RESTART ;

	public final static int GL_NO_ERROR = WebGLRenderingContext.NO_ERROR ;
	public final static int GL_INVALID_ENUM = WebGLRenderingContext.INVALID_ENUM ;
	public final static int GL_INVALID_VALUE = WebGLRenderingContext.INVALID_VALUE ;
	public final static int GL_INVALID_OPERATION = WebGLRenderingContext.INVALID_OPERATION ;
	public final static int GL_INVALID_FRAMEBUFFER_OPERATION = WebGLRenderingContext.INVALID_FRAMEBUFFER_OPERATION ;
	public final static int GL_OUT_OF_MEMORY = WebGLRenderingContext.OUT_OF_MEMORY ;
	//public final static int GL_STACK_UNDERFLOW = WebGLRenderingContext.STACK_UNDERFLOW ;
	//public final static int GL_STACK_OVERFLOW = WebGLRenderingContext.STACK_OVERFLOW ;

	public final static int GL_ELEMENT_ARRAY_BUFFER = WebGLRenderingContext.ELEMENT_ARRAY_BUFFER ;
	public final static int GL_ARRAY_BUFFER = WebGLRenderingContext.ARRAY_BUFFER ;

	public final static int GL_STENCIL_TEST = WebGLRenderingContext.STENCIL_TEST ;
	public final static int GL_STENCIL_BUFFER_BIT = WebGLRenderingContext.STENCIL_BUFFER_BIT ;
	public final static int GL_COLOR_BUFFER_BIT = WebGLRenderingContext.COLOR_BUFFER_BIT ;
	public final static int GL_DEPTH_TEST = WebGLRenderingContext.DEPTH_TEST ;
	public final static int GL_DEPTH_BUFFER_BIT = WebGLRenderingContext.DEPTH_BUFFER_BIT ;

	public final static int GL_FLOAT = WebGLRenderingContext.FLOAT ;
	public final static int GL_UNSIGNED_BYTE = WebGLRenderingContext.UNSIGNED_BYTE ;
	public final static int GL_UNSIGNED_INT = WebGLRenderingContext.UNSIGNED_INT ;
	public final static int GL_UNSIGNED_SHORT = WebGLRenderingContext.UNSIGNED_SHORT ;

	public final static int GL_ALWAYS = WebGLRenderingContext.ALWAYS ;
	public final static int GL_KEEP = WebGLRenderingContext.KEEP ;
	public final static int GL_REPLACE = WebGLRenderingContext.REPLACE ;
	public final static int GL_EQUAL = WebGLRenderingContext.EQUAL ;

	public final static int GL_BLEND = WebGLRenderingContext.BLEND ;
	public final static int GL_SRC_ALPHA = WebGLRenderingContext.SRC_ALPHA ;
	public final static int GL_ONE_MINUS_SRC_ALPHA = WebGLRenderingContext.ONE_MINUS_SRC_ALPHA ;

	public final static int GL_CULL_FACE = WebGLRenderingContext.CULL_FACE ;
	public final static int GL_BACK = WebGLRenderingContext.BACK ;
	public final static int GL_FRONT_AND_BACK = WebGLRenderingContext.FRONT_AND_BACK ;
	public final static int GL_CCW = WebGLRenderingContext.CCW ;
	public final static int GL_CW = WebGLRenderingContext.CW ;

	public final static int GL_COMPILE_STATUS = WebGLRenderingContext.COMPILE_STATUS ;
	public final static int GL_LINK_STATUS = WebGLRenderingContext.LINK_STATUS ;

	//public final static int GL_DYNAMIC_COPY = WebGLRenderingContext.DYNAMIC_COPY ;
	public final static int GL_DYNAMIC_DRAW = WebGLRenderingContext.DYNAMIC_DRAW ;
	public final static int GL_STREAM_DRAW = WebGLRenderingContext.STREAM_DRAW ;

	public final static int GL_STENCIL_INDEX8 = WebGLRenderingContext.STENCIL_INDEX8 ;
	public final static int GL_RENDERBUFFER = WebGLRenderingContext.RENDERBUFFER ;
	
	public final static int GL_FRAMEBUFFER = WebGLRenderingContext.FRAMEBUFFER ;
	//public final static int GL_READ_FRAMEBUFFER = WebGLRenderingContext.READ_FRAMEBUFFER ;
	//public final static int GL_DRAW_FRAMEBUFFER = WebGLRenderingContext.DRAW_FRAMEBUFFER ;
	public final static int GL_FRAMEBUFFER_COMPLETE = WebGLRenderingContext.FRAMEBUFFER_COMPLETE ;
	public final static int GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT = WebGLRenderingContext.FRAMEBUFFER_INCOMPLETE_ATTACHMENT ;
	public final static int GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT  = WebGLRenderingContext.FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT  ;
	public final static int GL_FRAMEBUFFER_INCOMPLETE_DIMENSIONS = WebGLRenderingContext.FRAMEBUFFER_INCOMPLETE_DIMENSIONS ;
	public final static int GL_FRAMEBUFFER_UNSUPPORTED = WebGLRenderingContext.FRAMEBUFFER_UNSUPPORTED ;

	public final static int GL_TEXTURE0 = WebGLRenderingContext.TEXTURE0 ;
	
	public final static int GL_COLOR_ATTACHMENT0 = WebGLRenderingContext.COLOR_ATTACHMENT0 ;
	public final static int GL_STENCIL_ATTACHMENT = WebGLRenderingContext.STENCIL_ATTACHMENT ;
	public final static int GL_DEPTH_ATTACHMENT = WebGLRenderingContext.DEPTH_ATTACHMENT ;

	public final static int GL_VERTEX_SHADER = WebGLRenderingContext.VERTEX_SHADER ;
	//public final static int GL_GEOMETRY_SHADER = WebGLRenderingContext.GEOMETRY_SHADER ;
	public final static int GL_FRAGMENT_SHADER = WebGLRenderingContext.FRAGMENT_SHADER ;
	//public final static int GL_COMPUTE_SHADER = WebGLRenderingContext.COMPUTE_SHADER ;

	public MGL() {}

	public static void setGL( final WebGLRenderingContext _gl )
	{
		gl = _gl ;
	}

	public static void bindBuffer( final int _target, final WebGLBuffer _buffer )
	{
		gl.bindBuffer( _target, _buffer ) ;
	}

	public static void bufferData( final int _target, final ArrayBufferView _data, final int _usage )
	{
		gl.bufferData( _target, _data, _usage ) ;
	}

	public static void bufferData( final int _target, final int _size, final int _usage )
	{
		gl.bufferData( _target, _size, _usage ) ;
	}

	public static void bufferSubData( final int _target, final int _offset, final ArrayBufferView _data )
	{
		gl.bufferSubData( _target, _offset, _data ) ;
	}
	
	public static void bufferSubData( final int _target, final int _offset, final ArrayBuffer _data )
	{
		gl.bufferSubData( _target, _offset, _data ) ;
	}

	public static void enable( final int _cap )
	{
		gl.enable( _cap ) ;
	}
	
	public static void disable( final int _cap )
	{
		gl.disable( _cap ) ;
	}
	
	public static void clear( final int _mask )
	{
		gl.clear( _mask ) ;
	}
	
	public static void drawElements( final int _mode, final int _count, final int _type, final int _offset )
	{
		gl.drawElements( _mode, _count, _type, _offset ) ;
	}
	
	public static void useProgram( final WebGLProgram _program )
	{
		gl.useProgram( _program ) ;
	}

	public static void uniform1i( final WebGLUniformLocation _location, final int _value )
	{
		gl.uniform1i( _location, _value ) ;
	}

	public static void uniform2i( final WebGLUniformLocation _location, final int _v0, final int _v1 )
	{
		gl.uniform2i( _location, _v0, _v1 ) ;
	}

	public static void uniform3i( final WebGLUniformLocation _location, final int _v0, final int _v1, final int _v2 )
	{
		gl.uniform3i( _location, _v0, _v1, _v2 ) ;
	}

	public static void uniform4i( final WebGLUniformLocation _location, final int _v0, final int _v1, final int _v2, final int _v3 )
	{
		gl.uniform4i( _location, _v0, _v1, _v2, _v3 ) ;
	}

	public static void uniform1ui( final WebGLUniformLocation _location, final int _value )
	{
		gl.uniform1i( _location, _value ) ;
	}

	public static void uniform2ui( final WebGLUniformLocation _location, final int _v0, final int _v1 )
	{
		gl.uniform2i( _location, _v0, _v1 ) ;
	}

	public static void uniform3ui( final WebGLUniformLocation _location, final int _v0, final int _v1, final int _v2 )
	{
		gl.uniform3i( _location, _v0, _v1, _v2 ) ;
	}

	public static void uniform4ui( final WebGLUniformLocation _location, final int _v0, final int _v1, final int _v2, final int _v3 )
	{
		gl.uniform4i( _location, _v0, _v1, _v2, _v3 ) ;
	}

	public static void uniform1f( final WebGLUniformLocation _location, final float _v0 )
	{
		gl.uniform1f( _location, _v0 ) ;
	}

	public static void uniform2f( final WebGLUniformLocation _location, final float _v0, final float _v1 )
	{
		gl.uniform2f( _location, _v0, _v1 ) ;
	}

	public static void uniform3f( final WebGLUniformLocation _location, final float _v0, final float _v1, final float _v2 )
	{
		gl.uniform3f( _location, _v0, _v1, _v2 ) ;
	}

	public static void uniform4f( final WebGLUniformLocation _location, final float _v0, final float _v1, final float _v2, final float _v3 )
	{
		gl.uniform4f( _location, _v0, _v1, _v2, _v3 ) ;
	}

	public static void uniformMatrix4fv( final WebGLUniformLocation _location, final boolean _transpose, final float[] _values )
	{
		if( _transpose )
		{
			transpose( _values ) ;
		}

		gl.uniformMatrix4fv( _location, false, matrix ) ;
	}

	private static void transpose( final float[] _source )
	{
		FloatBuffer.copy( _source, matrix ) ;
		FloatBuffer.swap( matrix, 1, 4 ) ;
		FloatBuffer.swap( matrix, 2, 8 ) ;
		FloatBuffer.swap( matrix, 3, 12 ) ;
		FloatBuffer.swap( matrix, 6, 9 ) ;
		FloatBuffer.swap( matrix, 7, 13 ) ;
		FloatBuffer.swap( matrix, 11, 14 ) ;
	}

	public static void stencilFunc( final int _func, final int _ref, final int _mask )
	{
		gl.stencilFunc( _func, _ref, _mask ) ;
	}
	
	public static void stencilOp( final int _fail, final int _zfail, final int _zpass )
	{
		gl.stencilOp( _fail, _zfail, _zpass ) ;
	}
	
	public static void stencilMask( final int _mask )
	{
		gl.stencilMask( _mask ) ;
	}
	
	public static void colorMask( final boolean _red, final boolean _green, final boolean _blue, final boolean _alpha )
	{
		gl.colorMask( _red, _green, _blue, _alpha ) ;
	}
	
	public static void depthMask( final boolean _flag )
	{
		gl.depthMask( _flag ) ;
	}
	
	public static void enableVertexAttribArray( final int _index )
	{
		gl.enableVertexAttribArray( _index ) ;
	}
	
	public static void disableVertexAttribArray( final int _index )
	{
		gl.disableVertexAttribArray( _index ) ;
	}
	
	public static void vertexAttribPointer( final int _index, final int _size, final int _type, final boolean _normalized, final int _stride, final int _offset )
	{
		gl.vertexAttribPointer( _index, _size, _type, _normalized, _stride, _offset ) ;
	}

	public static WebGLTexture createTexture()
	{
		return gl.createTexture() ; 
	}

	public static void activeTexture( final int _texture )
	{
		gl.activeTexture( _texture ) ;
	}

	public static void bindTexture( final int _target, final WebGLTexture _texture )
	{
		gl.bindTexture( _target, _texture ) ;
	}

	public static void copyTexImage2D( final int _target, final int _level, final int _internalformat, final int _x, final int _y, final int _width, final int _height, final int _border)
	{
		gl.copyTexImage2D( _target, _level, _internalformat, _x, _y, _width, _height, _border ) ;
	}

	public static void texParameteri( final int _target, final int _pname, final int _param )
	{
		gl.texParameteri( _target, _pname, _param ) ;
	}

	public static WebGLBuffer createBuffer()
	{
		return gl.createBuffer() ;
	}
	
	public static WebGLFramebuffer createFramebuffer()
	{
		return gl.createFramebuffer() ;
	}

	public static WebGLRenderbuffer createRenderbuffer()
	{
		return gl.createRenderbuffer() ;
	}

	public static WebGLProgram createProgram()
	{
		return gl.createProgram() ;
	}

	public static void deleteBuffer( final WebGLBuffer _buffer )
	{
		gl.deleteBuffer( _buffer ) ;
	}

	public static void deleteProgram( final WebGLProgram _program )
	{
		gl.deleteProgram( _program ) ;
	}

	public static void deleteFramebuffer( final WebGLFramebuffer _frameBuffer )
	{
		gl.deleteFramebuffer( _frameBuffer ) ;
	}

	public static void deleteRenderbuffer( final WebGLRenderbuffer _renderBuffer )
	{
		gl.deleteRenderbuffer( _renderBuffer ) ;
	}

	public static void attachShader( final WebGLProgram _program, final WebGLShader _shader )
	{
		gl.attachShader( _program, _shader ) ;
	}

	public static void bindAttribLocation( final WebGLProgram _program, final int _index, final String _name )
	{
		gl.bindAttribLocation( _program, _index, _name ) ;
	}
	
	public static void linkProgram( final WebGLProgram _program )
	{
		gl.linkProgram( _program ) ;
	}

	public static WebGLUniformLocation getUniformLocation( final WebGLProgram _program, final String _name )
	{
		return gl.getUniformLocation( _program, _name ) ;
	}

	public static void detachShader( final WebGLProgram _program, final WebGLShader _shader )
	{
		gl.detachShader( _program, _shader ) ;
	}

	public static void deleteShader( final WebGLShader _shader )
	{
		gl.deleteShader( _shader ) ;
	}

	public static boolean getProgramParameterb( final WebGLProgram _program, final int _pname )
	{
		return gl.getProgramParameterb( _program, _pname ) ;
	}

	public static String getProgramInfoLog( final WebGLProgram _program )
	{
		return gl.getProgramInfoLog( _program ) ;
	}

	public static WebGLShader createShader( final int _type )
	{
		return gl.createShader( _type ) ;
	}

	public static void shaderSource( final WebGLShader _shader, final String _source )
	{
		gl.shaderSource( _shader, _source ) ;
	}

	public static void compileShader( final WebGLShader _shader )
	{
		gl.compileShader( _shader ) ;
	}

	public static boolean getShaderParameterb( final WebGLShader _shader, final int _pname )
	{
		return gl.getShaderParameterb( _shader, _pname ) ;
	}

	public static String getShaderInfoLog( final WebGLShader _shader )
	{
		return gl.getShaderInfoLog( _shader ) ;
	}

	public static void deleteTexture( final WebGLTexture _texture )
	{
		gl.deleteTexture( _texture ) ;
	}

	public static void bindFramebuffer( final int _target, final WebGLFramebuffer _framebuffer )
	{
		gl.bindFramebuffer( _target, _framebuffer ) ;
	}

	public static void framebufferTexture2D( final int _target, final int _attachment, final int _textarget, final WebGLTexture _texture, final int _level )
	{
		gl.framebufferTexture2D( _target, _attachment, _textarget, _texture, _level ) ;
	}

	public static void framebufferRenderbuffer( final int _target, final int _attachment, final int _renderBufferTarget, final WebGLRenderbuffer _renderBuffer )
	{
		gl.framebufferRenderbuffer( _target, _attachment, _renderBufferTarget, _renderBuffer ) ;
	}

	public static int checkFramebufferStatus( final int _target )
	{
		return gl.checkFramebufferStatus( _target ) ;
	}

	public static void texImage2D( final int _target, final int _level, final int _internalFormat, final int _width, final int _height, final int _border, final int _format, final int _type, final ArrayBufferView _pixels )
	{
		gl.texImage2D( _target, _level, _internalFormat, _width, _height, _border, _format, _type, _pixels ) ;
	}
	
	public static void texImage2D( final int _target, final int _level, final int _internalFormat, final int _format, final int _type, final HTMLImageElement _image )
	{
		gl.texImage2D( _target, _level, _internalFormat, _format, _type, _image ) ;
	}

	public static void texImage2D( final int _target, final int _level, final int _internalFormat, final int _format, final int _type, final HTMLCanvasElement _canvas )
	{
		gl.texImage2D( _target, _level, _internalFormat, _format, _type, _canvas ) ;
	}
	
	public static void bindRenderbuffer( final int _target, final WebGLRenderbuffer _renderBuffer )
	{
		gl.bindRenderbuffer( _target, _renderBuffer ) ;
	}

	public static void renderbufferStorage( final int _target, final int _internalFormat, final int _width, final int _height )
	{
		gl.renderbufferStorage( _target, _internalFormat, _width, _height ) ;
	}

	public static void viewport( final int _x, final int _y, final int _width, final int _height )
	{
		gl.viewport( _x, _y, _width, _height ) ;
	}

	public static void generateMipmap( final int _target )
	{
		gl.generateMipmap( _target ) ;
	}

	public static void blendFunc( final int _sFactor, final int _dFactor)
	{
		gl.blendFunc( _sFactor, _dFactor ) ;
	}

	public static void cullFace( final int _mode )
	{
		gl.cullFace( _mode ) ;
	}

	public static void frontFace( final int _mode )
	{
		gl.frontFace( _mode ) ;
	}

	public static int getParameteri( final int _pName )
	{
		return gl.getParameteri( _pName ) ;
	}

	public static void clearColor( final float _red, final float _green, final float _blue, final float _alpha )
	{
		gl.clearColor( _red, _green, _blue, _alpha ) ;
	}

	public static void flush()
	{
		gl.flush() ;
	}

	public static void finish()
	{
		gl.finish() ;
	}

	public static int getError()
	{
		return gl.getError() ;
	}
}
