package gui;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import java.awt.*;
import javax.swing.*;
import javax.swing.text.DefaultCaret;

import java.beans.*;

public class ProgressBar extends JFrame
        implements PropertyChangeListener {

	private JProgressBar progressBar;
    private JTextArea taskOutput;
    private final Task task;
    private JFrame parent;
    
    private static final boolean textFieldVisible = true;  // set to false if text area not to be shown

    // int variable whose value is updated from 0..100 (percent)
    // to indicate progress
    private int progress;
    private String progress_text;
    private static ProgressBar pb; // this is the (singleton) object that is instantiated

    public ProgressBar(JFrame parent) {
    	this.parent = parent;
    	progressBar = new JProgressBar(0, 100);
        progressBar.setValue(0);
        progressBar.setStringPainted(true);
        taskOutput = new JTextArea(5, 20);
        taskOutput.setMargin(new Insets(5, 5, 5, 5));
        taskOutput.setEditable(false);
        //Instances of javax.swing.SwingWorker are not reusable, so
        //we create new instances as needed.
        task = new Task();
        task.addPropertyChangeListener(this);
        
        addWindowListener(new java.awt.event.WindowAdapter() {
		    @Override
		    public void windowClosing(java.awt.event.WindowEvent windowEvent) {
		    	parent.dispose();
		    	dispose();
		    }
		});
    }

    /**
     * Invoked when task's progress property changes. Method prints to text area
     * progress that has been made
     */
    public void propertyChange(PropertyChangeEvent evt) {
        if ("progress" == evt.getPropertyName()) {
            int progress = (Integer) evt.getNewValue();
            progressBar.setValue(progress);
            if (textFieldVisible) {
                taskOutput.append(this.progress_text + "\n");
            }
        }
    }

    /**
     * Create the GUI and show it. As with all GUI code, this must run on the
     * event-dispatching thread.
     */

    private static void createAndShowGUI(ProgressBar pb){
    	//Create and set up the window.
    	pb.setTitle("Loading BttF...");
        
        //Create and set up the content pane.
        Container newContentPane = pb.getContentPane();
        newContentPane.setLayout(new FlowLayout(FlowLayout.CENTER));
        //newContentPane.setOpaque(true); //content panes must be opaque
        pb.setContentPane(newContentPane);

        //Display the window.
        pb.progressBar = new JProgressBar(0, 100);
        pb.progressBar.setValue(0);
        pb.progressBar.setStringPainted(true);
        pb.taskOutput = new JTextArea(5, 20);
        pb.taskOutput.setMargin(new Insets(5, 5, 5, 5));
        pb.taskOutput.setEditable(false);
		
        JPanel panel = new JPanel();
        panel.add(pb.progressBar);
        pb.add(panel, BorderLayout.PAGE_START);
        if (textFieldVisible) {
        	DefaultCaret caret = (DefaultCaret) pb.taskOutput.getCaret();
            caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
            pb.add(new JScrollPane(pb.taskOutput, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER ), BorderLayout.CENTER);            
        } else {
            Dimension prefSize = pb.progressBar.getPreferredSize();
            System.out.println(prefSize.width);
            prefSize.width = 200;
            pb.progressBar.setPreferredSize(prefSize);
        }

        //setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        pb.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        
        //pb.pack();
        pb.setSize(280, 200);
        pb.setVisible(true);
        pb.setLocationRelativeTo(pb.getOwner());
        pb.task.execute();
    }

    /*private static void createAndShowGUI(ProgressBar pb) {
        //Create and set up the window.
    	frame = new MyFrame(pb.parent);
    	frame.setTitle("Loading BttF...");
        //frame = new JFrame("Loading BttF...");
        
        //frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        frame.addWindowListener(new java.awt.event.WindowAdapter() {
		    @Override
		    public void windowClosing(java.awt.event.WindowEvent windowEvent) {
		    	frame.parent.dispose();
		        windowEvent.getWindow().dispose();
		    }
		});

        //Create and set up the content pane.
        JComponent newContentPane = pb;
        newContentPane.setOpaque(true); //content panes must be opaque
        frame.setContentPane(newContentPane);

        //Display the window.
        frame.pack();
        frame.setVisible(true);
        frame.setLocation(150, 150);
        frame.setLocationRelativeTo(frame.getOwner());	
    }*/

    static class RunProgressBarGui implements Runnable {
        public void run() {
            createAndShowGUI(ProgressBar.pb);
        	//createAndShowGUI();
        }
    }
    
    public void setProgress(int progress) { 
    	this.progress = progress;
    }
    
    public void setProgressText(String progress_text){
    	this.progress_text = progress_text;
    }
    
    public int getProgress() { 
        return this.progress;
    }
    
    public String getProgressText(){
    	return this.progress_text;
    }
    
    public static ProgressBar StartProgressBar(JFrame parent) {
    	ProgressBar.pb = new ProgressBar(parent);
        RunProgressBarGui runnable = new RunProgressBarGui();
        javax.swing.SwingUtilities.invokeLater(runnable);
        return ProgressBar.pb;
    }
    
    
    private class Task extends SwingWorker<Void, Void> {

        /*
         * Main task. Executed in background thread.
         *
         * MsP: tie progress to the number of files parsed -- the public
         * variable below, called progress, is an integer which is init
         * to 0 automatically.  You need to periodically update its value 
         * (an integer percentage) every .5 seconds or so.  When it
         * reaches 100, the progress bar will turn off automatically
         */
        @Override
        public Void doInBackground() {
            progress = 0;
            //Initialize progress property.
            setProgress(0);
            taskOutput.append("Initializing BttF, wait a couple seconds...\n");
            while (progress < 100) {
                //Sleep for up to one second.
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ignore) {
                }
                setProgress(Math.min(progress, 100));
            }
            if (textFieldVisible) {
                taskOutput.append("Done!\n");
            }

            return null;
        }

        /*
         * Executed in event dispatching thread
         */
        @Override
        public void done() {
            setCursor(null); //turn off the wait cursor
            try {
                Thread.sleep(500);
            } catch (InterruptedException ex) {
                //ignore
            }
            pb.dispose();
            //frame = null;

        }
    }
    
}
