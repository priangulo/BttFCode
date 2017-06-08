package guidsl;import javax.swing.*;

abstract class star$$dsl$guidsl$gspec extends term {

    star$$dsl$guidsl$gspec( String name ) {
      super( name );
        pattern.current.terms.add( this );
    }
      
    public void visit( GVisitor v ) {
        v.action( ((star) this) );
    }
}



 public class star extends  star$$dsl$guidsl$gspec  {
    public JComponent draw (int several) {
        if (var.hidden)
           return null;
        else
           return prod.draw(3);
    }
      // inherited constructors



    star (  String name ) { super(name); }
}
