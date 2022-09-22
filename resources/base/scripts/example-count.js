function create() {
	logger.println( "Build Script", logger.NORMAL ) ;
	const components = [] ;

	return {
		start: () => {
			logger.println( "Finding CountComponents: ", logger.NORMAL ) ;
			for( let entity of entities ) {
				let component = entity.getComponentBySimpleName( 'CountComponent' ) ;
				if( component != null ) {
					components.push( component ) ;
				}
			}
			logger.println( "Found: " + components.length + " components.", logger.NORMAL ) ;
		},
		update: ( _dt ) => {
			for( let component of components ) {
				component.count() ;
				logger.println( component.getCount(), logger.NORMAL ) ;	

				if( component.getCount() >= 20 ) {
					if( component.isDead() == true ) {
						continue ;
					}

					component.destroy() ;
				}
			}
		},
		end: () => {
			logger.println( "End Call: ", logger.NORMAL ) ;
		},
		destroyed: () => {
			logger.println( "Callback has said this is destroyed.", logger.NORMAL ) ;
		}
	}
}
