package microgram.impl.rest.replication;

import utils.JSON;

/**
 * Note: Data that is stored in thread local storage...
 * 
 **/
public class Version {
	
	public static <T> T getOrElse( T orElse, Class<T> clazz)  {
		String v = jsonVersionIn.get();
		if( v != null ) 
			return JSON.decode(v, clazz );

		jsonVersionIn.set( JSON.encode( orElse ) );
		return orElse;
	}
	
	public static void set( Object newValue ) {
		jsonVersionOut.set( JSON.encode( newValue ) );
	}

	static ThreadLocal<String> jsonVersionIn = new ThreadLocal<>(), jsonVersionOut = new ThreadLocal<>();
}
