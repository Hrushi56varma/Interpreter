

/* *
 * Developed  for the class project in COP5556 Programming Language Principles 
 * at the University of Florida, Fall 2019.
 * 
 * This software is solely for the educational benefit of students 
 * enrolled in the course during the Fall 2019 semester.  
 * 
 * This software, and any software derived from it,  may not be shared with others or posted to public web sites or repositories,
 * either during the course or afterwards.
 * 
 *  @Beverly A. Sanders, 2019
 */
package cop5556fa19; 

import  cop5556fa19.Token;
import static cop5556fa19.Token.Kind.*;


import java.io.IOException;
import java.io.Reader;

public class Scanner {
	
	Reader r;
	int ch;
	public int pos,line;
	private enum Cond {start};


	@SuppressWarnings("serial")
	public static class LexicalException extends Exception {	
		public LexicalException(String arg0) {
			super(arg0);
		}
	}
	
	public Scanner(Reader r) throws IOException {
		this.r = r;
	}


	public Token getNext() throws Exception {

		Token t = null;
		StringBuilder  st = new StringBuilder();
		Cond cond = Cond.start;
		while (t==null) {
			ch = r.read();
			switch(cond) {
			case start:{
				pos += 1;
		          switch (ch) {
		          case 32: {  
		        	  r.mark(Integer.MAX_VALUE);
						ch = r.read();
						if(ch != 32) {
							r.reset();
						}
					}
		          
					break;
					case 9: {
						pos += 3;
						ch = r.read();
					}
					break;
					case 12: {
						ch = r.read();
					}
					break;
					case 10: {
						line += 1;
						pos = 0;
											}
					break;
					case 13: {
						
						ch = r.read();
						if(ch == 10) {
							line += 1;
							pos = 0;
							ch = r.read();
						}
					}
					break;
		          case 45: {
		        	  r.mark(Integer.MAX_VALUE);
		        	  ch = r.read();
		        	  if (ch == 45)
		        	  {
		        		  pos =+1;
		        		  while( ch != -1 && ch != 10 && ch != 13) {
		        			  pos += 1;
		        		  ch = r.read();}
		        	  }
		        	  else {
		        		  r.reset();
		        		  t = new Token(OP_MINUS, "-", pos, line);	
		        		  
		        	  }
		        	  break;
		          }
		          case 60:{//<
		        	  r.mark(Integer.MAX_VALUE);
		        	  ch = r.read();
		        	  if (ch == 60)
		        	  {
		        		  t = new Token(BIT_SHIFTL, "<<", pos, line);
		        	  }
		        	  else if (ch == 61) {
		        		  t = new Token(REL_LE, "<=", pos, line);
		        	  }
		        	  else {
		        		 r.reset();
		        		 t = new Token(REL_LT, "<", pos, line);
		        	  }
		        	  }
		          break;
		          case 61:{//=
		        	  r.mark(Integer.MAX_VALUE);
		        	  ch = r.read();
		        	  if (ch == 61) {
		        		//==
		        		  t = new Token(REL_EQEQ, "==", pos, line);
		        	  }
		        	  else {
		        		 r.reset();
		        		 t = new Token(ASSIGN, "=", pos, line);
		        	  } 
		          }
		          break;
		          case 62:{//>
		        	  r.mark(Integer.MAX_VALUE);
		        	  ch = r.read();
		        	  if (ch == 62) {
		        		  //>>
		        		  t = new Token(BIT_SHIFTR, ">>", pos, line);
		        	  }
		        	  else if(ch == 61) {
		        		  //>=
		        		  t = new Token(REL_GE, ">=", pos, line);
		        	  }
		        	  else {
		        		  r.reset();
		        		  //only>
		        		  t = new Token(REL_GT, ">", pos, line);
		        	  }		        	 	        		  
		          }
		          break;
		          case 58:{//:
		        	  r.mark(Integer.MAX_VALUE);
		        	  ch = r.read();
		        	  if (ch == 58) {
		        		  //::
		        		  t = new Token(COLONCOLON, "::", pos, line);
		        	  }
		        	  else {
		        		  r.reset();
		        		  //only:
		        		  t = new Token(COLON, ":", pos, line);
		        	  }		        	  
		          }
		          break;
		          case 46:{//.
		        	  r.mark(1000);
		        	  ch = r.read();
		        	  if ( ch == 46) {//..
		        	  
		        		  r.mark(1);
		        		  ch = r.read();
		        		  if (ch == 46) {//...
		        			  //...
		        			  t = new Token(DOTDOTDOT, "...", pos, line);
		        		  }
		        		  else {
		        			  r.reset();
		        			  //..
		        			  t = new Token(DOTDOT, "..", pos, line);
		        		  }
		        		  }
		        	  else {
		        		  r.reset();
		        		  //.
		        		  t = new Token(DOT, ".", pos, line);
		        	  }
		        	  break;
		        	  }
		          case 47:{//  /
		        	  r.mark(Integer.MAX_VALUE);
		        	  ch = r.read();
		        	  if ( ch == 47) {// //
		        		  t = new Token(OP_DIVDIV, "//", pos, line);
		        	  }
		        	  else {
		        		  r.reset();
		        		  // only /
		        		  t = new Token(OP_DIV, "/", pos, line);
		        	  }
		          }break;
		          case 126:{ // ~
		        	  r.mark(Integer.MAX_VALUE);
		        	  ch = r.read();
	        	  if ( ch == 61) {// ~=
		        		  // ~=
	        		  t = new Token(REL_NOTEQ, "~=", pos, line);
		        	  }
		        	  else {
		        		  r.reset();
		        		  t = new Token(BIT_XOR, "~", pos, line);
		        		  // only ~
		        	  }break;
		          }
		        		  
		          case 43: {
						t = new Token(OP_PLUS, "+", pos, line);	
					}
					break;
					case 42: {
						t = new Token(OP_TIMES, "*", pos, line);	
					}
					break;
					
					case 94: {
						t = new Token(OP_POW, "^", pos, line);	
					}
					break;
					case 35: {
						t = new Token(OP_HASH, "#", pos, line);	
					}
					break;
					case 38: {
						t = new Token(BIT_AMP, "&", pos, line);	
					}
					break;
					case 124: {
						t = new Token(BIT_OR, "|", pos, line);	
					}
					break;
					case 40: {
						t = new Token(LPAREN, "(", pos, line);	
					}
					break;
					case 41: {
						t = new Token(RPAREN, ")", pos, line);	
					}
					break;
					case 123: {
						t = new Token(LCURLY, "{", pos, line);	
					}
					break;
					case 125: {
						t = new Token(RCURLY, "}", pos, line);	
					}
					break;
					case 91: {
						t = new Token(LSQUARE, "[", pos, line);	
					}
					break;
					case 37:{
						t = new Token(OP_MOD, "%", pos, line);	
					}
					break;
					case 93: {
						t = new Token(RSQUARE, "]", pos, line);	
					}
					break;
					case 59: {
						t = new Token(SEMI, ";", pos, line);	
					}
					break;
					case 44: {
						t = new Token(COMMA, ",", pos, line);
					}
					break;
					case -1:{
						t = new Token(EOF, "EOF", pos, line);
						break;
					}
					case 92:{
						if (ch == 'a') {
							st.append("\u0007");
						}
						
						else if (ch == 'b') {
							st.append("\u0008");
						}
						
						else if ( ch == 'f') {
							st.append("\u0012");
						}
						else if ( ch == 'n') {
							st.append("\u00010");
						}
						else if ( ch == 'r') {
							st.append("\u00013");
						}
						else if ( ch == 't') {
							st.append("\u0009");
						}
						else if ( ch == 'v') {
							st.append("\u0011");
						}
						else if ( ch == 92) {
							st.append("\\");
						}
						else if ( ch == '"') {
							st.append('"');
						}
						else if ( ch == 39) {
							st.append('\'');
						}
						else {
							st.append((char)ch);
							ch = r.read();
							}
							}break;
					
					case 34: {
						st.append((char)ch);
						ch = r.read();
						while(ch != 34) {
						if ( ch == 92) {
							if (ch == 'a') {
								st.append("\u0007");
							}
							
							else if (ch == 'b') {
								st.append("\u0008");
							}
							
							else if ( ch == 'f') {
								st.append("\u0012");
							}
							else if ( ch == 'n') {
								st.append("\u00010");
							}
							else if ( ch == 'r') {
								st.append("\u00013");
							}
							else if ( ch == 't') {
								st.append("\u0009");
							}
							else if ( ch == 'v') {
								st.append("\u0011");
							}
							else if ( ch == 92) {
								st.append("\\");
							}
							else if ( ch == '"') {
								st.append('"');
							}
							else if ( ch == 39) {
								st.append('\'');
							}
								}
						else {
							st.append((char)ch);
							ch = r.read();
							}
						}
						st.append((char)ch);
						t = new Token(STRINGLIT,st.toString(),pos,line);
						break;}
						default:{
							if(Character.isDigit(ch))
							{
							//ch = r.read();
							if (ch == 0)
							{
								t = new Token(INTLIT, "0", pos, line);
							}
							else
							{
								while(Character.isDigit(ch))
								{
									r.mark(Integer.MAX_VALUE);
									st.append((char)ch);
									ch = r.read();
									t = new Token(INTLIT, st.toString(), pos-st.length(), line);
								}r.reset();
							}
						}
							else if(Character.isJavaIdentifierStart(ch) && ch != 36 && ch != 95)
						{
						st.append((char)ch);
						r.mark(Integer.MAX_VALUE);
						ch = r.read();
						
							while(Character.isJavaIdentifierPart(ch) || Character.isDigit(ch) || ch == 36 || ch == 95){
								r.mark(Integer.MAX_VALUE);
								st.append((char)ch);
								ch = r.read();
							}
							r.reset(); 
							t = new Token(NAME, st.toString(), pos-st.length(), line); 
							t = checkInkeywords(t,st);
						}
							else{
								throw new LexicalException("Invalid characters found");
								}
			}
						
		    //if (r.read() == -1) { return new Token(EOF,"eof",0,0);}
			//throw new LexicalException("Useful error message");
		
	}

}
			}}
		return t;}
	
private Token checkInkeywords(Token t,StringBuilder st) {
	if(st.toString().equals("and")) {
		t = new Token(KW_and, st.toString(), pos-st.length(), line); 
	}
	else if (st.toString().equals("break")) {
		t = new Token(KW_break, st.toString(), pos-st.length(), line); 
	}
	else if (st.toString().equals("do")) {
		t = new Token(KW_do, st.toString(), pos-st.length(), line); 
	}
	else if (st.toString().equals("else")) {
		t = new Token(KW_else, st.toString(), pos-st.length(), line); 
	}
	else if (st.toString().equals("elseif")) {
		t = new Token(KW_elseif, st.toString(), pos-st.length(), line); 
	}
	else if (st.toString().equals("end")) {
		t = new Token(KW_end, st.toString(), pos-st.length(), line); 
	}
	else if (st.toString().equals("false")) {
		t = new Token(KW_false, st.toString(), pos-st.length(), line); 
	}
	else if (st.toString().equals("for")) {
		t = new Token(KW_for, st.toString(), pos-st.length(), line); 
	}
	else if (st.toString().equals("function")) {
		t = new Token(KW_function, st.toString(), pos-st.length(), line); 
	}
	else if (st.toString().equals("goto")) {
		t = new Token(KW_goto, st.toString(), pos-st.length(), line); 
	}
	else if (st.toString().equals("if")) {
		t = new Token(KW_if, st.toString(), pos-st.length(), line); 
	}
	else if (st.toString().equals("in")) {
		t = new Token(KW_in, st.toString(), pos-st.length(), line); 
	}
	else if (st.toString().equals("local")) {
		t = new Token(KW_local, st.toString(), pos-st.length(), line); 
	}
	else if (st.toString().equals("nil")) {
		t = new Token(KW_nil, st.toString(), pos-st.length(), line); 
	}
	else if (st.toString().equals("not")) {
		t = new Token(KW_not, st.toString(), pos-st.length(), line); 
	}
	else if (st.toString().equals("or")) {
		t = new Token(KW_or, st.toString(), pos-st.length(), line); 
	}
	else if (st.toString().equals("repeat")) {
		t = new Token(KW_repeat, st.toString(), pos-st.length(), line); 
	}
	else if (st.toString().equals("return")) {
		t = new Token(KW_return, st.toString(), pos-st.length(), line); 
	}
	else if (st.toString().equals("then")) {
		t = new Token(KW_then, st.toString(), pos-st.length(), line); 
	}
	else if (st.toString().equals("true")) {
		t = new Token(KW_true, st.toString(), pos-st.length(), line); 
	}
	else if (st.toString().equals("until")) {
		t = new Token(KW_until, st.toString(), pos-st.length(), line); 
	}
	else if (st.toString().equals("while")) {
		t = new Token(KW_while, st.toString(), pos-st.length(), line); 
	
	}/*else {
		t = new Token(NAME, st.toString(), pos-st.length(), line); 
		//throw new LexicalException("Invalid tokens found");
	}*/
	
	
	return t;
}
}
			
