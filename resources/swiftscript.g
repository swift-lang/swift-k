header {
package org.globus.swift.parser;

import org.antlr.stringtemplate.*;
import java.util.List;
import java.util.Iterator;
import antlr.Token;

}

class SwiftScriptParser extends Parser;

options {
    k=2;
    codeGenMakeSwitchThreshold = 2;
    codeGenBitsetTestThreshold = 3;
    defaultErrorHandler=false;
}

{
protected StringTemplateGroup m_templates=null;
protected String currentFunctionName=null;
protected SwiftScriptLexer swiftLexer=null;

public void setTemplateGroup(StringTemplateGroup tempGroup) {
    m_templates = tempGroup;
}

/** TODO this can perhaps be extracted from the superclass, but I don't
    have javadocs available at the time of writing. */
public void setSwiftLexer(SwiftScriptLexer sl) {
    swiftLexer = sl;
}

StringTemplate template(String name) {
    StringTemplate t = m_templates.getInstanceOf(name);
    t.setAttribute("sourcelocation","line "+swiftLexer.getLine());
    return t;
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
    return s.replaceAll("\\\\\"", "&quot;");
}

}

// The specification for a SwiftScript program
program returns [StringTemplate code=template("program")]
    :
    (nsdecl[code])*        //namespace declaration
    (importStatement[code])*
    (topLevelStatement[code])*
    EOF
    ;

nsdecl [StringTemplate code]
{StringTemplate ns=template("nsDef");}
    :   "namespace" (prefix:ID{ns.setAttribute("prefix", prefix.getText());})? uri:STRING_LITERAL SEMI
    {
      ns.setAttribute("uri", uri.getText());
      code.setAttribute("namespaces", ns);
      if (ns.getAttribute("prefix") == null)
         code.setAttribute("targetNS", ns.getAttribute("uri"));
    }
    ;

importStatement [StringTemplate code]
    : "import" name:STRING_LITERAL SEMI {
        StringTemplate i = template("import");
        i.setAttribute("target", name.getText());
        code.setAttribute("imports", i);
    }
    ;

typedecl [StringTemplate code]
{StringTemplate r=template("typeDef");
 StringTemplate t=null;}
    :    "type" id:ID {    r.setAttribute("name", id.getText()); }
    (
        SEMI
        |
        (t=type
        {
           r.setAttribute("type", t);
        }
        SEMI
        )
        | structdecl[r]
    )
    {code.setAttribute("types", r);}
    ;

structdecl [StringTemplate code]
{StringTemplate e=null, e1=null, t=null; String thisType = null;}
    :   LCURLY
    (t=type id:ID
    {
    thisType = (String) t.getAttribute("name");
    e=template("memberdefinition");
    e.setAttribute("name", id.getText());
    }
    (LBRACK RBRACK { thisType = thisType + "[]"; })*
    {
      StringTemplate thisTypeTemplate;
      thisTypeTemplate=template("type");
      thisTypeTemplate.setAttribute("name", thisType);
      e.setAttribute("type", thisTypeTemplate);
      code.setAttribute("members", e);
    }
    (
        COMMA
        id1:ID
        {
        thisType = (String) t.getAttribute("name");
        e1=template("memberdefinition");
        e1.setAttribute("name", id1.getText());
        }
        (LBRACK RBRACK { thisType = thisType + "[]"; })*
        {
           StringTemplate thisTypeTemplate;
           thisTypeTemplate=template("type");
           thisTypeTemplate.setAttribute("name", thisType);
           e1.setAttribute("type", thisTypeTemplate);
           code.setAttribute("members", e1);
         }
    )*
    SEMI
    )*
    RCURLY
    (options {
       warnWhenFollowAmbig = false;
     }
    :SEMI
    )?
    ;

topLevelStatement[StringTemplate code]
{StringTemplate d=null; }
   :

// these are ll(1) and easy to predict

      typedecl[code]
    | d=ll1statement
       {
        code.setAttribute("statements",d);
       }

// these are non-declaration assign-like statements

    |   (predictAssignStat) => d=assignStat
       {
        code.setAttribute("statements",d);
       }

// these are non-declaration append-associative array statements

	|   (predictAppendStat) => d=appendStat
		{
		 code.setAttribute("statements",d);
		}

// they all begin with (id name)
    | (predictDeclaration) => declaration[code]
	| (predictProceduredecl) => d=proceduredecl {code.setAttribute("functions", d);}
// more complicated function invocations
// note that function invocations can happen in above statements too
// this section is just the remaining more specialised invocations

    |   (predictProcedurecallCode) => d=procedurecallCode
       {
        code.setAttribute("statements",d);
       }

    |   (predictProcedurecallStatAssignManyReturnParam) => procedurecallStatAssignManyReturnParam[code]

// this is a declaration, but not sorted out the predications yet to
// group it into a decl block
    | ("app") => d=appproceduredecl {code.setAttribute("functions",d);}
    ;

predictDeclaration {StringTemplate x,y;} : ("global") | (x=type y=declarator) ;

declaration [StringTemplate code]
{StringTemplate t=null;
 boolean isGlobal = false;}
    : ("global" {isGlobal = true;})?
      t=type
      declpart[code, t, isGlobal]
      (COMMA declpart[code, t, isGlobal])*
      SEMI
    ;

declpart [StringTemplate code, StringTemplate t, boolean isGlobal]
    {
        StringTemplate n=null;
        StringTemplate thisTypeTemplate=null;
        String thisType = (String) t.getAttribute("name");
        StringTemplate variable=null;
        StringTemplate m = null;
        StringTemplate sTemp = null;
		String sType = "";
    }
    :
     n=declarator
	(LBRACK
	(sTemp=type {sType = (String) sTemp.getAttribute("name") ;} )?
	RBRACK {thisType = thisType + "[" + sType + "]" ; sType = ""; } )*
     {
        thisTypeTemplate=template("type");
        thisTypeTemplate.setAttribute("name", thisType);
        variable = template("variable");
        variable.setAttribute("name", n);
        variable.setAttribute("type", thisTypeTemplate);
        variable.setAttribute("global", ""+isGlobal);
        code.setAttribute("statements", variable);
    }

    (LT (m=mappingdecl | f:STRING_LITERAL) GT
    {
       if (m!=null)
           variable.setAttribute("mapping", m);
       else
           variable.setAttribute("lfn", quote(f.getText()));
    })?

// TODO: mapping does here...
// which means construction of the variable template goes here, rather than
// in variableDecl

    (
      variableDecl[code, thisTypeTemplate, n, variable]
// nice to lose this distinction entirely...
//    | (predictDatasetdecl) => datasetdecl[code, thisTypeTemplate, n]
// TODO can shorten variableDecl predictor now we dont' need to
//  distinguish it from datasetdecl?
    )
    ;

variableDecl [StringTemplate code, StringTemplate t, StringTemplate d, StringTemplate v1]
{StringTemplate i1=null, m=null;

}
    :

    (i1=varInitializer
    {

        if (i1 != null) {
          StringTemplate valueAssignment = template("assign");
          StringTemplate vr = template("variableReference");
          vr.setAttribute("name",d);
          valueAssignment.setAttribute("lhs",vr);
          valueAssignment.setAttribute("rhs",i1);
          code.setAttribute("statements", valueAssignment);
        }
    })?
    ;

declarator returns [StringTemplate code=null]
    :   id:ID {code=text(id.getText());}
    ;

appDeclarator returns [StringTemplate code=null]
    :   id:ID {code=text(id.getText());}
    |	s:STRING_LITERAL {code=text(s.getText());}
    ;


varInitializer returns [StringTemplate code=null]
    :   ASSIGN code=expression
    ;

// This is an initializer used to set up an array.
// currently does not support nested array.
arrayInitializer returns [StringTemplate code=template("arrayInit")]
{StringTemplate e=null,from=null,to=null,step=null;}
    :    LBRACK
    (
     (expression COLON) =>
     (
      from=expression COLON to=expression (COLON step=expression)?
      {
        StringTemplate range=template("range");
        range.setAttribute("from", from);
        range.setAttribute("to", to);
        if (step != null)
        range.setAttribute("step", step);
        code.setAttribute("range", range);
      }
     )
     |
     (
      e=expression {code.setAttribute("elements", e);}
      (
        // CONFLICT: does a COMMA after an initializer start a new
      //           initializer or start the option ',' at end?
      //           ANTLR generates proper code by matching
      //             the comma as soon as possible.
        options {
          warnWhenFollowAmbig = false;
        } : COMMA e=expression {code.setAttribute("elements", e);}
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
structInitializer returns [StringTemplate code = template("structInit")]
	{
		StringTemplate key = null, expr = null;
		code.setAttribute("sourcelocation", "line " + swiftLexer.getLine());
	}
    :    
    	LCURLY
    	(
    		key = expression COLON expr = expression
    		{
    			StringTemplate fieldInit = template("structFieldInit");
    			fieldInit.setAttribute("key", key);
    			fieldInit.setAttribute("value", expr);
    			code.setAttribute("fields", fieldInit);
    		}
    	)?
    	( 
    		COMMA key = expression COLON expr = expression
    		{
    			StringTemplate fieldInit = template("structFieldInit");
    			fieldInit.setAttribute("key", key);
    			fieldInit.setAttribute("value", expr);
    			code.setAttribute("fields", fieldInit);
    		}
    	)*
    	RCURLY
    ;

mappingdecl returns [StringTemplate code=template("mapping")]
{StringTemplate p=null, d=null;}
    :  d=declarator {code.setAttribute("descriptor",d);} SEMI
       mapparamdecl[code]
    ;

mapparamdecl [StringTemplate code]
{StringTemplate p=null;}
    :  (  p=mapparam {code.setAttribute("params", p);}
          ( COMMA p=mapparam {code.setAttribute("params", p);} )*
       )?
    ;

mapparam returns [StringTemplate code=template("mapParam")]
{StringTemplate n=null, v=null;}
    :  n=declarator ASSIGN v=mappingExpr
    {
        code.setAttribute("name", n);
        code.setAttribute("value", v);
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
{StringTemplate f=null;}
    : ( id:ID LPAREN
        ( f=formalParameter (COMMA f=formalParameter)* )?
        RPAREN
        LCURLY
      )
      |
      (
        LPAREN
        f=formalParameter
        (   COMMA f=formalParameter
        )*
        RPAREN ID LPAREN
      )
    ;

proceduredecl returns [StringTemplate code=template("function")]
{StringTemplate f=null;}
    :  ( LPAREN
        f=formalParameter
        {
        f.setAttribute("outlink", "true");
        code.setAttribute("outputs", f);
        }
        (   COMMA f=formalParameter
            {
            f.setAttribute("outlink", "true");
            code.setAttribute("outputs", f);
            }
        )*
        RPAREN )?
        id:ID {currentFunctionName=id.getText();} LPAREN
        (   f=formalParameter
            {
            code.setAttribute("inputs", f);
            }
            (   COMMA f=formalParameter
                {
                code.setAttribute("inputs", f);
                }
            )*
        )?
        RPAREN
         LCURLY
        (
        atomicBody[code]
        |
        compoundBody[code]
        )
        RCURLY
        {
        code.setAttribute("name", id.getText());
        currentFunctionName=null;
        }
    ;

appproceduredecl returns [StringTemplate code=template("function")]
{StringTemplate f=null;
 StringTemplate app=template("app");
 StringTemplate exec=null; }
    :   "app"
        ( LPAREN
        f=formalParameter
        {
        f.setAttribute("outlink", "true");
        code.setAttribute("outputs", f);
        }
        (   COMMA f=formalParameter
            {
            f.setAttribute("outlink", "true");
            code.setAttribute("outputs", f);
            }
        )*
        RPAREN )?
        id:ID {currentFunctionName=id.getText();} LPAREN
        (   f=formalParameter
            {
            code.setAttribute("inputs", f);
            }
            (   COMMA f=formalParameter
                {
                code.setAttribute("inputs", f);
                }
            )*
        )?
        RPAREN
        LCURLY
        ( appProfile[app] )*
        (exec=appDeclarator)
        {
       		app.setAttribute("exec", exec);
        }
        ( appArg[app] )* SEMI
        {code.setAttribute("config",app);}
        RCURLY
        {
        code.setAttribute("name", id.getText());
        currentFunctionName=null;
        }
    ;

appProfile [StringTemplate code]
{   StringTemplate p=null;
    StringTemplate k=null;
    StringTemplate v=null;}
    : "profile" k=expression ASSIGN v=expression SEMI
        {
            p=template("app_profile");
            p.setAttribute("key", k);
            p.setAttribute("value", v);
            code.setAttribute("profiles", p);
        }
    ;

// TODO in here, why do we have an | between LBRACKBRACK and ASSIGN?
// does this mean that we don't have array initialisation in formal
// params? this wouldn't surprise me given the previous treatment
// of arrays. investigate this and fix...
formalParameter returns [StringTemplate code=template("parameter")]
{StringTemplate t=null,d=null,v=null; String thisType = null; }
    :   (t=type d=declarator
        {
        thisType = (String) t.getAttribute("name");
        code.setAttribute("name", d);
        }
        (LBRACK RBRACK {thisType = thisType + "[]"; })*
        (ASSIGN v=constant
          {
          String value = (String)v.getAttribute("value");
          if (v.getName().equals("sConst")) {
            v.removeAttribute("value");
             v.setAttribute("value", quote(value));
          }
          code.setAttribute("defaultv", v);
          }
        )?) {
          StringTemplate thisTypeTemplate;
          thisTypeTemplate=template("type");
          thisTypeTemplate.setAttribute("name", thisType);
          code.setAttribute("type", thisTypeTemplate);
        }
    ;

type returns [ StringTemplate code = null ]
	{ StringBuilder buf = new StringBuilder(); }
:
	id:ID {
		code = template("type");
		buf.append(id.getText());
	}
	(typeSubscript[buf]) * {
		code.setAttribute("name", buf.toString());
	}
;

typeSubscript[StringBuilder buf] :
	LBRACK { buf.append('['); }
	(id:ID { buf.append(id.getText()); })?
	RBRACK { buf.append(']'); }
;

compoundStat[StringTemplate code]
    :   LCURLY
    ( innerStatement[code] )*
        RCURLY
    ;

compoundBody[StringTemplate code]
    :    ( innerStatement[code] )*
    ;

innerStatement[StringTemplate code]
{StringTemplate s=null;}
    : (predictDeclaration) => declaration[code]
    |
    ((
       s=ll1statement
    |  (procedurecallCode) => s=procedurecallCode
    |  (predictAssignStat) => s=assignStat
    |  (predictAppendStat) => s=appendStat
    )
       {
        code.setAttribute("statements",s);
       })
    |  (procedurecallStatAssignManyReturnParam[code]) => procedurecallStatAssignManyReturnParam[code]
    ;

caseInnerStatement [StringTemplate statements]
{ StringTemplate code = null; }
    :
    (  code=ll1statement
    |  (procedurecallCode) => code=procedurecallCode
    |  (predictAssignStat) => code=assignStat
    |  (predictAppendStat) => code=appendStat
    ) {statements.setAttribute("statements",code);}
    |   (procedurecallStatAssignManyReturnParam[statements]) => procedurecallStatAssignManyReturnParam[statements]
    ;

// These are the statements that we can predict with ll(1) grammer
// i.e. with one token of lookahead
ll1statement returns [StringTemplate code=null]
    :
    code=ifStat
    | code=foreachStat
    | code=switchStat
    | code=iterateStat
    ;

ifStat returns [StringTemplate code=template("if")]
{
  StringTemplate cond=null;
  StringTemplate body=template("statementList");
  StringTemplate els=template("statementList");
}
    :  "if" LPAREN cond=expression RPAREN {
        	code.setAttribute("cond", cond);
        }
        compoundStat[body] {code.setAttribute("body", body);}
        (
          options {
              warnWhenFollowAmbig = false;
          }
          : "else" els = bodyOrIf {code.setAttribute("els", els);}
        )?
    ;
    
bodyOrIf returns [StringTemplate stat] {stat = template("statementList");}:
	stat = ifStat
	| compoundStat[stat];

foreachStat returns [StringTemplate code=template("foreach")]
{
  StringTemplate ds=null;
  StringTemplate body=template("statementList");
}
    :  "foreach" id:ID (COMMA indexId:ID)? "in" ds=expression
    {
        code.setAttribute("var", id.getText());
        code.setAttribute("in", ds);
        if (indexId != null) {
           code.setAttribute("index", indexId.getText());
        }
    }
    compoundStat[body] {code.setAttribute("body", body);}
    ;

iterateStat returns [StringTemplate code=template("iterate")]
{
  StringTemplate cond=null;
  StringTemplate body=template("statementList");
}
    :  "iterate" id:ID
    compoundStat[body] {code.setAttribute("body", body);}
    "until" LPAREN cond=expression RPAREN SEMI
    {
    code.setAttribute("var", id.getText());
    code.setAttribute("cond", cond);
    }
    ;

switchStat returns [StringTemplate code=template("switch")]
{
  StringTemplate cond=null, b=null;
}
    :    "switch" LPAREN cond=expression RPAREN
    {code.setAttribute("cond", cond);}
    LCURLY
    ( b = casesGroup {code.setAttribute("cases", b);} )*
    RCURLY
    ;

casesGroup returns [StringTemplate code=template("case")]
{StringTemplate b=null;}
    :    (    // CONFLICT: to which case group do the statements bind?
            //           ANTLR generates proper code: it groups the
            //           many "case"/"default" labels together then
            //           follows them with the statements
            options {
                greedy = true;
            }
            :
            aCase[code]
        )
        caseSList[code]
    ;

aCase [StringTemplate code]
{StringTemplate v=null;}
    :    (
          "case" v=expression {code.setAttribute("value", v);}
          | "default"
                )
        COLON
    ;

caseSList [StringTemplate code]
{StringTemplate s=null;}
    :    ( caseInnerStatement[code] )*
    ;

predictAssignStat
{StringTemplate x=null;}
    : x=identifier ASSIGN ;

predictAppendStat
{StringTemplate x=null;}
    : x=identifier APPEND ;

assignStat returns [StringTemplate code=null]
{StringTemplate id=null;}
    :
    id=identifier
    ASSIGN
    code=variableAssign {
		code.setAttribute("lhs",id);
    }
;

appendStat returns [ StringTemplate code = null ]
	{ StringTemplate id=null; }
:
    id=identifier
    APPEND
	code=arrayAppend {
		code.setAttribute("array", id);
	}
;

arrayAppend returns [ StringTemplate code = null ]
	{ StringTemplate a = null, e = null, id = null; }
:
	e = expression SEMI {
		code = template("append");
		code.setAttribute("value", e);
	}
;


variableAssign returns [StringTemplate code=null]
{StringTemplate a=null, e=null, id=null;}
    :
    e=expression SEMI
        {
            code=template("assign");
            code.setAttribute("rhs", e);
        }
    ;
    
predictProcedurecallCode:
	ID LPAREN
;

procedurecallCode returns [StringTemplate code=template("call")]
{StringTemplate f=null;}
    :
        procedureInvocation[code]
    ;

procedureInvocation [StringTemplate code]
    :
        procedureInvocationWithoutSemi[code]
        SEMI
    ;

procedureInvocationWithoutSemi [StringTemplate code]
{StringTemplate f=null;}
    :
        id:ID {code.setAttribute("func", id.getText());}
        LPAREN 
        (
        	f=actualParameter {
        		code.setAttribute("inputs", f);
        	}
            ( COMMA f=actualParameter {
        		code.setAttribute("inputs", f);
            })*
        )?
        RPAREN
    ;

procedureInvocationExpr [StringTemplate code]
{StringTemplate f=null;}
    :
        id:ID {code.setAttribute("func", id.getText());}
        LPAREN (
        	f=actualParameter {
        		code.setAttribute("inputs", f);
        	}
       		(
       			COMMA f=actualParameter {
        			code.setAttribute("inputs", f);
            	}
            )*
        )?
        RPAREN
    ;

procedureCallExpr returns [StringTemplate code=template("call")]
{StringTemplate f=null;}
    :
        procedureInvocationExpr[code]
    ;

predictProcedurecallStatAssignManyReturnParam:
	LPAREN
    predictProcedurecallStatAssignManyReturnOutput
    (COMMA predictProcedurecallStatAssignManyReturnOutput)*
    RPAREN
    ASSIGN
;

predictProcedurecallStatAssignManyReturnOutput {StringTemplate s;}:
	ID (ID)? (ASSIGN s=expression)?
;

procedurecallStatAssignManyReturnParam [StringTemplate s]
{ StringTemplate code=template("call"); }
    :
        LPAREN
        procedurecallStatAssignManyReturnOutput[s,code]
        (   COMMA procedurecallStatAssignManyReturnOutput[s,code] )*
        RPAREN
        ASSIGN
        procedureInvocation[code]
        {
            s.setAttribute("statements",code);
        }
    ;

procedurecallStatAssignManyReturnOutput [StringTemplate s, StringTemplate code]
{StringTemplate var = null, f = null; }
    :
        f=returnParameter
        {
            code.setAttribute("outputs", f);
            var = template("variable");
            if(f.getAttribute("type") != null) {
                StringTemplate nameST = (StringTemplate)f.getAttribute("name");
                var.setAttribute("name",nameST.getAttribute("name"));
                var.setAttribute("type",f.getAttribute("type"));
                var.setAttribute("global", Boolean.FALSE);

                s.setAttribute("statements",var);
            }
        }
    ;

returnParameter returns [ StringTemplate code = template("returnParam") ]
	{ StringTemplate t = null, id = null, d = null; StringBuilder buf = new StringBuilder(); }
:
	t = identifier { buf.append(t.getAttribute("name")); }
	(typeSubscript[buf])*
	( id = identifier )?
	{
		if (id == null) {
			code.setAttribute("name", t);
		}
		else {
			t = template("type");
			t.setAttribute("name", buf.toString());
			code.setAttribute("name", id);
			code.setAttribute("type", t);
		}
	}
	((ASSIGN declarator) => (ASSIGN d=declarator) {
		code.setAttribute("bind", d);
	})?
;

actualParameter returns [StringTemplate code=template("actualParam")]:
	(predictNamedParam) => namedParam[code] 
	| positionalParam[code]
;

predictNamedParam {StringTemplate d = null;}:
	d=declarator ASSIGN
;

namedParam[StringTemplate code] {StringTemplate d = null;}:
	d=declarator ASSIGN {
		code.setAttribute("bind", d);
	}
	positionalParam[code]
;

positionalParam[StringTemplate code] {StringTemplate id = null;}:
	id=expression {
		code.setAttribute("value", id);
	}
;

atomicBody [StringTemplate code]
{StringTemplate app=null, svc=null;}
    :      app=appSpec
    {code.setAttribute("config",app);}
    ;

/* This is the deprecated format for app { } blocks */
appSpec returns [StringTemplate code=template("app")]
{StringTemplate exec=null;}
    :  "app" LCURLY
    exec=declarator
    { code.setAttribute("exec", exec);}
    (
      appArg[code]
    )*
    SEMI RCURLY
    ;

appArg [StringTemplate code]
{StringTemplate arg=null;}
    :   arg=mappingExpr
    {code.setAttribute("arguments", arg);}
    |
    stdioArg[code]
    ;

mappingExpr returns [StringTemplate code=null]
{StringTemplate e=null;}
    :
    e = expression
    {
      code=template("mappingExpr");
      code.setAttribute("expr", e);
    }
    ;

functionInvocation returns [StringTemplate code=template("functionInvocation")]
{StringTemplate func=null, e=null;}
    :   AT (
    (declarator LPAREN) =>
    (func=declarator
     {
       code.setAttribute("name", func);
     }
     LPAREN
     (
     functionInvocationArgument[code]
     (
       COMMA
       functionInvocationArgument[code]
     )*)?
     RPAREN
    )
    |
    (e=identifier | (LPAREN e=identifier RPAREN) )
    {
      /*
       * This is matched on expressions like @varname,
       * which are a shortcut for filename(varname).
       * The interpretation of what a function invocation
       * with an empty file name means was moved to the swiftx -> ?
       * compiler and allows that layer to distinguish between
       * '@filename(x)' and '@(x)', the former of which 
       * has been deprecated.
       */
      code.setAttribute("name", "");
      code.setAttribute("args", e);
    }
    )
    ;

functionInvocationArgument [StringTemplate code]
{StringTemplate e = null;}
    :
     e=expression
     {
      code.setAttribute("args", e);
     }
     ;

stdioArg [StringTemplate code]
{StringTemplate t=null,m=null; String name=null;}
    :    ("stdin" {t=template("stdin"); name="stdin";}
    |
    "stdout" {t=template("stdout"); name="stdout";}
    |
    "stderr" {t=template("stderr"); name="stderr";}
    )
    ASSIGN
    m=mappingExpr
    {
        t.setAttribute("content", m);
        code.setAttribute(name, t);
    }
    ;

expression returns [StringTemplate code=null]
    :   code=orExpr
    ;

orExpr returns [StringTemplate code=null]
{StringTemplate a,b;}
    :   code=andExpr
        (   OR b=andExpr
            {
            a = code;
            code=template("or");
            code.setAttribute("left", a);
            code.setAttribute("right", b);
            }
        )*
    ;

andExpr returns [StringTemplate code=null]
{StringTemplate a,b;}
    :   code=equalExpr
        (   AND b=equalExpr
            {
            a = code;
            code=template("and");
            code.setAttribute("left", a);
            code.setAttribute("right", b);
            }
        )*
    ;

equalExpr returns [StringTemplate code=null]
{
StringTemplate a,b=null;
Token op=null;
}
    :   code=condExpr
        (
           {op=LT(1);}
            ( EQ | NE ) b=condExpr
            {
            a = code;
            code=template("cond");
            code.setAttribute("op", escape(op.getText()));
            code.setAttribute("left", a);
            code.setAttribute("right", b);
            }
        )?
    ;

condExpr returns [StringTemplate code=null]
{
StringTemplate a,b=null;
Token op=null;
}
    :   code=additiveExpr
        (
        options {
        greedy = true;
        //warnWhenFollowAmbig = false;
        }
        :
           {op=LT(1);}
            ( LT | LE | GT | GE ) b=additiveExpr
            {
            a = code;
            code=template("cond");
            code.setAttribute("op", escape(op.getText()));
            code.setAttribute("left", a);
            code.setAttribute("right", b);
            }
        )?
    ;

additiveExpr returns [StringTemplate code=null]
{
StringTemplate a,b=null;
Token op=null;
}
    :   code=multiExpr
    (
        options {
        greedy = true;
        //warnWhenFollowAmbig = false;
        }
        :
            {op=LT(1);}
            ( PLUS | MINUS ) b=multiExpr
            {
            a = code;
            code=template("arith");
            code.setAttribute("op", escape(op.getText()));
            code.setAttribute("left", a);
            code.setAttribute("right", b);
            }
        )*
    ;

multiExpr returns [StringTemplate code=null]
{
StringTemplate a,b=null;
Token op=null;
}
    :   code=unaryExpr
    (
        options {
        greedy = true;
        //warnWhenFollowAmbig = false;
        }
        :
            {op=LT(1);}
            ( STAR | IDIV | FDIV | MOD ) b=unaryExpr
            {
            a = code;
            code=template("arith");
            code.setAttribute("op", escape(op.getText()));
            code.setAttribute("left", a);
            code.setAttribute("right", b);
            }
        )*
    ;

unaryExpr returns [StringTemplate code=null]
{StringTemplate u=null;}
    : MINUS u=unaryExpr
      {code=template("unaryNegation"); code.setAttribute("exp", u);}
    | PLUS u=unaryExpr // unary plus has no effect
      {code=u;}
    | NOT u=unaryExpr
      {code=template("not"); code.setAttribute("exp", u);}
    | code=primExpr
    ;

primExpr returns [StringTemplate code=null]
{StringTemplate id=null, exp=null;}
    : (predictProcedureCallExpr) => code=procedureCallExpr
    | code=identifier
    | LPAREN exp=orExpr RPAREN { code=template("paren");
        code.setAttribute("exp", exp);}
    | code=constant
    | code=functionInvocation
    ;

predictProcedureCallExpr
    : ID LPAREN ;

// TODO - redo identifier parsing to fit in with the XML style
// that this patch introduces

// specifically, need the base ID to be distinct from all the
// other IDs

identifier returns [StringTemplate code=null]
{
  StringTemplate c=null;
  code=template("variableReference");
}
    :
    base:ID {code.setAttribute("name",base.getText());}

    ( ( c=arrayIndex { c.setAttribute("array",code); code=c; } )
      |
      ( c=memberName {c.setAttribute("structure",code); code=c;} )
    )*
    ;


arrayIndex returns [StringTemplate code=null]
{StringTemplate e=null;}
    :
    LBRACK
    (e=expression | s:STAR)
    RBRACK
    {
      code=template("arraySubscript");
      if(e != null) code.setAttribute("subscript",e);
      if(s != null) {
        StringTemplate st = template("sConst");
        st.setAttribute("value","*");
        code.setAttribute("subscript",st);
      }
    }
    ;

memberName returns [StringTemplate code=null]
    :
    d:DOT (member:ID | s:STAR)
    {
      code=template("memberAccess");
      if(member != null) code.setAttribute("name",member.getText());
      if(s != null) code.setAttribute("name","*");
    }
    ;

constant returns [StringTemplate code=null]
    : i:INT_LITERAL
      {
        code=template("iConst");
        code.setAttribute("value",i.getText());
      }
    | d:FLOAT_LITERAL
      {
        code=template("fConst");
        code.setAttribute("value",d.getText());
      }
    | s:STRING_LITERAL
      {
        code=template("sConst");
        code.setAttribute("value",quote(escape(s.getText())));
      }
    | t:"true"
      {
        code=template("bConst");
        code.setAttribute("value", t.getText());
      }
    | f:"false"
      {
        code=template("bConst");
        code.setAttribute("value", f.getText());
      }
    | code=arrayInitializer
    | code=structInitializer
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

ID     options
        {
          paraphrase = "an identifier";
          testLiterals = true;
        }
    :
    ('a'..'z'|'A'..'Z'|'_') ('a'..'z'|'A'..'Z'|'0'..'9'|'_')*
    ;

// string literals
STRING_LITERAL
//    :    '"'! (~('"'|'\n'|'\r'))* '"'!
    :    '"'! (ESC|~('"'|'\\'|'\n'|'\r'))* '"'!
    ;

NUMBER
    :
    ( INTPART {_ttype=INT_LITERAL; }
      ('.' FLOATPART {_ttype=FLOAT_LITERAL; })?
      (EXPONENT {_ttype=FLOAT_LITERAL; })?
    )
    |
    ( '.' { _ttype=DOT; }
      ((FLOATPART {_ttype=FLOAT_LITERAL; })
      (EXPONENT)?)?
    )
    ;

protected INTPART : (ANYDIGIT)+;

protected ANYDIGIT : ('0'..'9');

protected FLOATPART : (ANYDIGIT)+;

protected EXPONENT : ('e'|'E') ('+'|'-')? (ANYDIGIT)+ ;

// white spaces
WS  :   (   ' '
        |   '\t'
        |   '\r'
        |   '\n' {newline();}
        )+
        { $setType(Token.SKIP); }
    ;

// Single-line comments, c style
SL_CCOMMENT
    :    "//"
        (~('\n'|'\r'))* ('\n'|'\r'('\n')?)
        {$setType(Token.SKIP); newline();}
    ;

// Single-line comments, shell style
SL_SCOMMENT
    :    "#"
        (~('\n'|'\r'))* ('\n'|'\r'('\n')?)
        {$setType(Token.SKIP); newline();}
    ;

// multiple-line comments
ML_COMMENT
    :    "/*"
        (
        options {
            generateAmbigWarnings=false;
        }
        :
            { LA(2)!='/' }? '*'
        |    '\r' '\n'        {newline();}
        |    '\r'            {newline();}
        |    '\n'            {newline();}
        |    ~('*'|'\n'|'\r')
        )*
        "*/"
        {$setType(Token.SKIP);}
    ;

// escape sequence
protected
ESC
    :    '\\'
    (    'n'
    |    'r'
    |    't'
    |    'b'
    |    'f'
    |    '"'
    |    '\\'
    )
    ;

