package com.linxonline.mallet.input ;

public enum KeyCode
{
	NONE,
	a( 'a' ), b( 'b' ), c( 'c' ), d( 'd' ), e( 'e' ), f( 'f' ), g( 'g' ), h( 'h' ), i( 'i' ), j( 'j' ), k( 'k' ), 
	l( 'l' ), m( 'm' ), n( 'n' ), o( 'o' ), p( 'p' ), q( 'q' ), r( 'r' ), s( 's' ), t( 't' ), u( 'u' ), v( 'v' ),
	w( 'w' ), x( 'x' ), y( 'y' ), z( 'z' ),
	A( 'A' ), B( 'B' ), C( 'C' ), D( 'D' ), E( 'E' ), F( 'F' ), G( 'G' ), H( 'H' ), I( 'I' ), J( 'J' ), K( 'K' ),
	L( 'L' ), M( 'M' ), N( 'N' ), O( 'O' ), P( 'P' ), Q( 'Q' ), R( 'R' ), S( 'S' ), T( 'T' ), U( 'U' ), V( 'V' ),
	W( 'W' ), X( 'X' ), Y( 'Y' ), Z( 'Z' ),
	SPACEBAR( ' ' ), SHIFT, CTRL, ALT, ALTGROUP, META, BACKSPACE, ENTER ;

	public final char character ;

	private KeyCode()
	{
		character = '\0' ;
	}
	
	private KeyCode( final char _character )
	{
		character = _character ;
	}

	public static KeyCode getKeyCode( final char _char )
	{
		switch( _char )
		{
			case ' ' : return SPACEBAR ;
			/* LOWERCASE LETTERS */
			case 'a' : return a ;
			case 'b' : return b ;
			case 'c' : return c ;
			case 'd' : return d ;
			case 'e' : return e ;
			case 'f' : return f ;
			case 'g' : return g ;
			case 'h' : return h ;
			case 'i' : return i ;
			case 'j' : return j ;
			case 'k' : return k ;
			case 'l' : return l ;
			case 'm' : return m ;
			case 'n' : return n ;
			case 'o' : return o ;
			case 'p' : return p ;
			case 'q' : return q ;
			case 'r' : return r ;
			case 's' : return s ;
			case 't' : return t ;
			case 'u' : return u ;
			case 'v' : return v ;
			case 'w' : return w ;
			case 'x' : return x ;
			case 'y' : return y ;
			case 'z' : return z ;
			/* UPPERCASE LETTERS */
			case 'A' : return A ;
			case 'B' : return B ;
			case 'C' : return C ;
			case 'D' : return D ;
			case 'E' : return E ;
			case 'F' : return F ;
			case 'G' : return G ;
			case 'H' : return H ;
			case 'I' : return I ;
			case 'J' : return J ;
			case 'K' : return K ;
			case 'L' : return L ;
			case 'M' : return M ;
			case 'N' : return N ;
			case 'O' : return O ;
			case 'P' : return P ;
			case 'Q' : return Q ;
			case 'R' : return R ;
			case 'S' : return S ;
			case 'T' : return T ;
			case 'U' : return U ;
			case 'V' : return V ;
			case 'W' : return W ;
			case 'X' : return X ;
			case 'Y' : return Y ;
			case 'Z' : return Z ;
		}

		return KeyCode.NONE ;
	}
}