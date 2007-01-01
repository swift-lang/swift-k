// $ANTLR 2.7.5 (20050128): "../../../../../grammars/VDL.g" -> "VDLtParser.java"$

package org.griphyn.vdl.parser;

import org.antlr.stringtemplate.*;
import java.util.List;
import java.util.Iterator;

import antlr.TokenBuffer;
import antlr.TokenStreamException;
import antlr.TokenStreamIOException;
import antlr.ANTLRException;
import antlr.LLkParser;
import antlr.Token;
import antlr.TokenStream;
import antlr.RecognitionException;
import antlr.NoViableAltException;
import antlr.MismatchedTokenException;
import antlr.SemanticException;
import antlr.ParserSharedInputState;
import antlr.collections.impl.BitSet;

public class VDLtParser extends antlr.LLkParser       implements VDLtParserTokenTypes
 {

protected StringTemplateGroup m_templates=null;
protected String currentFunctionName=null;

public void setTemplateGroup(StringTemplateGroup tempGroup) {
    m_templates = tempGroup;
}

StringTemplate template(String name) {
    return m_templates.getInstanceOf(name);
}

StringTemplate text(String t) {
    return new StringTemplate(m_templates,t);
}

String escape(String s) {
    String s1 = s.replaceAll("&", "&amp;");
    String s2 = s1.replaceAll("<", "&lt;");
    String s3 = s2.replaceAll(">", "&gt;");
    return s3;
}

String quote(String s) {
    return s.replaceAll("\"", "&quot;");
}

void setReturnVariables(StringTemplate container, StringTemplate statement) {
    if (!statement.getName().equals("call"))
	 return;
    Object outputs = statement.getAttribute("outputs");

    if (outputs == null)
	 return;
    if (outputs instanceof List) {
         for (Iterator it=((List)outputs).iterator(); it.hasNext();) {
	     StringTemplate param = (StringTemplate) it.next();
	     Object type = param.getAttribute("type");
             if (type != null) {
	        StringTemplate var = template("variable");
	        var.setAttribute("name", param.getAttribute("name"));
	        var.setAttribute("type", type); 
                container.setAttribute("statements", var);
             }
         }
    } else {
	 StringTemplate param = (StringTemplate) outputs;
	 Object type = param.getAttribute("type");
         if (type != null) {
	    StringTemplate var = template("variable");
	    var.setAttribute("name", param.getAttribute("name"));
	    var.setAttribute("type", type); 
            container.setAttribute("statements", var);
         }
    }	 
}

protected VDLtParser(TokenBuffer tokenBuf, int k) {
  super(tokenBuf,k);
  tokenNames = _tokenNames;
}

public VDLtParser(TokenBuffer tokenBuf) {
  this(tokenBuf,3);
}

protected VDLtParser(TokenStream lexer, int k) {
  super(lexer,k);
  tokenNames = _tokenNames;
}

public VDLtParser(TokenStream lexer) {
  this(lexer,3);
}

public VDLtParser(ParserSharedInputState state) {
  super(state,3);
  tokenNames = _tokenNames;
}

	public final StringTemplate  program() throws RecognitionException, TokenStreamException {
		StringTemplate code=template("program");
		
		
		try {      // for error handling
			{
			_loop3:
			do {
				if ((LA(1)==LITERAL_namespace)) {
					nsdecl(code);
				}
				else {
					break _loop3;
				}
				
			} while (true);
			}
			{
			_loop5:
			do {
				if ((LA(1)==LITERAL_type)) {
					typedecl(code);
				}
				else {
					break _loop5;
				}
				
			} while (true);
			}
			{
			_loop7:
			do {
				if ((_tokenSet_0.member(LA(1)))) {
					declaration(code);
				}
				else {
					break _loop7;
				}
				
			} while (true);
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_1);
			} else {
			  throw ex;
			}
		}
		return code;
	}
	
	public final void nsdecl(
		StringTemplate code
	) throws RecognitionException, TokenStreamException {
		
		StringTemplate ns=null;
		
		try {      // for error handling
			ns=nsdef();
			if ( inputState.guessing==0 ) {
				
					  code.setAttribute("namespaces", ns);
					  if (ns.getAttribute("prefix") == null)
					     code.setAttribute("targetNS", ns.getAttribute("uri"));
					
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_2);
			} else {
			  throw ex;
			}
		}
	}
	
	public final void typedecl(
		StringTemplate code
	) throws RecognitionException, TokenStreamException {
		
		StringTemplate t=null;
		
		try {      // for error handling
			t=typedef();
			if ( inputState.guessing==0 ) {
				code.setAttribute("types", t);
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_3);
			} else {
			  throw ex;
			}
		}
	}
	
	public final void declaration(
		StringTemplate code
	) throws RecognitionException, TokenStreamException {
		
		StringTemplate f=null;
		
		try {      // for error handling
			boolean synPredMatched25 = false;
			if (((_tokenSet_0.member(LA(1))) && (_tokenSet_4.member(LA(2))) && (_tokenSet_5.member(LA(3))))) {
				int _m25 = mark();
				synPredMatched25 = true;
				inputState.guessing++;
				try {
					{
					declORstat(code);
					}
				}
				catch (RecognitionException pe) {
					synPredMatched25 = false;
				}
				rewind(_m25);
				inputState.guessing--;
			}
			if ( synPredMatched25 ) {
				declORstat(code);
			}
			else if ((LA(1)==ID||LA(1)==LPAREN) && (_tokenSet_6.member(LA(2))) && (_tokenSet_7.member(LA(3)))) {
				f=function();
				if ( inputState.guessing==0 ) {
					code.setAttribute("functions", f);
				}
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_8);
			} else {
			  throw ex;
			}
		}
	}
	
	public final StringTemplate  nsdef() throws RecognitionException, TokenStreamException {
		StringTemplate code=template("nsDef");
		
		Token  prefix = null;
		Token  uri = null;
		
		try {      // for error handling
			match(LITERAL_namespace);
			{
			switch ( LA(1)) {
			case ID:
			{
				prefix = LT(1);
				match(ID);
				if ( inputState.guessing==0 ) {
					code.setAttribute("prefix", prefix.getText());
				}
				break;
			}
			case STRING_LITERAL:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			uri = LT(1);
			match(STRING_LITERAL);
			match(SEMI);
			if ( inputState.guessing==0 ) {
				
					  code.setAttribute("uri", uri.getText());
					
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_2);
			} else {
			  throw ex;
			}
		}
		return code;
	}
	
	public final StringTemplate  typedef() throws RecognitionException, TokenStreamException {
		StringTemplate code=template("typeDef");
		
		Token  id = null;
		StringTemplate t=null;
		
		try {      // for error handling
			match(LITERAL_type);
			id = LT(1);
			match(ID);
			if ( inputState.guessing==0 ) {
					code.setAttribute("name", id.getText());
			}
			{
			switch ( LA(1)) {
			case ID:
			case LITERAL_int:
			case LITERAL_string:
			case LITERAL_float:
			case LITERAL_date:
			case LITERAL_uri:
			case LITERAL_bool:
			{
				{
				t=type();
				if ( inputState.guessing==0 ) {
					
						       code.setAttribute("type", t);
						
				}
				match(SEMI);
				}
				break;
			}
			case LCURLY:
			{
				structdecl(code);
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_3);
			} else {
			  throw ex;
			}
		}
		return code;
	}
	
	public final StringTemplate  type() throws RecognitionException, TokenStreamException {
		StringTemplate code=null;
		
		Token  id = null;
		
		try {      // for error handling
			switch ( LA(1)) {
			case LITERAL_int:
			case LITERAL_string:
			case LITERAL_float:
			case LITERAL_date:
			case LITERAL_uri:
			case LITERAL_bool:
			{
				code=builtInType();
				break;
			}
			case ID:
			{
				id = LT(1);
				match(ID);
				if ( inputState.guessing==0 ) {
					
					code=template("type_user_object");
					code.setAttribute("name", id.getText());
					
				}
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_9);
			} else {
			  throw ex;
			}
		}
		return code;
	}
	
	public final void structdecl(
		StringTemplate code
	) throws RecognitionException, TokenStreamException {
		
		Token  id = null;
		Token  id1 = null;
		StringTemplate e=null, e1=null, t=null;
		
		try {      // for error handling
			match(LCURLY);
			{
			_loop21:
			do {
				if ((_tokenSet_10.member(LA(1)))) {
					t=type();
					id = LT(1);
					match(ID);
					if ( inputState.guessing==0 ) {
						
							e=template("member");
							e.setAttribute("type", t);
							e.setAttribute("name", id.getText());
							
					}
					{
					switch ( LA(1)) {
					case LBRACK:
					{
						match(LBRACK);
						match(RBRACK);
						if ( inputState.guessing==0 ) {
							e.setAttribute("isArray", "true");
						}
						break;
					}
					case SEMI:
					case COMMA:
					{
						break;
					}
					default:
					{
						throw new NoViableAltException(LT(1), getFilename());
					}
					}
					}
					if ( inputState.guessing==0 ) {
						code.setAttribute("members", e);
					}
					{
					_loop20:
					do {
						if ((LA(1)==COMMA)) {
							match(COMMA);
							id1 = LT(1);
							match(ID);
							if ( inputState.guessing==0 ) {
								
										e1=template("member");
										e1.setAttribute("type", t);
										e1.setAttribute("name", id1.getText());
									
							}
							{
							switch ( LA(1)) {
							case LBRACK:
							{
								match(LBRACK);
								match(RBRACK);
								if ( inputState.guessing==0 ) {
									e1.setAttribute("isArray", "true");
								}
								break;
							}
							case SEMI:
							case COMMA:
							{
								break;
							}
							default:
							{
								throw new NoViableAltException(LT(1), getFilename());
							}
							}
							}
							if ( inputState.guessing==0 ) {
								code.setAttribute("members", e1);
							}
						}
						else {
							break _loop20;
						}
						
					} while (true);
					}
					match(SEMI);
				}
				else {
					break _loop21;
				}
				
			} while (true);
			}
			match(RCURLY);
			{
			if ((LA(1)==SEMI) && (_tokenSet_3.member(LA(2))) && (_tokenSet_4.member(LA(3)))) {
				match(SEMI);
			}
			else if ((_tokenSet_3.member(LA(1))) && (_tokenSet_4.member(LA(2))) && (_tokenSet_5.member(LA(3)))) {
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_3);
			} else {
			  throw ex;
			}
		}
	}
	
	public final void declORstat(
		StringTemplate code
	) throws RecognitionException, TokenStreamException {
		
		StringTemplate s=null;
		
		try {      // for error handling
			boolean synPredMatched77 = false;
			if (((_tokenSet_10.member(LA(1))) && (LA(2)==ID) && (_tokenSet_11.member(LA(3))))) {
				int _m77 = mark();
				synPredMatched77 = true;
				inputState.guessing++;
				try {
					{
					variable(code);
					}
				}
				catch (RecognitionException pe) {
					synPredMatched77 = false;
				}
				rewind(_m77);
				inputState.guessing--;
			}
			if ( synPredMatched77 ) {
				variable(code);
			}
			else {
				boolean synPredMatched79 = false;
				if (((_tokenSet_10.member(LA(1))) && (LA(2)==ID) && (LA(3)==LBRACK||LA(3)==LT))) {
					int _m79 = mark();
					synPredMatched79 = true;
					inputState.guessing++;
					try {
						{
						datasetdecl(code);
						}
					}
					catch (RecognitionException pe) {
						synPredMatched79 = false;
					}
					rewind(_m79);
					inputState.guessing--;
				}
				if ( synPredMatched79 ) {
					datasetdecl(code);
				}
				else if ((_tokenSet_0.member(LA(1))) && (_tokenSet_4.member(LA(2))) && (_tokenSet_12.member(LA(3)))) {
					s=statement();
					if ( inputState.guessing==0 ) {
						
							    code.setAttribute("statements",s);
							    setReturnVariables(code, s);
							
					}
				}
				else {
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
			}
			catch (RecognitionException ex) {
				if (inputState.guessing==0) {
					reportError(ex);
					recover(ex,_tokenSet_13);
				} else {
				  throw ex;
				}
			}
		}
		
	public final StringTemplate  function() throws RecognitionException, TokenStreamException {
		StringTemplate code=template("function");
		
		Token  id = null;
		StringTemplate f=null;
		
		try {      // for error handling
			{
			switch ( LA(1)) {
			case LPAREN:
			{
				match(LPAREN);
				f=formalParameter();
				if ( inputState.guessing==0 ) {
					
							f.setAttribute("outlink", "true");
							code.setAttribute("outputs", f);
							
				}
				{
				_loop58:
				do {
					if ((LA(1)==COMMA)) {
						match(COMMA);
						f=formalParameter();
						if ( inputState.guessing==0 ) {
							
								    	f.setAttribute("outlink", "true");
								    	code.setAttribute("outputs", f);
								    	
						}
					}
					else {
						break _loop58;
					}
					
				} while (true);
				}
				match(RPAREN);
				break;
			}
			case ID:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			id = LT(1);
			match(ID);
			if ( inputState.guessing==0 ) {
				currentFunctionName=id.getText();
			}
			match(LPAREN);
			{
			switch ( LA(1)) {
			case ID:
			case LITERAL_int:
			case LITERAL_string:
			case LITERAL_float:
			case LITERAL_date:
			case LITERAL_uri:
			case LITERAL_bool:
			{
				f=formalParameter();
				if ( inputState.guessing==0 ) {
					
						    	code.setAttribute("inputs", f);
						    	
				}
				{
				_loop61:
				do {
					if ((LA(1)==COMMA)) {
						match(COMMA);
						f=formalParameter();
						if ( inputState.guessing==0 ) {
							
											code.setAttribute("inputs", f);
								        	
						}
					}
					else {
						break _loop61;
					}
					
				} while (true);
				}
				break;
			}
			case RPAREN:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			match(RPAREN);
			match(LCURLY);
			{
			switch ( LA(1)) {
			case LITERAL_app:
			case LITERAL_service:
			{
				atomicBody(code);
				break;
			}
			case ID:
			case SEMI:
			case LCURLY:
			case RCURLY:
			case LPAREN:
			case LITERAL_int:
			case LITERAL_string:
			case LITERAL_float:
			case LITERAL_date:
			case LITERAL_uri:
			case LITERAL_bool:
			case LITERAL_break:
			case LITERAL_continue:
			case LITERAL_for:
			case LITERAL_if:
			case LITERAL_foreach:
			case LITERAL_while:
			case LITERAL_repeat:
			case LITERAL_switch:
			{
				compoundBody(code);
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			match(RCURLY);
			if ( inputState.guessing==0 ) {
				
				code.setAttribute("name", id.getText());
				currentFunctionName=null;
				
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_8);
			} else {
			  throw ex;
			}
		}
		return code;
	}
	
	public final void variable(
		StringTemplate code
	) throws RecognitionException, TokenStreamException {
		
		Token  b1 = null;
		Token  b2 = null;
		StringTemplate v1=null, v2=null,t=null, d=null, i1=null, i2=null;
		
		try {      // for error handling
			t=type();
			d=declarator();
			{
			switch ( LA(1)) {
			case LBRACK:
			{
				b1 = LT(1);
				match(LBRACK);
				match(RBRACK);
				break;
			}
			case SEMI:
			case COMMA:
			case ASSIGN:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			i1=varInitializer();
			if ( inputState.guessing==0 ) {
				
						if ( currentFunctionName==null ) {
							v1 = template("globalVariable");
						}
						else {
							v1 = template("variable");
						}
						v1.setAttribute("type", t);
						v1.setAttribute("name", d);
						if (b1 != null)
							v1.setAttribute("isArray", "true");
						if (i1 != null)
							v1.setAttribute("value", i1);
						code.setAttribute("statements", v1);
					
			}
			{
			_loop30:
			do {
				if ((LA(1)==COMMA)) {
					match(COMMA);
					d=declarator();
					{
					switch ( LA(1)) {
					case LBRACK:
					{
						b2 = LT(1);
						match(LBRACK);
						match(RBRACK);
						break;
					}
					case SEMI:
					case COMMA:
					case ASSIGN:
					{
						break;
					}
					default:
					{
						throw new NoViableAltException(LT(1), getFilename());
					}
					}
					}
					i2=varInitializer();
					if ( inputState.guessing==0 ) {
						
						if ( currentFunctionName==null ) {
						v2 = template("globalVariable");
						}
						else {
						v2 = template("variable");
						}
						v2.setAttribute("type", t);
						v2.setAttribute("name", d);
							 	    if (b2 != null)
								       v2.setAttribute("isArray", "true");
							    	if (i2 != null)
							    	   v2.setAttribute("value", i2);
							    	code.setAttribute("statements", v2);
						
					}
				}
				else {
					break _loop30;
				}
				
			} while (true);
			}
			match(SEMI);
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_13);
			} else {
			  throw ex;
			}
		}
	}
	
	public final StringTemplate  declarator() throws RecognitionException, TokenStreamException {
		StringTemplate code=null;
		
		Token  id = null;
		
		try {      // for error handling
			id = LT(1);
			match(ID);
			if ( inputState.guessing==0 ) {
				code=text(id.getText());
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_14);
			} else {
			  throw ex;
			}
		}
		return code;
	}
	
	public final StringTemplate  varInitializer() throws RecognitionException, TokenStreamException {
		StringTemplate code=null;
		
		
		try {      // for error handling
			{
			switch ( LA(1)) {
			case ASSIGN:
			{
				match(ASSIGN);
				code=initializer();
				break;
			}
			case SEMI:
			case COMMA:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_15);
			} else {
			  throw ex;
			}
		}
		return code;
	}
	
	public final StringTemplate  initializer() throws RecognitionException, TokenStreamException {
		StringTemplate code=null;
		
		
		try {      // for error handling
			switch ( LA(1)) {
			case ID:
			case STRING_LITERAL:
			case LPAREN:
			case AT:
			case PLUS:
			case MINUS:
			case NOT:
			case INT_LITERAL:
			case FLOAT_LITERAL:
			case LITERAL_true:
			case LITERAL_false:
			case LITERAL_null:
			{
				code=expression();
				break;
			}
			case LBRACK:
			{
				code=arrayInitializer();
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_15);
			} else {
			  throw ex;
			}
		}
		return code;
	}
	
	public final StringTemplate  expression() throws RecognitionException, TokenStreamException {
		StringTemplate code=null;
		
		
		try {      // for error handling
			code=orExpr();
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_16);
			} else {
			  throw ex;
			}
		}
		return code;
	}
	
	public final StringTemplate  arrayInitializer() throws RecognitionException, TokenStreamException {
		StringTemplate code=template("arrayInit");
		
		StringTemplate e=null,from=null,to=null,step=null;
		
		try {      // for error handling
			match(LBRACK);
			{
			boolean synPredMatched38 = false;
			if (((_tokenSet_17.member(LA(1))) && (_tokenSet_18.member(LA(2))) && (_tokenSet_19.member(LA(3))))) {
				int _m38 = mark();
				synPredMatched38 = true;
				inputState.guessing++;
				try {
					{
					expression();
					match(COLON);
					}
				}
				catch (RecognitionException pe) {
					synPredMatched38 = false;
				}
				rewind(_m38);
				inputState.guessing--;
			}
			if ( synPredMatched38 ) {
				{
				from=expression();
				match(COLON);
				to=expression();
				{
				switch ( LA(1)) {
				case COLON:
				{
					match(COLON);
					step=expression();
					break;
				}
				case RBRACK:
				{
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
				if ( inputState.guessing==0 ) {
					
						    StringTemplate range=template("range");
						    range.setAttribute("from", from);
						    range.setAttribute("to", to);
						    if (step != null)
							range.setAttribute("step", step);
						    code.setAttribute("range", range);
						
				}
				}
			}
			else if ((_tokenSet_17.member(LA(1))) && (_tokenSet_20.member(LA(2))) && (_tokenSet_21.member(LA(3)))) {
				{
				e=expression();
				if ( inputState.guessing==0 ) {
					code.setAttribute("elements", e);
				}
				{
				_loop43:
				do {
					if ((LA(1)==COMMA) && (_tokenSet_17.member(LA(2)))) {
						match(COMMA);
						e=expression();
						if ( inputState.guessing==0 ) {
							code.setAttribute("elements", e);
						}
					}
					else {
						break _loop43;
					}
					
				} while (true);
				}
				{
				switch ( LA(1)) {
				case COMMA:
				{
					match(COMMA);
					break;
				}
				case RBRACK:
				{
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
				}
			}
			else if ((LA(1)==RBRACK)) {
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
			}
			match(RBRACK);
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_22);
			} else {
			  throw ex;
			}
		}
		return code;
	}
	
	public final void datasetdecl(
		StringTemplate code
	) throws RecognitionException, TokenStreamException {
		
		Token  b1 = null;
		Token  f = null;
		StringTemplate dataset=null, t=null, m=null, d=null;
		
		try {      // for error handling
			t=type();
			d=declarator();
			{
			switch ( LA(1)) {
			case LBRACK:
			{
				b1 = LT(1);
				match(LBRACK);
				match(RBRACK);
				break;
			}
			case LT:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			match(LT);
			{
			switch ( LA(1)) {
			case ID:
			{
				m=mappingdecl();
				break;
			}
			case STRING_LITERAL:
			{
				f = LT(1);
				match(STRING_LITERAL);
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			match(GT);
			match(SEMI);
			if ( inputState.guessing==0 ) {
				
					   dataset=template("dataset");
					   dataset.setAttribute("type", t);
					   dataset.setAttribute("name", d);
					   if (m!=null)
					       dataset.setAttribute("mapping", m);
					   else
					       dataset.setAttribute("lfn", f.getText());
					   if (b1 != null)
					       dataset.setAttribute("isArray", "true");
					   code.setAttribute("statements", dataset);
					
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_13);
			} else {
			  throw ex;
			}
		}
	}
	
	public final StringTemplate  mappingdecl() throws RecognitionException, TokenStreamException {
		StringTemplate code=template("mapping");
		
		StringTemplate p=null, d=null;
		
		try {      // for error handling
			d=declarator();
			if ( inputState.guessing==0 ) {
				code.setAttribute("descriptor",d);
			}
			match(SEMI);
			mapparamdecl(code);
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_23);
			} else {
			  throw ex;
			}
		}
		return code;
	}
	
	public final void mapparamdecl(
		StringTemplate code
	) throws RecognitionException, TokenStreamException {
		
		StringTemplate p=null;
		
		try {      // for error handling
			{
			switch ( LA(1)) {
			case ID:
			{
				p=mapparam();
				if ( inputState.guessing==0 ) {
					code.setAttribute("params", p);
				}
				{
				_loop52:
				do {
					if ((LA(1)==COMMA) && (LA(2)==ID)) {
						match(COMMA);
						p=mapparam();
						if ( inputState.guessing==0 ) {
							code.setAttribute("params", p);
						}
					}
					else {
						break _loop52;
					}
					
				} while (true);
				}
				{
				switch ( LA(1)) {
				case COMMA:
				{
					match(COMMA);
					break;
				}
				case GT:
				{
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
				break;
			}
			case GT:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_23);
			} else {
			  throw ex;
			}
		}
	}
	
	public final StringTemplate  mapparam() throws RecognitionException, TokenStreamException {
		StringTemplate code=template("mapParam");
		
		StringTemplate n=null, v=null;
		
		try {      // for error handling
			n=declarator();
			match(ASSIGN);
			v=mappingExpr();
			if ( inputState.guessing==0 ) {
				
					    code.setAttribute("name", n);
					    code.setAttribute("value", v);
					
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_24);
			} else {
			  throw ex;
			}
		}
		return code;
	}
	
	public final StringTemplate  mappingExpr() throws RecognitionException, TokenStreamException {
		StringTemplate code=null;
		
		StringTemplate e=null;
		
		try {      // for error handling
			boolean synPredMatched132 = false;
			if (((LA(1)==AT) && (LA(2)==ID||LA(2)==LPAREN) && (_tokenSet_25.member(LA(3))))) {
				int _m132 = mark();
				synPredMatched132 = true;
				inputState.guessing++;
				try {
					{
					mappingFunc();
					}
				}
				catch (RecognitionException pe) {
					synPredMatched132 = false;
				}
				rewind(_m132);
				inputState.guessing--;
			}
			if ( synPredMatched132 ) {
				code=mappingFunc();
			}
			else if ((_tokenSet_17.member(LA(1))) && (_tokenSet_26.member(LA(2))) && (_tokenSet_27.member(LA(3)))) {
				{
				e=expression();
				if ( inputState.guessing==0 ) {
					
						  code=template("mappingExpr");
						  code.setAttribute("expr", e);
						
				}
				}
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_28);
			} else {
			  throw ex;
			}
		}
		return code;
	}
	
	public final StringTemplate  formalParameter() throws RecognitionException, TokenStreamException {
		StringTemplate code=template("parameter");
		
		StringTemplate t=null,d=null,v=null;
		
		try {      // for error handling
			t=type();
			d=declarator();
			if ( inputState.guessing==0 ) {
				
				code.setAttribute("type", t);
				code.setAttribute("name", d);
				
			}
			{
			switch ( LA(1)) {
			case LBRACK:
			{
				{
				match(LBRACK);
				match(RBRACK);
				if ( inputState.guessing==0 ) {
					code.setAttribute("isArray", "true");
				}
				}
				break;
			}
			case ASSIGN:
			{
				{
				match(ASSIGN);
				v=constant();
				if ( inputState.guessing==0 ) {
					
						  	String value = (String)v.getAttribute("value");
						  	if (v.getName().equals("sConst")) {
						    	v.removeAttribute("value");
						     	v.setAttribute("value", quote(value));
					}
						  	code.setAttribute("defaultv", v);
						
				}
				}
				break;
			}
			case COMMA:
			case RPAREN:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_29);
			} else {
			  throw ex;
			}
		}
		return code;
	}
	
	public final void atomicBody(
		StringTemplate code
	) throws RecognitionException, TokenStreamException {
		
		StringTemplate app=null, svc=null;
		
		try {      // for error handling
			switch ( LA(1)) {
			case LITERAL_app:
			{
				app=appSpec();
				if ( inputState.guessing==0 ) {
					code.setAttribute("config",app);
				}
				break;
			}
			case LITERAL_service:
			{
				svc=serviceSpec();
				if ( inputState.guessing==0 ) {
					code.setAttribute("config",svc);
				}
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_30);
			} else {
			  throw ex;
			}
		}
	}
	
	public final void compoundBody(
		StringTemplate code
	) throws RecognitionException, TokenStreamException {
		
		
		try {      // for error handling
			{
			_loop74:
			do {
				if ((_tokenSet_0.member(LA(1)))) {
					declORstat(code);
				}
				else {
					break _loop74;
				}
				
			} while (true);
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_30);
			} else {
			  throw ex;
			}
		}
	}
	
	public final StringTemplate  constant() throws RecognitionException, TokenStreamException {
		StringTemplate code=null;
		
		Token  i = null;
		Token  d = null;
		Token  s = null;
		Token  t = null;
		Token  f = null;
		Token  n = null;
		
		try {      // for error handling
			switch ( LA(1)) {
			case INT_LITERAL:
			{
				i = LT(1);
				match(INT_LITERAL);
				if ( inputState.guessing==0 ) {
					
					code=template("iConst");
					code.setAttribute("value",i.getText());
					
				}
				break;
			}
			case FLOAT_LITERAL:
			{
				d = LT(1);
				match(FLOAT_LITERAL);
				if ( inputState.guessing==0 ) {
					
					code=template("fConst");
					code.setAttribute("value",d.getText());
					
				}
				break;
			}
			case STRING_LITERAL:
			{
				s = LT(1);
				match(STRING_LITERAL);
				if ( inputState.guessing==0 ) {
					
					code=template("sConst");
					code.setAttribute("value",quote("\""+s.getText()+"\""));
					code.setAttribute("innervalue",quote(s.getText()));
					
				}
				break;
			}
			case LITERAL_true:
			{
				t = LT(1);
				match(LITERAL_true);
				if ( inputState.guessing==0 ) {
					
					code=template("bConst"); 
					code.setAttribute("value", t.getText()); 
					
				}
				break;
			}
			case LITERAL_false:
			{
				f = LT(1);
				match(LITERAL_false);
				if ( inputState.guessing==0 ) {
					
					code=template("bConst");
					code.setAttribute("value", f.getText()); 
					
				}
				break;
			}
			case LITERAL_null:
			{
				n = LT(1);
				match(LITERAL_null);
				if ( inputState.guessing==0 ) {
					
					code=template("null");
					
				}
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_31);
			} else {
			  throw ex;
			}
		}
		return code;
	}
	
	public final StringTemplate  builtInType() throws RecognitionException, TokenStreamException {
		StringTemplate code=null;
		
		
		try {      // for error handling
			switch ( LA(1)) {
			case LITERAL_int:
			{
				match(LITERAL_int);
				if ( inputState.guessing==0 ) {
					code=template("type_int");
				}
				break;
			}
			case LITERAL_string:
			{
				match(LITERAL_string);
				if ( inputState.guessing==0 ) {
					code=template("type_string");
				}
				break;
			}
			case LITERAL_float:
			{
				match(LITERAL_float);
				if ( inputState.guessing==0 ) {
					code=template("type_float");
				}
				break;
			}
			case LITERAL_date:
			{
				match(LITERAL_date);
				if ( inputState.guessing==0 ) {
					code=template("type_date");
				}
				break;
			}
			case LITERAL_uri:
			{
				match(LITERAL_uri);
				if ( inputState.guessing==0 ) {
					code=template("type_uri");
				}
				break;
			}
			case LITERAL_bool:
			{
				match(LITERAL_bool);
				if ( inputState.guessing==0 ) {
					code=template("type_bool");
				}
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_9);
			} else {
			  throw ex;
			}
		}
		return code;
	}
	
	public final void compoundStat(
		StringTemplate code
	) throws RecognitionException, TokenStreamException {
		
		
		try {      // for error handling
			match(LCURLY);
			{
			_loop71:
			do {
				if ((_tokenSet_0.member(LA(1)))) {
					declORstat(code);
				}
				else {
					break _loop71;
				}
				
			} while (true);
			}
			match(RCURLY);
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_32);
			} else {
			  throw ex;
			}
		}
	}
	
	public final StringTemplate  statement() throws RecognitionException, TokenStreamException {
		StringTemplate code=null;
		
		
		try {      // for error handling
			switch ( LA(1)) {
			case LCURLY:
			{
				compoundStat(code=template("statementList"));
				break;
			}
			case LITERAL_for:
			{
				code=forStat();
				break;
			}
			case LITERAL_if:
			{
				code=ifStat();
				break;
			}
			case LITERAL_foreach:
			{
				code=foreachStat();
				break;
			}
			case LITERAL_switch:
			{
				code=switchStat();
				break;
			}
			case LITERAL_repeat:
			{
				code=repeatStat();
				match(SEMI);
				break;
			}
			case LITERAL_while:
			{
				code=whileStat();
				break;
			}
			case LITERAL_break:
			{
				match(LITERAL_break);
				if ( inputState.guessing==0 ) {
					code=template("break");
				}
				match(SEMI);
				break;
			}
			case LITERAL_continue:
			{
				match(LITERAL_continue);
				if ( inputState.guessing==0 ) {
					code=template("continue");
				}
				match(SEMI);
				break;
			}
			case ID:
			case LPAREN:
			case LITERAL_int:
			case LITERAL_string:
			case LITERAL_float:
			case LITERAL_date:
			case LITERAL_uri:
			case LITERAL_bool:
			{
				code=assignStat();
				match(SEMI);
				break;
			}
			case SEMI:
			{
				match(SEMI);
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_33);
			} else {
			  throw ex;
			}
		}
		return code;
	}
	
	public final StringTemplate  forStat() throws RecognitionException, TokenStreamException {
		StringTemplate code=template("forLoop");
		
		StringTemplate e1=null,e2=null,e3=null,s=null;
		
		try {      // for error handling
			match(LITERAL_for);
			match(LPAREN);
			e1=assignStat();
			match(SEMI);
			e2=expression();
			match(SEMI);
			e3=assignStat();
			match(RPAREN);
			compoundStat(code);
			if ( inputState.guessing==0 ) {
				
				code.setAttribute("e1", e1);
				code.setAttribute("e2", e2);
				code.setAttribute("e3", e3);
				
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_33);
			} else {
			  throw ex;
			}
		}
		return code;
	}
	
	public final StringTemplate  ifStat() throws RecognitionException, TokenStreamException {
		StringTemplate code=template("if");
		
		
		StringTemplate cond=null;
		StringTemplate body=template("statementList");
		StringTemplate els=template("statementList");
		
		
		try {      // for error handling
			match(LITERAL_if);
			match(LPAREN);
			cond=expression();
			match(RPAREN);
			if ( inputState.guessing==0 ) {
				
						code.setAttribute("cond", cond);
						
			}
			compoundStat(body);
			if ( inputState.guessing==0 ) {
				code.setAttribute("body", body);
			}
			{
			switch ( LA(1)) {
			case LITERAL_else:
			{
				match(LITERAL_else);
				compoundStat(els);
				if ( inputState.guessing==0 ) {
					code.setAttribute("els", els);
				}
				break;
			}
			case EOF:
			case ID:
			case SEMI:
			case LCURLY:
			case RCURLY:
			case LPAREN:
			case LITERAL_int:
			case LITERAL_string:
			case LITERAL_float:
			case LITERAL_date:
			case LITERAL_uri:
			case LITERAL_bool:
			case LITERAL_break:
			case LITERAL_continue:
			case LITERAL_for:
			case LITERAL_if:
			case LITERAL_foreach:
			case LITERAL_while:
			case LITERAL_repeat:
			case LITERAL_switch:
			case LITERAL_case:
			case LITERAL_default:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_33);
			} else {
			  throw ex;
			}
		}
		return code;
	}
	
	public final StringTemplate  foreachStat() throws RecognitionException, TokenStreamException {
		StringTemplate code=template("foreach");
		
		Token  id = null;
		Token  indexId = null;
		
		StringTemplate ds=null, t=null;
		StringTemplate body=template("statementList");
		
		
		try {      // for error handling
			match(LITERAL_foreach);
			{
			if ((_tokenSet_10.member(LA(1))) && (LA(2)==ID)) {
				t=type();
			}
			else if ((LA(1)==ID) && (LA(2)==COMMA||LA(2)==LITERAL_in)) {
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
			}
			id = LT(1);
			match(ID);
			{
			switch ( LA(1)) {
			case COMMA:
			{
				match(COMMA);
				indexId = LT(1);
				match(ID);
				break;
			}
			case LITERAL_in:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			match(LITERAL_in);
			ds=expression();
			if ( inputState.guessing==0 ) {
				
					    if (t != null) {
				StringTemplate v= template("variable");
					       v.setAttribute("type", t);
					       v.setAttribute("name", id.getText());
					       code.setAttribute("variables", v);
					    }
					    code.setAttribute("var", id.getText());
					    code.setAttribute("in", ds);
					    if (indexId != null) {
					       code.setAttribute("index", indexId.getText());
					    }
					
			}
			compoundStat(body);
			if ( inputState.guessing==0 ) {
				code.setAttribute("body", body);
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_33);
			} else {
			  throw ex;
			}
		}
		return code;
	}
	
	public final StringTemplate  switchStat() throws RecognitionException, TokenStreamException {
		StringTemplate code=template("switch");
		
		
		StringTemplate cond=null, b=null;
		
		
		try {      // for error handling
			match(LITERAL_switch);
			match(LPAREN);
			cond=expression();
			match(RPAREN);
			if ( inputState.guessing==0 ) {
				code.setAttribute("cond", cond);
			}
			match(LCURLY);
			{
			_loop91:
			do {
				if ((LA(1)==LITERAL_case||LA(1)==LITERAL_default)) {
					b=casesGroup();
					if ( inputState.guessing==0 ) {
						code.setAttribute("cases", b);
					}
				}
				else {
					break _loop91;
				}
				
			} while (true);
			}
			match(RCURLY);
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_33);
			} else {
			  throw ex;
			}
		}
		return code;
	}
	
	public final StringTemplate  repeatStat() throws RecognitionException, TokenStreamException {
		StringTemplate code=template("repeat");
		
		
		StringTemplate cond=null;
		StringTemplate body=template("statementList");
		
		
		try {      // for error handling
			match(LITERAL_repeat);
			compoundStat(body);
			if ( inputState.guessing==0 ) {
				code.setAttribute("body", body);
			}
			match(LITERAL_until);
			match(LPAREN);
			cond=expression();
			match(RPAREN);
			if ( inputState.guessing==0 ) {
				
					code.setAttribute("cond", cond);
					
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_34);
			} else {
			  throw ex;
			}
		}
		return code;
	}
	
	public final StringTemplate  whileStat() throws RecognitionException, TokenStreamException {
		StringTemplate code=template("while");
		
		
		StringTemplate cond=null;
		StringTemplate body=template("statementList");
		
		
		try {      // for error handling
			match(LITERAL_while);
			match(LPAREN);
			cond=expression();
			match(RPAREN);
			if ( inputState.guessing==0 ) {
				
					code.setAttribute("cond", cond);
					
			}
			compoundStat(body);
			if ( inputState.guessing==0 ) {
				code.setAttribute("body", body);
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_33);
			} else {
			  throw ex;
			}
		}
		return code;
	}
	
	public final StringTemplate  assignStat() throws RecognitionException, TokenStreamException {
		StringTemplate code=null;
		
		StringTemplate a=null, e=null, id=null;
		
		try {      // for error handling
			boolean synPredMatched101 = false;
			if (((_tokenSet_6.member(LA(1))) && (_tokenSet_35.member(LA(2))) && (_tokenSet_36.member(LA(3))))) {
				int _m101 = mark();
				synPredMatched101 = true;
				inputState.guessing++;
				try {
					{
					functioncallStat();
					}
				}
				catch (RecognitionException pe) {
					synPredMatched101 = false;
				}
				rewind(_m101);
				inputState.guessing--;
			}
			if ( synPredMatched101 ) {
				code=functioncallStat();
			}
			else if ((LA(1)==ID) && (_tokenSet_37.member(LA(2))) && (_tokenSet_38.member(LA(3)))) {
				id=identifier();
				match(ASSIGN);
				{
				switch ( LA(1)) {
				case ID:
				case STRING_LITERAL:
				case LPAREN:
				case AT:
				case PLUS:
				case MINUS:
				case NOT:
				case INT_LITERAL:
				case FLOAT_LITERAL:
				case LITERAL_true:
				case LITERAL_false:
				case LITERAL_null:
				{
					e=expression();
					break;
				}
				case LBRACK:
				{
					a=arrayInitializer();
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
				if ( inputState.guessing==0 ) {
					
					code=template("assign");
					code.setAttribute("lhs", id);
						    if (e != null ) 
					code.setAttribute("rhs", e);
						    else
					code.setAttribute("rhs", a);
					
				}
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_39);
			} else {
			  throw ex;
			}
		}
		return code;
	}
	
	public final StringTemplate  casesGroup() throws RecognitionException, TokenStreamException {
		StringTemplate code=template("case");
		
		StringTemplate b=null;
		
		try {      // for error handling
			{
			aCase(code);
			}
			caseSList(code);
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_40);
			} else {
			  throw ex;
			}
		}
		return code;
	}
	
	public final void aCase(
		StringTemplate code
	) throws RecognitionException, TokenStreamException {
		
		StringTemplate v=null;
		
		try {      // for error handling
			{
			switch ( LA(1)) {
			case LITERAL_case:
			{
				match(LITERAL_case);
				v=expression();
				if ( inputState.guessing==0 ) {
					code.setAttribute("value", v);
				}
				break;
			}
			case LITERAL_default:
			{
				match(LITERAL_default);
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			match(COLON);
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_41);
			} else {
			  throw ex;
			}
		}
	}
	
	public final void caseSList(
		StringTemplate code
	) throws RecognitionException, TokenStreamException {
		
		StringTemplate s=null;
		
		try {      // for error handling
			{
			_loop98:
			do {
				if ((_tokenSet_0.member(LA(1)))) {
					s=statement();
					if ( inputState.guessing==0 ) {
						code.setAttribute("statements", s);
					}
				}
				else {
					break _loop98;
				}
				
			} while (true);
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_40);
			} else {
			  throw ex;
			}
		}
	}
	
	public final StringTemplate  functioncallStat() throws RecognitionException, TokenStreamException {
		StringTemplate code=template("call");
		
		Token  id = null;
		StringTemplate f=null;
		
		try {      // for error handling
			{
			if ((_tokenSet_6.member(LA(1))) && (_tokenSet_42.member(LA(2)))) {
				{
				switch ( LA(1)) {
				case LPAREN:
				{
					{
					match(LPAREN);
					f=returnParameter();
					if ( inputState.guessing==0 ) {
						
							      code.setAttribute("outputs", f);
						
					}
					{
					_loop108:
					do {
						if ((LA(1)==COMMA)) {
							match(COMMA);
							f=returnParameter();
							if ( inputState.guessing==0 ) {
								
									          code.setAttribute("outputs", f);
									
							}
						}
						else {
							break _loop108;
						}
						
					} while (true);
					}
					match(RPAREN);
					}
					break;
				}
				case ID:
				case LITERAL_int:
				case LITERAL_string:
				case LITERAL_float:
				case LITERAL_date:
				case LITERAL_uri:
				case LITERAL_bool:
				{
					{
					f=returnParameter();
					if ( inputState.guessing==0 ) {
						code.setAttribute("outputs", f);
					}
					}
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
				match(ASSIGN);
			}
			else if ((LA(1)==ID) && (LA(2)==LPAREN)) {
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
			}
			id = LT(1);
			match(ID);
			if ( inputState.guessing==0 ) {
				code.setAttribute("func", id.getText());
			}
			match(LPAREN);
			{
			switch ( LA(1)) {
			case ID:
			case STRING_LITERAL:
			case LBRACK:
			case LPAREN:
			case AT:
			case PLUS:
			case MINUS:
			case NOT:
			case INT_LITERAL:
			case FLOAT_LITERAL:
			case LITERAL_true:
			case LITERAL_false:
			case LITERAL_null:
			{
				f=actualParameter();
				if ( inputState.guessing==0 ) {
					
						    code.setAttribute("inputs", f);
						
				}
				{
				_loop112:
				do {
					if ((LA(1)==COMMA)) {
						match(COMMA);
						f=actualParameter();
						if ( inputState.guessing==0 ) {
							
									code.setAttribute("inputs", f);
								
						}
					}
					else {
						break _loop112;
					}
					
				} while (true);
				}
				break;
			}
			case RPAREN:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			match(RPAREN);
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_39);
			} else {
			  throw ex;
			}
		}
		return code;
	}
	
	public final StringTemplate  identifier() throws RecognitionException, TokenStreamException {
		StringTemplate code=null;
		
		Token  d = null;
		
		String s=""; 
		StringTemplate sub1=null, sub2=null, t=null, t1=null;
		
		
		try {      // for error handling
			sub1=subscript();
			if ( inputState.guessing==0 ) {
				
					t=template("id");
					code = t;
					t.setAttribute("var", sub1);
				
			}
			{
			_loop187:
			do {
				if ((LA(1)==DOT)) {
					d = LT(1);
					match(DOT);
					{
					switch ( LA(1)) {
					case ID:
					{
						sub2=subscript();
						break;
					}
					case STAR:
					{
						match(STAR);
						if ( inputState.guessing==0 ) {
							sub2=template("subscript"); sub2.setAttribute("var","*");
						}
						break;
					}
					default:
					{
						throw new NoViableAltException(LT(1), getFilename());
					}
					}
					}
					if ( inputState.guessing==0 ) {
						
							t1=template("id");
							t1.setAttribute("var", sub2);
							t.setAttribute("path", t1);
							t=t1;
						
					}
				}
				else {
					break _loop187;
				}
				
			} while (true);
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_43);
			} else {
			  throw ex;
			}
		}
		return code;
	}
	
	public final StringTemplate  returnParameter() throws RecognitionException, TokenStreamException {
		StringTemplate code=template("returnParam");
		
		StringTemplate t=null, id=null, d=null;
		
		try {      // for error handling
			{
			if ((_tokenSet_10.member(LA(1))) && (LA(2)==ID)) {
				t=type();
				if ( inputState.guessing==0 ) {
					code.setAttribute("type", t);
				}
			}
			else if ((LA(1)==ID) && (_tokenSet_44.member(LA(2)))) {
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
			}
			id=identifier();
			if ( inputState.guessing==0 ) {
				
				code.setAttribute("name", id);
				
			}
			{
			boolean synPredMatched117 = false;
			if (((LA(1)==ASSIGN) && (LA(2)==ID) && (_tokenSet_45.member(LA(3))))) {
				int _m117 = mark();
				synPredMatched117 = true;
				inputState.guessing++;
				try {
					{
					match(ASSIGN);
					declarator();
					}
				}
				catch (RecognitionException pe) {
					synPredMatched117 = false;
				}
				rewind(_m117);
				inputState.guessing--;
			}
			if ( synPredMatched117 ) {
				{
				match(ASSIGN);
				d=declarator();
				}
				if ( inputState.guessing==0 ) {
					
						  	code.setAttribute("bind", d);
						  	
				}
			}
			else if ((_tokenSet_45.member(LA(1))) && (_tokenSet_46.member(LA(2))) && (_tokenSet_47.member(LA(3)))) {
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_45);
			} else {
			  throw ex;
			}
		}
		return code;
	}
	
	public final StringTemplate  actualParameter() throws RecognitionException, TokenStreamException {
		StringTemplate code=template("actualParam");
		
		StringTemplate d=null, id=null, ai=null;
		
		try {      // for error handling
			{
			boolean synPredMatched122 = false;
			if (((LA(1)==ID) && (LA(2)==ASSIGN))) {
				int _m122 = mark();
				synPredMatched122 = true;
				inputState.guessing++;
				try {
					{
					declarator();
					match(ASSIGN);
					}
				}
				catch (RecognitionException pe) {
					synPredMatched122 = false;
				}
				rewind(_m122);
				inputState.guessing--;
			}
			if ( synPredMatched122 ) {
				{
				d=declarator();
				match(ASSIGN);
				}
				if ( inputState.guessing==0 ) {
					
						  		code.setAttribute("bind", d);
						  	
				}
			}
			else if ((_tokenSet_48.member(LA(1))) && (_tokenSet_49.member(LA(2)))) {
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
			}
			{
			switch ( LA(1)) {
			case ID:
			case STRING_LITERAL:
			case LPAREN:
			case AT:
			case PLUS:
			case MINUS:
			case NOT:
			case INT_LITERAL:
			case FLOAT_LITERAL:
			case LITERAL_true:
			case LITERAL_false:
			case LITERAL_null:
			{
				id=expression();
				if ( inputState.guessing==0 ) {
					
					code.setAttribute("value", id);
					
				}
				break;
			}
			case LBRACK:
			{
				ai=arrayInitializer();
				if ( inputState.guessing==0 ) {
					
						  code.setAttribute("value", ai);
						
				}
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_29);
			} else {
			  throw ex;
			}
		}
		return code;
	}
	
	public final StringTemplate  appSpec() throws RecognitionException, TokenStreamException {
		StringTemplate code=template("app");
		
		StringTemplate exec=null;
		
		try {      // for error handling
			match(LITERAL_app);
			match(LCURLY);
			exec=declarator();
			if ( inputState.guessing==0 ) {
				code.setAttribute("exec", exec);
			}
			{
			_loop128:
			do {
				if ((_tokenSet_50.member(LA(1)))) {
					appArg(code);
				}
				else {
					break _loop128;
				}
				
			} while (true);
			}
			match(SEMI);
			match(RCURLY);
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_30);
			} else {
			  throw ex;
			}
		}
		return code;
	}
	
	public final StringTemplate  serviceSpec() throws RecognitionException, TokenStreamException {
		StringTemplate code=template("service");
		
		
		String w=null, p=null, o=null;
		StringTemplate m=null;
		
		
		try {      // for error handling
			match(LITERAL_service);
			match(LCURLY);
			w=wsdl();
			{
			switch ( LA(1)) {
			case LITERAL_portType:
			{
				p=port();
				break;
			}
			case LITERAL_operation:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			o=operation();
			if ( inputState.guessing==0 ) {
				
					    code.setAttribute("wsdlURI", w);
					    if (p != null)
					        code.setAttribute("portType", p);
					    code.setAttribute("operation", o);
					
			}
			{
			_loop146:
			do {
				if ((LA(1)==LITERAL_request||LA(1)==LITERAL_response)) {
					m=message();
					if ( inputState.guessing==0 ) {
						code.setAttribute("messages", m);
					}
				}
				else {
					break _loop146;
				}
				
			} while (true);
			}
			match(RCURLY);
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_30);
			} else {
			  throw ex;
			}
		}
		return code;
	}
	
	public final void appArg(
		StringTemplate code
	) throws RecognitionException, TokenStreamException {
		
		StringTemplate arg=null;
		
		try {      // for error handling
			switch ( LA(1)) {
			case ID:
			case STRING_LITERAL:
			case LPAREN:
			case AT:
			case PLUS:
			case MINUS:
			case NOT:
			case INT_LITERAL:
			case FLOAT_LITERAL:
			case LITERAL_true:
			case LITERAL_false:
			case LITERAL_null:
			{
				arg=mappingExpr();
				if ( inputState.guessing==0 ) {
					code.setAttribute("arguments", arg);
				}
				break;
			}
			case LITERAL_stdin:
			case LITERAL_stdout:
			case LITERAL_stderr:
			{
				stdioArg(code);
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_51);
			} else {
			  throw ex;
			}
		}
	}
	
	public final void stdioArg(
		StringTemplate code
	) throws RecognitionException, TokenStreamException {
		
		StringTemplate t=null,m=null;
		
		try {      // for error handling
			{
			switch ( LA(1)) {
			case LITERAL_stdin:
			{
				match(LITERAL_stdin);
				if ( inputState.guessing==0 ) {
					t=template("stdin");
				}
				break;
			}
			case LITERAL_stdout:
			{
				match(LITERAL_stdout);
				if ( inputState.guessing==0 ) {
					t=template("stdout");
				}
				break;
			}
			case LITERAL_stderr:
			{
				match(LITERAL_stderr);
				if ( inputState.guessing==0 ) {
					t=template("stderr");
				}
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			match(ASSIGN);
			m=mappingExpr();
			if ( inputState.guessing==0 ) {
				
					    t.setAttribute("content", m);
					    code.setAttribute("stdio", t);	
					
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_51);
			} else {
			  throw ex;
			}
		}
	}
	
	public final StringTemplate  mappingFunc() throws RecognitionException, TokenStreamException {
		StringTemplate code=template("mappingFunc");
		
		StringTemplate func=null, e=null;
		
		try {      // for error handling
			match(AT);
			{
			boolean synPredMatched137 = false;
			if (((LA(1)==ID) && (LA(2)==LPAREN) && (_tokenSet_17.member(LA(3))))) {
				int _m137 = mark();
				synPredMatched137 = true;
				inputState.guessing++;
				try {
					{
					declarator();
					match(LPAREN);
					}
				}
				catch (RecognitionException pe) {
					synPredMatched137 = false;
				}
				rewind(_m137);
				inputState.guessing--;
			}
			if ( synPredMatched137 ) {
				{
				func=declarator();
				match(LPAREN);
				e=expression();
				match(RPAREN);
				if ( inputState.guessing==0 ) {
					
						  code.setAttribute("name", func);
						  code.setAttribute("arg", e);
						
				}
				}
			}
			else if ((LA(1)==ID||LA(1)==LPAREN) && (_tokenSet_52.member(LA(2))) && (_tokenSet_53.member(LA(3)))) {
				{
				switch ( LA(1)) {
				case ID:
				{
					e=identifier();
					break;
				}
				case LPAREN:
				{
					{
					match(LPAREN);
					e=identifier();
					match(RPAREN);
					}
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
				if ( inputState.guessing==0 ) {
					
						  code.setAttribute("name", "filename");
						  code.setAttribute("arg", e);
						
				}
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_31);
			} else {
			  throw ex;
			}
		}
		return code;
	}
	
	public final String  wsdl() throws RecognitionException, TokenStreamException {
		String code=null;
		
		Token  u = null;
		
		try {      // for error handling
			match(LITERAL_wsdlURI);
			match(ASSIGN);
			u = LT(1);
			match(STRING_LITERAL);
			match(SEMI);
			if ( inputState.guessing==0 ) {
				code=u.getText();
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_54);
			} else {
			  throw ex;
			}
		}
		return code;
	}
	
	public final String  port() throws RecognitionException, TokenStreamException {
		String code=null;
		
		Token  u = null;
		
		try {      // for error handling
			match(LITERAL_portType);
			match(ASSIGN);
			u = LT(1);
			match(STRING_LITERAL);
			match(SEMI);
			if ( inputState.guessing==0 ) {
				code=u.getText();
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_55);
			} else {
			  throw ex;
			}
		}
		return code;
	}
	
	public final String  operation() throws RecognitionException, TokenStreamException {
		String code=null;
		
		Token  u = null;
		
		try {      // for error handling
			match(LITERAL_operation);
			match(ASSIGN);
			u = LT(1);
			match(STRING_LITERAL);
			match(SEMI);
			if ( inputState.guessing==0 ) {
				code=u.getText();
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_56);
			} else {
			  throw ex;
			}
		}
		return code;
	}
	
	public final StringTemplate  message() throws RecognitionException, TokenStreamException {
		StringTemplate code=template("message");
		
		Token  element = null;
		StringTemplate p=null, e=null;
		
		try {      // for error handling
			{
			switch ( LA(1)) {
			case LITERAL_request:
			{
				{
				match(LITERAL_request);
				if ( inputState.guessing==0 ) {
					code.setAttribute("type", "request");
				}
				}
				break;
			}
			case LITERAL_response:
			{
				{
				match(LITERAL_response);
				if ( inputState.guessing==0 ) {
					code.setAttribute("type", "response");
				}
				}
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			element = LT(1);
			match(ID);
			match(ASSIGN);
			if ( inputState.guessing==0 ) {
				code.setAttribute("name", element.getText());
			}
			{
			switch ( LA(1)) {
			case LCURLY:
			{
				{
				match(LCURLY);
				{
				int _cnt157=0;
				_loop157:
				do {
					if ((LA(1)==ID)) {
						p=part();
						if ( inputState.guessing==0 ) {
							code.setAttribute("parts", p);
						}
					}
					else {
						if ( _cnt157>=1 ) { break _loop157; } else {throw new NoViableAltException(LT(1), getFilename());}
					}
					
					_cnt157++;
				} while (true);
				}
				match(RCURLY);
				}
				break;
			}
			case ID:
			case STRING_LITERAL:
			case LPAREN:
			case AT:
			case PLUS:
			case MINUS:
			case NOT:
			case INT_LITERAL:
			case FLOAT_LITERAL:
			case LITERAL_true:
			case LITERAL_false:
			case LITERAL_null:
			{
				{
				e=mappingExpr();
				if ( inputState.guessing==0 ) {
					code.setAttribute("expr", e);
				}
				}
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			match(SEMI);
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_56);
			} else {
			  throw ex;
			}
		}
		return code;
	}
	
	public final StringTemplate  part() throws RecognitionException, TokenStreamException {
		StringTemplate code=template("part");
		
		Token  p = null;
		StringTemplate e=null;
		
		try {      // for error handling
			p = LT(1);
			match(ID);
			match(ASSIGN);
			if ( inputState.guessing==0 ) {
				code.setAttribute("name", p.getText());
			}
			{
			e=mappingExpr();
			if ( inputState.guessing==0 ) {
				code.setAttribute("expr", e);
			}
			}
			match(SEMI);
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_57);
			} else {
			  throw ex;
			}
		}
		return code;
	}
	
	public final StringTemplate  orExpr() throws RecognitionException, TokenStreamException {
		StringTemplate code=null;
		
		StringTemplate a,b;
		
		try {      // for error handling
			code=andExpr();
			{
			_loop164:
			do {
				if ((LA(1)==OR)) {
					match(OR);
					b=andExpr();
					if ( inputState.guessing==0 ) {
						
						a = code;
						code=template("or");
						code.setAttribute("left", a);
						code.setAttribute("right", b);
						
					}
				}
				else {
					break _loop164;
				}
				
			} while (true);
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_16);
			} else {
			  throw ex;
			}
		}
		return code;
	}
	
	public final StringTemplate  andExpr() throws RecognitionException, TokenStreamException {
		StringTemplate code=null;
		
		StringTemplate a,b;
		
		try {      // for error handling
			code=equalExpr();
			{
			_loop167:
			do {
				if ((LA(1)==AND)) {
					match(AND);
					b=equalExpr();
					if ( inputState.guessing==0 ) {
						
						a = code;
						code=template("and");
						code.setAttribute("left", a);
						code.setAttribute("right", b);
						
					}
				}
				else {
					break _loop167;
				}
				
			} while (true);
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_58);
			} else {
			  throw ex;
			}
		}
		return code;
	}
	
	public final StringTemplate  equalExpr() throws RecognitionException, TokenStreamException {
		StringTemplate code=null;
		
		
		StringTemplate a,b=null;
		Token op=null;
		
		
		try {      // for error handling
			code=condExpr();
			{
			switch ( LA(1)) {
			case EQ:
			case NE:
			{
				if ( inputState.guessing==0 ) {
					op=LT(1);
				}
				{
				switch ( LA(1)) {
				case EQ:
				{
					match(EQ);
					break;
				}
				case NE:
				{
					match(NE);
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
				b=condExpr();
				if ( inputState.guessing==0 ) {
					
					a = code;
					code=template("cond");
						    code.setAttribute("op", escape(op.getText()));
					code.setAttribute("left", a);
					code.setAttribute("right", b);
					
				}
				break;
			}
			case ID:
			case STRING_LITERAL:
			case SEMI:
			case LCURLY:
			case RBRACK:
			case COMMA:
			case COLON:
			case GT:
			case LPAREN:
			case RPAREN:
			case AT:
			case LITERAL_stdin:
			case LITERAL_stdout:
			case LITERAL_stderr:
			case OR:
			case AND:
			case PLUS:
			case MINUS:
			case NOT:
			case INT_LITERAL:
			case FLOAT_LITERAL:
			case LITERAL_true:
			case LITERAL_false:
			case LITERAL_null:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_59);
			} else {
			  throw ex;
			}
		}
		return code;
	}
	
	public final StringTemplate  condExpr() throws RecognitionException, TokenStreamException {
		StringTemplate code=null;
		
		
		StringTemplate a,b=null;
		Token op=null;
		
		
		try {      // for error handling
			code=additiveExpr();
			{
			if ((_tokenSet_60.member(LA(1))) && (_tokenSet_17.member(LA(2))) && (_tokenSet_61.member(LA(3)))) {
				if ( inputState.guessing==0 ) {
					op=LT(1);
				}
				{
				switch ( LA(1)) {
				case LT:
				{
					match(LT);
					break;
				}
				case LE:
				{
					match(LE);
					break;
				}
				case GT:
				{
					match(GT);
					break;
				}
				case GE:
				{
					match(GE);
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
				b=additiveExpr();
				if ( inputState.guessing==0 ) {
					
					a = code;
					code=template("cond");
						    code.setAttribute("op", escape(op.getText()));
					code.setAttribute("left", a);
					code.setAttribute("right", b);
					
				}
			}
			else if ((_tokenSet_62.member(LA(1))) && (_tokenSet_53.member(LA(2))) && (_tokenSet_63.member(LA(3)))) {
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_62);
			} else {
			  throw ex;
			}
		}
		return code;
	}
	
	public final StringTemplate  additiveExpr() throws RecognitionException, TokenStreamException {
		StringTemplate code=null;
		
		
		StringTemplate a,b=null;
		Token op=null;
		
		
		try {      // for error handling
			code=multiExpr();
			{
			_loop177:
			do {
				if ((LA(1)==PLUS||LA(1)==MINUS) && (_tokenSet_17.member(LA(2))) && (_tokenSet_52.member(LA(3)))) {
					if ( inputState.guessing==0 ) {
						op=LT(1);
					}
					{
					switch ( LA(1)) {
					case PLUS:
					{
						match(PLUS);
						break;
					}
					case MINUS:
					{
						match(MINUS);
						break;
					}
					default:
					{
						throw new NoViableAltException(LT(1), getFilename());
					}
					}
					}
					b=multiExpr();
					if ( inputState.guessing==0 ) {
						
						a = code;
						code=template("arith");
							    code.setAttribute("op", escape(op.getText()));
						code.setAttribute("left", a);
						code.setAttribute("right", b);
						
					}
				}
				else {
					break _loop177;
				}
				
			} while (true);
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_64);
			} else {
			  throw ex;
			}
		}
		return code;
	}
	
	public final StringTemplate  multiExpr() throws RecognitionException, TokenStreamException {
		StringTemplate code=null;
		
		
		StringTemplate a,b=null;
		Token op=null;
		
		
		try {      // for error handling
			code=unaryExpr();
			{
			_loop181:
			do {
				if (((LA(1) >= STAR && LA(1) <= MOD))) {
					if ( inputState.guessing==0 ) {
						op=LT(1);
					}
					{
					switch ( LA(1)) {
					case STAR:
					{
						match(STAR);
						break;
					}
					case DIV:
					{
						match(DIV);
						break;
					}
					case MOD:
					{
						match(MOD);
						break;
					}
					default:
					{
						throw new NoViableAltException(LT(1), getFilename());
					}
					}
					}
					b=unaryExpr();
					if ( inputState.guessing==0 ) {
						
						a = code;
						code=template("arith");
							    code.setAttribute("op", escape(op.getText()));
						code.setAttribute("left", a);
						code.setAttribute("right", b);
						
					}
				}
				else {
					break _loop181;
				}
				
			} while (true);
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_64);
			} else {
			  throw ex;
			}
		}
		return code;
	}
	
	public final StringTemplate  unaryExpr() throws RecognitionException, TokenStreamException {
		StringTemplate code=null;
		
		StringTemplate u=null;
		
		try {      // for error handling
			switch ( LA(1)) {
			case MINUS:
			{
				match(MINUS);
				u=unaryExpr();
				if ( inputState.guessing==0 ) {
					code=template("unary"); code.setAttribute("sign", "-"); code.setAttribute("exp", u);
				}
				break;
			}
			case PLUS:
			{
				match(PLUS);
				u=unaryExpr();
				if ( inputState.guessing==0 ) {
					code=template("unary"); code.setAttribute("sign", "+"); code.setAttribute("exp", u);
				}
				break;
			}
			case NOT:
			{
				match(NOT);
				u=unaryExpr();
				if ( inputState.guessing==0 ) {
					code=template("not"); code.setAttribute("exp", u);
				}
				break;
			}
			case ID:
			case STRING_LITERAL:
			case LPAREN:
			case AT:
			case INT_LITERAL:
			case FLOAT_LITERAL:
			case LITERAL_true:
			case LITERAL_false:
			case LITERAL_null:
			{
				code=primExpr();
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_31);
			} else {
			  throw ex;
			}
		}
		return code;
	}
	
	public final StringTemplate  primExpr() throws RecognitionException, TokenStreamException {
		StringTemplate code=null;
		
		StringTemplate id=null, exp=null;
		
		try {      // for error handling
			switch ( LA(1)) {
			case ID:
			{
				code=identifier();
				break;
			}
			case LPAREN:
			{
				match(LPAREN);
				exp=orExpr();
				match(RPAREN);
				if ( inputState.guessing==0 ) {
					code=template("paren");
							code.setAttribute("exp", exp);
				}
				break;
			}
			case STRING_LITERAL:
			case INT_LITERAL:
			case FLOAT_LITERAL:
			case LITERAL_true:
			case LITERAL_false:
			case LITERAL_null:
			{
				code=constant();
				break;
			}
			case AT:
			{
				code=mappingFunc();
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_31);
			} else {
			  throw ex;
			}
		}
		return code;
	}
	
	public final StringTemplate  subscript() throws RecognitionException, TokenStreamException {
		StringTemplate code=null;
		
		Token  id = null;
		Token  t = null;
		StringTemplate e=null, s=null;
		
		try {      // for error handling
			id = LT(1);
			match(ID);
			{
			switch ( LA(1)) {
			case LBRACK:
			{
				match(LBRACK);
				{
				switch ( LA(1)) {
				case ID:
				case STRING_LITERAL:
				case LPAREN:
				case AT:
				case PLUS:
				case MINUS:
				case NOT:
				case INT_LITERAL:
				case FLOAT_LITERAL:
				case LITERAL_true:
				case LITERAL_false:
				case LITERAL_null:
				{
					e=expression();
					break;
				}
				case STAR:
				{
					t = LT(1);
					match(STAR);
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
				match(RBRACK);
				break;
			}
			case ID:
			case STRING_LITERAL:
			case SEMI:
			case LCURLY:
			case RBRACK:
			case COMMA:
			case ASSIGN:
			case COLON:
			case LT:
			case GT:
			case LPAREN:
			case RPAREN:
			case AT:
			case LITERAL_stdin:
			case LITERAL_stdout:
			case LITERAL_stderr:
			case OR:
			case AND:
			case EQ:
			case NE:
			case LE:
			case GE:
			case PLUS:
			case MINUS:
			case STAR:
			case DIV:
			case MOD:
			case NOT:
			case DOT:
			case INT_LITERAL:
			case FLOAT_LITERAL:
			case LITERAL_true:
			case LITERAL_false:
			case LITERAL_null:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			if ( inputState.guessing==0 ) {
				
					code=template("subscript");
					code.setAttribute("var", id.getText());
					if (e != null) code.setAttribute("index", e);
					if (t != null) 
					{ s=template("sConst");
					  s.setAttribute("value", "*");
					  code.setAttribute("index", s);
					}
				
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_65);
			} else {
			  throw ex;
			}
		}
		return code;
	}
	
	
	public static final String[] _tokenNames = {
		"<0>",
		"EOF",
		"<2>",
		"NULL_TREE_LOOKAHEAD",
		"\"namespace\"",
		"ID",
		"STRING_LITERAL",
		"SEMI",
		"\"type\"",
		"LCURLY",
		"LBRACK",
		"RBRACK",
		"COMMA",
		"RCURLY",
		"ASSIGN",
		"COLON",
		"LT",
		"GT",
		"LPAREN",
		"RPAREN",
		"\"int\"",
		"\"string\"",
		"\"float\"",
		"\"date\"",
		"\"uri\"",
		"\"bool\"",
		"\"break\"",
		"\"continue\"",
		"\"for\"",
		"\"if\"",
		"\"else\"",
		"\"foreach\"",
		"\"in\"",
		"\"while\"",
		"\"repeat\"",
		"\"until\"",
		"\"switch\"",
		"\"case\"",
		"\"default\"",
		"\"app\"",
		"AT",
		"\"stdin\"",
		"\"stdout\"",
		"\"stderr\"",
		"\"service\"",
		"\"wsdlURI\"",
		"\"portType\"",
		"\"operation\"",
		"\"request\"",
		"\"response\"",
		"OR",
		"AND",
		"EQ",
		"NE",
		"LE",
		"GE",
		"PLUS",
		"MINUS",
		"STAR",
		"DIV",
		"MOD",
		"NOT",
		"DOT",
		"INT_LITERAL",
		"FLOAT_LITERAL",
		"\"true\"",
		"\"false\"",
		"\"null\"",
		"NUMBER",
		"WS",
		"SL_CCOMMENT",
		"SL_SCOMMENT",
		"ML_COMMENT",
		"ESC",
		"EXPONENT"
	};
	
	private static final long[] mk_tokenSet_0() {
		long[] data = { 97709720224L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_0 = new BitSet(mk_tokenSet_0());
	private static final long[] mk_tokenSet_1() {
		long[] data = { 2L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_1 = new BitSet(mk_tokenSet_1());
	private static final long[] mk_tokenSet_2() {
		long[] data = { 97709720498L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_2 = new BitSet(mk_tokenSet_2());
	private static final long[] mk_tokenSet_3() {
		long[] data = { 97709720482L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_3 = new BitSet(mk_tokenSet_3());
	private static final long[] mk_tokenSet_4() {
		long[] data = { 4611686116137133730L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_4 = new BitSet(mk_tokenSet_4());
	private static final long[] mk_tokenSet_5() {
		long[] data = { -1801438649431263518L, 15L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_5 = new BitSet(mk_tokenSet_5());
	private static final long[] mk_tokenSet_6() {
		long[] data = { 66322464L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_6 = new BitSet(mk_tokenSet_6());
	private static final long[] mk_tokenSet_7() {
		long[] data = { 66584608L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_7 = new BitSet(mk_tokenSet_7());
	private static final long[] mk_tokenSet_8() {
		long[] data = { 97709720226L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_8 = new BitSet(mk_tokenSet_8());
	private static final long[] mk_tokenSet_9() {
		long[] data = { 160L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_9 = new BitSet(mk_tokenSet_9());
	private static final long[] mk_tokenSet_10() {
		long[] data = { 66060320L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_10 = new BitSet(mk_tokenSet_10());
	private static final long[] mk_tokenSet_11() {
		long[] data = { 21632L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_11 = new BitSet(mk_tokenSet_11());
	private static final long[] mk_tokenSet_12() {
		long[] data = { -1801438201680988446L, 15L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_12 = new BitSet(mk_tokenSet_12());
	private static final long[] mk_tokenSet_13() {
		long[] data = { 97709728418L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_13 = new BitSet(mk_tokenSet_13());
	private static final long[] mk_tokenSet_14() {
		long[] data = { -6701339752852007712L, 15L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_14 = new BitSet(mk_tokenSet_14());
	private static final long[] mk_tokenSet_15() {
		long[] data = { 4224L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_15 = new BitSet(mk_tokenSet_15());
	private static final long[] mk_tokenSet_16() {
		long[] data = { -6701339752851924256L, 15L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_16 = new BitSet(mk_tokenSet_16());
	private static final long[] mk_tokenSet_17() {
		long[] data = { -6701355146015408032L, 15L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_17 = new BitSet(mk_tokenSet_17());
	private static final long[] mk_tokenSet_18() {
		long[] data = { -1124800394722208L, 15L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_18 = new BitSet(mk_tokenSet_18());
	private static final long[] mk_tokenSet_19() {
		long[] data = { -1124800394197920L, 15L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_19 = new BitSet(mk_tokenSet_19());
	private static final long[] mk_tokenSet_20() {
		long[] data = { -1124800394748832L, 15L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_20 = new BitSet(mk_tokenSet_20());
	private static final long[] mk_tokenSet_21() {
		long[] data = { -1124800394224416L, 15L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_21 = new BitSet(mk_tokenSet_21());
	private static final long[] mk_tokenSet_22() {
		long[] data = { 528512L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_22 = new BitSet(mk_tokenSet_22());
	private static final long[] mk_tokenSet_23() {
		long[] data = { 131072L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_23 = new BitSet(mk_tokenSet_23());
	private static final long[] mk_tokenSet_24() {
		long[] data = { 135168L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_24 = new BitSet(mk_tokenSet_24());
	private static final long[] mk_tokenSet_25() {
		long[] data = { -2089653734425094944L, 15L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_25 = new BitSet(mk_tokenSet_25());
	private static final long[] mk_tokenSet_26() {
		long[] data = { -1109407231961888L, 15L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_26 = new BitSet(mk_tokenSet_26());
	private static final long[] mk_tokenSet_27() {
		long[] data = { -264982301281056L, 15L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_27 = new BitSet(mk_tokenSet_27());
	private static final long[] mk_tokenSet_28() {
		long[] data = { -6701339752852483872L, 15L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_28 = new BitSet(mk_tokenSet_28());
	private static final long[] mk_tokenSet_29() {
		long[] data = { 528384L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_29 = new BitSet(mk_tokenSet_29());
	private static final long[] mk_tokenSet_30() {
		long[] data = { 8192L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_30 = new BitSet(mk_tokenSet_30());
	private static final long[] mk_tokenSet_31() {
		long[] data = { -4612795425658791200L, 15L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_31 = new BitSet(mk_tokenSet_31());
	private static final long[] mk_tokenSet_32() {
		long[] data = { 545460069026L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_32 = new BitSet(mk_tokenSet_32());
	private static final long[] mk_tokenSet_33() {
		long[] data = { 510026588834L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_33 = new BitSet(mk_tokenSet_33());
	private static final long[] mk_tokenSet_34() {
		long[] data = { 128L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_34 = new BitSet(mk_tokenSet_34());
	private static final long[] mk_tokenSet_35() {
		long[] data = { 4611686018493727776L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_35 = new BitSet(mk_tokenSet_35());
	private static final long[] mk_tokenSet_36() {
		long[] data = { -1801438751435762592L, 15L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_36 = new BitSet(mk_tokenSet_36());
	private static final long[] mk_tokenSet_37() {
		long[] data = { 4611686018427405312L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_37 = new BitSet(mk_tokenSet_37());
	private static final long[] mk_tokenSet_38() {
		long[] data = { -6413124769863695264L, 15L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_38 = new BitSet(mk_tokenSet_38());
	private static final long[] mk_tokenSet_39() {
		long[] data = { 524416L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_39 = new BitSet(mk_tokenSet_39());
	private static final long[] mk_tokenSet_40() {
		long[] data = { 412316868608L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_40 = new BitSet(mk_tokenSet_40());
	private static final long[] mk_tokenSet_41() {
		long[] data = { 510026588832L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_41 = new BitSet(mk_tokenSet_41());
	private static final long[] mk_tokenSet_42() {
		long[] data = { 4611686018493465632L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_42 = new BitSet(mk_tokenSet_42());
	private static final long[] mk_tokenSet_43() {
		long[] data = { -4612795425658774816L, 15L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_43 = new BitSet(mk_tokenSet_43());
	private static final long[] mk_tokenSet_44() {
		long[] data = { 4611686018427933696L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_44 = new BitSet(mk_tokenSet_44());
	private static final long[] mk_tokenSet_45() {
		long[] data = { 544768L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_45 = new BitSet(mk_tokenSet_45());
	private static final long[] mk_tokenSet_46() {
		long[] data = { 66076704L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_46 = new BitSet(mk_tokenSet_46());
	private static final long[] mk_tokenSet_47() {
		long[] data = { 4611686018428195872L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_47 = new BitSet(mk_tokenSet_47());
	private static final long[] mk_tokenSet_48() {
		long[] data = { -6701355146015407008L, 15L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_48 = new BitSet(mk_tokenSet_48());
	private static final long[] mk_tokenSet_49() {
		long[] data = { -1124800394224544L, 15L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_49 = new BitSet(mk_tokenSet_49());
	private static final long[] mk_tokenSet_50() {
		long[] data = { -6701339752852619168L, 15L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_50 = new BitSet(mk_tokenSet_50());
	private static final long[] mk_tokenSet_51() {
		long[] data = { -6701339752852619040L, 15L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_51 = new BitSet(mk_tokenSet_51());
	private static final long[] mk_tokenSet_52() {
		long[] data = { -1109407231402272L, 15L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_52 = new BitSet(mk_tokenSet_52());
	private static final long[] mk_tokenSet_53() {
		long[] data = { -264472274927902L, 15L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_53 = new BitSet(mk_tokenSet_53());
	private static final long[] mk_tokenSet_54() {
		long[] data = { 211106232532992L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_54 = new BitSet(mk_tokenSet_54());
	private static final long[] mk_tokenSet_55() {
		long[] data = { 140737488355328L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_55 = new BitSet(mk_tokenSet_55());
	private static final long[] mk_tokenSet_56() {
		long[] data = { 844424930140160L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_56 = new BitSet(mk_tokenSet_56());
	private static final long[] mk_tokenSet_57() {
		long[] data = { 8224L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_57 = new BitSet(mk_tokenSet_57());
	private static final long[] mk_tokenSet_58() {
		long[] data = { -6700213852945081632L, 15L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_58 = new BitSet(mk_tokenSet_58());
	private static final long[] mk_tokenSet_59() {
		long[] data = { -6697962053131396384L, 15L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_59 = new BitSet(mk_tokenSet_59());
	private static final long[] mk_tokenSet_60() {
		long[] data = { 54043195528642560L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_60 = new BitSet(mk_tokenSet_60());
	private static final long[] mk_tokenSet_61() {
		long[] data = { -55152602759913760L, 15L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_61 = new BitSet(mk_tokenSet_61());
	private static final long[] mk_tokenSet_62() {
		long[] data = { -6684451254249284896L, 15L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_62 = new BitSet(mk_tokenSet_62());
	private static final long[] mk_tokenSet_63() {
		long[] data = { -264436841447710L, 15L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_63 = new BitSet(mk_tokenSet_63());
	private static final long[] mk_tokenSet_64() {
		long[] data = { -6630408058720773408L, 15L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_64 = new BitSet(mk_tokenSet_64());
	private static final long[] mk_tokenSet_65() {
		long[] data = { -1109407231386912L, 15L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_65 = new BitSet(mk_tokenSet_65());
	
	}
