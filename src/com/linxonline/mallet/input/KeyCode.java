package com.linxonline.mallet.input ;

/**
	Provides engine specific implementation of keycodes.
	Because the keycodes used by the operating-system is unknown until
	runtime, the engine provides its own identifiers.
	It is the responsibility of the Input System to convert O/S keycodes 
	to Mallet Engine keycodes.
**/
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
	FORWARD_SLASH( '/' ), BACKWARD_SLASH( '\\' ), BRACKET_OPEN( '(' ), BRACKET_CLOSED( ')' ),

	UP, DOWN, LEFT, RIGHT,

	F1, F2, F3, F4, F5, F6, F7, F8, F9, F10, F11, F12,
	DELETE, HOME, END, PAGE_UP, PAGE_DOWN, PRINT_SCREEN, SCROLL_LOCK, INSERT,
	ESCAPE, SHIFT, CTRL, ALT, ALTGROUP, META, BACKSPACE, ENTER, TAB, CAPS_LOCK, WINDOWS,

	NUM_LOCK, NUMPAD0, NUMPAD1, NUMPAD2, NUMPAD3, NUMPAD4, NUMPAD5, NUMPAD6, NUMPAD7, NUMPAD8, NUMPAD9,

	GAMEPAD_A, GAMEPAD_B, GAMEPAD_X, GAMEPAD_Y, GAMEPAD_UP, GAMEPAD_DOWN, GAMEPAD_LEFT, GAMEPAD_RIGHT, GAMEPAD_START,
	GAMEPAD_SELECT, GAMEPAD_R1, GAMEPAD_R2, GAMEPAD_L1, GAMEPAD_L2, GAMEPAD_STICK_1, GAMEPAD_STICK_2 ;

	public final char character ;

	private KeyCode()
	{
		character = '\0' ;
	}

	private KeyCode( final char _character )
	{
		character = _character ;
	}

	/**
		Returns the keycode that matches _char.
	**/
	public static KeyCode getKeyCode( final int _code )
	{
		switch( _code )
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
			case '!' : return EXCLAMATION_MARK ;
			case '\"' : return QUOTATION ;
			case '#' : return HASH_TAG ;
			case '$' : return DOLLAR_SIGN ;
			case '%' : return PERCENTAGE ;
			case '&' : return AMPERSAND ;
			case '(' : return BRACKET_OPEN ;
			case ')' : return BRACKET_CLOSED ;
			case '*' : return ASTERIK ;
			case '+' : return PLUS ;
			case ',' : return COMMA ;
			case '-' : return MINUS ;
			case '.' : return FULL_STOP ;
			case '/' : return FORWARD_SLASH ;
			case ':' : return COLON ;
			case ';' : return SEMICOLON ;
			case '<' : return LESS_THAN ;
			case '=' : return EQUALS ;
			case '>' : return GREATER_THAN ;
			case '?' : return QUESTION_MARK ;
			case '@' : return AT ;
			case '[' : return SQUARE_BRACKET_OPEN ;
			case '\\': return BACKWARD_SLASH ;
			case ']' : return SQUARE_BRACKET_CLOSED ;
			case '^' : return CIRCUMFLEX ;
			case '\'' : return APOSTRAPHE ;
			case '{' : return CURLY_BRACKET_OPEN ;
			case '}' : return CURLY_BRACKET_CLOSED ;
			case '~' : return TIDEL;
			case '£' : return POUND_SIGN ;
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
			default  : return KeyCode.NONE ;
		}
	}
}
