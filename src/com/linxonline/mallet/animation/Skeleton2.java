package com.linxonline.mallet.animation ; 

import java.util.Map ;

import com.linxonline.mallet.maths.Vector2 ;
import com.linxonline.mallet.util.MalletMap ;

public class Skeleton2
{
	private Bone2 root = null ;
	private final Map<String, Bone2> bones = MalletMap.<String, Bone2>newMap() ;

	public Skeleton2() {}

	public void constructSkeleton( final Bone2 _root )
	{
		root = _root ;
		bones.put( root.name, root ) ;
		addBones( root ) ;
	}

	public void translateFromRoot( final Vector2 _translate )
	{
		root.translate( _translate ) ;
	}

	public void translateFromBone( final String _name, final Vector2 _translate )
	{
		if( bones.containsKey( _name ) == true )
		{
			bones.get( _name ).translate( _translate ) ;
		}
	}

	public void rotateFromRoot( final float _rotate )
	{
		root.rotate( _rotate ) ;
	}

	public void rotateFromBone( final String _name, final float _rotate )
	{
		if( bones.containsKey( _name ) == true )
		{
			bones.get( _name ).rotate( _rotate ) ;
		}
	}
	
	// Go through all the bones and add them to the Map
	private void addBones( final Bone2 _bone )
	{
		// Reached the end of a branch
		if( _bone == null )
		{
			return ;
		}

		final int length = _bone.children.size() ;
		Bone2 child = null ;

		for( int i = 0; i < length; ++i )
		{
			child = _bone.children.get( i ) ;
			addBones( child ) ;
			bones.put( child.name, child ) ;
		}
	}
}
