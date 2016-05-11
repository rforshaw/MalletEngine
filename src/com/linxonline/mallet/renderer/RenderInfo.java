package com.linxonline.mallet.renderer ;

import com.linxonline.mallet.maths.Vector2 ;
import com.linxonline.mallet.maths.Vector3 ;
import com.linxonline.mallet.input.InputAdapterInterface ;
import com.linxonline.mallet.system.GlobalConfig ;

/**
	Convenience class to handle Display & Render Dimensions.
	Provides scaling information between Display & Render dimensions, 
	retaining or not Aspect Ratio.
	Also handles conversion for user-input between Display & Render.
*/
public final class RenderInfo implements InputAdapterInterface
{
	private boolean holdToRenderRatio = true ;							// By default will scale inrespect to Aspect Ratio.
	private Vector2 displayDimensions = new Vector2( 800, 600 ) ;		// Dimensions of the Window
	private Vector2 renderDimensions = new Vector2( 800, 600 ) ;		// Dimensions of the render-buffer

	private Vector3 cameraPosition = new Vector3() ;					// Camera position
	private final Vector3 cameraZoom = new Vector3( 1, 1, 1 ) ;

	private final Vector2 scaledRenderDimensions = new Vector2( 0, 0 ) ;
	private final Vector2 halfRenderDimensions = new Vector2() ;
	private final Vector2 scaleRtoD = new Vector2( 0, 0 ) ;
	private Vector2 screenOffset = new Vector2( 0, 0 ) ;
	private Vector2 ratioRtoD = new Vector2( 0, 0 ) ;

	public RenderInfo( final Vector2 _displayDimension, 
					   final Vector2 _renderDimensions )
	{
		displayDimensions = _displayDimension ;
		renderDimensions = _renderDimensions ;
		updateInfo() ;
	}

	private void updateInfo()
	{
		ratioRtoD = Vector2.divide( displayDimensions, renderDimensions ) ;
		if( holdToRenderRatio == true )
		{
			// Choose a scaling Ratio that allows the renderDimensions to keep 
			// its aspect-ratio, but fill the display as much as possible 
			if( ratioRtoD.x < ratioRtoD.y )
			{
				scaleRtoD.x = ratioRtoD.x ;
				scaleRtoD.y = ratioRtoD.x ;
			}
			else
			{
				scaleRtoD.x = ratioRtoD.y ;
				scaleRtoD.y = ratioRtoD.y ;
			}
		}
		else
		{
			// Don't care about holding the renderDimensions natural aspect-ratio
			// just fill the display, will cause stretching
			scaleRtoD.x = ratioRtoD.x ;
			scaleRtoD.y = ratioRtoD.y ;
		}

		scaledRenderDimensions.x = renderDimensions.x * scaleRtoD.x ;
		scaledRenderDimensions.y = renderDimensions.y * scaleRtoD.y ;
		screenOffset = Vector2.multiply( Vector2.subtract( displayDimensions, scaledRenderDimensions ), 0.5f ) ;
	}

	public Vector2 scaledToRenderDimensions( final Vector2 _dimensions )
	{
		return Vector2.divide( renderDimensions, _dimensions ) ;
	}

	public float applyScreenOffsetX( final float _x )
	{
		return _x - screenOffset.x ;
	}

	public float applyScreenOffsetY( final float _y )
	{
		return _y - screenOffset.y ;
	}

	public float convertInputToRenderX( final Camera _camera, final float _x )
	{
		CameraAssist.getPosition( _camera, cameraPosition ) ;
		CameraAssist.getScale( _camera, cameraZoom ) ;

		final float t1 = ( ( ( _x - screenOffset.x ) * renderDimensions.x ) / scaledRenderDimensions.x ) - halfRenderDimensions.x ;
		final float cam = ( t1 * cameraZoom.x ) + cameraPosition.x ;
		return cam ;
	}

	public float convertInputToRenderY( final Camera _camera, final float _y )
	{
		CameraAssist.getPosition( _camera, cameraPosition ) ;
		CameraAssist.getScale( _camera, cameraZoom ) ;

		final float t1 = ( ( ( _y - screenOffset.y ) * renderDimensions.y ) / scaledRenderDimensions.y ) - halfRenderDimensions.y ;
		final float cam = ( t1 * cameraZoom.y ) + cameraPosition.y  ;
		return cam ;
	}

	public Vector2 convertInputToRender( final Camera _camera, final Vector2 _input )
	{
		return new Vector2( convertInputToRenderX( _camera, _input.x ), convertInputToRenderY( _camera, _input.y ) ) ;
	}

	public float convertInputToUIRenderX( final float _x )
	{
		return ( ( ( _x - screenOffset.x ) * renderDimensions.x ) / scaledRenderDimensions.x ) ;
	}

	public float convertInputToUIRenderY( final float _y )
	{
		return ( ( ( _y - screenOffset.y ) * renderDimensions.y ) / scaledRenderDimensions.y ) ;
	}

	public Vector2 convertInputUIToRender( final Vector2 _input )
	{
		return new Vector2( convertInputToUIRenderX( _input.x ), convertInputToUIRenderY( _input.y ) ) ;
	}

	/**
		Check to see if the position is located within the Scaled Render Dimensions.
		The renderDimensions could be 640x480, but the display 1024x768.
	**/
	public boolean isInScreen( final Vector2 _position )
	{
		if( ( _position.x < 0 )|| ( _position.x > scaledRenderDimensions.x )
			|| ( _position.y < 0 )|| ( _position.y > scaledRenderDimensions.y ) )
		{
			return false ;
		}
		
		return true ;
	}

	/**
		Used to set the dimensions of the Window that the program will render into.
	**/
	public void setDisplayDimensions( final int _width, final int _height )
	{
		displayDimensions.setXY( _width, _height ) ;
		GlobalConfig.addInteger( "DISPLAYWIDTH", _width ) ;
		GlobalConfig.addInteger( "DISPLAYHEIGHT", _height ) ;
		updateInfo() ;
	}

	/**
		Set the Dimensions of the canvas in which things will be rendered onto.
	**/
	public void setRenderDimensions( final int _width, final int _height )
	{
		renderDimensions.setXY( _width, _height ) ;
		GlobalConfig.addInteger( "RENDERWIDTH", _width ) ;
		GlobalConfig.addInteger( "RENDERHEIGHT", _height ) ;

		halfRenderDimensions.setXY( renderDimensions.x / 2.0f, renderDimensions.y / 2.0f ) ;
		updateInfo() ;
	}

	public void setKeepRenderRatio( final boolean _ratio )
	{
		holdToRenderRatio = _ratio ;
		updateInfo() ;
	}

	public final Vector2 getDisplayDimensions()
	{
		return displayDimensions ;
	}

	/**
		Return the render dimensions.
		For example: if the render-buffer was 800x600 
		it will return x:800, y:600.
	*/
	public final Vector2 getRenderDimensions()
	{
		return renderDimensions ;
	}

	public final Vector2 getHalfRenderDimensions()
	{
		return halfRenderDimensions ;
	}
	
	/**
		Return the render dimensions scaled to display dimensions.
		For example: if the render-buffer was 800x600 & display was 1024x768
		it will return x:1024, y:768. The scale result will change depending on 
		aspect-ratio and whether it is being enforced.
	*/
	public final Vector2 getScaledRenderDimensions()
	{
		return scaledRenderDimensions ;
	}

	/**
		Used to provide blackbars when render aspect-ratio does not fit with display aspect-ratio.
	*/
	public final Vector2 getScreenOffset()
	{
		return screenOffset ;
	}

	public final Vector2 getRatioRenderToDisplay()
	{
		return ratioRtoD ;
	}
	
	public final Vector2 getScaleRenderToDisplay()
	{
		return scaleRtoD ;
	}
}