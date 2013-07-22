package com.linxonline.mallet.input ;

public enum KeyCode
{
	NONE,
	NUM1( '1' ), NUM2( '2' ), NUM3( '3' ), NUM4( '4' ), NUM5( '5' ),
	NUM6( '6' ), NUM7( '7' ), NUM8( '8' ), NUM9( '9' ), NUM0( '0' ),

	a( 'a' ), b( 'b' ), c( 'c' ), d( 'd' ), e( 'e' ), f( 'f' ), g( 'g' ), h( 'h' ), i( 'i' ), j( 'j' ), k( 'k' ), 
	l( 'l' ), m( 'm' ), n( 'n' ), o( 'o' ), p( 'p' ), q( 'q' ), r( 'r' ), s( 's' ), t( 't' ), u( 'u' ), v( 'v' ),
	w( 'w' ), x( 'x' ), y( 'y' ), z( 'z' ),

	A( 'A' ), B( 'B' ), C( 'C' ), D( 'D' ), E( 'E' ), F( 'F' ), G( 'G' ), H( 'H' ), I( 'I' ), J( 'J' ), K( 'K' ),
	L( 'L' ), M( 'M' ), N( 'N' ), O( 'O' ), P( 'P' ), Q( 'Q' ), R( 'R' ), S( 'S' ), T( 'T' ), U( 'U' ), V( 'V' ),
	W( 'W' ), X( 'X' ), Y( 'Y' ), Z( 'Z' ),

	QUESTION_MARK( '?' ), EXCLAMATION_MARK( '!' ), SPACEBAR( ' ' ), POUND_SIGN( '£' ), DOLLAR_SIGN( '$' ),
	PERCENTAGE( '%' ), LESS_THAN( '<' ), GREATER_THAN( '>' ), TIDEL( '~' ), QUOTATION( '\"' ),
	CURLY_BRACKET_OPEN( '{' ), CURLY_BRACKET_CLOSED( '}' ), SQUARE_BRACKET_OPEN( '[' ), SQUARE_BRACKET_CLOSED( ']' ),
	ASTERIK( '*' ), HASH_TAG( '#' ), FULL_STOP( '.' ), COMMA( ',' ), APOSTRAPHE( '\'' ), AMPERSAND( '&' ), AT( '@' ),
	COLON( ':' ), SEMICOLON( ';' ), CIRCUMFLEX( '^' ), EQUALS( '=' ), PLUS( '+' ), MINUS( '-' ),

	SHIFT, CTRL, ALT, ALTGROUP, META, BACKSPACE, ENTER ;

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
			/* NUMBERS */
			case '0' : return NUM0 ;
			case '1' : return NUM1 ;
			case '2' : return NUM2 ;
			case '3' : return NUM3 ;
			case '4' : return NUM4 ;
			case '5' : return NUM5 ;
			case '6' : return NUM6 ;
			case '7' : return NUM7 ;
			case '8' : return NUM8 ;
			case '9' : return NUM9 ;
			/* SPECIAL CHARACTERS */
			case ' ' : return SPACEBAR ;
			case '?' : return QUESTION_MARK ;
			case '!' : return EXCLAMATION_MARK ;
			case '£' : return POUND_SIGN ;
			case '$' : return DOLLAR_SIGN ;
			case '%' : return PERCENTAGE ;
			case '<' : return LESS_THAN ;
			case '>' : return GREATER_THAN ;
			case '~' : return TIDEL;
			case '\"': return QUOTATION ;
			case '{' : return CURLY_BRACKET_OPEN ;
			case '}' : return CURLY_BRACKET_CLOSED ;
			case '[' : return SQUARE_BRACKET_OPEN ;
			case ']' : return SQUARE_BRACKET_CLOSED ;
			case '*' : return ASTERIK ;
			case '#' : return HASH_TAG ;
			case '.' : return FULL_STOP ;
			case ',' : return COMMA ;
			case '\'': return APOSTRAPHE ;
			case '&' : return AMPERSAND ;
			case '@' : return AT ;
			case ':' : return COLON ;
			case ';' : return SEMICOLON ;
			case '^' : return CIRCUMFLEX ;
			case '=' : return EQUALS ;
			case '+' : return PLUS ;
			case '-' : return MINUS ;
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