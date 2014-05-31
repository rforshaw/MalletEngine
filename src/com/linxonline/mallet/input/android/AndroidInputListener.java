package com.linxonline.mallet.input.android ;

import android.view.KeyEvent ;
import android.view.MotionEvent ;

public interface AndroidInputListener
{
	public void onKeyDown( int _keyCode, KeyEvent _event ) ;
	public void onKeyUp( int _keyCode, KeyEvent _event ) ;

	public void onTouchEvent( MotionEvent _event ) ;
}