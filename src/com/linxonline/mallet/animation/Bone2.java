package com.linxonline.mallet.animation ; 

import java.lang.Math ;
import java.util.List ;

import com.linxonline.mallet.util.MalletList ;
import com.linxonline.mallet.maths.Vector2 ; 

public class Bone2
{
	public String name = null ;
	public List<Bone2> children = MalletList.<Bone2>newList() ;
	private Vector2 defaultBone = new Vector2() ;
	private Vector2 modifiedBone = new Vector2() ;
	private Vector2 offset = new Vector2() ;

	public Bone2( final String _name )
	{
		name = _name ;
	}

	public void setBone( final Vector2 _bone )
	{
		defaultBone.setXY( _bone.x, _bone.y ) ;
		reset() ;
	}

	public void setOffset( final Vector2 _offset )
	{
		offset.setXY( _offset.x, _offset.y ) ;
		reset() ;
	}

	public void addChildBone( final Bone2 _child )
	{
		children.add( _child ) ;
	}

	public void translate( final Vector2 _translate )
	{
		modifiedBone.x += _translate.x ;
		modifiedBone.y += _translate.y ;
		translateChildren() ;
	}

	public void rotate( final float _rotate )
	{
		// TODO: propagate rotate through children instead or recomputing it.
		final float rotateCos = ( float )( Math.cos( _rotate ) ) ;
		final float rotateSin = ( float )( Math.sin( _rotate ) ) ;
		final float modY = offset.y - modifiedBone.y ;
		final float modX = modifiedBone.x - offset.x ;

		modifiedBone.x = offset.x + ( modX * rotateCos ) - ( modY * rotateSin ) ;
		modifiedBone.y = offset.y +  ( modY * rotateCos ) - ( modX * rotateSin ) ;

		rotateChildren( _rotate, modifiedBone ) ;
	}

	public void rotate( final float _rotate, final Vector2 _origin )
	{
		// TODO: propagate rotate through children instead or recomputing it.
		final float rotateCos = ( float )( Math.cos( _rotate ) ) ;
		final float rotateSin = ( float )( Math.sin( _rotate ) ) ;
		final float modY = _origin.y - modifiedBone.y ;
		final float modX = modifiedBone.x - _origin.x ;

		modifiedBone.x = _origin.x + ( modX * rotateCos ) - ( modY * rotateSin ) ;
		modifiedBone.y = _origin.y +  ( modY * rotateCos ) - ( modX * rotateSin ) ;

		rotateChildren( _rotate, modifiedBone ) ;
	}

	public void reset()
	{
		modifiedBone.setXY( defaultBone.x, defaultBone.y ) ;
		translate( offset ) ;
		translateChildren() ;
	}
	
	private void rotateChildren( final float _rotate, final Vector2 _origin )
	{
		final int length = children.size() ;
		for( int i = 0; i < length; ++i )
		{
			children.get( i ).rotate( _rotate, _origin ) ;
		}
	}
	
	private void translateChildren()
	{
		final int length = children.size() ;
		for( int i = 0; i < length; ++i )
		{
			children.get( i ).translate( modifiedBone ) ;
		}
	}
}
