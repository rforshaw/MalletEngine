function create() {
	logger.println( "Build Script", logger.NORMAL ) ;
	const components = [] ;

	return {
		/**
		 * Called when script is started.
		 */
		start: () => {
			// logger can be usd to print messages to the terminal.
			// Hooks into MalletEngine Logger implementation.
			logger.println( "Finding CountComponents: ", logger.NORMAL ) ;
			
			// Passed in entities can be interrogated for components that
			// can be called within the script.
			// Only components flagged to be accessed by the scripting system
			// will be available to access.
			for( let entity of entities ) {
				let component = entity.getComponentBySimpleName( 'CountComponent' ) ;
				if( component != null ) {
					components.push( component ) ;
				}
			}
			logger.println( "Found: " + components.length + " components.", logger.NORMAL ) ;
		},
		/**
		 * If an update function is defined this will be called
		 * during the game-logic update cycle.
		 */
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
		/**
		 * Called when script has ended.
		 */
		end: () => {
			logger.println( "End Call: ", logger.NORMAL ) ;
		},
		/**
		 * Called when script is destroyed.
		 * This function is defined by the IDestroyed interface defined
		 * within the game-test.
		 * 
		 * The Script on the engine side can be given a set of interfaces
		 * that our Javascript is expected to implement. Though they are intended
		 * to exist the engine will only print a message.
		 *
		 * NOTE: That the game-test expects a function called notAValidFunction()
		 * to be implemented, but it is clearly not part of this script.
		 */
		destroyed: () => {
			logger.println( "Callback has said this is destroyed.", logger.NORMAL ) ;
		}
	}
}
