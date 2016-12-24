package gui;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.stream.Collectors;

import javax.swing.*;

import bttf.Element;
/*
 * Created by JFormDesigner on Tue Feb 09 17:30:10 CST 2016
 */



/**
 * @author Priscila Angulo
 */
public class DeclarationMoreInfo extends JFrame {
	private Element current_element;
	
	public DeclarationMoreInfo(Element element) {
		current_element = element;
		initComponents();
		fillComponents();
	}

	private void fillComponents(){
		ta_code.setText(current_element.getCode());
		//System.out.print(current_element.getCode());
		
		ls_calledfrom.setListData(
				current_element.getRefToThis()
				.stream()
				.map(e -> e.simpleDisplayElement())
				.collect(Collectors.toList()).toArray(new String[0])
		);
		
		ls_callsto.setListData(
				current_element.getRefFromThis()
				.stream()
				.map(e -> e.simpleDisplayElement())
				.collect(Collectors.toList()).toArray(new String[0])
		);
		
		ta_comment.setText(current_element.getUser_comment());
	}
	
	private void initComponents() {
		lb_code = new JLabel();
		scrollPane1 = new JScrollPane();
		ta_code = new JTextArea();
		lb_calledfrom = new JLabel();
		scrollPane2 = new JScrollPane();
		ls_calledfrom = new JList<String>();
		lb_callsto = new JLabel();
		scrollPane3 = new JScrollPane();
		ls_callsto = new JList<String>();
		lb_comment = new JLabel();
		scrollPane4 = new JScrollPane();
		ta_comment = new JTextArea();
		panel1 = new JPanel();
		bt_savecomment = new JButton();
		
		//======== fr_moreinfoContentPane ========
		{
			setTitle("More information about Declaration");
			Container fr_moreinfoContentPane = getContentPane();
			fr_moreinfoContentPane.setLayout(new FlowLayout(FlowLayout.LEFT));

			//---- lb_code ----
			lb_code.setText("Code: ");
			lb_code.setPreferredSize(new Dimension(40, 14));
			fr_moreinfoContentPane.add(lb_code);

			//======== scrollPane1 ========
			{
				ta_code.setFont(new Font("Courier", Font.PLAIN, 12));
				ta_code.setEditable(false);
				scrollPane1.setPreferredSize(new Dimension(600, 200));
				scrollPane1.setViewportView(ta_code);
			}
			fr_moreinfoContentPane.add(scrollPane1);

			//---- lb_calledfrom ----
			lb_calledfrom.setText("Called from / Referenced from: ");
			fr_moreinfoContentPane.add(lb_calledfrom);

			//======== scrollPane2 ========
			{
				scrollPane2.setPreferredSize(new Dimension(600, 100));
				scrollPane2.setRequestFocusEnabled(false);
				scrollPane2.setViewportView(ls_calledfrom);
			}
			fr_moreinfoContentPane.add(scrollPane2);

			//---- lb_callsto ----
			lb_callsto.setText("Calls to / References to: ");
			fr_moreinfoContentPane.add(lb_callsto);

			//======== scrollPane3 ========
			{
				scrollPane3.setPreferredSize(new Dimension(600, 100));
				scrollPane3.setViewportView(ls_callsto);
			}
			fr_moreinfoContentPane.add(scrollPane3);
			
			//---- lb_comment ----
			lb_comment.setText("User comments: ");
			fr_moreinfoContentPane.add(lb_comment);
			//======== scrollPane4 ========
			{
				ta_comment.setFont(new Font("Tahoma", Font.PLAIN, 12));
				ta_comment.setEditable(true);
				scrollPane4.setPreferredSize(new Dimension(600, 100));
				scrollPane4.setViewportView(ta_comment);
			}
			fr_moreinfoContentPane.add(scrollPane4);
			
			//======== panel1 ========
			{
				panel1.setLayout(new BoxLayout(panel1, BoxLayout.LINE_AXIS));
				panel1.setPreferredSize(new Dimension(600, 30));
				panel1.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

				bt_savecomment.setPreferredSize(new Dimension(160, 20));
				bt_savecomment.setText("Save comment");
				panel1.add(bt_savecomment);
				fr_moreinfoContentPane.add(panel1);
			}
			
			setSize(625, 680);
			setLocationRelativeTo(getOwner());	
			setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		}
		
		bt_savecomment.addMouseListener(new MouseAdapter(){
			@Override
			public void mouseClicked(MouseEvent e){
				bt_saveCommentClicked(e);
			}
		});
		
		this.addWindowListener(new java.awt.event.WindowAdapter() {
		    @Override
		    public void windowClosing(java.awt.event.WindowEvent windowEvent) {
		        if(diff_text(current_element.getUser_comment(), ta_comment.getText())){
		        	bt_saveCommentClicked(null);
		        }
		        windowEvent.getWindow().dispose();
		    }
		});
	}
	
	
	
	private void bt_saveCommentClicked(MouseEvent e){
		current_element.setUser_comment(fix_format(ta_comment.getText()));
		JOptionPane.showMessageDialog(this.getContentPane(), "User comment saved.", "Comment saved.", JOptionPane.INFORMATION_MESSAGE);
	}
	
	
	private String fix_format(String text){
		return text.replaceAll("\\r\\n|\\r|\\n", " ").replaceAll(",", ";").trim();
	}
	
	private boolean diff_text(String text1, String text2){
		if(text1 == null && text2 == null){
			return false;
		}
		if(text1 != null && !text1.isEmpty() && text2 == null){
			return true;
		}
		if(text1 == null && text2 != null && !text2.isEmpty() ){
			return true;
		}
		if(text1 != null && text2 != null){
			if(!fix_format(text1).equals(fix_format(text2))){
				return true;
			}
		}
		
		return false;
	}
	
	
	private JLabel lb_code;
	private JScrollPane scrollPane1;
	private JTextArea ta_code;
	private JLabel lb_calledfrom;
	private JScrollPane scrollPane2;
	private JList<String> ls_calledfrom;
	private JLabel lb_callsto;
	private JScrollPane scrollPane3;
	private JList<String> ls_callsto;
	private JLabel lb_comment;
	private JScrollPane scrollPane4;
	private JTextArea ta_comment;
	private JPanel panel1;
	private JButton bt_savecomment;
}

