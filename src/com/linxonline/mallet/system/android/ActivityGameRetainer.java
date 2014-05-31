package com.linxonline.mallet.system.android ;

import java.util.ArrayList ;

import com.linxonline.mallet.game.android.* ;
import com.linxonline.mallet.system.android.AndroidSystem ;
import com.linxonline.mallet.input.android.AndroidInputListener ;

import com.linxonline.mallet.game.* ;

public class ActivityGameRetainer
{
	public ArrayList<AndroidInputListener> inputListeners = new ArrayList<AndroidInputListener>() ;
	public AndroidSystem androidSystem = null ;
	public GameSystem gameSystem = new GameSystem() ;

	public ActivityGameRetainer() {}
}