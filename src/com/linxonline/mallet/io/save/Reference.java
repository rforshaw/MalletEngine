package com.linxonline.mallet.io.save ;

import java.lang.annotation.ElementType ;
import java.lang.annotation.Target ;
import java.lang.annotation.Retention ;
import java.lang.annotation.RetentionPolicy ;

@Retention( RetentionPolicy.RUNTIME )
@Target( ElementType.FIELD )			// Field is a reference, doesn't own the object being pointed to.
public @interface Reference {}