package com.linxonline.mallet.renderer ;

/**
	Defines a subset of buffers that can be directly
	added to a World or Group.
*/
public sealed interface IManageCompatible extends ICompatibleBuffer permits
	DrawBuffer,
	TextBuffer,
	Stencil,
	Depth,
	GroupBuffer
{
}
