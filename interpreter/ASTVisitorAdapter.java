package interpreter;

import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import cop5556fa19.Token;
import cop5556fa19.Token.Kind;
import cop5556fa19.AST.ASTVisitor;
import cop5556fa19.AST.Block;
import cop5556fa19.AST.Chunk;
import cop5556fa19.AST.Exp;
import cop5556fa19.AST.ExpBinary;
import cop5556fa19.AST.ExpFalse;
import cop5556fa19.AST.ExpFunction;
import cop5556fa19.AST.ExpFunctionCall;
import cop5556fa19.AST.ExpInt;
import cop5556fa19.AST.ExpList;
import cop5556fa19.AST.ExpName;
import cop5556fa19.AST.ExpNil;
import cop5556fa19.AST.ExpString;
import cop5556fa19.AST.ExpTable;
import cop5556fa19.AST.ExpTableLookup;
import cop5556fa19.AST.ExpTrue;
import cop5556fa19.AST.ExpUnary;
import cop5556fa19.AST.ExpVarArgs;
import cop5556fa19.AST.Field;
import cop5556fa19.AST.FieldExpKey;
import cop5556fa19.AST.FieldImplicitKey;
import cop5556fa19.AST.FieldList;
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
import cop5556fa19.Parser;
import cop5556fa19.Parser.SyntaxException;
import interpreter.built_ins.print;
import interpreter.built_ins.println;
import interpreter.built_ins.toNumber;
import static cop5556fa19.Token.Kind.*;
public abstract class ASTVisitorAdapter implements ASTVisitor {
	
	@SuppressWarnings("serial")
	public static class StaticSemanticException extends Exception{
		
			public StaticSemanticException(Token first, String msg) {
				super(first.line + ":" + first.pos + " " + msg);
			}
		}
	
	
	@SuppressWarnings("serial")
	public
	static class TypeException extends Exception{

		public TypeException(String msg) {
			super(msg);
		}
		
		public TypeException(Token first, String msg) {
			super(first.line + ":" + first.pos + " " + msg);
		}
		
	}
	

   boolean ret = false;
	
	
	public abstract List<LuaValue> load(Reader r) throws Exception;
	
	public Object checkExp(Exp e, Object arg) throws Exception{
		if(e instanceof ExpInt) {
			ExpInt v = (ExpInt) e;
			return visitExpInt(v, arg);
		}
		else if(e instanceof ExpString) {
			ExpString v = (ExpString) e;
			return visitExpString(v, arg);
		}
		else if(e instanceof ExpBinary) {
			ExpBinary v = (ExpBinary) e;
			return visitExpBin(v, arg);
		}
		else if(e instanceof ExpName) {
			return ((LuaTable)arg).get(e.toString());
		}
		else if(e instanceof ExpTable) {
			return visitExpTable((ExpTable)e, arg);
		}
		else if(e instanceof ExpFunctionCall) {
			ExpFunctionCall a = (ExpFunctionCall) e;
			if(a.f instanceof ExpName) {
				List<LuaValue> arguments = new ArrayList<LuaValue>();
				for (int i = 0; i < a.args.size(); i++) {
					arguments.add((LuaValue)checkExp(a.args.get(i), arg));
				}
				if(((ExpName)a.f).name.matches("print")) {
					print ai = new print();
					ai.call(arguments);
				}
				else if(((ExpName)a.f).name.matches("println")) {
					println ai = new println();
					ai.call(arguments);
				}
				else if(((ExpName)a.f).name.matches("toNumber")) {
					toNumber ai =  new toNumber();
					ai.call(arguments);
				}
			}

		}
		else if (e instanceof ExpUnary) {
			return visitUnExp((ExpUnary)e , arg);
		}
		else if (e instanceof ExpTableLookup) {
			return visitExpTableLookup((ExpTableLookup)e, arg);
		}
		return null;
	}

	@Override
	public Object visitExpTable(ExpTable expTable , Object arg) throws Exception {
		LuaTable lt = new LuaTable();
		List<Field> fl = expTable.fields;
		for (int i = 0; i< fl.size(); i++) {
			if(fl.get(i) instanceof FieldImplicitKey) {
				FieldImplicitKey fik = (FieldImplicitKey) fl.get(i);
				lt.putImplicit((LuaValue)checkExp(fik.exp, arg));
			}
			else if(fl.get(i) instanceof FieldExpKey) {
				FieldExpKey fek = (FieldExpKey) fl.get(i);
				lt.put((LuaValue)checkExp(fek.key, arg), (LuaValue)checkExp(fek.value,arg));
			}
			else if(fl.get(i) instanceof FieldNameKey) {
				FieldNameKey fnk = (FieldNameKey) fl.get(i);
				lt.put(fnk.name.name, (LuaValue)checkExp(fnk.exp, arg));
			}
		}return lt;
	}
	@Override
	public Object visitExpNil(ExpNil expNil, Object arg) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExpBin(ExpBinary expBin, Object arg) throws Exception {
		LuaValue v0 = (LuaValue)checkExp(expBin.e0, arg);
		LuaValue v1 = (LuaValue)checkExp(expBin.e1, arg);
		Kind op = expBin.op;
		if(v0 instanceof LuaInt && v1 instanceof LuaInt) {
			LuaInt vi0 = (LuaInt) v0;
			LuaInt vi1 = (LuaInt) v1;
			switch (op) {
			case OP_PLUS :
				return new LuaInt(vi0.v + vi1.v);		
			case OP_POW :
				return new LuaInt((int)Math.pow(vi0.v, vi1.v));
			case OP_MINUS :
				return new LuaInt(vi0.v - vi1.v);
			case OP_TIMES :
				return new LuaInt(vi0.v * vi1.v);
			case OP_DIV :
				return new LuaInt(vi0.v / vi1.v);
			case OP_MOD :
				return new LuaInt(vi0.v % vi1.v);
			case REL_LE :
				return new LuaBoolean(vi0.v <= vi1.v);
			case REL_GE :
				return new LuaBoolean(vi0.v >= vi1.v);
			case REL_LT :
				return new LuaBoolean(vi0.v < vi1.v);
			case REL_GT :
				return new LuaBoolean(vi0.v > vi1.v);
			case OP_DIVDIV :
				return new LuaInt((int)Math.floorDiv(vi0.v, vi1.v));
			case BIT_AMP :
				return new LuaInt(vi0.v & vi1.v);
			case BIT_OR :
				return new LuaInt(vi0.v | vi1.v);
			case BIT_XOR :
				return new LuaInt(vi0.v ^ vi1.v);
			case BIT_SHIFTR :
				return new LuaInt(vi0.v >> vi1.v);
			case BIT_SHIFTL :
				return new LuaInt(vi0.v << vi1.v);
			default :
					return null;
		}
		}
		else if(v0 instanceof LuaString && v1 instanceof LuaString) {
			LuaString vs0 = (LuaString) v0;
			LuaString vs1 = (LuaString) v1;
		    switch (op) {
		    case DOTDOT :
				return new LuaString(vs0.value + vs1.value);
		    case REL_LE :
				return new LuaBoolean(vs0.value.charAt(0) <= vs1.value.charAt(0));
			case REL_GE :
				return new LuaBoolean(vs0.value.charAt(0) >= vs1.value.charAt(0));
			case REL_LT :
				return new LuaBoolean(vs0.value.charAt(0) < vs1.value.charAt(0));
			case REL_GT :
				return new LuaBoolean(vs0.value.charAt(0) > vs1.value.charAt(0));
			default :
				return null;
		}	 
	} 
		else {
		return null;
	}
	}

	@Override
	public Object visitUnExp(ExpUnary unExp, Object arg) throws Exception {
		LuaValue val = (LuaValue)checkExp(unExp.e, arg);
		Kind op = unExp.op;
		LuaInt va = (LuaInt) val;
		switch(op) {
		case OP_MINUS :
			return new LuaInt(va.v * (-1));
		case OP_HASH:
			return null;
		case BIT_XOR:
			return new LuaInt(va.v*(-1) - 1);
		case KW_not:	
			if(unExp.e instanceof ExpTrue || unExp.e instanceof ExpString || unExp.e instanceof ExpInt  ) {
				return new LuaBoolean(false);
			}
			else if(unExp.e instanceof ExpFalse) {
				return new LuaBoolean(true);
			}
			else {
				throw new StaticSemanticException(null,"semantic exception for unary operator");
			}
			default :
				return null;
		}
	}

	@Override
	public Object visitExpInt(ExpInt expInt, Object arg) {
		return new LuaInt(expInt.v);
	}

	@Override
	public Object visitExpString(ExpString expString, Object arg) {
		return new LuaString(expString.v);
	}

	

	@Override
	public Object visitExpList(ExpList expList, Object arg) throws Exception {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitParList(ParList parList, Object arg) throws Exception {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitFunDef(ExpFunction funcDec, Object arg) throws Exception {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitName(Name name, Object arg) {
		
		throw new UnsupportedOperationException();
	}



	@Override
	public Object visitStatBreak(StatBreak statBreak, Object arg, Object arg2) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitStatBreak(StatBreak statBreak, Object arg) throws Exception {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitStatGoto(StatGoto statGoto, Object arg) throws Exception {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitStatDo(StatDo statDo, Object arg) throws Exception {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitStatWhile(StatWhile statWhile, Object arg) throws Exception {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitStatRepeat(StatRepeat statRepeat, Object arg) throws Exception {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitStatIf(StatIf statIf, Object arg) throws Exception {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitStatFor(StatFor statFor1, Object arg) throws Exception {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitStatForEach(StatForEach statForEach, Object arg) throws Exception {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitFuncName(FuncName funcName, Object arg) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitStatFunction(StatFunction statFunction, Object arg) throws Exception {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitStatLocalFunc(StatLocalFunc statLocalFunc, Object arg) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitStatLocalAssign(StatLocalAssign statLocalAssign, Object arg) throws Exception {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitRetStat(RetStat retStat, Object arg) throws Exception {
		List<LuaValue> luaval = new ArrayList<>();
		List<Exp> retexp = retStat.el;
		for(int i = 0; i< retexp.size(); i++) {
			Exp e = retexp.get(i);
	          	luaval.add((LuaValue)checkExp(e, arg));
			}return luaval;
	}
	public boolean brk = false;
	List<Integer> lbreak = new ArrayList<>();
	boolean fg = false;
	String gotoname = "";
	String labelname = "";
	@Override
	public Object visitBlock(Block b, Object arg) throws Exception {
		
		List<Stat> sl = b.stats;
		boolean ex = true;
		List<LuaValue> iflist = new ArrayList<>();
		int i = 0;
	    ex = true;
		while(i<sl.size()) {
			if(ret) {
				break;
				}
			if(brk) {
				//System.out.println(lbreak.get(i));
					if(lbreak.get(lbreak.size()-1) == 2) {
						Boolean loop = false;
						int j = lbreak.size()-1;
						while(j>=0) {
							if(lbreak.get(j) == 1) {
								loop = true;
								break;
							}j--;
						}
						if(loop) {
							break;
						}
						else {
							brk = false;
							break;
						}
					}
					else if(lbreak.get(lbreak.size()-1) == 1) {
						break;
					}
				
			}
			Stat s = sl.get(i);
			if( s instanceof RetStat && !fg) {
				ret = true;
				return visitRetStat((RetStat)s, arg );
				
			}
			else if( s instanceof StatAssign && !fg) {
				StatAssign v = (StatAssign) s;
				List<Exp> varList = v.varList;
				List<Exp> expList = v.expList;
				int j = 0;
				while(j<varList.size()) {
					if(varList.get(j) instanceof ExpName) {
						((LuaTable)arg).put(varList.get(j).toString(), (LuaValue) checkExp(expList.get(j), arg));
					}
					else if(varList.get(j) instanceof ExpTableLookup) {
						ExpTableLookup r1 = (ExpTableLookup) varList.get(j);
						LuaTable table = (LuaTable)((LuaTable)arg).get(r1.table.toString());
						LuaValue luaKey = (LuaValue)checkExp(r1.key, arg);
						table.put(luaKey, (LuaValue)checkExp(expList.get(j),arg));
					}
					j++;
				}
			}
			else if (s instanceof StatIf && !fg) {
				lbreak.add(2);
				StatIf co = (StatIf) s;
				int nexp = co.es.size();
				for(int j=0;j<nexp;j++) {
					System.out.println(co.es.get(j));
					if(co.es.get(j) instanceof ExpTrue || co.es.get(j) instanceof ExpInt) {
						ex = false;
					  iflist.addAll((List<LuaValue>) visitBlock(co.bs.get(j), arg));
						break;
					}
					else if(checkExp(co.es.get(j),arg) != null) {
						if(checkExp(co.es.get(j),arg) instanceof LuaInt) {
							iflist.addAll((List<LuaValue>)visitBlock(co.bs.get(j), arg));
							ex = false;
							break;
						}
					   else if (!(checkExp(co.es.get(j),arg) instanceof LuaNil)) {
						if(((LuaBoolean)checkExp(co.es.get(j),arg)).value) {
							iflist.addAll((List<LuaValue>)visitBlock(co.bs.get(j), arg));
							ex = false;
							break;
						}
					}
					}
				}
				if(ex && co.es.size()< co.bs.size()) {
					iflist.addAll((List<LuaValue>) visitBlock(co.bs.get(co.bs.size()-1), arg));
				}lbreak.remove(lbreak.size()-1);
				
			
			}
			else if( s instanceof StatDo && !fg) {
				lbreak.add(2);
				StatDo d = (StatDo) s;
				iflist.addAll((List<LuaValue>)visitBlock(d.b, arg));
				lbreak.remove(lbreak.size()-1);
			}
			else if(s instanceof StatWhile && !fg) {
				lbreak.add(1);
				StatWhile w = (StatWhile) s;
				Exp we = w.e;
				Block wb = w.b;
				while(((LuaBoolean)checkExp(w.e, arg)).value) {
					iflist.addAll((List<LuaValue>) visitBlock(wb, arg));
					if (brk) {
						break;
					}
				}brk = false;
				lbreak.remove(lbreak.size()-1);
			}
			else if(s instanceof StatRepeat && !fg) {
				lbreak.add(1);
				StatRepeat w = (StatRepeat) s;
				Exp we = w.e;
				Block wb = w.b;
				iflist.addAll((List<LuaValue>) visitBlock(wb, arg));
				while(!((LuaBoolean)checkExp(w.e, arg)).value) {
					iflist.addAll((List<LuaValue>) visitBlock(wb, arg));
					if (brk) {
						break;
					}
				}brk = false;
				lbreak.remove(lbreak.size()-1);
			}
			else if(s instanceof StatBreak) {
				brk = true;
			}
			
		    else if(s instanceof StatGoto && !fg) {
				StatGoto g = (StatGoto) s;
				gotoname = g.name.name;
				fg = true;
			}
			else if(s instanceof StatLabel) {
				StatLabel l = (StatLabel) s;
				labelname = l.label.name;
				if(gotoname.matches(labelname)) {
					System.out.println("i am here too");
					System.out.println(labelname);
					fg = false;
				}
			}
			i++;
		}return iflist;
	}
	@Override
	public Object visitChunk(Chunk chunk, Object arg) throws Exception {
		Block b = chunk.block;
		Object a = visitBlock(b, arg);
		if(fg) {
			System.out.println("i am here");
			throw new interpreter.StaticSemanticException(new Token(null, null, 0, 0), "Goto syntax error");
		}
		return a;
	}
	@Override
	public Object visitFieldExpKey(FieldExpKey fieldExpKey, Object object) throws Exception {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitFieldNameKey(FieldNameKey fieldNameKey, Object arg) throws Exception {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public Object visitFieldImplicitKey(FieldImplicitKey fieldImplicitKey, Object arg) throws Exception {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExpTrue(ExpTrue expTrue, Object arg) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExpFalse(ExpFalse expFalse, Object arg) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitFuncBody(FuncBody funcBody, Object arg) throws Exception {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExpVarArgs(ExpVarArgs expVarArgs, Object arg) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitStatAssign(StatAssign statAssign, Object arg) throws Exception {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExpTableLookup(ExpTableLookup expTableLookup, Object arg) throws Exception {
		    throw new UnsupportedOperationException();

		
	}

	@Override
	public Object visitExpFunctionCall(ExpFunctionCall expFunctionCall, Object arg) throws Exception {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitLabel(StatLabel statLabel, Object ar) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitFieldList(FieldList fieldList, Object arg) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExpName(ExpName expName, Object arg) {
		throw new UnsupportedOperationException();
	}



}
