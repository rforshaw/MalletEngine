package com.linxonline.mallet.renderer ;

/**
	Simple Handler for modify an Draw Request.
	Use DrawAssist to modify the underlying data-structure.
	
	Draw is extended for each rendering-system. Though currently 
	the renders make use of DrawData and extend upon that, there is 
	no future guarantees.
	
	Don't cast unless you know what you're doing.
*/
public interface Draw<T extends Draw> {}
