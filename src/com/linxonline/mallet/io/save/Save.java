package com.linxonline.mallet.io.save ;

import java.lang.annotation.ElementType ;
import java.lang.annotation.Target ;
import java.lang.annotation.Retention ;
import java.lang.annotation.RetentionPolicy ;


/**
	Save the field tagged.
	The Field that has been tagged with @Save 
	is assumed to be the owner of the object being 
	referenced. If Referenced is also tagged on the field,
	then it is possible for that object to be referenced in 
	multiple areas.
*/
@Retention( RetentionPolicy.RUNTIME )
@Target( ElementType.FIELD )
public @interface Save {}