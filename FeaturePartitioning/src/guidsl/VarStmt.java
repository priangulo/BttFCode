// Automatically generated code.  Edit at your own risk!
// Generated by bali2jak v2002.09.03.

package guidsl;



abstract class VarStmt$$dgram extends Vars {

    final public static int ARG_LENGTH = 1 ;
    final public static int TOK_LENGTH = 1 ;

    public AvarList getAvarList () {
        
        return (AvarList) arg [0] ;
    }

    public boolean[] printorder () {
        
        return new boolean[] {true, false} ;
    }

    public VarStmt setParms (AstToken tok0, AvarList arg0) {
        
        arg = new AstNode [ARG_LENGTH] ;
        tok = new AstTokenInterface [TOK_LENGTH] ;
        
        tok [0] = tok0 ;            /* "##" */
        arg [0] = arg0 ;            /* AvarList */
        
        InitChildren () ;
        return (VarStmt) ((VarStmt) this) ;
    }

}



 public class VarStmt extends  VarStmt$$dgram  {

    public void visit( Visitor v ) {
        
        v.action( ((VarStmt) this) );
    }

}
