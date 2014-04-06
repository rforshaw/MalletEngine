package com.linxonline.mallet.resources ;

import com.linxonline.mallet.maths.Vector2 ;
import com.linxonline.mallet.maths.Vector3 ;

public interface GeometryInterface
{
	public void initIndexBufferSize( final int _size ) ;
	public void initVertexBufferSize( final int _size ) ;

	public void addIndices( final int _index ) ;

	public void addVertex( final Vector3 _position, 
						   final Vector3 _normal,
						   final Vector2 _texCoord ) ;
}