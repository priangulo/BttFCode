package gui;

import java.awt.*;
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
			setSize(625, 540);
			setLocationRelativeTo(getOwner());	
			setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		}
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
}

