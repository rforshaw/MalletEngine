package com.linxonline.mallet.renderer ;

import com.linxonline.mallet.renderer.Shape ;
import com.linxonline.mallet.maths.Vector3 ;

/**
	A Draw request contains the basic components 
	required for the renderer to draw content to the 
	screen, it should be considered as platform agnostic.
*/
public interface Draw
{
	public Shape setShape( final Shape _shape ) ;
	public Shape getShape() ;

	public int setOrder( final int _order ) ;
	public int getOrder() ;

	public MalletColour setColour( final MalletColour _colour ) ;
	public MalletColour getColour() ;

	public Program setProgram( final Program _program ) ;
	public Program getProgram() ;

	public void setPosition( final float _x, final float _y, final float _z ) ;
	public Vector3 getPosition( final Vector3 _fill ) ;

	public void setOffset( final float _x, final float _y, final float _z ) ;
	public Vector3 getOffset( final Vector3 _fill ) ;

	public void setRotation( final float _x, final float _y, final float _z ) ;
	public Vector3 getRotation( final Vector3 _fill ) ;

	public void setScale( final float _x, final float _y, final float _z ) ;
	public Vector3 getScale( final Vector3 _fill ) ;
}
