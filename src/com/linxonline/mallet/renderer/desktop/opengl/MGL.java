package com.linxonline.mallet.renderer.desktop.opengl ;

import java.nio.* ;
import com.jogamp.opengl.GL ;
import com.jogamp.opengl.GL2 ;
import com.jogamp.opengl.GL3 ;

public final class MGL
{
	private static GL3 gl ;

	public final static int GL_MAX_TEXTURE_SIZE = GL3.GL_MAX_TEXTURE_SIZE ;
	public final static int GL_TEXTURE_WRAP_S = GL3.GL_TEXTURE_WRAP_S ;
	public final static int GL_TEXTURE_WRAP_T = GL3.GL_TEXTURE_WRAP_T ;
	public final static int GL_TEXTURE_MAG_FILTER = GL3.GL_TEXTURE_MAG_FILTER ;
	public final static int GL_TEXTURE_MIN_FILTER = GL3.GL_TEXTURE_MIN_FILTER ;
	public final static int GL_CLAMP_TO_EDGE = GL3.GL_CLAMP_TO_EDGE ;
	public final static int GL_REPEAT = GL3.GL_REPEAT ;
	public final static int GL_LINEAR = GL3.GL_LINEAR ;
	public final static int GL_NEAREST = GL3.GL_NEAREST ;

	public final static int GL_ABGR_EXT = GL2.GL_ABGR_EXT ;
	public final static int GL_COMPRESSED_RGBA = GL3.GL_COMPRESSED_RGBA ;
	public final static int GL_COMPRESSED_RGB = GL3.GL_COMPRESSED_RGB ;
	public final static int GL_RGBA = GL3.GL_RGBA ;
	public final static int GL_RGB = GL3.GL_RGB ;
	public final static int GL_BGR = GL3.GL_BGR ;
	public final static int GL_RED = GL3.GL_RED ;

	public final static int GL_UNPACK_ALIGNMENT = GL3.GL_UNPACK_ALIGNMENT ;

	public final static int GL_LINES = GL3.GL_LINES ;
	public final static int GL_LINE_STRIP = GL3.GL_LINE_STRIP ;
	public final static int GL_TRIANGLES = GL3.GL_TRIANGLES ;

	public final static int GL_TEXTURE_2D = GL.GL_TEXTURE_2D ;
	public final static int GL_PRIMITIVE_RESTART = GL3.GL_PRIMITIVE_RESTART ;

	public final static int GL_NO_ERROR = GL3.GL_NO_ERROR ;
	public final static int GL_INVALID_ENUM = GL3.GL_INVALID_ENUM ;
	public final static int GL_INVALID_VALUE = GL3.GL_INVALID_VALUE ;
	public final static int GL_INVALID_OPERATION = GL3.GL_INVALID_OPERATION ;
	public final static int GL_INVALID_FRAMEBUFFER_OPERATION = GL3.GL_INVALID_FRAMEBUFFER_OPERATION ;
	public final static int GL_OUT_OF_MEMORY = GL3.GL_OUT_OF_MEMORY ;
	public final static int GL_STACK_UNDERFLOW = GL3.GL_STACK_UNDERFLOW ;
	public final static int GL_STACK_OVERFLOW = GL3.GL_STACK_OVERFLOW ;

	public final static int GL_ELEMENT_ARRAY_BUFFER = GL3.GL_ELEMENT_ARRAY_BUFFER ;
	public final static int GL_ARRAY_BUFFER = GL3.GL_ARRAY_BUFFER ;

	public final static int GL_STENCIL_TEST = GL3.GL_STENCIL_TEST ;
	public final static int GL_STENCIL_BUFFER_BIT = GL3.GL_STENCIL_BUFFER_BIT ;
	public final static int GL_COLOR_BUFFER_BIT = GL3.GL_COLOR_BUFFER_BIT ;
	public final static int GL_DEPTH_BUFFER_BIT = GL3.GL_DEPTH_BUFFER_BIT ;

	public final static int GL_FLOAT = GL3.GL_FLOAT ;
	public final static int GL_UNSIGNED_BYTE = GL3.GL_UNSIGNED_BYTE ;
	public final static int GL_UNSIGNED_INT = GL3.GL_UNSIGNED_INT ;

	public final static int GL_ALWAYS = GL3.GL_ALWAYS ;
	public final static int GL_KEEP = GL3.GL_KEEP ;
	public final static int GL_REPLACE = GL3.GL_REPLACE ;
	public final static int GL_EQUAL = GL3.GL_EQUAL ;

	public final static int GL_BLEND = GL3.GL_BLEND ;
	public final static int GL_SRC_ALPHA = GL3.GL_SRC_ALPHA ;
	public final static int GL_ONE_MINUS_SRC_ALPHA = GL3.GL_ONE_MINUS_SRC_ALPHA ;

	public final static int GL_CULL_FACE = GL3.GL_CULL_FACE ;
	public final static int GL_BACK = GL3.GL_BACK ;
	public final static int GL_CCW = GL3.GL_CCW ;

	public final static int GL_COMPILE_STATUS = GL3.GL_COMPILE_STATUS ;
	public final static int GL_INFO_LOG_LENGTH = GL3.GL_INFO_LOG_LENGTH ;
	public final static int GL_LINK_STATUS = GL3.GL_LINK_STATUS ;

	public final static int GL_TRUE = GL3.GL_TRUE ;
	public final static int GL_FALSE = GL3.GL_FALSE ;

	public final static int GL_DYNAMIC_DRAW = GL3.GL_DYNAMIC_DRAW ;
	public final static int GL_STREAM_DRAW = GL3.GL_STREAM_DRAW ;

	public final static int GL_STENCIL_INDEX8 = GL3.GL_STENCIL_INDEX8 ;
	public final static int GL_RENDERBUFFER = GL3.GL_RENDERBUFFER ;
	
	public final static int GL_FRAMEBUFFER = GL3.GL_FRAMEBUFFER ;
	public final static int GL_READ_FRAMEBUFFER = GL3.GL_READ_FRAMEBUFFER ;
	public final static int GL_DRAW_FRAMEBUFFER = GL3.GL_DRAW_FRAMEBUFFER ;
	public final static int GL_FRAMEBUFFER_COMPLETE = GL3.GL_FRAMEBUFFER_COMPLETE ;
	public final static int GL_FRAMEBUFFER_UNDEFINED = GL3.GL_FRAMEBUFFER_UNDEFINED ;
	public final static int GL_FRAMEBUFFER_UNSUPPORTED = GL3.GL_FRAMEBUFFER_UNSUPPORTED ;

	public final static int GL_TEXTURE0 = GL3.GL_TEXTURE0 ;
	
	public final static int GL_COLOR_ATTACHMENT0 = GL3.GL_COLOR_ATTACHMENT0 ;
	public final static int GL_STENCIL_ATTACHMENT = GL3.GL_STENCIL_ATTACHMENT ;
	public final static int GL_DEPTH_ATTACHMENT = GL3.GL_DEPTH_ATTACHMENT ;

	public final static int GL_VERTEX_SHADER = GL3.GL_VERTEX_SHADER ;
	public final static int GL_GEOMETRY_SHADER = GL3.GL_GEOMETRY_SHADER ;
	public final static int GL_FRAGMENT_SHADER = GL3.GL_FRAGMENT_SHADER ;
	public final static int GL_COMPUTE_SHADER = GL3.GL_COMPUTE_SHADER ;
	
	public final static int GL_SHADER_STORAGE_BLOCK = GL3.GL_SHADER_STORAGE_BLOCK ;

	public MGL() {}

	public static void setGL( final GL3 _gl )
	{
		gl = _gl ;
	}

	public static void setSwapInterval( final int _interval )
	{
		gl.setSwapInterval( _interval ) ;
	}

	public static void glEnable( final int _flag )
	{
		gl.glEnable( _flag ) ;
	}

	public static void glDisable( final int _flag )
	{
		gl.glDisable( _flag ) ;
	}

	public static void glPrimitiveRestartIndex( final int _index )
	{
		gl.glPrimitiveRestartIndex( _index ) ;
	}

	public static void glBlendFunc( final int _a, final int _b )
	{
		gl.glBlendFunc( _a, _b ) ;
	}

	public static void glCullFace( final int _face )
	{
		gl.glCullFace( _face ) ;
	}

	public static void glFrontFace( final int _face )
	{
		gl.glFrontFace( _face ) ;
	}

	public static void glGetIntegerv( final int _pname, final int[] _params, final int _offset )
	{
		gl.glGetIntegerv( _pname, _params, _offset ) ;
	}

	public static void glUniformMatrix4fv( final int _location, final int _count, final boolean _transpose, final float[] _value, final int _offset )
	{
		gl.glUniformMatrix4fv( _location, _count, _transpose, _value, _offset ) ;
	}

	public static void glViewport( final int _x, final int _y, final int _width, final int _height )
	{
		gl.glViewport( _x, _y, _width, _height ) ;
	}

	public static void glBufferSubData( final int _target, final long _offset, final long _size, final Buffer _data )
	{
		gl.glBufferSubData( _target, _offset, _size, _data ) ;
	}

	public static void glBindBuffer( final int _target, final int _buffer )
	{
		gl.glBindBuffer( _target, _buffer ) ;
	}

	public static void glDrawElements( final int _mode, final int _count, final int _type, final long _offset )
	{
		gl.glDrawElements( _mode, _count, _type, _offset ) ;
	}

	public static void glStencilFunc( final int _func, final int _ref, final int _mask )
	{
		gl.glStencilFunc( _func, _ref, _mask ) ;
	}

	public static void glStencilOp( final int _fail, final int _zFail, final int _zPass )
	{
		gl.glStencilOp( _fail, _zFail, _zPass ) ;
	}

	public static void glStencilMask( final int _mask )
	{
		gl.glStencilMask( _mask ) ;
	}

	public static void glColorMask( final boolean _red, final boolean _green, final boolean _blue, final boolean _alpha )
	{
		gl.glColorMask( _red, _green, _blue, _alpha ) ;
	}

	public static void glDepthMask( final boolean _mask )
	{
		gl.glDepthMask( _mask ) ;
	}

	public static void glClear( final int _target )
	{
		gl.glClear( _target ) ;
	}

	public static void glEnableVertexAttribArray( final int _index )
	{
		gl.glEnableVertexAttribArray( _index ) ;
	}

	public static void glVertexAttribPointer( final int _index, final int _size, final int _type, final boolean _normalised, final int _stride, final int _offset )
	{
		gl.glVertexAttribPointer( _index, _size, _type, _normalised, _stride, _offset ) ;
	}

	public static void glDisableVertexAttribArray( final int _index )
	{
		gl.glDisableVertexAttribArray( _index ) ;
	}

	public static void glActiveTexture( final int _texture )
	{
		gl.glActiveTexture( _texture ) ;
	}

	public static void glBindTexture( final int _target, final int _texture )
	{
		gl.glBindTexture( _target, _texture ) ;
	}

	public static void glBufferData( final int _target, final long _size, final Buffer _data, final int _usage )
	{
		gl.glBufferData( _target, _size, _data, _usage ) ;
	}

	public static void glUseProgram( final int _program )
	{
		gl.glUseProgram( _program ) ;
	}

	public static int glCreateProgram()
	{
		return gl.glCreateProgram() ;
	}

	public static void glDeleteProgram( final int _program )
	{
		gl.glDeleteProgram( _program ) ;
	}

	public static int glCreateShader( final int _type )
	{
		return gl.glCreateShader( _type ) ;
	}

	public static void glShaderSource( final int _shader, final int _count, final String[] _string, IntBuffer _length )
	{
		gl.glShaderSource( _shader, _count, _string, _length ) ;
	}

	public static void glCompileShader( final int _shader )
	{
		gl.glCompileShader( _shader ) ;
	}

	public static void glGetShaderiv( final int _shader, final int _name, final int[] _params, final int _offset )
	{
		gl.glGetShaderiv( _shader, _name, _params, _offset ) ;
	}

	public static void glGetShaderInfoLog( final int _shader, final int bufSize, final int[] _length, final int _lengthOffset, final byte[] _log, final int _logOffset )
	{
		gl.glGetShaderInfoLog( _shader, bufSize, _length, _lengthOffset, _log, _logOffset ) ;
	}

	public static void glGetProgramiv( final int _program, final int _name, final int[] _params, final int _offset )
	{
		gl.glGetProgramiv( _program, _name, _params, _offset ) ;
	}

	public static void glGetProgramInfoLog( final int _program, final int bufSize, final int[] _length, final int _lengthOffset, final byte[] _log, final int _logOffset )
	{
		gl.glGetProgramInfoLog( _program, bufSize, _length, _lengthOffset, _log, _logOffset ) ;
	}

	public static void glAttachShader( final int _program, final int _shader )
	{
		gl.glAttachShader( _program, _shader ) ;
	}

	public static void glGenBuffers( final int _num, final int[] _buffers, final int _offset )
	{
		gl.glGenBuffers( _num, _buffers, _offset ) ;
	}

	public static void glDeleteBuffers( final int _num, final int[] _buffers, final int _offset )
	{
		gl.glDeleteBuffers( _num, _buffers, _offset ) ;
	}

	public static void glTexParameteri( final int _target, final int _name, final int _param )
	{
		gl.glTexParameteri( _target, _name, _param ) ;
	}

	public static boolean isExtensionAvailable( final String _ext )
	{
		return gl.isExtensionAvailable( _ext ) ;
	}

	public static void glDeleteTextures( final int _size, final int[] _buffers, final int _offset )
	{
		gl.glDeleteTextures( _size, _buffers, _offset ) ;
	}

	public static void glBindAttribLocation( final int _program, final int _index, final String _name )
	{
		gl.glBindAttribLocation( _program, _index, _name ) ;
	}

	public static void glLinkProgram( final int _program )
	{
		gl.glLinkProgram( _program ) ;
	}

	public static int glGetUniformLocation( final int _program, final String _name )
	{
		return gl.glGetUniformLocation( _program, _name ) ;
	}

	public static void glDetachShader( final int _program, final int _shader )
	{
		gl.glDetachShader( _program, _shader ) ;
	}

	public static void glDeleteShader( final int _shader )
	{
		gl.glDeleteShader( _shader ) ;
	}

	public static void glBindFramebuffer( final int _target, final int _framebuffer )
	{
		gl.glBindFramebuffer( _target, _framebuffer ) ;
	}

	public static void glCopyTexImage2D( final int _target, final int _level, final int _internalFormat, final int _x, final int _y, final int _width, final int _height, final int _border )
	{
		gl.glCopyTexImage2D( _target, _level, _internalFormat, _x, _y, _width, _height, _border ) ;
	}

	public static void glGenTextures( final int _num, final int[] _params, final int _offset )
	{
		gl.glGenTextures( _num, _params, _offset ) ;
	}

	public static void glGenRenderbuffers( final int _num, final int[] _params, final int _offset )
	{
		gl.glGenRenderbuffers( _num, _params, _offset ) ;
	}

	public static void glFramebufferTexture2D( final int _target, final int _attachment, final int _texTarget, final int _texture, final int _level )
	{
		gl.glFramebufferTexture2D( _target, _attachment, _texTarget, _texture, _level ) ;
	}

	public static void glFramebufferRenderbuffer( final int _target, final int _attachment, final int _renTarget, final int _render )
	{
		gl.glFramebufferRenderbuffer( _target, _attachment, _renTarget, _render ) ;
	}

	public static int glCheckFramebufferStatus( final int _target )
	{
		return gl.glCheckFramebufferStatus( _target ) ;
	}

	public static void glPixelStorei( final int _target, final int _align )
	{
		gl.glPixelStorei( _target, _align ) ;
	}

	public static void glTexImage2D( final int _target, final int _level, final int _internalFormat, final int _width, final int _height, final int _border, final int _format, final int _type, final Buffer _pixels )
	{
		gl.glTexImage2D( _target, _level, _internalFormat, _width, _height, _border, _format, _type, _pixels ) ;
	}

	public static void glGenerateMipmap( final int _target )
	{
		gl.glGenerateMipmap( _target ) ;
	}

	public static void glGenFramebuffers( final int _num, final int[] _params, final int _offset )
	{
		gl.glGenFramebuffers( _num, _params, _offset ) ;
	}

	public static void glDeleteFramebuffers( final int _num, final int[] _params, final int _offset )
	{
		gl.glDeleteFramebuffers( _num, _params, _offset ) ;
	}

	public static void glDeleteRenderbuffers( final int _num, final int[] _params, final int _offset )
	{
		gl.glDeleteRenderbuffers( _num, _params, _offset ) ;
	}

	public static void glBindRenderbuffer( final int _target, final int _renderBuffer )
	{
		gl.glBindRenderbuffer( _target, _renderBuffer ) ;
	}

	public static void glRenderbufferStorage( final int _target, final int _internalFormat, final int _width, final int _height )
	{
		gl.glRenderbufferStorage( _target, _internalFormat, _width, _height ) ;
	}

	public static void glBlitFramebuffer( final int _srcX0, final int _srcY0, final int _srcX1, final int _srcY1,
										  final int _dstX0, final int _dstY0, final int _dstX1, final int _dstY1, final int _mask, final int _filter )
	{
		gl.glBlitFramebuffer( _srcX0, _srcY0, _srcX1, _srcY1,
							  _dstX0, _dstY0, _dstX1, _dstY1, _mask, _filter ) ;
	}

	public static int glGetProgramResourceIndex( int _program, int _programInterface, String _name )
	{
		final byte[] name = _name.getBytes() ;
		return gl.glGetProgramResourceIndex( _program, _programInterface, name, 0 ) ;
	}

	public static int glGetError()
	{
		return gl.glGetError() ;
	}
}
