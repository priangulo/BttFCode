// Automatically generated code.  Edit at your own risk!
// Generated by bali2jak v2002.09.03.

package guidsl;

import Jakarta.util.*;
import java.io.*;
import java.util.*;



abstract class TermName$$dgram extends GTerm {

    final public static int ARG_LENGTH = 1 /* Kludge! */ ;
    final public static int TOK_LENGTH = 1 ;

    public AstToken getIDENTIFIER () {
        
        return (AstToken) tok [0] ;
    }

    public boolean[] printorder () {
        
        return new boolean[] {true} ;
    }

    public TermName setParms (AstToken tok0) {
        
        arg = new AstNode [ARG_LENGTH] ;
        tok = new AstTokenInterface [TOK_LENGTH] ;
        
        tok [0] = tok0 ;            /* IDENTIFIER */
        
        InitChildren () ;
        return (TermName) ((TermName) this) ;
    }

}



 public class TermName extends  TermName$$dgram  {

    public void visit( Visitor v ) {
        
        v.action( ((TermName) this) );
    }

}
