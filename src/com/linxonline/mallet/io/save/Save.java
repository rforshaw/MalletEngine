package com.linxonline.mallet.io.save ;

import java.lang.annotation.ElementType ;
import java.lang.annotation.Target ;
import java.lang.annotation.Retention ;
import java.lang.annotation.RetentionPolicy ;

@Retention( RetentionPolicy.RUNTIME )
//@Target( ElementType.TYPE )				// Save all Fields contained within class
@Target( ElementType.FIELD )			// Save a specific field
public @interface Save {}