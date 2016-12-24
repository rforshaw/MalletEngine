package com.linxonline.mallet.physics ;

import java.util.List ;

import com.linxonline.mallet.util.Utility ;

public class Group
{
	private final static List<ID> ids = Utility.<ID>newArrayList() ;
	static
	{
		set( -1, "NO_GROUP", null ) ;
	}

	private Group() {}

	public static ID set( final int _id, final String _name, final Class _parent )
	{
		ID id = get( _id ) ;
		if( id != null )
		{
			id.set( _name, _parent ) ;
			return id ;
		}

		id = new ID( _id, _name, _parent ) ;
		synchronized( ids )
		{
			ids.add( id ) ;
		}
		return id ;
	}

	public static ID get( final int _id )
	{
		synchronized( ids )
		{
			final int size = ids.size() ;
			for( int i = 0; i < size; i++ )
			{
				final ID id = ids.get( i ) ;
				if( id.id == _id )
				{
					return id ; 
				}
			}
		}

		return null ;
	}

	public static ID get( final String _name )
	{
		synchronized( ids )
		{
			final int size = ids.size() ;
			for( int i = 0; i < size; i++ )
			{
				final ID id = ids.get( i ) ;
				if( id.name == _name )
				{
					return id ; 
				}
			}
		}

		return null ;
	}

	public static class ID
	{
		private final int id ;		// Group ID
		private String name ;		// Group name
		private Class parent ;		// Parent object type for this group

		private ID( final int _id, final String _name, final Class _parent )
		{
			id = _id ;
			set( _name, _parent ) ;
		}

		public void set( final String _name, final Class _parent )
		{
			name = _name ;
			parent = _parent ;
		}

		public boolean isExpected( final Object _obj )
		{
			if( parent != null && _obj == null )
			{
				// Expects an object for the group.
				return false ;
			}

			if( _obj != null && parent == null )
			{
				// Does not expect an object for group.
				return false ;
			}

			if( parent == null && _obj == null )
			{
				// Group does not expect an object 
				// and no object was provided.
				return true ;
			}

			// An object is expected and it must 
			// be an instance of the parent type.
			return parent.isInstance( _obj ) ;
		}

		public int getID()
		{
			return id ;
		}

		@Override
		public boolean equals( final Object _id )
		{
			if( _id instanceof ID )
			{
				final ID temp = ( ID )_id ;
				return id == temp.id ;
			}

			return false ;
		}

		@Override
		public int hashCode()
		{
			return id ;
		}
	}
}
