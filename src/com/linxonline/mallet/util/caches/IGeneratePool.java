package com.linxonline.mallet.util.caches ;

import com.linxonline.mallet.io.formats.json.JObject ;

public interface IGeneratePool<T>
{
	public T create( final String _path ) ;

	public interface IGenerator<T>
	{
		public T generate( final String _path, final JObject _obj ) ;
	}
}
