header {
package org.globus.swift.parser;

import org.globus.swift.parsetree.*;
import java.util.List;
import java.util.Iterator;
import antlr.Token;

}

class SwiftScriptParser extends Parser;

options {
    k=2;
    codeGenMakeSwitchThreshold = 2;
    codeGenBitsetTestThreshold = 3;
    defaultErrorHandler = false;
}

{
	protected SwiftScriptLexer swiftLexer = null;
	protected Program program;

	/** TODO this can perhaps be extracted from the superclass, but I don't
    	have javadocs available at the time of writing. */
	public void setSwiftLexer(SwiftScriptLexer sl) {
    	swiftLexer = sl;
	}

	<T extends AbstractNode> T setLine(T node) {
		node.setLine(swiftLexer.getLine());
		return node;
	} 
}

// The specification for a SwiftScript program
program returns [Program program = setLine(new Program())]
{
	this.program = program;
}
:
    (importStatement[program])*
    (topLevelStatement[program])*
    EOF
;

importStatement [Program program]
: 
	"import" name:STRING_LITERAL SEMI {
    	Import i = setLine(new Import());
    	i.setTarget(name.getText());
    	program.addImport(i);
    }
;

typedecl [Program program]
{
	TypeDeclaration r = setLine(new TypeDeclaration());
 	String t = null;
}
:    
	"type" 
	id:ID {    
		r.setName(id.getText()); 
    }
    (
        SEMI
        | 
        (
        	t = type { 
        		r.setTypeAlias(t); 
        	} 
        	SEMI
        )
        | 
        structdecl[r]
    ) {
    	program.addType(r);
    }
;

structdecl [TypeDeclaration td]
{
	TypeMemberDeclaration mdef = null, mdef2 = null; 
	String t = null; String thisType = null;
}
:   
	LCURLY
    (
    	t = type id:ID {
    		thisType = t;
    		mdef = setLine(new TypeMemberDeclaration());
    		mdef.setName(id.getText());
    	}
    	(LBRACK RBRACK { thisType = thisType + "[]"; })* {
      		mdef.setType(thisType);
      		td.addMember(mdef);
    	}
    	(
        	COMMA
        	id1:ID {
        		thisType = t;
        		mdef2 = setLine(new TypeMemberDeclaration());
        		mdef2.setName(id1.getText());
        	}
        	(LBRACK RBRACK { thisType = thisType + "[]"; })* {
           		mdef2.setType(thisType);
           		td.addMember(mdef2);
         	}
    	)*
    	SEMI
    )*
    RCURLY
    (
    	options {
			warnWhenFollowAmbig = false;
		}
    	:SEMI
    )?
;

topLevelStatement[Program program]
{
	Statement d = null;
	FunctionDeclaration f = null;
}
:

	// these are ll(1) and easy to predict

    typedecl[program]
    | 
    d = ll1statement {
    	program.addStatement(d);
    }

	// these are non-declaration assign-like statements

    | 
    (predictAssignStat) => d = assignStat {
		program.addStatement(d);
    }

	// these are non-declaration append-associative array statements

	| 
	(predictAppendStat) => d = appendStat {
		program.addStatement(d);
	}

	// they all begin with (id name)
    | 
    (predictDeclaration) => declaration[program.getBody()]
	| 
	(predictProceduredecl) => f = proceduredecl {
		program.addFunctionDeclaration(f);
	}
	// more complicated function invocations
	// note that function invocations can happen in above statements too
	// this section is just the remaining more specialised invocations

    | 
    (predictProcedurecallCode) => d = procedurecallCode {
    	program.addStatement(d);
    }

    | 
    (predictProcedurecallStatAssignManyReturnParam) => procedurecallStatAssignManyReturnParam[program.getBody()]

	// this is a declaration, but not sorted out the predications yet to
	// group it into a decl block
    | 
    ("app") => f = appproceduredecl {
    	program.addFunctionDeclaration(f);
    }
;

predictDeclaration 
{
	Object dummy = null;
}
:
		("global") | (dummy = type dummy = declarator) 
;

declaration [StatementContainer scope]
{
	String t = null; 
	boolean isGlobal = false;
}
: 
	(
		"global" {
			isGlobal = true;
		}
	)?
	t = type
	declpart[scope, t, isGlobal]
	(COMMA declpart[scope, t, isGlobal])*
	SEMI
;

declpart [StatementContainer scope, String thisType, boolean isGlobal]
{
    VariableDeclaration vdecl = null;
    String keyType = "";
    String name = null;
    MappingDeclaration mdecl = null;
}
:
	name = declarator
	(
		LBRACK
		(keyType = type)?
		RBRACK {
			thisType = thisType + "[" + keyType + "]" ; keyType = "";
		} 
	)* {
     	vdecl = setLine(new VariableDeclaration());
     	vdecl.setName(name);
     	vdecl.setType(thisType);
     	vdecl.setGlobal(isGlobal);
        scope.addVariableDeclaration(vdecl);
    }
	(
		LT 
		(mdecl = mappingdecl | f:STRING_LITERAL) GT {
   			if (mdecl != null) {
       			vdecl.setMapping(mdecl);
       		}
   			else {
       			vdecl.setLFN(f.getText());
       		}
		}
	)?

	// TODO: mapping does here...
	// which means construction of the variable template goes here, rather than
	// in variableDecl

	(
		variableDecl[scope, thisType, name, vdecl]
		// nice to lose this distinction entirely...
		//    | (predictDatasetdecl) => datasetdecl[scope, thisType, name]
		// TODO can shorten variableDecl predictor now we dont' need to
		//  distinguish it from datasetdecl?
	)
;

variableDecl [StatementContainer scope, String vtype, String name, VariableDeclaration vdecl]
{
	Expression value = null;
}
:
    (
    	value = varInitializer {
        	if (value != null) {
        		Assignment assign = setLine(new Assignment());
        		assign.setLhs(new VariableReference(name));
        		assign.setRhs(value);
        		scope.addStatement(assign);
        	}
    	}
    )?
;

declarator returns [String name = null] 
:   
	id:ID {
		name = id.getText();
	}
;

appDeclarator returns [String app = null]
:       
    id:ID {
    	app = id.getText();
    }
    | 
    s:STRING_LITERAL {
    	app = s.getText();
    }
;


varInitializer returns [Expression value = null]
:   
	ASSIGN value = expression
;

// This is an initializer used to set up an array.
// currently does not support nested array.
arrayInitializer returns [ArrayInitializer ai = null]
{
	Expression e = null;
	Expression from = null;
	Expression to = null;
	Expression step = null;
	ai = setLine(new ArrayInitializer());
}
: 
	LBRACK
    (
		(expression COLON) => (
      		from = expression COLON to = expression (COLON step = expression)?
      		{
      			ai.setType(ArrayInitializer.Type.RANGE);
      			ArrayInitializer.Range range = new ArrayInitializer.Range();
      			range.setFrom(from);
      			range.setTo(to);
        		if (step != null) {
        			range.setStep(step);
        		}
        		ai.setRange(range);
      		}
     	)
     	|
     	(
     		{ ai.setType(ArrayInitializer.Type.ITEMS); }
      		e = expression {
      			ai.addItem(e);
      		}
      		(
        		// CONFLICT: does a COMMA after an initializer start a new
      			//           initializer or start the option ',' at end?
      			//           ANTLR generates proper code by matching
      			//             the comma as soon as possible.
        		options {
          			warnWhenFollowAmbig = false;
        		} : COMMA e = expression {
        			ai.addItem(e);
        		}
      		)*
      		(COMMA)?
     	)
    )?
    RBRACK
;

// there are two places where this can be used. One is
// structure initializers and the other is sparse array initializers.
// they differ based on the key type, which are identifiers for structs
// and expressions for arrays.
// The empty struct/array "{}" is ambiguous, so the determination
// of whether this is a struct initializer or sparse array initializer
// is made by the compiler rather than the parser 
structInitializer returns [StructInitializer si = null]
{
	Expression key = null;
	Expression expr = null;
	si = setLine(new StructInitializer());
}
:    
   	LCURLY
   	(
   		key = expression COLON expr = expression {
   			si.addFieldInitializer(new FieldInitializer(key, expr));
   		}
   	)?
   	( 
   		COMMA key = expression COLON expr = expression {
   			si.addFieldInitializer(new FieldInitializer(key, expr));
   		}
   	)*
   	RCURLY
;

mappingdecl returns [MappingDeclaration mdecl = null]
{
	mdecl = setLine(new MappingDeclaration());
	String descriptor = null;
}
:  
	descriptor = declarator {
		mdecl.setDescriptor(descriptor);
	} 
	SEMI
    mapparamdecl[mdecl]
;

mapparamdecl [MappingDeclaration mdecl]
{
	MappingParameter mparam = null;
}
:
	(  
		mparam = mapparam {
			mdecl.addParameter(mparam);
		}
        ( 
        	COMMA 
        	mparam = mapparam {
        		mdecl.addParameter(mparam);
        	} 
        )*
	)?
;

mapparam returns [MappingParameter mparam = null]
{
	mparam = setLine(new MappingParameter());
	String name = null;
	Expression value = null;
}
:  
	name = declarator ASSIGN value = mappingExpr {
    	mparam.setName(name);
    	mparam.setValue(value);
    }
;

// This predicts in two different ways.
// The first choice is procedures with no return parameters. For these,
// we must predict as far as the opening { in order to distinguish
// from procedure calls to procedures with no return parameters.
// The second choice is for procedures with return parameters. Here we
// predict as far as the bracket after the procedure name. We have to
// predict on the return parameters, which means we won't get good
// error reporting when there is a syntax error in there.
predictProceduredecl 
{
	FormalParameter f = null;
}
: 
    ( 
    	id:ID LPAREN
        (f = formalParameter (COMMA f = formalParameter)* )?
        RPAREN
        LCURLY
     )
     |
     (
        LPAREN
        f = formalParameter
        (COMMA f = formalParameter)*
        RPAREN ID LPAREN
     )
;

proceduredecl returns [FunctionDeclaration fdecl = null]
{
	fdecl = setLine(new FunctionDeclaration());
	AppDeclaration appdecl = null;
	FormalParameter param = null;
}
:  
    ( 
    	LPAREN
        param = formalParameter {
        	fdecl.addReturn(param);
        }
        (   
        	COMMA param = formalParameter {
        		fdecl.addReturn(param);
            }
        )*
     	RPAREN 
     )?
     id:ID {fdecl.setName(id.getText());} LPAREN
     (   
      	param = formalParameter {
      		fdecl.addParameter(param);
        }
        (   
          	COMMA param = formalParameter {
              	fdecl.addParameter(param);
            }
        )*
     )?
     RPAREN
     LCURLY
     (
      	(predictAtomicBody) => {
       		appdecl = new AppDeclaration(fdecl);
       		fdecl = appdecl;
       	}
       	atomicBody[appdecl]
       	|
       	compoundBody[fdecl.getBody()]
     )
     RCURLY
;

predictAtomicBody
:
	"app"
;
    
appproceduredecl returns [AppDeclaration app = null]
{
	app = setLine(new AppDeclaration());
	FormalParameter param = null;
	String exec = null;
	AppCommand cmd = null;
}
: 
	"app"
	( 
		LPAREN
		param = formalParameter {
        	app.addReturn(param);
		}
		(
			COMMA param = formalParameter {
				app.addReturn(param);
			}
		)*
		RPAREN 
	)?
	id:ID {
		app.setName(id.getText());
	} 
	LPAREN
	(   
		param = formalParameter {
			app.addParameter(param);
		}
		(   
			COMMA param = formalParameter {
				app.addParameter(param);
			}
		)*
	)?
	RPAREN
	LCURLY
	(appProfile[app])*
	(
		(cmd = appCommand) {
			app.addCommand(cmd);
		}
	)*
	RCURLY
;

appCommand returns [AppCommand cmd = null] {
	cmd = setLine(new AppCommand());
	String exec = null;	
}:
	(exec = appDeclarator) {
		cmd.setExecutable(exec);
	}
    (appArg[cmd])* 
    SEMI
;

appProfile [AppDeclaration app]
{
	Expression name = null;
	Expression value = null;   
}
: 
	"profile" name = expression 
	ASSIGN 
	value = expression SEMI {
		app.addProfile(new AppProfile(name, value));
    }
;

// TODO in here, why do we have an | between LBRACKBRACK and ASSIGN?
// does this mean that we don't have array initialisation in formal
// params? this wouldn't surprise me given the previous treatment
// of arrays. investigate this and fix...
formalParameter returns [FormalParameter param = null]
{
	param = setLine(new FormalParameter());
	String type_ = null;
	String name = null;
	Expression value = null;
}
:   
	(
    	type_ = type
    	name = declarator {
    		param.setName(name);
	    }
        (LBRACK RBRACK { type_ = type_ + "[]"; })*
        (
        	ASSIGN value = constant {
        		param.setDefaultValue(value);
        	}
        )?
	) {
    	param.setType(type_);
	}
;

type returns [String typeName = null]
{ 
	StringBuilder buf = new StringBuilder(); 
}
:
	id:ID {
		buf.append(id.getText());
	}
	(typeSubscript[buf]) * {
		typeName = buf.toString();
	}
;

typeSubscript[StringBuilder buf] 
:
	LBRACK { 
		buf.append('['); 
	}
	(
		id:ID { 
			buf.append(id.getText());
		}
	)?
	RBRACK { 
		buf.append(']'); 
	}
;

compoundStat[StatementContainer scope]
:   
   	LCURLY
   	compoundBody[scope]
    RCURLY
;

compoundBody[StatementContainer scope]
:
    (innerStatement[scope])*
;
    
innerStatement[StatementContainer scope]
{
	Statement s;
}
: 
	(predictDeclaration) => declaration[scope]
    |
    (
    	(
			s = ll1statement
    		|  (predictProcedurecallCode) => s = procedurecallCode
    		|  (predictAssignStat) => s = assignStat
    		|  (predictAppendStat) => s = appendStat
    	) {
			scope.addStatement(s);
       	}
    )
    |
    (procedurecallStatAssignManyReturnParam[scope]) => procedurecallStatAssignManyReturnParam[scope]
;

caseInnerStatement [StatementContainer scope]
{ 
	Statement s = null; 
}
:
    (  
    	s = ll1statement
    	|  
    	(predictProcedurecallCode) => s = procedurecallCode
    	|  
    	(predictAssignStat) => s = assignStat
    	|  
    	(predictAppendStat) => s = appendStat
    ) {
    	scope.addStatement(s);
    }
    |   
    (procedurecallStatAssignManyReturnParam[scope]) => procedurecallStatAssignManyReturnParam[scope]
;

// These are the statements that we can predict with ll(1) grammer
// i.e. with one token of lookahead
ll1statement returns [Statement s = null]
:
    s = ifStat
    | 
    s = foreachStat
    | 
    s = switchStat
    | 
    s = iterateStat
;

ifStat returns [IfStatement s = null]
{
	s = setLine(new IfStatement());
  	Expression cond = null;
  	StatementContainer else_ = s.getElseScope();
}
:  
	"if" LPAREN cond = expression RPAREN {
		s.setCondition(cond);
	}
	compoundStat[s.getThenScope()]
	(
		options {
			warnWhenFollowAmbig = false;
		}
        : "else" bodyOrIf[else_]
	)?
;
    
bodyOrIf [StatementContainer scope]
{
	IfStatement ifs = null;
}
:
	ifs = ifStat {
		scope.addStatement(ifs);
	}
	| 
	compoundStat[scope]
;

foreachStat returns [ForeachStatement fs = null]
{
	fs = setLine(new ForeachStatement());
	Expression inexp = null;
}
    :  "foreach" id:ID (COMMA indexId:ID)? "in" inexp = expression
    	{
    		fs.setVar(id.getText());
        	fs.setInExpression(inexp);
        	if (indexId != null) {
        		fs.setIndexVar(indexId.getText());
        	}
    	}
    	compoundStat[fs.getBody()]
;

iterateStat returns [IterateStatement is]
{
	is = setLine(new IterateStatement());
  	Expression cond = null;
}
:
	"iterate" id:ID
	compoundStat[is.getBody()] 
	"until" LPAREN cond = expression RPAREN SEMI {
		is.setVar(id.getText());
		is.setCondition(cond);
	}
;

switchStat returns [SwitchStatement ss]
{
	ss = setLine(new SwitchStatement());
	Expression cond = null;
	SwitchCase case_ = null;
}
:    
	"switch" LPAREN cond = expression RPAREN {
    	ss.setCondition(cond);
	}
	LCURLY
	( 
		case_ = casesGroup {
			ss.addCase(case_);
		} 
	)*
	RCURLY
;

casesGroup returns [SwitchCase sc]
{
	sc = setLine(new SwitchCase());
}
:
	(    
		// CONFLICT: to which case group do the statements bind?
		//           ANTLR generates proper code: it groups the
		//           many "case"/"default" labels together then
		//           follows them with the statements
		options {
			greedy = true;
		}: aCase[sc]
	)
	caseSList[sc]
;

aCase [SwitchCase sc]
{
	Expression value = null;
}
:
	(
		"case" value = expression {
			sc.setValue(value);
		}
      	| 
      	"default" {
      		sc.setIsDefault(true);
		}
	)
	COLON
;

caseSList [SwitchCase sc]
:    
	(
		caseInnerStatement[sc.getBody()]
	)*
;

predictAssignStat
{
	Object dummy = null;
}
: 
	dummy = lvalue ASSIGN 
;

predictAppendStat
{
	Object dummy = null;
}
: 
	dummy = lvalue APPEND
;

assignStat returns [Assignment assign = null]
{
	assign = setLine(new Assignment());
	LValue id = null;
	Expression value = null;  
}
:
	id = lvalue
	ASSIGN
	value = variableAssign {
		assign.setLhs(id);
		assign.setRhs(value);
	}
;

appendStat returns [ Append append = null ]
{
	append = setLine(new Append()); 
	LValue id = null;
	Expression value = null; 
}
:
    id = lvalue
    APPEND
	value = arrayAppend {
		append.setLhs(id);
		append.setRhs(value);
	}
;

arrayAppend returns [Expression e = null]
:
	e = expression SEMI
;


variableAssign returns [Expression e = null]
:
	e = expression SEMI
;
    
predictProcedurecallCode:
	ID LPAREN
;

procedurecallCode returns [Call call = setLine(new Call())]
:
	procedureInvocation[call]
;

procedureInvocation [Call call]
:
	procedureInvocationWithoutSemi[call]
	SEMI
;

procedureInvocationWithoutSemi [Call call]
{
	ActualParameter param = null;
}
:
    id:ID {
    	call.setName(id.getText());
    }
    LPAREN 
    (
    	param = actualParameter {
    		call.addParameter(param);
    	}
        ( 
        	COMMA param = actualParameter {
        		call.addParameter(param);
        	}
        )*
    )?
    RPAREN
;

procedureInvocationExpr [Call call]
{
	ActualParameter param = null;
}
:
    id:ID {
    	call.setName(id.getText());
    }
    LPAREN (
    	param = actualParameter {
    		call.addParameter(param);
    	}
   		(
   			COMMA param = actualParameter {
    			call.addParameter(param);
        	}
        )*
    )?
    RPAREN
;

procedureCallExpr returns [Call call = null]
{
	call = setLine(new Call());
}
:
	procedureInvocationExpr[call]
;

predictProcedurecallStatAssignManyReturnParam:
	LPAREN
    predictProcedurecallStatAssignManyReturnOutput
    (COMMA predictProcedurecallStatAssignManyReturnOutput)*
    RPAREN
    ASSIGN
;

predictProcedurecallStatAssignManyReturnOutput 
{
	Expression dummy = null;
}
:
	ID (ID)? (ASSIGN dummy = expression)?
;

procedurecallStatAssignManyReturnParam [StatementContainer scope]
{ 
	Call call = setLine(new Call()); 
}
:
	LPAREN
	procedurecallStatAssignManyReturnOutput[scope, call]
	(
		COMMA procedurecallStatAssignManyReturnOutput[scope, call] 
	)*
	RPAREN
	ASSIGN
	procedureInvocation[call] {
		scope.addStatement(call);
	}
;

procedurecallStatAssignManyReturnOutput [StatementContainer scope, Call call]
{
	ReturnParameter ret = null;
}
:
	ret = returnParameter[scope] {
		call.addReturn(ret);
	}
;

returnParameter [StatementContainer scope] returns [ReturnParameter ret = null]
{
	ret = setLine(new ReturnParameter());
}
:
	(predictReturnDeclaration) => returnDeclaration[ret, scope]
	|
	plainReturnParameter[ret]
;

predictReturnDeclaration
{
	Object dummy = null;
}
:
	dummy = type dummy = identifier
;

returnDeclaration [ReturnParameter ret, StatementContainer scope]
{
	String type_ = null;
	String id = null;
	String binding = null;
}
:
	type_ = type
	id = identifier {
		ret.setLValue(new VariableReference(id));
		ret.setType(type_);
		
		// probably not the right place for this
		VariableDeclaration decl = new VariableDeclaration();
		decl.setName(id);
		decl.setType(type_);
               
		scope.addVariableDeclaration(decl);
	}
	(
		ASSIGN
		binding = identifier {
			ret.setBinding(binding);
		}
	)?
;

plainReturnParameter [ReturnParameter ret]
{
	LValue lvalue_ = null;
	String id = null;
}
:
	lvalue_ = lvalue {
		ret.setLValue(lvalue_);
	}
	(
		ASSIGN
		id = identifier {
			ret.setBinding(id);
		}
	)?
;

actualParameter returns [ActualParameter param = setLine(new ActualParameter())]
:
	(predictNamedParam) => namedParam[param] 
	| 
	positionalParam[param]
;

predictNamedParam
{
	Object dummy = null;
}
:
	dummy = declarator ASSIGN
;

namedParam[ActualParameter param] 
{
	String binding = null;
}
:
	binding = declarator ASSIGN {
		param.setBinding(binding);
	}
	positionalParam[param]
;

positionalParam[ActualParameter param] 
{
	Expression expr = null;
}
:
	expr = expression {
		param.setValue(expr);
	}
;

atomicBody [AppDeclaration app] {
	AppCommand cmd = setLine(new AppCommand());
	app.addCommand(cmd);
}
:      
	appSpec[cmd]
;

/* This is the deprecated format for app { } blocks */
appSpec [AppCommand cmd]
{
	String exec = null;
}
:  
	"app" LCURLY
    exec = declarator { 
    	cmd.setExecutable(exec);
    }
    (
    	appArg[cmd]
    )*
    SEMI RCURLY
;

appArg [AppCommand cmd]
{
	Expression arg = null;
}
:   
	arg = mappingExpr	{
		app.addArgument(arg);
	}
    |
    stdioArg[app]
;

mappingExpr returns [Expression arg = null]
:
    arg = expression
;

functionInvocation returns [FunctionInvocation fi = setLine(new FunctionInvocation())]
{
	String name = null;
	LValue ref = null;
}
:   
	AT (
		(
			(declarator LPAREN) =>
	    		(
	    			name = declarator {
	    				fi.setName(name);
	     			}
	     			LPAREN
	     			(
	     				functionInvocationArgument[fi]
	     				(
	       					COMMA
	       					functionInvocationArgument[fi]
	     				)
	     			*)?
	     			RPAREN
	    		)
	    		|
	    		(name = identifier | (LPAREN name = identifier RPAREN)) {
	      			/*
			       	* This is matched on expressions like @varname,
			       	* which are a shortcut for filename(varname).
			       	* The interpretation of what a function invocation
			       	* with an empty file name means was moved to the swiftx -> ?
			       	* compiler and allows that layer to distinguish between
			       	* '@filename(x)' and '@(x)', the former of which 
			       	* has been deprecated.
			       	*/
			       	fi.setName("");
			       	ActualParameter param = new ActualParameter();
			       	VariableReference var = new VariableReference(name);
			       	param.setValue(var);
			       	fi.addParameter(param);
	    		}
	    )
	    |
	    (ref = lvalue | (LPAREN ref = lvalue RPAREN)) {
  			/*
	       	* This is matched on expressions like @struct.field
	       	*/
	       	fi.setName("");
	       	ActualParameter param = new ActualParameter();
	       	param.setValue(ref);
	       	fi.addParameter(param);
		}
	)
;

functionInvocationArgument [FunctionInvocation fi]
{
	Expression expr = null;
}
:
     expr = expression {
     	ActualParameter ap = new ActualParameter();
     	ap.setValue(expr);
     	fi.addParameter(ap);
     }
;

stdioArg [AppCommand cmd]
{
	Expression expr = null;
	String name = null;
}
:    
	(
		"stdin" {name = "stdin";}
    	|
    	"stdout" {name="stdout";}
    	|
    	"stderr" {name="stderr";}
    )
    ASSIGN
    expr = expression {
    	cmd.addRedirect(name, expr);
    }
;

expression returns [Expression expr = null]
:   
	expr = orExpr
;

orExpr returns [Expression expr = null]
{
	Expression a = null;
	Expression b = null;
}
:   
	expr = andExpr
    (   
    	OR b = andExpr {
            a = expr;
            expr = new BinaryOperator(Expression.Type.OR, a, b);
        }
    )*
;

andExpr returns [Expression expr = null]
{
	Expression a = null;
	Expression b = null;
}
:   
	expr = equalExpr
    (   
    	AND b = equalExpr {
            a = expr;
            expr = new BinaryOperator(Expression.Type.AND, a, b);
        }
    )*
;

equalExpr returns [Expression expr = null]
{
	Expression a = null;
	Expression b = null;
	Token op = null;
}
:   
	expr = condExpr
    (
    	{
    		op = LT(1);
    	}
        ( EQ | NE ) b = condExpr {
            a = expr;
            expr = new BinaryOperator(op.getText(), a, b);
        }
    )?
;

condExpr returns [Expression expr = null]
{
	Expression a = null;
	Expression b = null;
	Token op = null;
}
:   
	expr = additiveExpr
    (
        options {
        	greedy = true;
        	//warnWhenFollowAmbig = false;
        }
        :
        {
       		op=LT(1);
       	}
        ( LT | LE | GT | GE ) b = additiveExpr {
        	a = expr;
        	expr = new BinaryOperator(op.getText(), a, b);
        }
    )?
;

additiveExpr returns [Expression expr = null]
{
	Expression a = null;
	Expression b = null;
	Token op = null;
}
:   
	expr = multiExpr
    (
        options {
        	greedy = true;
        	//warnWhenFollowAmbig = false;
        }
        :
        {
        	op = LT(1);
        }
        ( PLUS | MINUS ) b = multiExpr {
            a = expr;
            expr = new BinaryOperator(op.getText(), a, b);
        }
    )*
;

multiExpr returns [Expression expr = null]
{
	Expression a = null;
	Expression b = null;
	Token op = null;
}
:
	expr = unaryExpr
    (
        options {
        	greedy = true;
        	//warnWhenFollowAmbig = false;
        }
        :
        {
        	op=LT(1);
        }
        ( STAR | IDIV | FDIV | MOD ) b = unaryExpr {
        	a = expr;
        	expr = new BinaryOperator(op.getText(), a, b);
        }
    )*
;

unaryExpr returns [Expression expr = null]
{
	Expression arg = null;
}
: 
    MINUS arg = unaryExpr {
    	expr = new UnaryOperator(Expression.Type.NEGATION, arg);
    }
    | 
    PLUS arg = unaryExpr {
    	// unary plus has no effect 
    	// do we decide here though?
    	expr = arg;
    }
    | 
    NOT arg = unaryExpr {
    	expr = new UnaryOperator(Expression.Type.NOT, arg);
    }
    | 
    expr = primExpr
;

primExpr returns [Expression expr = null]
{
	String var = null;
}
: 
	(predictProcedureCallExpr) => expr = procedureCallExpr
    | 
    expr = lvalue
    | 
    LPAREN expr = orExpr RPAREN 
    | 
    expr = constant
    | 
    expr = functionInvocation
;

predictProcedureCallExpr
: 
	ID LPAREN 
;

// specifically, need the base ID to be distinct from all the
// other IDs

lvalue returns [LValue lv = null]
{
  	Expression index = null;
  	String field = null;
}
:
   	base:ID {
   		lv = new VariableReference(base.getText());
   	}

   	( 
   		(
   			index = arrayIndex {
   				lv = new ArrayReference(lv, index);
   			} 
   		)
   		|
   		( 
   			field = memberName {
   				lv = new StructReference(lv, field);
   			} 
   		)
  	)*
;


arrayIndex returns [Expression ix = null]
:
	LBRACK
	(ix = expression | s:STAR)
	RBRACK {
		if (ix == null) {
			ix = new KleeneStar();
		}
	}
;

memberName returns [String field = null]
:
    d:DOT (f:ID | s:STAR) {
    	if (f == null) {
    		field = "*";
    	}
    	else {
    		field = f.getText();
    	}
    }
;

identifier returns [String id = null]
:
	b:ID {
		id = b.getText();
	}
;

constant returns [Expression c = null]
: 
	i:INT_LITERAL {
      	c = new IntegerConstant(i.getText());
    }
    | 
    d:FLOAT_LITERAL {
      	c = new FloatConstant(d.getText());
    }
    | 
    s:STRING_LITERAL {
    	c = new StringConstant(s.getText());
    }
    | 
    t:"true" {
      	c = new BooleanConstant(true);
    }
    | 
    f:"false" {
      	c = new BooleanConstant(false);
    }
    | 
    c = arrayInitializer
    | 
    c = structInitializer
;

// TODO ^^^^^^ array literal -- rename and rearrange the methods

class SwiftScriptLexer extends Lexer;

options {
    charVocabulary = '\1'..'\377';
    testLiterals=false;    // don't automatically test for literals
    k=2;
}

AT        :   "@" ;
PLUS    :   "+" ;
MINUS   :   '-' ;
FDIV        :   '/' ;
IDIV        :   "%/" ;
MOD        :   "%%" ;
EQ      :   "==" ;
NE        :   "!=" ;
LT      :   '<' ;
LE        :   "<=" ;
GT        :   ">" ;
GE        :   ">=";
APPEND    :   "<<";
ASSIGN  :   '=' ;
AND        :   "&&";
OR        :   "||";
NOT        :   "!";
LBRACK options { paraphrase = "'['"; }   :   '[' ;
RBRACK options { paraphrase = "']'"; }   :   ']' ;
LPAREN options { paraphrase = "'('"; } :   '(' ;
RPAREN options { paraphrase = "')'"; } :   ')' ;
LCURLY options { paraphrase = "'{'"; } :   '{' ;
RCURLY options { paraphrase = "'}'"; } :   '}' ;
SEMI options { paraphrase = "a semicolon"; } : ';' ;
COMMA   :   ',' ;
COLON    :   ':' ;
STAR    :   '*' ;


ID	options {
	paraphrase = "an identifier";
	testLiterals = true;
}
:
	('a'..'z'|'A'..'Z'|'_') ('a'..'z'|'A'..'Z'|'0'..'9'|'_')*
;

// string literals
STRING_LITERAL
:
	'"'! (ESC|~('"'|'\\'|'\n'|'\r'))* '"'!
;

NUMBER
:
	(
		INTPART {
			_ttype=INT_LITERAL;
		}
      	(
      		'.' 
      		FLOATPART {
      			_ttype=FLOAT_LITERAL; 
      		}
      	)?
		(
			EXPONENT {
				_ttype=FLOAT_LITERAL; 
			}
		)?
	)
	|
    (
    	'.' {
    		_ttype=DOT; 
    	}
		(
			(
				FLOATPART {
					_ttype=FLOAT_LITERAL; 
				}
			)
			(EXPONENT)?
		)?
    )
;

protected INTPART: (ANYDIGIT)+;

protected ANYDIGIT: ('0'..'9');

protected FLOATPART: (ANYDIGIT)+;

protected EXPONENT: ('e'|'E') ('+'|'-')? (ANYDIGIT)+;

// white spaces
WS:   
	(   
		' '
        |   '\t'
        |   '\r'
        |   '\n' {newline();}
	)+ { 
		$setType(Token.SKIP); 
	}
;

// Single-line comments, c style
SL_CCOMMENT
:
	"//"
	(~('\n'|'\r'))* ('\n'|'\r'('\n')?) {
		$setType(Token.SKIP);
		newline();
	}
;

// Single-line comments, shell style
SL_SCOMMENT
:
	"#"
	(~('\n'|'\r'))* ('\n'|'\r'('\n')?) {
		$setType(Token.SKIP); 
		newline();
	}
;

// multiple-line comments
ML_COMMENT
:    
	"/*"
	(
		options {
			generateAmbigWarnings=false;
		}:
		{ LA(2)!='/' }? '*'
        |    
        '\r' '\n' {
        	newline();
        }
        |
        '\r' {
        	newline();
        }
        |
        '\n' {
        	newline();
        }
        |
        ~('*'|'\n'|'\r')
	)*
	"*/" {
		$setType(Token.SKIP);
	}
;

// escape sequence
protected
ESC
:    
	'\\' ('n' | 'r' | 't' | 'b' | 'f' | '"' | '\\')
;