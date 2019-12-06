/**
 * Developed  for the class project in COP5556 Programming Language Principles 
 * at the University of Florida, Fall 2019.
 * 
 * This software is solely for the educational benefit of students 
 * enrolled in the course during the Fall 2019 semester.  
 * 
 * This software, and any software derived from it,  may not be shared with others or posted to public web sites,
 * either during the course or afterwards.
 * 
 *  @Beverly A. Sanders, 2019
 */

package cop5556fa19;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cop5556fa19.AST.Block;
import cop5556fa19.AST.Chunk;
import cop5556fa19.AST.Exp;
import cop5556fa19.AST.ExpBinary;
import cop5556fa19.AST.ExpFalse;
import cop5556fa19.AST.ExpFunction;
import cop5556fa19.AST.ExpFunctionCall;
import cop5556fa19.AST.ExpInt;
import cop5556fa19.AST.ExpName;
import cop5556fa19.AST.ExpNil;
import cop5556fa19.AST.ExpString;
import cop5556fa19.AST.ExpTable;
import cop5556fa19.AST.ExpTableLookup;
import cop5556fa19.AST.ExpTrue;
import cop5556fa19.AST.ExpUnary;
import cop5556fa19.AST.ExpVarArgs;
import cop5556fa19.AST.Expressions;
import cop5556fa19.AST.Field;
import cop5556fa19.AST.FieldExpKey;
import cop5556fa19.AST.FieldImplicitKey;
import cop5556fa19.AST.FieldNameKey;
import cop5556fa19.AST.FuncBody;
import cop5556fa19.AST.FuncName;
import cop5556fa19.AST.Name;
import cop5556fa19.AST.ParList;
import cop5556fa19.AST.RetStat;
import cop5556fa19.AST.Stat;
import cop5556fa19.AST.StatAssign;
import cop5556fa19.AST.StatBreak;
import cop5556fa19.AST.StatDo;
import cop5556fa19.AST.StatFor;
import cop5556fa19.AST.StatForEach;
import cop5556fa19.AST.StatFunction;
import cop5556fa19.AST.StatGoto;
import cop5556fa19.AST.StatIf;
import cop5556fa19.AST.StatLabel;
import cop5556fa19.AST.StatLocalAssign;
import cop5556fa19.AST.StatLocalFunc;
import cop5556fa19.AST.StatRepeat;
import cop5556fa19.AST.StatWhile;
import cop5556fa19.Token.Kind;
import static cop5556fa19.Token.Kind.*;

public class Parser {
	
	@SuppressWarnings("serial")
	public class SyntaxException extends Exception {
		Token t;
		
		public SyntaxException(Token t, String message) {
			super(t.line + ":" + t.pos + " " + message);
		}
	}
	
	final Scanner scanner;
	Token t;  //invariant:  this is the next token


	public Parser(Scanner s) throws Exception {
		this.scanner = s;
		t = scanner.getNext(); //establish invariant
	}


	Exp exp() throws Exception {
		Token first = t;
		Exp e0 = andExp();
		while (isKind(KW_or)) {
			Token op = consume();
			Exp e1 = andExp();
			e0 = new ExpBinary(first, e0, op, e1);
		}
		return e0;
	}

	
private Exp andExp() throws Exception{
	
	Token first = t;
	// TODO Auto-generated method stub
	// throw new UnsupportedOperationException("andExp"); //I find this is a more
	// useful placeholder than returning null.
	Exp e0 = CompareExp();
	while (isKind(KW_and)) {
		Token op = consume();
		Exp e1 = CompareExp();
		e0 = new ExpBinary(first, e0, op, e1);
	}
	return e0;
		// TODO Auto-generated method stub
		 //I find this is a more useful placeholder than returning null.
	}

private Exp CompareExp() throws Exception {
	Token first = t;
	Exp e0 = BitorExp();
	while (isKind(REL_GT) || isKind(REL_LT) || isKind(REL_GE) || isKind(REL_LE) || isKind(REL_EQEQ)
			|| isKind(REL_NOTEQ)) {
		Token op = consume();
		Exp e1 = BitorExp();
		e0 = new ExpBinary(first, e0, op, e1);
	}
	return e0;
}

private Exp BitorExp() throws Exception {
	Token first = t;
	Exp e0 = BitXorExp();
	while (isKind(BIT_OR)) {
		Token op = consume();
		Exp e1 = BitXorExp();
		e0 = new ExpBinary(first, e0, op, e1);
	}
	return e0;
}

private Exp BitXorExp() throws Exception {
	Token first = t;
	Exp e0 = BitAmpExp();
	while (isKind(BIT_XOR)) {
		Token op = consume();
		Exp e1 = BitAmpExp();
		e0 = new ExpBinary(first, e0, op, e1);
	}
	return e0;
}

private Exp BitAmpExp() throws Exception {
	Token first = t;
	Exp e0 = BitShiftExp();
	while (isKind(BIT_AMP)) {
		Token op = consume();
		Exp e1 = BitShiftExp();
		e0 = new ExpBinary(first, e0, op, e1);
	}
	return e0;
}

private Exp BitShiftExp() throws Exception {
	Token first = t;
	Exp e0 = DotDotExp();
	while (isKind(BIT_SHIFTL) || isKind(BIT_SHIFTR)) {
		Token op = consume();
		Exp e1 = DotDotExp();
		e0 = new ExpBinary(first, e0, op, e1);
	}
	return e0;
}

private Exp DotDotExp() throws Exception {
	Token first = t;
	Exp e0 = SumExp();
	while (isKind(DOTDOT)) {
		Token op = consume();
		Exp e1 = SumExp();
		if(isKind(DOTDOT)) {
			match(DOTDOT);
			e0 = new ExpBinary(first,e0,op,Expressions.makeBinary(e1, DOTDOT, DotDotExp()));
		}
		else {
		e0 = new ExpBinary(first, e0, op, e1);
		}
	}
	return e0;
}

private Exp SumExp() throws Exception {
	Token first = t;
	Exp e0 = FactorExp();
	while (isKind(OP_PLUS) || isKind(OP_MINUS)) {
		Token op = consume();
		Exp e1 = FactorExp();
		e0 = new ExpBinary(first, e0, op, e1);
	}
	return e0;
}

private Exp FactorExp() throws Exception {
	Token first = t;
	Exp e0 = UnaryExp();
	while (isKind(OP_TIMES) || isKind(OP_DIV) || isKind(OP_DIVDIV) || isKind(OP_MOD)) {
		Token op = consume();
		Exp e1 = UnaryExp();
		e0 = new ExpBinary(first, e0, op, e1);
	}
	return e0;
}

private Exp UnaryExp() throws Exception {
	Token first = t;
	Exp e0 = PowerExp();
	while (isKind(OP_HASH) || isKind(KW_not) || isKind(OP_MINUS) || isKind(BIT_XOR)) {
		Token op = consume();
		Exp e1 = PowerExp();
		e0 = new ExpBinary(first, e0, op, e1);

	}
	return e0;
}

private Exp PowerExp() throws Exception {
	Token first = t;
	Exp e0 = term();
	while (isKind(OP_POW)) {
		Token op = consume();
		Exp e1 = term();
		if(isKind(OP_POW)) {
			match(OP_POW);
			e0 = new ExpBinary(first,e0,op,Expressions.makeBinary(e1, OP_POW, PowerExp()));
		}
		else {
		e0 = new ExpBinary(first, e0, op, e1);
		}
	}
	return e0;
}

private Exp term() throws Exception {
	Token first = t;
	Exp e0 = null;
	if (isKind(STRINGLIT)) {
		e0 = new ExpString(t);
		consume();
	} else if (isKind(INTLIT)) {
		e0 = new ExpInt(t);
		consume();
	} else if (isKind(KW_nil)) {
		e0 = new ExpNil(t);
		consume();
	} else if (isKind(KW_false)) {
		e0 = new ExpFalse(t);
		consume();
	} else if (isKind(KW_true)) {
		e0 = new ExpTrue(t);
		consume();
	} else if (isKind(OP_MINUS)) {
		Token op = consume();
		Exp e1 = exp();
		e0 = new ExpUnary(first, OP_MINUS, e1);
	} else if (isKind(OP_PLUS)) {
		Token op = consume();
		Exp e1 = exp();
		e0 = new ExpUnary(first, OP_PLUS, e1);
	} 
	else if (isKind(DOTDOTDOT)) {
		e0 = new ExpVarArgs(t);
		consume();
	} else if (isKind(KW_function)) {
		match(KW_function);
		FuncBody e1 = funcbody();
		e0 = new ExpFunction(first, e1);
		
	} else if(isKind(LCURLY)) {
		match(LCURLY);
		List<Field> e1 = fieldlist();
		e0 = new ExpTable(first, e1);
	} else if(isKind(NAME)) {
		
		e0 = new ExpName(first);
		match(NAME);
		
	} else if(isKind(LPAREN)) {
		match(LPAREN);
	    e0 = exp();
	    if(isKind(RPAREN)) {
	    	match(RPAREN);
	    }
	    
	} else {
		throw new SyntaxException(t,"Invalid Syntax");
	}
	return e0;
	// else if ()
}

private FuncBody funcbody() throws Exception {
	Token first = t;
	FuncBody fbody = null;
	if (isKind(LPAREN)) {
		match(LPAREN);
		if (isKind(NAME)) {
			parlist();
		}
		if (isKind(RPAREN)) {
			match(RPAREN);
			dblock(KW_end);
			if (isKind(KW_end)) {
				match(KW_end);
			} else {
				Token t = consume();
				error(t.kind);
			}
		} 
		
	} else {
		Token t = consume();
		error(t.kind);
	}
	return fbody;
}

private ParList parlist() throws Exception {
	Token first = t;
	ParList pl = null;
	boolean hasVarArgs = false;
	if (isKind(DOTDOTDOT)) {
		List<Name> nl = new ArrayList<>();
		pl = new ParList(first, nl, true);
		consume();
	} else if (isKind(NAME)) {
		List<Name> nl = namelist();
		while (isKind(COMMA)) {
			consume();
			if (isKind(DOTDOTDOT)) {
				hasVarArgs = true;
				consume();
			} else {
				Token t = consume();
				error(t.kind);
			}
			pl = new ParList(first, nl, hasVarArgs);
		}
	}
	return pl;
}

private List<Name> namelist() throws Exception {
	Token first = t;
	boolean hasVarArgs = false;
	List<Name> nl = new ArrayList<>();
	if (isKind(NAME)) {
		nl.add(new Name(t, t.text));
		match(NAME);
		while (isKind(COMMA)) {
			consume();
			if (isKind(NAME)) {
				nl.add(new Name(first, t.text));
				consume();
			} else if (isKind(DOTDOTDOT)) {
				hasVarArgs = true;
				consume();
				
			}
			else {
				Token t = consume();
				error(t.kind);
			}

		}
	}
	return nl;

}

private List<Field> fieldlist() throws Exception {
	Token first = t;
	
	List<Field> fl = new ArrayList<>();
	while(!isKind(RCURLY)) {
	if (isKind(LSQUARE)) {
		match(LSQUARE);
		Exp e0 = exp();
		match(RSQUARE);
		match(ASSIGN);
		Exp e1 = exp();
		Field f0 = new FieldExpKey(first, e0, e1);
		fl.add(f0);
		if (isKind(COMMA)) {
			match(COMMA);
		} else if (isKind(SEMI)) {
			match(SEMI);
		} else {
			if(!isKind(RCURLY)) {
			Token t = consume();
			error(t.kind);
			}
		}
	} else if (isKind(NAME)) {
		Token temp = t;
		Exp e0 = exp();
		//Field f0 = new FieldImplicitKey(first,e0);
		//fl.add(f0); //name is matched
		if(isKind(ASSIGN)) {
			match(ASSIGN);
			Exp e1 = exp();
			Field f0 = new FieldNameKey(temp, new Name(temp, temp.text), e1);
			fl.add(f0);
			if (isKind(COMMA)) {
				match(COMMA);
			} else if (isKind(SEMI)) {
				match(SEMI);
			} else {
				if(!isKind(RCURLY)) {
				Token t = consume();
				error(t.kind);
		}}
		}else {
			Field f0 = new FieldImplicitKey(first, e0);
			fl.add(f0);
			if(isKind(COMMA)) {
				match(COMMA);
			}
		}
		//else {
			//Exp e1 = exp();
			//Field f0 = new FieldImplicitKey(first, e1);
			//fl.add(f0);
			//if (isKind(COMMA)) {
				//match(COMMA);
			//} else if (isKind(SEMI)) {
				//match(SEMI);
			//}
	   //}
	} else{
		Exp e0 = exp();
		Field f0 = new FieldImplicitKey(first, e0);
		fl.add(f0);
		if (isKind(COMMA)) {
			match(COMMA);
		} else if (isKind(SEMI)) {
			match(SEMI);
		}
	}
	}match(RCURLY);
return fl;
}


	private Block block() throws Exception { 
		List<Stat> sl = new ArrayList<>();
		if(isKind(EOF)) {
			t = new Token(SEMI," dummy", 0, 0);
		}
		while(!isKind(EOF)) { 
		
		Stat s = statement();
		if(s!=null) {
		sl.add(s);
		}
		
		}
		return new Block(t, sl); //this is OK for Assignment 2
	}
	public boolean returnachived = false;
	public Stat statement()  throws Exception{
		Token first = t;
		Stat e0 = null;
		switch(t.kind) {
		case SEMI:{
			match(SEMI);
		}break;
		case COLONCOLON : {
			e0 = label();
	        
		}break;
		case KW_break : {
			e0 = BreakBlock();
		}break;
		
		case KW_goto : {
			e0 = Goto();
		}break;
		
		case KW_do : {
			e0 = Do();
		}break;
		
		case KW_while : {
			e0 = While();
		}break;
		
		case KW_repeat : {
			e0 = Repeat();
		}break;
		
		case KW_if : {
			e0 = If();
		}break;
		
		case KW_for : {
			e0 = For();
		}break;
		
		case KW_function : {
			e0 = Function();
		}break;
		
		case KW_local : {
			e0 = Local();
		}break;
		case KW_return : {
			if(returnachived) {
				throw new SyntaxException(t, "Double Return Exception");
			}
				e0 = Return();
				returnachived = true;
		}break;
		
		case NAME : {
			List<Exp> varlist = new ArrayList<>();
			Exp e = Varexp();
			varlist.add(e);
			while(isKind(COMMA)) {
				match(COMMA);
				Exp e1 = Varexp();
				varlist.add(e1);
			}
			match(ASSIGN);
			List<Exp> explist = new ArrayList<>();
			Exp ex = exp();
			while(isKind(LSQUARE)) {
				match(LSQUARE);
				List<Exp> argsexpl = new ArrayList<>();
				Exp ea = exp();
				match(RSQUARE);
				e = new ExpTableLookup(first, ex, ea);
		}
			ex = exprlist(ex);
			explist.add(ex);
			
			while(isKind(COMMA)) {
				match(COMMA);
				Exp e1 = exp();
				Exp exp = exprlist(e1);
				explist.add(exp);
			}
			 e0 = new StatAssign(first, varlist, explist);
			break;
		}
		
		
		default : {
			
		}
		}
		return e0; //return statement
		
	}
	
	
	public Exp exprlist(Exp ex) throws Exception {
		Token first = t;
		Exp e = ex;
			while(isKind(LPAREN) || isKind(LSQUARE) || isKind(DOT) || isKind(COLON)) {
			if(isKind(LPAREN)) {
				match(LPAREN);
				List<Exp> argsexpl = new ArrayList<>();
				if(!isKind(RPAREN)) {
					if(isKind(NAME)) {
						Exp ename = exp();
						while(isKind(LSQUARE)) {
							match(LSQUARE);
							List<Exp> argsexplintsble = new ArrayList<>();
							Exp e1 = exp();
							match(RSQUARE);
							e = new ExpTableLookup(first, ename, e1);
						}
						while(isKind(DOT)) {
							match(DOT);
							List<Exp> argsexplintsble = new ArrayList<>();
							Exp e1 = exp();
							e = new ExpTableLookup(first, ename, e1);
						}
						while(isKind(COLON)) {
							match(COLON);
							List<Exp> argsexplintsble = new ArrayList<>();
							Exp e1 = exp();
							e = new ExpTableLookup(first, ename, e1);
						}
							
						
					}
					else {
						argsexpl.add(exp());
					}
				}
				while(isKind(COMMA)) {
					match(COMMA);	
					Exp ea = exp();
					argsexpl.add(ea);
				}match(RPAREN);
				e = new ExpFunctionCall(first, e , argsexpl );
			}else if(isKind(LSQUARE)) {
				match(LSQUARE);
				 Exp a = exp();
				if(isKind(DOT)) {	
				while(isKind(DOT)) {
					match(DOT);
					Token b = consume();
					ExpString n = new ExpString(b);	
				a = new ExpTableLookup(first, a, n);
				}
				}
				e = new ExpTableLookup(first, e, a);
				match(RSQUARE);
			}
			else if(isKind(DOT)) {
				match(DOT);
				Token b = consume();
				ExpString n = new ExpString(b);	
				e = new ExpTableLookup(first, e, n );
			}else if(isKind(COLON)) {
				match(COLON);
				List<Exp> argsexpl = new ArrayList<>();
				argsexpl.add(e);
				Token b = consume();
				ExpString n = new ExpString(b);
				e = new ExpTableLookup(first, e, n);
				if(isKind(LPAREN)) {
					match(LPAREN);
					Exp earg = exp();
					argsexpl.add(earg);
					while(isKind(COMMA)) {
						match(COMMA);
						Exp erg = exp();
						argsexpl.add(erg);
					}match(RPAREN);
					e = new ExpFunctionCall(first, e, argsexpl);
				}else if(isKind(STRINGLIT)) {
					Token s = consume();
					argsexpl.add(new ExpString(s));
				} else if(isKind(LCURLY)) {
					match(LCURLY);
					List<Field> e1 = fieldlist();
					Exp et = new ExpTable(first, e1);
					argsexpl.add(et);
				    e = new ExpFunctionCall(first, e, argsexpl);//verify
				}
						
			}
			}return e;
	}
	
	public Exp Varexp() throws Exception{
		Token first = t;
		Exp e = null;
		//reserved for args
		if(isKind(NAME)) {
			Token a = consume();
			 e = new ExpName(a);
			while(isKind(LSQUARE) || isKind(DOT) || isKind(COLON) || isKind(LPAREN)) {
				if(isKind(LSQUARE)) {
					match(LSQUARE);
					List<Exp> argsexpl = new ArrayList<>();
					Exp e1 = exp();
					match(RSQUARE);
					e = new ExpTableLookup(first, e, e1);
				}
				else if(isKind(DOT)) {
					match(DOT);
					Token b = consume();
					ExpString n = new ExpString(b);	
					e = new ExpTableLookup(first, e, n );
				}
				else if(isKind(COLON)) {
					match(COLON);
					List<Exp> argsexpl = new ArrayList<>();
					Exp e2 = exp();
					argsexpl.add(e2);
					Token b = consume();
					ExpString n  = new ExpString(b);
					e = new ExpTableLookup(first, e, n );
					if(isKind(LPAREN)) {
						match(LPAREN);
						while(isKind(COMMA)) {
							match(COMMA);
							Exp ea = exp();
							argsexpl.add(ea);
						}match(RPAREN);
						e = new ExpFunctionCall(first, e, argsexpl);
					} else if(isKind(STRINGLIT)) {
						Token s = consume();
						argsexpl.add(new ExpString(s));
					} else if(isKind(LCURLY)) {
						List<Field> e1 = fieldlist();
						Exp et = new ExpTable(first, e1);
						argsexpl.add(et);
					    e = new ExpFunctionCall(first, e, argsexpl);//verify
					}
				} else if(isKind(LPAREN)) {
					match(LPAREN);
					List<Exp> argsexpl = new ArrayList<>();
					if(!isKind(RPAREN)) {
						Exp ea1 = exp();
						argsexpl.add(ea1);
					while(isKind(COMMA)) {
						match(COMMA);
						Exp ea = exp();
						argsexpl.add(ea);
					}}match(RPAREN);
					e = new ExpFunctionCall(first, e, argsexpl);
			        }
		}
	}else if(isKind(LPAREN)) {
		match(LPAREN);
		 e = exp();
		match(RPAREN);
		while(isKind(LSQUARE) || isKind(DOT) || isKind(COLON) || isKind(LPAREN)) {
			if(isKind(LSQUARE)) {
				match(LSQUARE);
				List<Exp> argsexpl = new ArrayList<>();
				Exp e1 = exp();
				match(RSQUARE);
				e = new ExpTableLookup(first, e, e1);
			}
			else if(isKind(DOT)) {
				match(DOT);
				Token b = consume();
				ExpString n = new ExpString(b);	
				e = new ExpTableLookup(first, e, n );
			}
			else if(isKind(COLON)) {
				match(COLON);
				List<Exp> argsexpl = new ArrayList<>();
				Exp e2 = exp();
				argsexpl.add(e2);
				Token b = consume();
				ExpString n  = new ExpString(b);
				e = new ExpTableLookup(first, e, n );
				if(isKind(LPAREN)) {
					match(LPAREN);
					while(isKind(COMMA)) {
						match(COMMA);
						Exp ea = exp();
						argsexpl.add(ea);
					}match(RPAREN);
					e = new ExpFunctionCall(first, e, argsexpl);
				} else if(isKind(STRINGLIT)) {
					Token s = consume();
					argsexpl.add(new ExpString(s));
				} else if(isKind(LCURLY)) {
					List<Field> e1 = fieldlist();
					Exp et = new ExpTable(first, e1);
					argsexpl.add(et);
				    e = new ExpFunctionCall(first, e, argsexpl);//verify
				}
			} else if(isKind(LPAREN)) {
				match(LPAREN);
				List<Exp> argsexpl = new ArrayList<>();
				while(isKind(COMMA)) {
					match(COMMA);
					Exp ea = exp();
					argsexpl.add(ea);
				}match(RPAREN);
				e = new ExpFunctionCall(first, e, argsexpl);
		        }
	}
	}return e;
	}
	public Stat Return() throws Exception{
		match(KW_return);
		Token first = t;
		Stat e0 = null;
		List<Exp> retexp = new ArrayList<>();
		Exp e1 = exp();
		retexp.add(e1);
		while(isKind(COMMA)) {
			match(COMMA);
			Exp e = exp();
			retexp.add(e);
		}e0 = new RetStat(first, retexp);
		
		return e0;
	}

	public Stat BreakBlock() throws Exception{
		Token first = t;
		match(KW_break);
		Stat e0 = null;
		e0 = new StatBreak(first);
		return e0;
	}
	
	public Stat label() throws Exception {
		match(COLONCOLON);
		Stat e0 = null;
		// name object
		if(isKind(NAME)) {
			Token a = consume();
			Name name = new Name(a, a.text);
			e0 = new StatLabel(a, name, null, 0);
		}
		match(COLONCOLON);
		return e0;
	}
	
	public Stat Goto() throws Exception{
		Token first = t;
		Stat e0 = null;
		match(KW_goto);
		Token a = consume();
		Name name = new Name(a, a.text);
		e0 = new StatGoto(first, name);
			
			//e0 = new StatLabel(a, name);
		
		return e0;
	} 
	
	public Stat Do() throws Exception {
		boolean loctemp = returnachived;
		Token first = t;
		Stat e0 = null;
		match(KW_do);
		returnachived = false;
		Block b = dblock(KW_end);
		e0 =  new StatDo(first, b);
		returnachived = loctemp;
		match(KW_end);
		return e0;
	} 
	
	public Stat While() throws Exception {
		boolean loctemp = returnachived;
		Token first = t;
		Stat e0 = null;
		match(KW_while);
		Exp e1 = exp();
		match(KW_do);
		returnachived = false;
		Block b = dblock(KW_end);
		match(KW_end);
		returnachived = loctemp;
		e0 = new StatWhile(first, e1, b);
		return e0;
	}

	public Stat Repeat() throws Exception {
		boolean loctemp = returnachived;
		Token first = t;
		Stat e0 = null;
		match(KW_repeat);
		returnachived = false;
		Block b = dblock(KW_until);
		match(KW_until);
		Exp e1 = exp();
		returnachived = loctemp;
		e0 = new StatRepeat(first, b, e1);
		
		return e0;
	}
	
	public Stat If() throws Exception {
		boolean loctemp = returnachived;
		Token first = t;
		Stat e0 = null;
		match(KW_if);
		List<Exp> expl = new ArrayList<>();
		List<Block> bl = new ArrayList<>();
		Exp e1 = exp();
		expl.add(e1);
		match(KW_then);
		returnachived = false;
		Block b = dblock(KW_elseif, KW_else, KW_end);
		returnachived = loctemp;
		bl.add(b);
		while(isKind(KW_elseif)) {
			match(KW_elseif);
			Exp e = exp();
			expl.add(e);
			match(KW_then);
			returnachived = false;
			Block bei = dblock(KW_else, KW_elseif, KW_end);
			returnachived = loctemp;
			bl.add(bei);
		}
		if(isKind(KW_else)) {
			match(KW_else);
			returnachived = false;
			Block be = dblock(KW_end);
			returnachived = loctemp;
			bl.add(be);
		}
		match(KW_end);
		e0 = new StatIf(first, expl, bl);
		return e0;
	}
	
	public Stat For() throws Exception {
		boolean loctemp = returnachived;
		Token first = t;
		Stat e0 = null;
		match(KW_for);
		Token a = consume();
		List<ExpName> expn = new ArrayList<>();
		expn.add(new ExpName(a));
		List<Exp> exps = new ArrayList<>();
		if(isKind(COMMA) || isKind(KW_in)) {
			if(isKind(COMMA)) {
			match(COMMA);
			Token c = consume();
			expn.add(new ExpName(c));
			while(isKind(COMMA)) {
				match(COMMA);
				Token b = consume();
				expn.add(new ExpName(b));
			}
			}
			if(isKind(KW_in)) {
			match(KW_in);
			Exp e1 = exp();
			exps.add(e1);
			while(isKind(COMMA)) {
				match(COMMA);
				Exp e = exp();
				exps.add(e);
			}
			}
			
			match(KW_do);
			returnachived = false;
			Block b = dblock(KW_end);
			match(KW_end);
			returnachived = loctemp;
			e0 = new StatForEach(first, expn, exps, b);
			}
		else if (isKind(ASSIGN)) {
			match(ASSIGN);
			ExpName e = new ExpName(a);
			Exp e1 = exp();
			match(COMMA);
			Exp e2 = exp();
			Exp e3 = null;
			if(isKind(COMMA)) {
				match(COMMA);
				 e3 = exp();
			}// e3 error
			match(KW_do);
			Block b = dblock(KW_end);
			match(KW_end);
			e0 = new StatFor(first, e, e1, e2, e3, b);
		}return e0;
	}
	
	public Stat Function() throws Exception{
		Token first = t;
		Stat e0 = null;
		match(KW_function);
		List<ExpName> expf = new ArrayList<>();	
		//match(LPAREN);
		if(isKind(NAME)) {
			Token a = consume();
			expf.add(new ExpName(a));
			while(isKind(COMMA)) {
				match(COMMA);
				Token a1 = consume();
				expf.add(new ExpName(a1));
			}
		}
		while(isKind(DOT)) {
			match(DOT);
			Token b = consume();
			expf.add(new ExpName(b));	
		}
		ExpName e = null;
		if(isKind(COLON)) {
			match(COLON);
			Token c = consume();
		    e = new ExpName(c);
		}
		match(RPAREN);
		FuncName fn = new FuncName(first, expf, e );
		FuncBody e1 = funcbody();
		e0 = new StatFunction(first, fn, e1);
		//e0 = new ExpFunction(first, e1);
		return e0;
	}
	
	public Stat Local() throws Exception{
		Token first = t;
		Stat e0 = null;
		List<ExpName> explocal = new ArrayList<>();	
		match(KW_local);
		if(isKind(KW_function)) {
			match(KW_function);
			Token a = consume();
			explocal.add(new ExpName(a));
			ExpName e = null;
			FuncName fn = new FuncName(first, explocal, e);
			FuncBody e1 = funcbody();
			e0 = new StatLocalFunc(first, fn, e1);
		}
		else if(isKind(NAME)) {
			Token a = consume();
			explocal.add(new ExpName(a));
			while(isKind(COMMA)) {
				match(COMMA);
				Token b = consume();
				explocal.add(new ExpName(b));
			}
			List<Exp> expalocal = new ArrayList<>();
			if (isKind(ASSIGN)) {
				match(ASSIGN);
				Exp e1 = exp();
				expalocal.add(e1);
				while(isKind(COMMA)) {
					match(COMMA);
					Exp e2 = exp();
					expalocal.add(e2);
				}
			}else {
				 expalocal = null;
			}match(KW_end);
			e0 = new StatLocalAssign(first, explocal, expalocal);
		}return e0;
		
}
	
	private Block dblock(Kind... kind) { 
		List<Stat> sl = new ArrayList<>();
		if(isKind(EOF)) {
			t = new Token(SEMI," dummy", 0, 0);
		}
		while(!isKind(kind)) { 
		try {
		Stat s = statement();
		if(s!=null) {
		sl.add(s);
		}
		}
		catch(Exception e) {
			System.out.println("Invalid match found"+e);
		}
		}
		return new Block(t, sl); //this is OK for Assignment 2
	}

	protected boolean isKind(Kind kind) {
		return t.kind == kind;
	}

	protected boolean isKind(Kind... kinds) {
		for (Kind k : kinds) {
			if (k == t.kind)
				return true;
		}
		return false;
	}

	/**
	 * @param kind
	 * @return
	 * @throws Exception
	 */
	Token match(Kind kind) throws Exception {
		Token tmp = t;
		if (isKind(kind)) {
			consume();
			return tmp;
		}
		error(kind);
		return null; // unreachable
	}

	/**
	 * @param kind
	 * @return
	 * @throws Exception
	 */
	Token match(Kind... kinds) throws Exception {
		Token tmp = t;
		if (isKind(kinds)) {
			consume();
			return tmp;
		}
		StringBuilder sb = new StringBuilder();
		for (Kind kind1 : kinds) {
			sb.append(kind1).append(kind1).append(" ");
		}
		error(kinds);
		return null; // unreachable
	}

	Token consume() throws Exception {
		Token tmp = t;
        t = scanner.getNext();
		return tmp;
	}
	
	void error(Kind... expectedKinds) throws SyntaxException {
		String kinds = Arrays.toString(expectedKinds);
		String message;
		if (expectedKinds.length == 1) {
			message = "Expected " + kinds + " at " + t.line + ":" + t.pos;
		} else {
			message = "Expected one of" + kinds + " at " + t.line + ":" + t.pos;
		}
		throw new SyntaxException(t, message);
	}

	void error(Token t, String m) throws SyntaxException {
		String message = m + " at " + t.line + ":" + t.pos;
		throw new SyntaxException(t, message);
	}


	public Chunk parse() throws Exception {
		Token first =t;
		Block b = block();
		Chunk c = new Chunk(first, b);
		// TODO Auto-generated method stub
		return c;
	}
	


}
