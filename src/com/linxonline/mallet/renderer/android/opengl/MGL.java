package com.linxonline.mallet.renderer.android.opengl ;

import java.nio.* ;
import android.opengl.GLES31 ;

public class MGL
{
	public final static int GL_ELEMENT_ARRAY_BUFFER = GLES31.GL_ELEMENT_ARRAY_BUFFER ;
	public final static int GL_ARRAY_BUFFER = GLES31.GL_ARRAY_BUFFER ;

	public final static int GL_DEPTH_TEST= GLES31.GL_DEPTH_TEST ;
	public final static int GL_STENCIL_TEST= GLES31.GL_STENCIL_TEST ;
	public final static int GL_FLOAT = GLES31.GL_FLOAT ;
	public final static int GL_UNSIGNED_BYTE = GLES31.GL_UNSIGNED_BYTE ;
	public final static int GL_UNSIGNED_SHORT= GLES31.GL_UNSIGNED_SHORT ;
	public final static int GL_LINES = GLES31.GL_LINES ;
	public final static int GL_LINE_STRIP = GLES31.GL_LINE_STRIP ;
	public final static int GL_TRIANGLES = GLES31.GL_TRIANGLES ;
	public final static int GL_PRIMITIVE_RESTART_FIXED_INDEX = GLES31.GL_PRIMITIVE_RESTART_FIXED_INDEX ;
	public final static int GL_BLEND = GLES31.GL_BLEND ;
	public final static int GL_SRC_ALPHA = GLES31.GL_SRC_ALPHA ;
	public final static int GL_ONE_MINUS_SRC_ALPHA = GLES31.GL_ONE_MINUS_SRC_ALPHA ;
	public final static int GL_ALWAYS = GLES31.GL_ALWAYS ;
	public final static int GL_KEEP = GLES31.GL_KEEP ;
	public final static int GL_REPLACE = GLES31.GL_REPLACE ;
	public final static int GL_EQUAL = GLES31.GL_EQUAL ;
	public final static int GL_STENCIL_BUFFER_BIT = GLES31.GL_STENCIL_BUFFER_BIT ;

	public final static int GL_DYNAMIC_COPY = GLES31.GL_DYNAMIC_COPY ;
	public final static int GL_DYNAMIC_DRAW = GLES31.GL_DYNAMIC_DRAW ;

	public final static int GL_MAX_TEXTURE_SIZE = GLES31.GL_MAX_TEXTURE_SIZE ;
	public final static int GL_CULL_FACE = GLES31.GL_CULL_FACE ;
	public final static int GL_BACK = GLES31.GL_BACK ;
	public final static int GL_CCW = GLES31.GL_CCW ;
	public final static int GL_NO_ERROR = GLES31.GL_NO_ERROR ;
	public final static int GL_INVALID_ENUM = GLES31.GL_INVALID_ENUM ;
	public final static int GL_INVALID_VALUE = GLES31.GL_INVALID_VALUE ;
	public final static int GL_INVALID_OPERATION = GLES31.GL_INVALID_OPERATION ;
	public final static int GL_INVALID_FRAMEBUFFER_OPERATION = GLES31.GL_INVALID_FRAMEBUFFER_OPERATION ;
	public final static int GL_OUT_OF_MEMORY = GLES31.GL_OUT_OF_MEMORY ;
	public final static int GL_TEXTURE_2D = GLES31.GL_TEXTURE_2D ;
	public final static int GL_TEXTURE0 = GLES31.GL_TEXTURE0 ;
	public final static int GL_LINK_STATUS = GLES31.GL_LINK_STATUS ;

	public final static int GL_FALSE = GLES31.GL_FALSE ;
	public final static int GL_TRUE = GLES31.GL_TRUE ;

	public final static int GL_COMPILE_STATUS = GLES31.GL_COMPILE_STATUS ;
	public final static int GL_VERTEX_SHADER = GLES31.GL_VERTEX_SHADER ;
	public final static int GL_FRAGMENT_SHADER = GLES31.GL_FRAGMENT_SHADER ;

	public final static int GL_TEXTURE_MIN_FILTER = GLES31.GL_TEXTURE_MIN_FILTER ;
	public final static int GL_TEXTURE_MAG_FILTER = GLES31.GL_TEXTURE_MAG_FILTER ;
	public final static int GL_UNPACK_ALIGNMENT = GLES31.GL_UNPACK_ALIGNMENT ;
	public final static int GL_REPEAT = GLES31.GL_REPEAT ;
	public final static int GL_CLAMP_TO_EDGE = GLES31.GL_CLAMP_TO_EDGE ;
	public final static int GL_LINEAR = GLES31.GL_LINEAR ;
	public final static int GL_NEAREST = GLES31.GL_NEAREST ;
	public final static int GL_TEXTURE_WRAP_T = GLES31.GL_TEXTURE_WRAP_T ;
	public final static int GL_TEXTURE_WRAP_S = GLES31.GL_TEXTURE_WRAP_S ;
	public final static int GL_FRAMEBUFFER = GLES31.GL_FRAMEBUFFER ;
	public final static int GL_RENDERBUFFER = GLES31.GL_RENDERBUFFER ;
	public final static int GL_DRAW_FRAMEBUFFER = GLES31.GL_DRAW_FRAMEBUFFER ;
	public final static int GL_READ_FRAMEBUFFER = GLES31.GL_READ_FRAMEBUFFER ;
	public final static int GL_COLOR_BUFFER_BIT = GLES31.GL_COLOR_BUFFER_BIT ;
	public final static int GL_DEPTH_BUFFER_BIT = GLES31.GL_DEPTH_BUFFER_BIT ;
	public final static int GL_FRAMEBUFFER_COMPLETE = GLES31.GL_FRAMEBUFFER_COMPLETE ;
	public final static int GL_FRAMEBUFFER_UNDEFINED = GLES31.GL_FRAMEBUFFER_UNDEFINED ;
	public final static int GL_FRAMEBUFFER_UNSUPPORTED = GLES31.GL_FRAMEBUFFER_UNSUPPORTED ;
	public final static int GL_RGBA = GLES31.GL_RGBA ;
	
	public final static int GL_STENCIL_INDEX8 = GLES31.GL_STENCIL_INDEX8 ;
	public final static int GL_COLOR_ATTACHMENT0 = GLES31.GL_COLOR_ATTACHMENT0 ;
	public final static int GL_STENCIL_ATTACHMENT = GLES31.GL_STENCIL_ATTACHMENT ;
	public final static int GL_DEPTH_ATTACHMENT = GLES31.GL_DEPTH_ATTACHMENT ;

	public final static int GL_NEAREST_MIPMAP_NEAREST = GLES31.GL_NEAREST_MIPMAP_NEAREST ;
	public final static int GL_LINEAR_MIPMAP_LINEAR = GLES31.GL_LINEAR_MIPMAP_LINEAR ;

	public final static int GL_DEPTH_COMPONENT = GLES31.GL_DEPTH_COMPONENT ;
	public final static int GL_DEPTH_STENCIL = GLES31.GL_DEPTH_STENCIL ;

	public final static int GL_SHADER_STORAGE_BUFFER = GLES31.GL_SHADER_STORAGE_BUFFER ;
	public final static int GL_SHADER_STORAGE_BLOCK = GLES31.GL_SHADER_STORAGE_BLOCK ;

	public MGL() {}

	public static void glBufferData( final int _target, final int _size, final Buffer _data, final int _usage )
	{
		GLES31.glBufferData( _target, _size, _data, _usage ) ;
	}

	public static void glBufferSubData( final int _target, final int _offset, final int _size, final Buffer _data )
	{
		GLES31.glBufferSubData( _target, _offset, _size, _data ) ;
	}

	public static void glEnable( final int _cap )
	{
		GLES31.glEnable( _cap ) ;
	}

	public static void glDisable( final int _cap )
	{
		GLES31.glDisable( _cap ) ;
	}

	public static void glUseProgram( final int _program )
	{
		GLES31.glUseProgram( _program ) ;
	}

	public static void glUniformMatrix4fv( final int _location, final int _count, final boolean _transpose, final float[] _value, final int _offset )
	{
		GLES31.glUniformMatrix4fv( _location, _count, _transpose, _value, _offset ) ;
	}

	public static void glUniform1i( final int _location, final int _v0 )
	{
		GLES31.glUniform1i( _location, _v0 ) ;
	}

	public static void glBlendFunc( final int _sfactor, final int _dfactor )
	{
		GLES31.glBlendFunc( _sfactor, _dfactor ) ;
	}

	public static void glBindBuffer( final int _target, final int _buffer )
	{
		GLES31.glBindBuffer( _target, _buffer ) ;
	}

	public static void glBindBufferBase( final int _target, final int _index, final int _buffer )
	{
		GLES31.glBindBufferBase( _target, _index, _buffer ) ;
	}

	public static void glDrawElements( final int _mode, final int _count, final int _type, final int _offset )
	{
		GLES31.glDrawElements( _mode, _count, _type, _offset ) ;
	}

	public static void glDrawElementsInstanced( final int _mode, final int _count, final int _type, final int _offset, final int _instanceCount )
	{
		GLES31.glDrawElementsInstanced( _mode, _count, _type, _offset, _instanceCount ) ;
	}

	public static void glStencilFunc( final int _func, final int _ref, final int _mask )
	{
		GLES31.glStencilFunc( _func, _ref, _mask ) ;
	}

	public static void glStencilOp( final int _fail, final int _zfail, final int _zpass )
	{
		GLES31.glStencilOp( _fail, _zfail, _zpass ) ;
	}

	public static void glStencilMask( final int _mask )
	{
		GLES31.glStencilMask( _mask ) ;
	}

	public static void glColorMask( final boolean _red, final boolean _green, final boolean _blue, final boolean _alpha )
	{
		GLES31.glColorMask( _red, _green, _blue, _alpha ) ;
	}

	public static void glDepthMask( final boolean _flag )
	{
		GLES31.glDepthMask( _flag ) ;
	}

	public static void glClear( final int _mask )
	{
		GLES31.glClear( _mask ) ;
	}

	public static void glClearColor( final float _red, final float _green, final float _blue, final float _alpha )
	{
		GLES31.glClearColor( _red, _green, _blue, _alpha ) ;
	}

	public static void glEnableVertexAttribArray( final int _index )
	{
		GLES31.glEnableVertexAttribArray( _index ) ;
	}

	public static void glVertexAttribPointer( final int _index, final int _size, final int _type, final boolean _normalized, final int _stride, final int _offset )
	{
		GLES31.glVertexAttribPointer( _index, _size, _type, _normalized, _stride, _offset ) ;
	}

	public static void glDisableVertexAttribArray( final int _index )
	{
		GLES31.glDisableVertexAttribArray( _index ) ;
	}

	public static void glDeleteBuffers( final int _n, final int[] _buffers, final int _offset )
	{
		GLES31.glDeleteBuffers( _n, _buffers, _offset ) ;
	}

	public static void glGetIntegerv( final int _pname, final int[] _params, final int _offset )
	{
		GLES31.glGetIntegerv( _pname, _params, _offset ) ;
	}

	public static void glCullFace( final int _mode  )
	{
		GLES31.glCullFace( _mode ) ;
	}

	public static void glFrontFace( final int _mode )
	{
		GLES31.glFrontFace( _mode ) ;
	}
	
	public static void glViewport( final int _x, final int _y, final int _width, final int _height )
	{
		GLES31.glViewport( _x, _y, _width, _height ) ;
	}

	public static int glGetError()
	{
		return GLES31.glGetError() ;
	}

	public static void glGenTextures( final int _n, final int[] _textures, final int _offset )
	{
		GLES31.glGenTextures( _n, _textures, _offset ) ;
	}

	public static void glDeleteTextures( final int _n, final int[] _textures, final int _offset )
	{
		GLES31.glDeleteTextures( _n, _textures, _offset ) ;
	}

	public static void glGenBuffers( final int _n, final int[] _buffers, final int _offset )
	{
		GLES31.glGenBuffers( _n, _buffers, _offset ) ;
	}

	public static void glActiveTexture( final int _texture )
	{
		GLES31.glActiveTexture( _texture ) ;
	}

	public static void glBindTexture( final int _target, final int _texture )
	{
		GLES31.glBindTexture( _target, _texture ) ;
	}

	public static void glDeleteProgram( final int _program )
	{
		GLES31.glDeleteProgram( _program ) ;
	}

	public static int glCreateProgram()
	{
		return GLES31.glCreateProgram() ;
	}

	public static void glAttachShader( final int _program, final int _shader )
	{
		GLES31.glAttachShader( _program, _shader ) ;
	}

	public static void glBindAttribLocation( final int _program, final int _index, final String _name )
	{
		GLES31.glBindAttribLocation( _program, _index, _name ) ;
	}

	public static void glLinkProgram( final int _program )
	{
		GLES31.glLinkProgram( _program ) ;
	}

	public static void glDetachShader( final int _program, final int _shader )
	{
		GLES31.glDetachShader( _program, _shader ) ;
	}

	public static void glDeleteShader( final int _shader )
	{
		GLES31.glDeleteShader( _shader ) ;
	}

	public static int glGetUniformLocation( final int _program, final String _name )
	{
		return GLES31.glGetUniformLocation( _program, _name ) ;
	}

	public static void glGetProgramiv( final int _program, final int _pname, final int[] _params, final int _offset )
	{
		GLES31.glGetProgramiv( _program, _pname, _params, _offset ) ;
	}

	public static String glGetProgramInfoLog( final int _program )
	{
		return GLES31.glGetProgramInfoLog( _program ) ;
	}

	public static int glCreateShader( final int _type )
	{
		return GLES31.glCreateShader( _type ) ;
	}
	
	public static void glShaderSource( final int _shader, final String _string )
	{
		GLES31.glShaderSource( _shader, _string ) ;
	}

	public static void glCompileShader( final int _shader )
	{
		GLES31.glCompileShader( _shader ) ;
	}

	public static void glGetShaderiv( final int _shader, final int _pname, final int[] _params, final int _offset )
	{
		GLES31.glGetShaderiv( _shader, _pname, _params, _offset ) ;
	}

	public static String glGetShaderInfoLog( final int _shader )
	{
		return GLES31.glGetShaderInfoLog( _shader ) ;
	}

	public static void glTexParameterf( final int _target, final int _pname, final float _param )
	{
		GLES31.glTexParameterf( _target, _pname, _param ) ;
	}

	public static void glTexParameteri( final int _target, final int _pname, final int _param )
	{
		GLES31.glTexParameteri( _target, _pname, _param ) ;
	}

	public static void glPixelStorei( final int _pname, final int _param )
	{
		GLES31.glPixelStorei( _pname, _param ) ;
	}
	
	public static void glGenRenderbuffers( final int _n, final int[] _renderbuffers, final int _offset )
	{
		GLES31.glGenRenderbuffers( _n, _renderbuffers, _offset ) ;
	}

	public static void glGenFramebuffers( final int _n, final int[] _framebuffers, final int _offset )
	{
		GLES31.glGenFramebuffers( _n, _framebuffers, _offset ) ;
	}

	public static void glBindFramebuffer( final int _target, final int _frame )
	{
		GLES31.glBindFramebuffer( _target, _frame ) ;
	}
	
	public static void glFramebufferRenderbuffer( final int _target, final int _attachment, final int _renderbuffertarget, final int _renderbuffer )
	{
		GLES31.glFramebufferRenderbuffer( _target, _attachment, _renderbuffertarget, _renderbuffer ) ;
	}

	public static void glFramebufferTexture2D( final int _target, final int _attachment, final int _textarget, final int _texture, final int _level )
	{
		GLES31.glFramebufferTexture2D( _target, _attachment, _textarget, _texture, _level ) ;
	}

	public static int glCheckFramebufferStatus( final int _target )
	{
		return GLES31.glCheckFramebufferStatus( _target ) ;
	}
	
	public static void glCopyTexImage2D( final int _target, final int _level, final int _internalformat, final int _x, final int _y, final int _width, final int _height, final int _border )
	{
		GLES31.glCopyTexImage2D( _target, _level, _internalformat, _x, _y, _width, _height, _border ) ;
	}

	public static void glDeleteFramebuffers( final int _n, final int[] _frames, final int _offset )
	{
		GLES31.glDeleteFramebuffers( _n, _frames, _offset ) ;
	}

	public static void glDeleteRenderbuffers( final int _n, final int[] _renders, final int _offset )
	{
		GLES31.glDeleteRenderbuffers( _n, _renders, _offset ) ;
	}

	public static void glBindRenderbuffer( final int _target, final int _render )
	{
		GLES31.glBindRenderbuffer( _target, _render ) ;
	}

	public static void glRenderbufferStorage( final int _target, final int _internalformat, final int _width, final int _height )
	{
		GLES31.glRenderbufferStorage( _target, _internalformat, _width, _height ) ;
	}

	public static void glDrawBuffers( final int _n, final int[] _ids, final int _offset )
	{
		GLES31.glDrawBuffers( _n, _ids, _offset ) ;
	}

	public static void glTexImage2D( final int _target, final int _level, final int _internalformat, final int _width, final int _height, final int _border, final int _format, final int _type, final Buffer _pixels )
	{
		GLES31.glTexImage2D( _target, _level, _internalformat, _width, _height, _border, _format, _type, _pixels ) ;
	}

	public static void glBlitFramebuffer( final int _srcX0, final int _srcY0, final int _srcX1, final int _srcY1, final int _dstX0, final int _dstY0, final int _dstX1, final int _dstY1, final int _mask, final int _filter )
	{
		GLES31.glBlitFramebuffer( _srcX0, _srcY0, _srcX1, _srcY1, _dstX0, _dstY0, _dstX1, _dstY1, _mask, _filter ) ;
	}

	public static int glGetProgramResourceIndex( int _program, int _programInterface, String _name )
	{
		return GLES31.glGetProgramResourceIndex( _program, _programInterface, _name ) ;
	}
}
