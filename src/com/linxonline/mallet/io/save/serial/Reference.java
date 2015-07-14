package com.linxonline.mallet.io.serial.save ;

import java.lang.annotation.ElementType ;
import java.lang.annotation.Target ;
import java.lang.annotation.Retention ;
import java.lang.annotation.RetentionPolicy ;

/**
	A Reference doesn't own the object being pointed to.
	It's possible for @Save and @Reference to be used 
	together, this denotes an object that may be shared 
	among multiple references, or is only referenced once.
*/
@Retention( RetentionPolicy.RUNTIME )
@Target( ElementType.FIELD )			
public @interface Reference {}