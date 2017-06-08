package gui;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

/*
 * Created by JFormDesigner on Fri Feb 12 17:39:42 CST 2016
 */
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import bttf.Cycle;
import bttf.Element;



/**
 * @author Priscila Angulo
 */
public class CycleElements extends JFrame {
	private ButtonGroup bg_options = new ButtonGroup();
	private BttFMain parent_frame;
	private ArrayList<Cycle> cycle_list = new ArrayList<Cycle>();
	
	public CycleElements(BttFMain parent_frame, ArrayList<Cycle> cycle_list) {
		this.parent_frame = parent_frame;
		this.cycle_list = cycle_list;
		initComponents();
		bg_options.add(rb_same);
		bg_options.add(rb_different);
		registerActions();
		show_cycles();
	}

	/*
	 * Accept button clicked
	 */
	private void bt_addAcceptMouseClicked(MouseEvent e){
		if(rb_same.isSelected() || rb_different.isSelected()){
			this.setVisible(false);
			parent_frame.setVisible(true);
			if(rb_same.isSelected()){
				parent_frame.setCycle_same_feature(true);
			}
			else{
				parent_frame.setCycle_same_feature(false);
			}
		}
		else
		{
			JOptionPane.showMessageDialog(this.getContentPane(), "Please select an option.", "Select an option.", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	private void show_cycles(){
		String cycles_text = ""; 
		for(Cycle c : cycle_list){
			cycles_text = cycles_text + "Cycle: ";
			for(Element e : c.getElements()){
				cycles_text = cycles_text + "\n    " + e.getIdentifier();
			}
			cycles_text = cycles_text + "\n";
		}
		ta_cycles.setText(cycles_text);
	}
	
	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		// Generated using JFormDesigner Evaluation license - Priscila Angulo
		lb_cyclesNotification = new JLabel();
		lb_cycles = new JLabel();
		scrollPane1 = new JScrollPane();
		ta_cycles = new JTextArea();
		lb_question = new JLabel();
		rb_same = new JRadioButton();
		rb_different = new JRadioButton();
		bt_accept = new JButton();

		//======== fr_cycles ========
		{
			setTitle("Cycles in code");
			Container fr_cyclesContentPane = getContentPane();
			fr_cyclesContentPane.setLayout(new FlowLayout(FlowLayout.RIGHT));

			//---- lb_cyclesNotification ----
			lb_cyclesNotification.setText("Cycles where detected in your code");
			lb_cyclesNotification.setForeground(Color.red);
			lb_cyclesNotification.setFont(new Font("Tahoma", Font.BOLD, 14));
			lb_cyclesNotification.setPreferredSize(new Dimension(600, 25));
			fr_cyclesContentPane.add(lb_cyclesNotification);

			//---- lb_cycles ----
			lb_cycles.setText("Cycles: ");
			lb_cycles.setPreferredSize(new Dimension(600, 14));
			fr_cyclesContentPane.add(lb_cycles);

			//======== scrollPane1 ========
			{
				scrollPane1.setPreferredSize(new Dimension(600, 250));
				scrollPane1.setViewportView(ta_cycles);
			}
			fr_cyclesContentPane.add(scrollPane1);

			//---- lb_question ----
			lb_question.setText("How you want to deal with this?");
			lb_question.setPreferredSize(new Dimension(600, 14));
			fr_cyclesContentPane.add(lb_question);

			//---- rb_same ----
			rb_same.setText("All the elements will belong to the same feature");
			rb_same.setPreferredSize(new Dimension(600, 23));
			fr_cyclesContentPane.add(rb_same);

			//---- rb_different ----
			rb_different.setText("The elements will belong to different features");
			rb_different.setPreferredSize(new Dimension(600, 23));
			fr_cyclesContentPane.add(rb_different);

			//---- bt_accept ----
			bt_accept.setText("Accept");
			bt_accept.setPreferredSize(new Dimension(130, 23));
			fr_cyclesContentPane.add(bt_accept);
			setSize(630, 480);
			setLocationRelativeTo(getOwner());
		}
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
	}

	private void registerActions(){
		bt_accept.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				bt_addAcceptMouseClicked(e);
			}
		});
	}
	
	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	// Generated using JFormDesigner Evaluation license - Priscila Angulo
	private JLabel lb_cyclesNotification;
	private JLabel lb_cycles;
	private JScrollPane scrollPane1;
	private JTextArea ta_cycles;
	private JLabel lb_question;
	private JRadioButton rb_same;
	private JRadioButton rb_different;
	private JButton bt_accept;
	// JFormDesigner - End of variables declaration  //GEN-END:variables
}

