/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package guidsl;

import org.junit.Test;

/**
 *
 * @author don
 */
public class ToolTest {
    static boolean debug = false;
    String ext;
    
    // Run one @Test at a time.  Look at the file "out.txt" which is in the main
    // directory of this netbeans project.  You'll see the output that is produced
    // by excuting a @Test. 

    public ToolTest() {
    }
    
    
    // how to make boolean expressions
    /*
    @Test
    public void illustrate() {
        RegTest.Utility.redirectStdOut("out.txt");  // redirects standard out to file "out.txt"
        // node == boolean expression
        //
        // expr = (A implies D) or not (B implies ( C or D ) )

        node a = new bterm("A");
        node b = new bterm("B");
        node c = new bterm("C");
        node d = new bterm("D");
        node e1 = new or(c, d);
        node e2 = new implies(b, e1);
        node e5 = new not(e2);
        node e3 = new implies(a, d);
        node expr = new or(e3, e5);

        System.out.println("expression = " + expr);
        System.out.println();
        System.out.println();
        expr = expr.simplify();
        System.out.println("simplified = " + expr);
        System.out.println();
        System.out.println();
        expr = expr.cnf();
        System.out.println("cnf expr   = " + expr);
        System.out.println();
        System.out.println();
        RegTest.Utility.validate("out.txt", "Correct/illustrate.txt", false); // test passes if files are equal
    }

    // how to check if a model is satisfiable
    // create a Tool that is initialized to a guidsl model
    // solve it and if it has a solution, print out its answer
    @Test
    public void isModelSatisfiableTest() {
        RegTest.Utility.redirectStdOut("out.txt");  // redirects standard out to file "out.txt"
        Tool t = new Tool("TestData/gpl.m",debug);
        if (t.solve()) {
            System.out.println("model is satisfiable");
            int sol[] = t.getSolution();
            for (int i = 0; i < sol.length; i++) {
                System.out.format("%d: %s is %b\n", i, variable.findVar(i + 1), sol(sol, i));
            }
        }
        ext = cnfout.debug ? "" : ".noc";
        RegTest.Utility.validate("out.txt", "Correct/isModelSatisfiableTest.txt"+ext, false); // test passes if files are equal
    }
     */
    private boolean sol(int sol[], int i) {
        return sol[i] > 0;
    }
	
    //  here's how to add an extra predicates to a guidsl model -- predicates
    // that your analyses will want to add.  
    // create a Tool that is initialized to a particular guidsl model
    // then add predicates that reference EXISTING variables in the model
    // then solve as before
   // @Test
    public void addExtraPredicatesTest() {
  //  RegTest.Utility.redirectStdOut("out.txt");  // redirects standard out to file "out.txt"
        Tool t = new Tool("F:\\workspace\\FeaturePartitioning\\gpl.m",debug);
        
        // TO DO: add extra predicate here!!
        
        
        // add  predicate (Directed or DFS) 
        //ExtraPredicates.add(new or(new bterm("Directed"), new bterm("DFS")));
       // add  predicate (Cycle) 
        //ExtraPredicates.add(new bterm("Cycle"));
        if (t.solve()) {
            System.out.println("model is satisfiable");
            int sol[] = t.getSolution();
            for (int i = 0; i < sol.length; i++) {
                System.out.format("%d: %s is %b\n", i, variable.findVar(i + 1), sol(sol, i));
            }
        }
        ext = cnfout.debug ? "" : ".noc";
    //    RegTest.Utility.validate("out.txt", "Correct/addExtraPredicatesTest.txt"+ext, false); // test passes if files are equal
    }

    /*
    // you'll want to define NEW variables to accompany your new predicates
    // Here's the rule: you must define new primitive variables (given their names)
    // BEFORE you reference them in predicates.  See below:
    @Test
    public void addExtraVarsAndPredicatesTest() {
        RegTest.Utility.redirectStdOut("out.txt");  // redirects standard out to file "out.txt"
        Tool t = new Tool("TestData/gpl.m",debug);

        // define new boolean variable
        t.defineNewPrim("newVar");
        // add predicate (GPL implies newVar) 
        ExtraPredicates.add(new implies(new bterm("GPL"), new bterm("newVar")));
        if (t.solve()) {
            System.out.println("model is satisfiable");
            int sol[] = t.getSolution();
            for (int i = 0; i < sol.length; i++) {
                System.out.format("%d: %s is %b\n", i, variable.findVar(i + 1), sol(sol, i));
            }
        }
        ext = cnfout.debug ? "" : ".noc";
        RegTest.Utility.validate("out.txt", "Correct/addExtraVarsAndPredicatesTest.txt"+ext, false); // test passes if files are equal
    }
    */
}
