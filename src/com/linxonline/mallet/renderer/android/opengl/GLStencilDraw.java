package com.linxonline.mallet.renderer.android.opengl ;

import java.util.List ;

import com.linxonline.mallet.renderer.Program ;
import com.linxonline.mallet.renderer.ProgramMap ;

import com.linxonline.mallet.renderer.Shape ;
import com.linxonline.mallet.renderer.Draw ;
import com.linxonline.mallet.renderer.StencilDraw ;
import com.linxonline.mallet.renderer.BasicDraw ;
import com.linxonline.mallet.renderer.TextData ;
import com.linxonline.mallet.renderer.UpdateType ;
import com.linxonline.mallet.renderer.Interpolation ;
import com.linxonline.mallet.renderer.MalletColour ;

import com.linxonline.mallet.maths.Matrix4 ;
import com.linxonline.mallet.maths.Vector3 ;

public class GLStencilDraw extends GLDraw implements StencilDraw
{
	private final BasicDraw<GLProgram> basic ;
	private int endOrder = 0 ;

	public GLStencilDraw()
	{
		this( UpdateType.ON_DEMAND,
			  Interpolation.LINEAR,
			  new Vector3(),
			  new Vector3(),
			  new Vector3(),
			  new Vector3( 1, 1, 1 ), 0 ) ;
	}

	public GLStencilDraw( final UpdateType _type,
						  final Interpolation _interpolation,
						  final Vector3 _position,
						  final Vector3 _offset,
						  final Vector3 _rotation,
						  final Vector3 _scale,
						  final int _order )
	{
		super( GLDraw.Mode.STENCIL ) ;
		basic = new BasicDraw( _type, _interpolation, _position, _offset, _rotation, _scale, _order ) ;
	}

	@Override
	public int setOrder( final int _order )
	{
		basic.setOrder( _order ) ;
		return _order ;
	}

	@Override
	public int getOrder()
	{
		return basic.getOrder() ;
	}

	@Override
	public int setEndOrder( final int _order )
	{
		endOrder = _order ;
		return _order ;
	}

	@Override
	public int getEndOrder()
	{
		return endOrder ;
	}

	@Override
	public MalletColour setColour( final MalletColour _colour )
	{
		basic.setColour( _colour ) ;
		return _colour ;
	}

	@Override
	public MalletColour getColour()
	{
		return basic.getColour() ;
	}

	@Override
	public Program setProgram( final Program _program )
	{
		basic.setProgram( ( ProgramMap<GLProgram> )_program ) ;
		return _program ;
	}

	@Override
	public Program getProgram()
	{
		return basic.getProgram() ;
	}

	@Override
	public void setPosition( final float _x, final float _y, final float _z )
	{
		basic.setPosition( _x, _y, _z ) ;
	}

	@Override
	public Vector3 getPosition( final Vector3 _fill )
	{
		return basic.getPosition( _fill ) ;
	}

	@Override
	public void setOffset( final float _x, final float _y, final float _z )
	{
		basic.setOffset( _x, _y, _z ) ;
	}

	@Override
	public Vector3 getOffset( final Vector3 _fill )
	{
		return basic.getOffset( _fill ) ;
	}

	@Override
	public void setRotation( final float _x, final float _y, final float _z )
	{
		basic.setRotation( _x, _y, _z ) ;
	}

	@Override
	public Vector3 getRotation( final Vector3 _fill )
	{
		return basic.getRotation( _fill ) ;
	}

	@Override
	public void setScale( final float _x, final float _y, final float _z )
	{
		basic.setScale( _x, _y, _z ) ;
	}

	@Override
	public Vector3 getScale( final Vector3 _fill )
	{
		return basic.getScale( _fill ) ;
	}

	@Override
	public BasicDraw<GLProgram> getBasicData()
	{
		return basic ;
	}

	@Override
	public TextData getTextData()
	{
		return null ;
	}

	@Override
	public void reset()
	{
		basic.reset() ;

		setNewLocation( null ) ;
		setShape( null ) ;
	}
}
