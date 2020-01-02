package com.linxonline.mallet.renderer.android.opengl ;

import java.nio.* ;
import android.opengl.GLES30 ;

public class MGL
{
	public final static int GL_ELEMENT_ARRAY_BUFFER = GLES30.GL_ELEMENT_ARRAY_BUFFER ;
	public final static int GL_ARRAY_BUFFER = GLES30.GL_ARRAY_BUFFER ;

	public final static int GL_STENCIL_TEST= GLES30.GL_STENCIL_TEST ;
	public final static int GL_FLOAT = GLES30.GL_FLOAT ;
	public final static int GL_UNSIGNED_BYTE = GLES30.GL_UNSIGNED_BYTE ;
	public final static int GL_UNSIGNED_SHORT= GLES30.GL_UNSIGNED_SHORT ;
	public final static int GL_LINES = GLES30.GL_LINES ;
	public final static int GL_LINE_STRIP = GLES30.GL_LINE_STRIP ;
	public final static int GL_TRIANGLES = GLES30.GL_TRIANGLES ;
	public final static int GL_PRIMITIVE_RESTART_FIXED_INDEX = GLES30.GL_PRIMITIVE_RESTART_FIXED_INDEX ;
	public final static int GL_BLEND = GLES30.GL_BLEND ;
	public final static int GL_SRC_ALPHA = GLES30.GL_SRC_ALPHA ;
	public final static int GL_ONE_MINUS_SRC_ALPHA = GLES30.GL_ONE_MINUS_SRC_ALPHA ;
	public final static int GL_ALWAYS = GLES30.GL_ALWAYS ;
	public final static int GL_KEEP = GLES30.GL_KEEP ;
	public final static int GL_REPLACE = GLES30.GL_REPLACE ;
	public final static int GL_EQUAL = GLES30.GL_EQUAL ;
	public final static int GL_STENCIL_BUFFER_BIT = GLES30.GL_STENCIL_BUFFER_BIT ;
	public final static int GL_DYNAMIC_DRAW = GLES30.GL_DYNAMIC_DRAW ;
	public final static int GL_MAX_TEXTURE_SIZE = GLES30.GL_MAX_TEXTURE_SIZE ;
	public final static int GL_CULL_FACE = GLES30.GL_CULL_FACE ;
	public final static int GL_BACK = GLES30.GL_BACK ;
	public final static int GL_CCW = GLES30.GL_CCW ;
	public final static int GL_NO_ERROR = GLES30.GL_NO_ERROR ;
	public final static int GL_INVALID_ENUM = GLES30.GL_INVALID_ENUM ;
	public final static int GL_INVALID_VALUE = GLES30.GL_INVALID_VALUE ;
	public final static int GL_INVALID_OPERATION = GLES30.GL_INVALID_OPERATION ;
	public final static int GL_INVALID_FRAMEBUFFER_OPERATION = GLES30.GL_INVALID_FRAMEBUFFER_OPERATION ;
	public final static int GL_OUT_OF_MEMORY = GLES30.GL_OUT_OF_MEMORY ;
	public final static int GL_TEXTURE_2D = GLES30.GL_TEXTURE_2D ;
	public final static int GL_TEXTURE0 = GLES30.GL_TEXTURE0 ;
	public final static int GL_LINK_STATUS = GLES30.GL_LINK_STATUS ;
	public final static int GL_FALSE = GLES30.GL_FALSE ;
	public final static int GL_TRUE = GLES30.GL_TRUE ;
	public final static int GL_COMPILE_STATUS = GLES30.GL_COMPILE_STATUS ;
	public final static int GL_VERTEX_SHADER = GLES30.GL_VERTEX_SHADER ;
	public final static int GL_FRAGMENT_SHADER = GLES30.GL_FRAGMENT_SHADER ;
	public final static int GL_TEXTURE_MIN_FILTER = GLES30.GL_TEXTURE_MIN_FILTER ;
	public final static int GL_TEXTURE_MAG_FILTER = GLES30.GL_TEXTURE_MAG_FILTER ;
	public final static int GL_UNPACK_ALIGNMENT = GLES30.GL_UNPACK_ALIGNMENT ;
	public final static int GL_REPEAT = GLES30.GL_REPEAT ;
	public final static int GL_CLAMP_TO_EDGE = GLES30.GL_CLAMP_TO_EDGE ;
	public final static int GL_LINEAR = GLES30.GL_LINEAR ;
	public final static int GL_NEAREST = GLES30.GL_NEAREST ;
	public final static int GL_TEXTURE_WRAP_T = GLES30.GL_TEXTURE_WRAP_T ;
	public final static int GL_TEXTURE_WRAP_S = GLES30.GL_TEXTURE_WRAP_S ;
	public final static int GL_FRAMEBUFFER = GLES30.GL_FRAMEBUFFER ;
	public final static int GL_RENDERBUFFER = GLES30.GL_RENDERBUFFER ;
	public final static int GL_DRAW_FRAMEBUFFER = GLES30.GL_DRAW_FRAMEBUFFER ;
	public final static int GL_READ_FRAMEBUFFER = GLES30.GL_READ_FRAMEBUFFER ;
	public final static int GL_COLOR_BUFFER_BIT = GLES30.GL_COLOR_BUFFER_BIT ;
	public final static int GL_DEPTH_BUFFER_BIT = GLES30.GL_DEPTH_BUFFER_BIT ;
	public final static int GL_FRAMEBUFFER_COMPLETE = GLES30.GL_FRAMEBUFFER_COMPLETE ;
	public final static int GL_FRAMEBUFFER_UNDEFINED = GLES30.GL_FRAMEBUFFER_UNDEFINED ;
	public final static int GL_FRAMEBUFFER_UNSUPPORTED = GLES30.GL_FRAMEBUFFER_UNSUPPORTED ;
	public final static int GL_RGBA = GLES30.GL_RGBA ;
	public final static int GL_STENCIL_INDEX8 = GLES30.GL_STENCIL_INDEX8 ;
	public final static int GL_COLOR_ATTACHMENT0 = GLES30.GL_COLOR_ATTACHMENT0 ;
	public final static int GL_STENCIL_ATTACHMENT = GLES30.GL_STENCIL_ATTACHMENT ;

	public MGL() {}

	public static void glBufferData( final int _target, final int _size, final Buffer _data, final int _usage )
	{
		GLES30.glBufferData( _target, _size, _data, _usage ) ;
	}

	public static void glBufferSubData( final int _target, final int _offset, final int _size, final Buffer _data )
	{
		GLES30.glBufferSubData( _target, _offset, _size, _data ) ;
	}

	public static void glEnable( final int _cap )
	{
		GLES30.glEnable( _cap ) ;
	}

	public static void glDisable( final int _cap )
	{
		GLES30.glDisable( _cap ) ;
	}

	public static void glUseProgram( final int _program )
	{
		GLES30.glUseProgram( _program ) ;
	}

	public static void glUniformMatrix4fv( final int _location, final int _count, final boolean _transpose, final float[] _value, final int _offset )
	{
		GLES30.glUniformMatrix4fv( _location, _count, _transpose, _value, _offset ) ;
	}

	public static void glBlendFunc( final int _sfactor, final int _dfactor )
	{
		GLES30.glBlendFunc( _sfactor, _dfactor ) ;
	}

	public static void glBindBuffer( final int _target, final int _buffer )
	{
		GLES30.glBindBuffer( _target, _buffer ) ;
	}

	public static void glDrawElements( final int _mode, final int _count, final int _type, final int _offset )
	{
		GLES30.glDrawElements( _mode, _count, _type, _offset ) ;
	}

	public static void glStencilFunc( final int _func, final int _ref, final int _mask )
	{
		GLES30.glStencilFunc( _func, _ref, _mask ) ;
	}

	public static void glStencilOp( final int _fail, final int _zfail, final int _zpass )
	{
		GLES30.glStencilOp( _fail, _zfail, _zpass ) ;
	}

	public static void glStencilMask( final int _mask )
	{
		GLES30.glStencilMask( _mask ) ;
	}

	public static void glColorMask( final boolean _red, final boolean _green, final boolean _blue, final boolean _alpha )
	{
		GLES30.glColorMask( _red, _green, _blue, _alpha ) ;
	}

	public static void glDepthMask( final boolean _flag )
	{
		GLES30.glDepthMask( _flag ) ;
	}

	public static void glClear( final int _mask )
	{
		GLES30.glClear( _mask ) ;
	}

	public static void glEnableVertexAttribArray( final int _index )
	{
		GLES30.glEnableVertexAttribArray( _index ) ;
	}

	public static void glVertexAttribPointer( final int _index, final int _size, final int _type, final boolean _normalized, final int _stride, final int _offset )
	{
		GLES30.glVertexAttribPointer( _index, _size, _type, _normalized, _stride, _offset ) ;
	}

	public static void glDisableVertexAttribArray( final int _index )
	{
		GLES30.glDisableVertexAttribArray( _index ) ;
	}

	public static void glDeleteBuffers( final int _n, final int[] _buffers, final int _offset )
	{
		GLES30.glDeleteBuffers( _n, _buffers, _offset ) ;
	}

	public static void glGetIntegerv( final int _pname, final int[] _params, final int _offset )
	{
		GLES30.glGetIntegerv( _pname, _params, _offset ) ;
	}

	public static void glCullFace( final int _mode  )
	{
		GLES30.glCullFace( _mode ) ;
	}

	public static void glFrontFace( final int _mode )
	{
		GLES30.glFrontFace( _mode ) ;
	}
	
	public static void glViewport( final int _x, final int _y, final int _width, final int _height )
	{
		GLES30.glViewport( _x, _y, _width, _height ) ;
	}

	public static int glGetError()
	{
		return GLES30.glGetError() ;
	}

	public static void glGenTextures( final int _n, final int[] _textures, final int _offset )
	{
		GLES30.glGenTextures( _n, _textures, _offset ) ;
	}

	public static void glDeleteTextures( final int _n, final int[] _textures, final int _offset )
	{
		GLES30.glDeleteTextures( _n, _textures, _offset ) ;
	}

	public static void glGenBuffers( final int _n, final int[] _buffers, final int _offset )
	{
		GLES30.glGenBuffers( _n, _buffers, _offset ) ;
	}

	public static void glActiveTexture( final int _texture )
	{
		GLES30.glActiveTexture( _texture ) ;
	}

	public static void glBindTexture( final int _target, final int _texture )
	{
		GLES30.glBindTexture( _target, _texture ) ;
	}

	public static void glDeleteProgram( final int _program )
	{
		GLES30.glDeleteProgram( _program ) ;
	}

	public static int glCreateProgram()
	{
		return GLES30.glCreateProgram() ;
	}

	public static void glAttachShader( final int _program, final int _shader )
	{
		GLES30.glAttachShader( _program, _shader ) ;
	}

	public static void glBindAttribLocation( final int _program, final int _index, final String _name )
	{
		GLES30.glBindAttribLocation( _program, _index, _name ) ;
	}

	public static void glLinkProgram( final int _program )
	{
		GLES30.glLinkProgram( _program ) ;
	}

	public static void glDetachShader( final int _program, final int _shader )
	{
		GLES30.glDetachShader( _program, _shader ) ;
	}

	public static void glDeleteShader( final int _shader )
	{
		GLES30.glDeleteShader( _shader ) ;
	}

	public static int glGetUniformLocation( final int _program, final String _name )
	{
		return GLES30.glGetUniformLocation( _program, _name ) ;
	}

	public static void glGetProgramiv( final int _program, final int _pname, final int[] _params, final int _offset )
	{
		GLES30.glGetProgramiv( _program, _pname, _params, _offset ) ;
	}

	public static String glGetProgramInfoLog( final int _program )
	{
		return GLES30.glGetProgramInfoLog( _program ) ;
	}

	public static int glCreateShader( final int _type )
	{
		return GLES30.glCreateShader( _type ) ;
	}
	
	public static void glShaderSource( final int _shader, final String _string )
	{
		GLES30.glShaderSource( _shader, _string ) ;
	}

	public static void glCompileShader( final int _shader )
	{
		GLES30.glCompileShader( _shader ) ;
	}

	public static void glGetShaderiv( final int _shader, final int _pname, final int[] _params, final int _offset )
	{
		GLES30.glGetShaderiv( _shader, _pname, _params, _offset ) ;
	}

	public static String glGetShaderInfoLog( final int _shader )
	{
		return GLES30.glGetShaderInfoLog( _shader ) ;
	}

	public static void glTexParameterf( final int _target, final int _pname, final float _param )
	{
		GLES30.glTexParameterf( _target, _pname, _param ) ;
	}

	public static void glTexParameteri( final int _target, final int _pname, final int _param )
	{
		GLES30.glTexParameteri( _target, _pname, _param ) ;
	}

	public static void glPixelStorei( final int _pname, final int _param )
	{
		GLES30.glPixelStorei( _pname, _param ) ;
	}
	
	public static void glGenRenderbuffers( final int _n, final int[] _renderbuffers, final int _offset )
	{
		GLES30.glGenRenderbuffers( _n, _renderbuffers, _offset ) ;
	}

	public static void glGenFramebuffers( final int _n, final int[] _framebuffers, final int _offset )
	{
		GLES30.glGenFramebuffers( _n, _framebuffers, _offset ) ;
	}

	public static void glBindFramebuffer( final int _target, final int _frame )
	{
		GLES30.glBindFramebuffer( _target, _frame ) ;
	}
	
	public static void glFramebufferRenderbuffer( final int _target, final int _attachment, final int _renderbuffertarget, final int _renderbuffer )
	{
		GLES30.glFramebufferRenderbuffer( _target, _attachment, _renderbuffertarget, _renderbuffer ) ;
	}

	public static void glFramebufferTexture2D( final int _target, final int _attachment, final int _textarget, final int _texture, final int _level )
	{
		GLES30.glFramebufferTexture2D( _target, _attachment, _textarget, _texture, _level ) ;
	}

	public static int glCheckFramebufferStatus( final int _target )
	{
		return GLES30.glCheckFramebufferStatus( _target ) ;
	}
	
	public static void glCopyTexImage2D( final int _target, final int _level, final int _internalformat, final int _x, final int _y, final int _width, final int _height, final int _border )
	{
		GLES30.glCopyTexImage2D( _target, _level, _internalformat, _x, _y, _width, _height, _border ) ;
	}

	public static void glDeleteFramebuffers( final int _n, final int[] _frames, final int _offset )
	{
		GLES30.glDeleteFramebuffers( _n, _frames, _offset ) ;
	}

	public static void glDeleteRenderbuffers( final int _n, final int[] _renders, final int _offset )
	{
		GLES30.glDeleteRenderbuffers( _n, _renders, _offset ) ;
	}

	public static void glBindRenderbuffer( final int _target, final int _render )
	{
		GLES30.glBindRenderbuffer( _target, _render ) ;
	}

	public static void glRenderbufferStorage( final int _target, final int _internalformat, final int _width, final int _height )
	{
		GLES30.glRenderbufferStorage( _target, _internalformat, _width, _height ) ;
	}
	
	public static void glTexImage2D( final int _target, final int _level, final int _internalformat, final int _width, final int _height, final int _border, final int _format, final int _type, final Buffer _pixels )
	{
		GLES30.glTexImage2D( _target, _level, _internalformat, _width, _height, _border, _format, _type, _pixels ) ;
	}

	public static void glBlitFramebuffer( final int _srcX0, final int _srcY0, final int _srcX1, final int _srcY1, final int _dstX0, final int _dstY0, final int _dstX1, final int _dstY1, final int _mask, final int _filter )
	{
		GLES30.glBlitFramebuffer( _srcX0, _srcY0, _srcX1, _srcY1, _dstX0, _dstY0, _dstX1, _dstY1, _mask, _filter ) ;
	}
}
