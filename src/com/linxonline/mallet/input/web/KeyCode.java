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
	FORWARD_SLASH( '/' ), BACKWARD_SLASH( '\\' ), BRACKET_OPEN( '(' ), BRACKET_CLOSED( ')' ), UNDERSCORE( '_' ),

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
			case '_' : return UNDERSCORE ;
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

	/**
		Returns the keycode that matches _char.
	**/
	public static KeyCode getKeyCode( final int _code )
	{
		switch( ( short )_code )
		{
			/* NUMBERS */
			/*case KeyEvent.VK_0 : return NUM0 ;
			case KeyEvent.VK_1 : return NUM1 ;
			case KeyEvent.VK_2 : return NUM2 ;
			case KeyEvent.VK_3 : return NUM3 ;
			case KeyEvent.VK_4 : return NUM4 ;
			case KeyEvent.VK_5 : return NUM5 ;
			case KeyEvent.VK_6 : return NUM6 ;
			case KeyEvent.VK_7 : return NUM7 ;
			case KeyEvent.VK_8 : return NUM8 ;
			case KeyEvent.VK_9 : return NUM9 ;*/
			/* SPECIAL CHARACTERS */
			/*case KeyEvent.VK_SPACE : return SPACEBAR ;
			case KeyEvent.VK_EXCLAMATION_MARK : return EXCLAMATION_MARK ;
			case KeyEvent.VK_QUOTEDBL : return QUOTATION ;
			case KeyEvent.VK_NUMBER_SIGN : return HASH_TAG ;
			case KeyEvent.VK_DOLLAR : return DOLLAR_SIGN ;
			case KeyEvent.VK_PERCENT : return PERCENTAGE ;
			case KeyEvent.VK_AMPERSAND : return AMPERSAND ;
			case KeyEvent.VK_LEFT_PARENTHESIS: return BRACKET_OPEN ;
			case KeyEvent.VK_RIGHT_PARENTHESIS : return BRACKET_CLOSED ;
			case KeyEvent.VK_ASTERISK : return ASTERIK ;
			case KeyEvent.VK_PLUS : return PLUS ;
			case KeyEvent.VK_COMMA : return COMMA ;
			case KeyEvent.VK_MINUS : return MINUS ;
			case KeyEvent.VK_PERIOD : return FULL_STOP ;
			case KeyEvent.VK_SLASH : return FORWARD_SLASH ;
			case KeyEvent.VK_COLON : return COLON ;
			case KeyEvent.VK_SEMICOLON : return SEMICOLON ;
			case KeyEvent.VK_LESS : return LESS_THAN ;
			case KeyEvent.VK_EQUALS : return EQUALS ;
			case KeyEvent.VK_GREATER : return GREATER_THAN ;
			case KeyEvent.VK_QUESTIONMARK : return QUESTION_MARK ;
			case KeyEvent.VK_AT : return AT ;
			case KeyEvent.VK_OPEN_BRACKET : return SQUARE_BRACKET_OPEN ;
			case KeyEvent.VK_BACK_SLASH: return BACKWARD_SLASH ;
			case KeyEvent.VK_CLOSE_BRACKET : return SQUARE_BRACKET_CLOSED ;
			case KeyEvent.VK_CIRCUMFLEX : return CIRCUMFLEX ;
			case KeyEvent.VK_QUOTE : return APOSTRAPHE ;
			case KeyEvent.VK_LEFT_BRACE : return CURLY_BRACKET_OPEN ;
			case KeyEvent.VK_RIGHT_BRACE : return CURLY_BRACKET_CLOSED ;
			case KeyEvent.VK_TILDE : return TIDEL;
			case KeyEvent.VK_ENTER : return ENTER ;*/
			/* UPPERCASE LETTERS */
			/*case KeyEvent.VK_A : return A ;
			case KeyEvent.VK_B : return B ;
			case KeyEvent.VK_C : return C ;
			case KeyEvent.VK_D : return D ;
			case KeyEvent.VK_E : return E ;
			case KeyEvent.VK_F : return F ;
			case KeyEvent.VK_G : return G ;
			case KeyEvent.VK_H : return H ;
			case KeyEvent.VK_I : return I ;
			case KeyEvent.VK_J : return J ;
			case KeyEvent.VK_K : return K ;
			case KeyEvent.VK_L : return L ;
			case KeyEvent.VK_M : return M ;
			case KeyEvent.VK_N : return N ;
			case KeyEvent.VK_O : return O ;
			case KeyEvent.VK_P : return P ;
			case KeyEvent.VK_Q : return Q ;
			case KeyEvent.VK_R : return R ;
			case KeyEvent.VK_S : return S ;
			case KeyEvent.VK_T : return T ;
			case KeyEvent.VK_U : return U ;
			case KeyEvent.VK_V : return V ;
			case KeyEvent.VK_W : return W ;
			case KeyEvent.VK_X : return X ;
			case KeyEvent.VK_Y : return Y ;
			case KeyEvent.VK_Z : return Z ;*/
			/*SPECIAL KEYS*/
			/*case KeyEvent.VK_WINDOWS     : return KeyCode.WINDOWS ;
			case KeyEvent.VK_INSERT      : return KeyCode.INSERT ;
			case KeyEvent.VK_SCROLL_LOCK : return KeyCode.SCROLL_LOCK ;
			case KeyEvent.VK_PRINTSCREEN : return KeyCode.PRINT_SCREEN ;
			case KeyEvent.VK_DELETE      : return KeyCode.DELETE ;
			case KeyEvent.VK_HOME        : return KeyCode.HOME ;
			case KeyEvent.VK_END         : return KeyCode.END ;
			case KeyEvent.VK_PAGE_DOWN   : return KeyCode.PAGE_UP ;
			case KeyEvent.VK_PAGE_UP     : return KeyCode.PAGE_DOWN ;
			case KeyEvent.VK_TAB         : return KeyCode.TAB ;
			case KeyEvent.VK_CAPS_LOCK   : return KeyCode.CAPS_LOCK ;
			case KeyEvent.VK_UP          : return KeyCode.UP ;
			case KeyEvent.VK_DOWN        : return KeyCode.DOWN ;
			case KeyEvent.VK_LEFT        : return KeyCode.LEFT ;
			case KeyEvent.VK_RIGHT       : return KeyCode.RIGHT ;
			case KeyEvent.VK_ESCAPE      : return KeyCode.ESCAPE ;
			case KeyEvent.VK_CONTROL     : return KeyCode.CTRL ;
			case KeyEvent.VK_ALT         : return KeyCode.ALT ;
			case KeyEvent.VK_SHIFT       : return KeyCode.SHIFT ;
			case KeyEvent.VK_META        : return KeyCode.META ;
			case KeyEvent.VK_ALT_GRAPH   : return KeyCode.ALTGROUP ;
			case KeyEvent.VK_BACK_SPACE  : return KeyCode.BACKSPACE ;
			case KeyEvent.VK_F1          : return KeyCode.F1 ;
			case KeyEvent.VK_F2          : return KeyCode.F2 ;
			case KeyEvent.VK_F3          : return KeyCode.F3 ;
			case KeyEvent.VK_F4          : return KeyCode.F4 ;
			case KeyEvent.VK_F5          : return KeyCode.F5 ;
			case KeyEvent.VK_F6          : return KeyCode.F6 ;
			case KeyEvent.VK_F7          : return KeyCode.F7 ;
			case KeyEvent.VK_F8          : return KeyCode.F8 ;
			case KeyEvent.VK_F9          : return KeyCode.F9 ;
			case KeyEvent.VK_F10         : return KeyCode.F10 ;
			case KeyEvent.VK_F11         : return KeyCode.F11 ;
			case KeyEvent.VK_F12         : return KeyCode.F12 ;
			case KeyEvent.VK_NUM_LOCK    : return KeyCode.NUM_LOCK ;
			case KeyEvent.VK_NUMPAD0     : return KeyCode.NUMPAD0 ;
			case KeyEvent.VK_NUMPAD1     : return KeyCode.NUMPAD1 ;
			case KeyEvent.VK_NUMPAD2     : return KeyCode.NUMPAD2 ;
			case KeyEvent.VK_NUMPAD3     : return KeyCode.NUMPAD3 ;
			case KeyEvent.VK_NUMPAD4     : return KeyCode.NUMPAD4 ;
			case KeyEvent.VK_NUMPAD5     : return KeyCode.NUMPAD5 ;
			case KeyEvent.VK_NUMPAD6     : return KeyCode.NUMPAD6 ;
			case KeyEvent.VK_NUMPAD7     : return KeyCode.NUMPAD7 ;
			case KeyEvent.VK_NUMPAD8     : return KeyCode.NUMPAD8 ;
			case KeyEvent.VK_NUMPAD9     : return KeyCode.NUMPAD9 ;*/
			default  : return KeyCode.NONE ;
		}
	}
}
