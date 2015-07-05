package com.linxonline.mallet.renderer ;

public enum DrawRequestType
{
	NOT_SET,
	CREATE_DRAW,
	MODIFY_EXISTING_DRAW,
	REMOVE_DRAW,
	SET_CAMERA_POSITION,
	GARBAGE_COLLECT_DRAW,

	TEXTURE,
	GEOMETRY,
	TEXT
}