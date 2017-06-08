package guidsl;

import Jakarta.util.*;
import java.io.*;

// This class provides a programming-interface to guidsl,
// and avoids the display of the GUI itself.  It is to be used
// for invoking the functionality of a guidsl tool.
// basic approach:
// Tool tool = new Tool( "modelFileName" );
// boolean result = true/false -- result of the SAT test
// SATtest t = new SATtest( "name of test" , result );
// t.add( "featureName or -FeatureName); // loop over this
// boolean output = true (for debugging)
// if (tool.modelDebug(t, output)) ... else ...;
class Tool {
    boolean printCNFFile = false;

    public variable defineNewPrim(String name) {
        variable v = variable.define(name, variable$$dsl$guidsl$gspec.Prim, null, false);
        v.userVisible = false;
        return v;
    }

    cnfModel model;

    // only one tool object can be active at one time.
    // reason -- there are lots of "static" variables that
    // may not be initialized properly
    
    // note Tool just parses the input file but doesn't create
    // cnf inputs to SAT solver
    public Tool(String modelFileName, boolean printCNFFile) {
        this.printCNFFile = printCNFFile;
        cnfout.debug = printCNFFile;
        
        production.init();
        pattern.init();
        ESList.init();
        grammar.init();
        ExtraPredicates.init();

        // Step 1: open the guidsl model file
        FileInputStream inputFile = null;
        try {
            inputFile = new FileInputStream(modelFileName);
        } catch (Exception e) {
            Util.fatalError("File " + modelFileName + " not found:"
                    + e.getMessage());
        }

        // Step 2: create a parser and parse input files
        //         inputRoot is root of parse tree of input file
        Parser myParser = Parser.getInstance(inputFile);
        Model inputRoot = null;
        try {
            inputRoot = (Model) myParser.parseAll();
        } catch (Exception e) {
            Util.fatalError("Parsing Exception Thrown in "
                    + modelFileName + ": " + e.getMessage());
        }

        // Step 3: transform parse tree here into the internal format
        try {
            Main.process(inputRoot);
        } catch (SemanticException e) {
            int errorCnt = Util.errorCount();
            Util.fatalError(errorCnt + " error(s) found");
        }

    } // end constructor
    
    SATSolver solver = null;
   //   Dummy solver = null;
    
    // this is the method that creates cnf input to the SAT solver
    // Sadly, cnfModel.init() creates MOST of the cnf model
    // method solve(model) actually finishes it
    public boolean solve() {
        model = cnfModel.init();
     //    solver = new Dummy();
        solver = new SATSolver();   //jongwook: error point!!
        return solver.solve(model,printCNFFile);
    }
    
    public int[] getSolution() {
        return solver.getSolution();
    }

    // call by:
    // Tool t = new Tool( GuiDslFileName );
    // SATTest st = // make SATtest, which is really a single CNF clause, ie, a disjunct
    // if (t.modelDebug(st,false)) { /* test succeeds */ } else { /*test fails*/ }
    public boolean modelDebug(SATtest t, boolean saveInFile) {
        boolean result = false;

        SATSolver s = new SATSolver();
        try {
            if (saveInFile) {
                solverTest.createOutputFile(model, t);
                result = s.solve(solverTest.input2SATSolver);
            } else { // use in-memory file
                solverTest.createOutputBuffer(model, t);
//                result = s.solve(new LineNumberReader(
//                        new StringReader(solverTest.cnfFileString)));
                result = s.solve( new ByteArrayInputStream( solverTest.cnfFileString.getBytes() ));
            }
        } catch (Exception e) {
            Util.fatalError("failed in debugging model " + e.getMessage());
        }
        return result == t.isSat;
    }

    // for Rui's extension -- saves all responses....
    // A SATtest is a guidsl "unit" test.  It takes a single clause -- a disjunction
    // of features and their negations, and returns true/false.
    public boolean modelDebug(SATtest t, boolean saveInFile, StringBuffer out) {
        boolean result = false;

        SATSolver s = new SATSolver();
        try {
            if (saveInFile) {
                solverTest.createOutputFile(model, t);
                result = s.solve(solverTest.input2SATSolver);
            } else { // use in-memory file
                solverTest.createOutputBuffer(model, t);
//                result = s.solve(new LineNumberReader(
//                        new StringReader(solverTest.cnfFileString)));
                result = s.solve( new ByteArrayInputStream( solverTest.cnfFileString.getBytes() ));
            }
            out.append(s.decode(true));
        } catch (Exception e) {
            Util.fatalError("failed in debugging model " + e.getMessage());
        }
        return result == t.isSat;
    }

    // here's how to use this --
    // let "x.cnf" be a cnf file, like _debug.cnf
    // Tool t = new Tool();
    // SATSolver ss = t.makeSATSolver();
    // boolean solution = ss.solve( args[0] );
    // System.out.println( "solution = " + solution)
    // if ( solution ) {
    //    ss.decode();
    // }
    //
    public SATSolver makeSATSolver() {
        return new SATSolver();
    }

    
}
