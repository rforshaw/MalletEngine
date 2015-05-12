package com.linxonline.mallet.io.save ;

import java.lang.annotation.ElementType ;
import java.lang.annotation.Target ;
import java.lang.annotation.Retention ;
import java.lang.annotation.RetentionPolicy ;

// Prevent the field from being saved even 
// if the class is tagged to have all fields 
// saved.
@Retention( RetentionPolicy.RUNTIME )
@Target( ElementType.TYPE )
public @interface SaveClass {}
