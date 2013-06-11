package com.linxonline.mallet.renderer ;

import com.linxonline.mallet.maths.Vector2 ;
import com.linxonline.mallet.maths.Vector3 ;
import com.linxonline.mallet.input.InputAdapterInterface ;

public final class RenderInfo implements InputAdapterInterface
{
	private Vector2 displayDimensions = null ;
	private Vector2 renderDimensions = null ;
	private Vector3 cameraPosition = null ;
	private Vector2 scaledRenderDimensions = new Vector2( 0, 0 ) ;
	private Vector2 screenOffset = new Vector2( 0, 0 ) ;
	private Vector2 ratioRtoD = new Vector2( 0, 0 ) ;
	private Vector2 scaleRtoD = new Vector2( 0, 0 ) ;
	private Vector3 realCameraPosition = new Vector3( 0, 0, 0 ) ;

	public RenderInfo( final Vector2 _displayDimension, 
					   final Vector2 _renderDimensions, 
					   final Vector3 _cameraPosition )
	{
		displayDimensions = _displayDimension ;
		renderDimensions = _renderDimensions ;
		cameraPosition = _cameraPosition ;
		updateInfo() ;
	}
	
	private void updateInfo()
	{
		ratioRtoD = Vector2.divide( displayDimensions, renderDimensions ) ;
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
		
		scaledRenderDimensions.x = renderDimensions.x * scaleRtoD.x ;
		scaledRenderDimensions.y = renderDimensions.y * scaleRtoD.y ;
		screenOffset = Vector2.multiply( Vector2.subtract( displayDimensions, scaledRenderDimensions ), 0.5f ) ;
	}

	public Vector2 scaledToRenderDimensions( final Vector2 _dimensions )
	{
		return Vector2.divide( renderDimensions, _dimensions ) ;
	}
	
	public float convertInputToRenderX( final float _x )
	{
		updateRealCameraPosition() ;
		return ( ( ( _x - screenOffset.x ) * renderDimensions.x ) / scaledRenderDimensions.x ) - realCameraPosition.x ;
	}
	
	public float convertInputToRenderY( final float _y )
	{
		updateRealCameraPosition() ;
		return ( ( ( _y - screenOffset.y ) * renderDimensions.y ) / scaledRenderDimensions.y ) - realCameraPosition.y ;
	}
	
	public Vector2 convertInputToRender( final Vector2 _input )
	{
		return new Vector2( convertInputToRenderX( _input.x ), 
							convertInputToRenderY( _input.y ) ) ;
	}
	
	public boolean isInScreen( final Vector2 _position )
	{
		if( ( _position.x < 0 )|| ( _position.x > scaledRenderDimensions.x )
			|| ( _position.y < 0 )|| ( _position.y > scaledRenderDimensions.y ) )
		{
			return false ;
		}
		
		return true ;
	}
	
	public void setCameraPosition( final Vector3 _cameraPosition )
	{
		cameraPosition = _cameraPosition ;
	}

	public void setDisplayDimensions( final Vector2 _displayDimension )
	{
		displayDimensions = _displayDimension ;
		updateInfo() ;
	}

	public void setRenderDimensions( final Vector2 _renderDimensions )
	{
		renderDimensions =_renderDimensions ;
		updateInfo() ;
	}

	public void addToCameraPosition( final Vector3 _acc )
	{
		cameraPosition.add( _acc ) ;
	}

	public final Vector2 getDisplayDimensions()
	{
		return displayDimensions ;
	}

	public final Vector2 getRenderDimensions()
	{
		return renderDimensions ;
	}
	
	public final Vector2 getScaledRenderDimensions()
	{
		return scaledRenderDimensions ;
	}
	
	public final Vector2 getScreenOffset()
	{
		return screenOffset ;
	}
	
	public final Vector3 getCameraPosition()
	{
		updateRealCameraPosition() ;
		return realCameraPosition ;
	}

	public final Vector2 getRatioRenderToDisplay()
	{
		return ratioRtoD ;
	}
	
	public final Vector2 getScaleRenderToDisplay()
	{
		return scaleRtoD ;
	}
	
	private void updateRealCameraPosition()
	{
		realCameraPosition.x = -cameraPosition.x + ( renderDimensions.x / 2 ) ;
		realCameraPosition.y = -cameraPosition.y + ( renderDimensions.y / 2 ) ;
	}
}