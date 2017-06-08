package guidsl;import java.util.*;
import Jakarta.util.*;import javax.swing.*;
import java.awt.*;
import java.awt.event.*;



class production extends gObj {
    public static final int unk = -1;
    public static final int opt = 1;
    public static final int plus = 2;
    public static final int star = 3;
    public static final int norm = 0;

    static int counter = 0;
    static production current;
    public int type; // must be { opt, plus, star, norm }
    public ArrayList pat;
    static public HashMap Ptable;
    
    
    public static void init() {
        counter = 0;
        Ptable = null;
        FPtable = new HashMap();
    }

    production( String name ) {
      super( name );

        // Step 1: if this is the first production, then
        //         define a grammar object
        if ( counter==0 ) {
            new grammar( name );
            grammar.rootProduction = (production) this;
            Ptable = new HashMap();
        }
        counter++;

        // Step 2: now define the production itself
        current = (production) this;
        this.type = unk; // unknown type at present
        grammar.productions.put( name,(production)this );
        pat = new ArrayList();
        Ptable.put( name, (production) this );
    }

    String getType() {
        if ( type == opt )
            return "optional";
        if ( type == plus )
            return "plus";
        if ( type == star )
            return "star";
        if ( type == norm )
            return "choose1";
        return "unknown";
    }

    public void visit( GVisitor v ) {
        v.action( (production) ((production) this) );
    }

    public void traverse( GVisitor v ) {
        pattern p = null;
        Iterator i = pat.iterator();
        while ( i.hasNext() ) {
            Object o = i.next();
            try {
            p = ( pattern ) o;
            } catch( Exception e ) { Util.fatalError( e.getMessage() + " " +
                    o.getClass().getName());
            }
            p.visit( v );
        }
    }

    public static void dumpPtable() {
        production p;
        int cnt = 0;
        System.out.println( "-------Begin Ptable Dump----------" );
        Iterator i = Ptable.values().iterator();
        while ( i.hasNext() ) {
            p = ( production ) i.next();
            p.print();
            System.out.println();
            cnt++;
        }
        System.out.println( cnt + " productions in all." );
        System.out.println( "-------End Ptable Dump----------" );
    }

    public void print() {
       String v = "null";
       if (var!=null) v = var.name; 
       System.out.print(name + " var is " + v + " " + getType());
       System.out.print( " formula = " + formula );
    }

    static public production find( String name ) {
        return ( production ) production.Ptable.get( name );
    }

   // used in first pass to contain names of productions

   public static HashMap FPtable = new HashMap();
   
    public node formula;

    static void makeFormula() {
        Iterator i = Ptable.values().iterator();
        while ( i.hasNext() ) {
            production p = ( production ) i.next();
            p.formula = p.makef();
        }
    }

    public node makef_1() {
        node n = null;
        node o = null;
		  int cnt = 0;
        Iterator i = pat.iterator();
        while ( i.hasNext() ) {
            pattern t = ( pattern ) i.next();
				cnt++;
            switch ( type )
            {
                case production.norm: /* choose 1 */
                case production.opt: /* treat like choose 1 */
                if ( o == null )
                    o = new bterm( t.name );
                else
                    o = new or( new bterm( t.name ), o );

                if ( n == null )
                    n = new bterm( t.name );
                else
                    n = new atmostone( new bterm( t.name ), n );
                break;
                case production.star: /* choose any number - treat like star */
                case production.plus:
                if ( n == null )
                    n = new bterm( t.name );
                else
                    n = new or( new bterm( t.name ), n );
                break;
                default:
                Util.error( "production " + name + " is not referenced in grammar" );
                return new bterm( t.name ); // return some dummy 
         }
        }
        switch ( type )
        {
            case production.norm:
            case production.opt:
				if (cnt>1) {
               // production iff (or of options) AND atmostone( list of options)
               return new and( new iff( new bterm( name ), o ), n ); 
				}
				else {
				   // production (prodname iff onlyPattern)
					// the atmostone(x) is always true
               return new iff( new bterm( name ), o );
				}
            case production.star:
            case production.plus:
            //  production iff (predicate associated with patterns)
            return new iff( new bterm( name ), n );
            default:
            Util.fatalError( "unrecognizable type: " + type +
                             " for production " + name );
        }
        // should never get here
        Util.fatalError( "should never get here" );
        return null;
    }

   public node simple;
   public node cnf;

   static void makeClauses() {
      if (Main.outputModelPredicate) {
         System.out.println();
         System.out.println("// Productions");
      }

      Iterator i = Ptable.values().iterator();
      while ( i.hasNext() ) {
         production p = ( production ) i.next();
         p.simple = p.formula.klone().simplify();
         if (Main.outputModelPredicate)
            System.out.println(p.simple + " and");  /* dsb */
         p.cnf    = p.simple.klone().cnf();
         ArrayList al = new ArrayList();
         p.cnf.reduce(al);
         cnfClause.setFormula(al, p.formula); 
         cnfClause.clist.addAll(al);
      }
   }

   public node makef() {
      node result = makef_1();
      switch (type) {
      case production.norm: result.incompleteMessage = "Choose 1"; break;
      case production.opt:  result.incompleteMessage = "Choose 1"; break;
      case production.star: result.incompleteMessage = "Choose 0 or more"; break;
      case production.plus: result.incompleteMessage = "Choose 1 or more"; break;
      }
      result.incompleteMessage = result.incompleteMessage + 
                                " in " + name + " panel";
      return result;
   }
     
   private static boolean toplevel = true;

    public JComponent draw (int several) {
       pattern p;
       // Step 1: create vertical panel and add patterns
       JPanel panel = new JPanel();
       panel.setLayout( new BoxLayout(panel, BoxLayout.Y_AXIS) );
       int size = pat.size();
       Iterator i = pat.iterator();

       boolean flag = false;//indicates whether we've started a new tab this time
       if (toplevel || var.tab){//either top level or annotation
            panel = new JPanel();
            panel.setLayout( new BoxLayout(panel, BoxLayout.Y_AXIS) );
            panel.setVisible(true);
            Gui.tabs.insertTab(var.disp, null, panel, "See " + var.disp, Gui.tabs.getTabCount());
            toplevel = false;
            flag = true;
       }
       if (size==1) {//consists of only one pattern
          p = (pattern) i.next();
           boolean ntd = p.hasNonOpt();
          if (several == 1 && ntd){//if optional parent has non-optional child
             JPanel row = new JPanel();
             row.setLayout( new FlowLayout(FlowLayout.LEFT) );
            JCheckBox cb = new JCheckBox(p.var.disp);
            p.var.userVisible = true;   // added *dsb*
            cb.setToolTipText("Optional");
            row.add(cb);
            p.setWidget(cb);
            row.add( p.draw(several) );
            panel.add( row);
        }
        else
          panel.add(p.draw(several));
       }
        else {
            int min = 0, max = 0;
            if (several == 1){
                min = 0; max = 1;
            } else if (several == 2){
                min = 1; max = 100;
            } else if (several == 0){
                min = 1; max = 1;
            }
            GroupButtons gp = new GroupButtons(min, max);//form a group of buttons

            while ( i.hasNext() ) {//iterate through the patterns
                p = (pattern) i.next();
                JPanel row = new JPanel();
                row.setLayout( new FlowLayout(FlowLayout.LEFT) );
                AbstractButton ab;
                if (several == 1 || several == 2){ //optional production, or plus production
                    JCheckBox cb = new JCheckBox(p.var.disp);
                    p.var.userVisible = true;   // added *dsb*
                    row.add(cb);
                    gp.addToGroup(cb);
                    ab = cb;
                    if(several == 2) //plus
                        ab.setToolTipText("Select at least one from this group");
                    if (several == 1)//opt
                        ab.setToolTipText("Optional");
                }
                else if (several == 3){//star production
                    JCheckBox cb = new JCheckBox(p.var.disp);
                    p.var.userVisible = true;   // added *dsb*
                    row.add(cb);
                    ab = cb;
                    ab.setToolTipText("Select zero or more from this group");
                }
                else{
                    JRadioButton jb = new JRadioButton(p.var.disp);
                    p.var.userVisible = true;   // added *dsb*
                    row.add( jb );
                    gp.addToGroup(jb);
                    jb.setToolTipText("Select exactly one from this group");
                    ab = jb;
                }
                p.setWidget(ab);
                row.add( p.draw(several) );//call the draw method of whatever the pattern points to
                panel.add( row);// add the entire thing to the panel
            }
       }


       if (flag){//if tab were inserted at this step
            //add a button to lead over to the next tab
            JButton seeNext = new JButton("See next: " + var.disp);
            final JPanel thisone = panel;
            seeNext.addMouseListener(new MouseListener(){
                    public void mouseReleased(MouseEvent e){}
                    public void mouseExited(MouseEvent e){}
                    public void mousePressed(MouseEvent e){}
                    public void mouseClicked(MouseEvent e){
                        Gui.tabs.setSelectedComponent(thisone);}
                    public void mouseEntered(MouseEvent e){}
                    });
            seeNext.setToolTipText("Proceed for more selections");
            return seeNext;
       }

       // Step 2: add title
       if (var == null)
          Util.fatalError( "var is null " + name );
       panel.setBorder( BorderFactory.createTitledBorder(var.disp) );

        return panel;
    }

    // method walks through the PTable, for each production
    // convert it into cnf formula, and then output the
    // converted formula into CNF format

    public static void toCnfFormat( cnfout out ) throws CNFException {
        Iterator i = Ptable.values().iterator();
        while ( i.hasNext() ) {
            production p = ( production ) i.next();
            node simple = p.formula.klone().simplify();
            node cnf = simple.klone().cnf();
            out.beginFormula( p.formula ); //original
            out.comment( simple );
            out.cnfcomment( cnf );
            cnf.toCnfFormat( out );
            out.endFormula();
        }
    }
    
    public static node getFormula( )  {
        return gObj.getFormula(Ptable);
    }

   

    /* Rests production's member variables so that different model inputs can be handled
       by guidls at runtime
    */
    public static void resetModel(){
         if (production.Ptable != null)
            production.Ptable.clear();
        if(production.FPtable != null)
            production.FPtable.clear();

        production.counter = 0;
        production.current=null;
    }
}
