function create() {
	logger.println( "Build Script", logger.NORMAL ) ;

	return {
		/**
		 * Called when script is started.
		 */
		start: () => {
			const { counter } = state ;
			const example = counter.create() ;
			const prims = counter.primitiveArray() ;
			const objects = counter.objectArray() ;
			const list = counter.objectList() ;

			logger.println( "Calling hello(): " + example.hello(), logger.NORMAL ) ;
			logger.println( "Initial count: " + counter.getCount(), logger.NORMAL ) ;
			logger.println( "Primitive Array: " + prims.length, logger.NORMAL ) ;
			logger.println( "Object Array: " + objects.length, logger.NORMAL ) ;
			logger.println( "List: " + list.length, logger.NORMAL ) ;
		},
		/**
		 * If an update function is defined this will be called
		 * during the game-logic update cycle.
		 */
		update: ( _dt ) => {
			const { counter } = state ;

			counter.count() ;
			logger.println( counter.getCount(), logger.NORMAL ) ;

			if( counter.getCount() >= 5 ) {
				counter.reset() ;
				script.removeScript() ;
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
		countReseted: () => {
			const { counter } = state ;

			logger.println( "Our counter state has been reset: " + counter.getCount(), logger.NORMAL ) ;
		}
	}
}
