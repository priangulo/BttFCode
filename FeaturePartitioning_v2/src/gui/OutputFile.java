package gui;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import bttf.Element;
import bttf.Fact;
import bttf.Feature;
import bttf.Inference;
import bttf.Partition;

public class OutputFile {
	private JFrame main_window;
	private Partition partition;
	private BufferedWriter writer = null;
	static String file_path = System.getProperty("user.home") + "/Desktop/";
	
	public OutputFile(JFrame main_window) {
		this.main_window = main_window;
	}
	
	private void save_feature_model(String task, String tag_file_name){
		if(task != null){
			try{
				//write feature model - partition task
				writer = new BufferedWriter( new FileWriter(tag_file_name,false));
				writer.append(partition.current_task);
				writer.flush();
			}catch (IOException e){
				JOptionPane.showMessageDialog(main_window.getContentPane(), "Error creating file.", "Error", JOptionPane.ERROR_MESSAGE);
				e.printStackTrace();
			}
			finally
			{
			    try
			    {
			        if ( writer != null)
			        writer.close( );
			    }
			    catch ( IOException e)
			    {
			    	e.printStackTrace();
			    }
			}
		}
		else{
			JOptionPane.showMessageDialog(main_window.getContentPane(), "There is no partition task. Partition task file was not saved.", "No task.", JOptionPane.WARNING_MESSAGE);
		}
		
		
	}
	
	public void save_elements_file(Partition partition){
		this.partition = partition;
		if(partition != null && partition.get_elements() != null && partition.get_elements().size() > 0){
			String datetime = new Date().toString().replace(":", ".");
			String file_name = file_path + "BttF - " + partition.get_project_name() + " - " + datetime + ".csv";
			String tag_file_name = file_name + ".tag";
			
			try{
				save_feature_model(partition.current_task, tag_file_name);
				
				writer = new BufferedWriter( new FileWriter(file_name,false));
				writer.append("Identifier,TypeID,Type,Package,Class,Member,Feature,Is_fprivate?,"
						+ "Is_inferred?,Parent_features,Is_terminal?,Is_hook?,Inferences\r\n");
				for(Element e : partition.get_elements()){
					int count_nohook_inferences = 0;
					StringBuilder inferences = new StringBuilder();
					for(Fact f : partition.get_facts()){
						for(Inference i : f.getInferences()){
							if(i.getElement().equals(e)){
								inferences.append(i.getInference()+"   ");
								if(!i.getInference().toLowerCase().contains("hook")){
									count_nohook_inferences++;
								}
							}
						}
					}
					
					StringBuilder parent_features = new StringBuilder();
					if(e.getFeature() != null){
						Feature parent_feature = e.getFeature().getParent_feature();
						while(parent_feature != null){
							parent_features.append(parent_feature.getFeature_name() + " ");
							parent_feature = parent_feature.getParent_feature();
						}
					}
					
					writer.append(
						e.getIdentifier().replace(",", ";")+ ","
						+ e.getElement_type_granularity_trick()+ ","
						+ e.getElement_type_trick()+ ","
						+ e.getPackageName()+ ","
						+ e.getClassName()+ ","
						+ e.getMemberName().replace(",", ";")+ ","
						+ ((e.getFeature() != null) ? e.getFeature().getFeature_name() : "") +","
						+ ((e.getFeature() != null && e.isIs_fPrivate()) ? "TRUE" : "FALSE") +","
						+ ( (inferences.toString().isEmpty() || count_nohook_inferences == 0 ) ? "FALSE" : "TRUE" ) +","
						+ parent_features.toString() +","
						+ ((e.isIs_terminal()) ? "TRUE" : "FALSE") +","
						+ (e.isIs_hook() ? "TRUE" : "FALSE") +","
						+ inferences.toString().replace(",", ";")
						+ "\r\n"
					);
				}
			    writer.flush();
			}catch (IOException e){
				JOptionPane.showMessageDialog(main_window.getContentPane(), "Error creating file.", "Error", JOptionPane.ERROR_MESSAGE);
				e.printStackTrace();
			}
			finally
			{
			    try
			    {
			        if ( writer != null)
			        writer.close( );
			        JOptionPane.showMessageDialog(main_window.getContentPane(), "List of elements saved on " + file_name , "File saved.", JOptionPane.INFORMATION_MESSAGE);
			    }
			    catch ( IOException e)
			    {
			    	e.printStackTrace();
			    }
			}
		}
		else{
			JOptionPane.showMessageDialog(main_window.getContentPane(), "No elements to save.", "Nothing to save.", JOptionPane.INFORMATION_MESSAGE);
		}
	}
	
	public void save_results_file(Partition partition, boolean html){
		this.partition = partition;
		String file_name = file_path + "BttF - " + partition.get_project_name() + " - results";
		if(html){
			output_to_html_file(file_name + ".html");
		}
		else{
			output_to_txt_file(file_name + ".txt");
		}
	}
	
	public void output_log_to_txt_file(ArrayList<String> log, String file_name, String message, int messageType){
		file_name = file_name + ".txt";
		try
		{
			writer = new BufferedWriter( new FileWriter(file_name,false));
			for(String item : log){
				writer.write("\r\n" + item);
			}
		    writer.flush();
		    writer.close();
		    
		    JOptionPane.showMessageDialog(main_window.getContentPane(), message + ".\nError log saved on " + file_name, "File saved.", messageType);
		}
		catch ( IOException e)
		{
			JOptionPane.showMessageDialog(main_window.getContentPane(), "Error saving file.", "Error", JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
		finally
		{
		    try
		    {
		        if ( writer != null)
		        writer.close();
		    }
		    catch ( IOException e)
		    {
		    	e.printStackTrace();
		    }
		}
	}
	
	private void output_to_txt_file(String file_name){
		try
		{
			writer = new BufferedWriter( new FileWriter(file_name,false));
		    writer.write("\r\nProgram: " + partition.get_project_name());
		    writer.write("\r\nFeature Model:  " + partition.get_feature_model());
		    writer.write("\r\n");
		    writer.write("\r\n#Facts: " + partition.get_facts().size() + "   #Inferences: " + ( partition.get_elements().size() - partition.get_facts().size()) );
		    writer.write("\r\n");
		    for(Feature f : partition.get_all_features()){
		    	writer.write("\r\nFeature: " + f.getFeature_name());
				for(Element e : f.getFeature_elements()){
					writer.write("\r\n\t" + e.displayElement_withFeatAttr());
				}
				writer.write("\r\n");
			}
		    writer.flush();
		    writer.close();
		    JOptionPane.showMessageDialog(main_window.getContentPane(), "Results saved on " + file_name , "File saved.", JOptionPane.INFORMATION_MESSAGE);
		}
		catch ( IOException e)
		{
			JOptionPane.showMessageDialog(main_window.getContentPane(), "Error saving file.", "Error", JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
		finally
		{
		    try
		    {
		        if ( writer != null)
		        writer.close();
		    }
		    catch ( IOException e)
		    {
		    	e.printStackTrace();
		    }
		}
		
	}
	
	private void output_to_html_file(String file_name){
		try
		{
			writer = new BufferedWriter( new FileWriter(file_name,false));
		    writer.write(html_template_head() + html_print_data(partition) + html_template_foot());
		    writer.flush();
		    writer.close();
		    JOptionPane.showMessageDialog(main_window.getContentPane(), "Results saved on " + file_name , "File saved.", JOptionPane.INFORMATION_MESSAGE);
		}
		catch ( IOException e)
		{
			JOptionPane.showMessageDialog(main_window.getContentPane(), "Error saving file.", "Error", JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
		finally
		{
		    try
		    {
		        if ( writer != null)
		        writer.close( );
		    }
		    catch ( IOException e)
		    {
		    	e.printStackTrace();
		    }
		}
		
	}
	
	private String html_template_head(){
		return "<html><head><style>" +
				"body {width: 90%; margin: 40px auto; font-family: 'trebuchet MS', 'Lucida sans', Arial; font-size: 14px; color: #444; } +"
				+ "table { *border-collapse: collapse; /* IE7 and lower */ border-spacing: 0; width: 100%;}"
				+ "td, th { padding: 10px; border-bottom: 1px solid #f2f2f2; }"
				+ "tbody tr:nth-child(even) { background: #f5f5f5; -webkit-box-shadow: 0 1px 0 rgba(255,255,255,.8) inset; -moz-box-shadow:0 1px 0 rgba(255,255,255,.8) inset; box-shadow: 0 1px 0 rgba(255,255,255,.8) inset; }"
				+ "th { text-align: left; text-shadow: 0 1px 0 rgba(255,255,255,.5); border-bottom: 1px solid #ccc; "
					+ "background-color: #eee; background-image: -webkit-gradient(linear, left top, left bottom, from(#f5f5f5), to(#eee)); "
					+ "background-image: -webkit-linear-gradient(top, #f5f5f5, #eee); "
					+ "background-image: -moz-linear-gradient(top, #f5f5f5, #eee); "
					+ "background-image: -ms-linear-gradient(top, #f5f5f5, #eee); "
					+ "background-image: -o-linear-gradient(top, #f5f5f5, #eee); "
					+ "background-image: linear-gradient(top, #f5f5f5, #eee); }"
					+ "th:first-child { -moz-border-radius: 6px 0 0 0; -webkit-border-radius: 6px 0 0 0; border-radius: 6px 0 0 0;}"
					+ "th:last-child { -moz-border-radius: 0 6px 0 0; -webkit-border-radius: 0 6px 0 0; border-radius: 0 6px 0 0;}"
					+ "th:only-child{ -moz-border-radius: 6px 6px 0 0; -webkit-border-radius: 6px 6px 0 0; border-radius: 6px 6px 0 0; }"
					+ "tfoot td { border-bottom: 0; border-top: 1px solid #fff; background-color: #f1f1f1; }"
					+ "tfoot td:first-child { -moz-border-radius: 0 0 0 6px; -webkit-border-radius: 0 0 0 6px; border-radius: 0 0 0 6px; }"
					+ "tfoot td:last-child { -moz-border-radius: 0 0 6px 0; -webkit-border-radius: 0 0 6px 0; border-radius: 0 0 6px 0; }"
					+ "tfoot td:only-child{ -moz-border-radius: 0 0 6px 6px; -webkit-border-radius: 0 0 6px 6px border-radius: 0 0 6px 6px }"
					+ "</style> </head>"
					+ "<h2>BttF Results</h2>";
	}
	
	private String html_template_foot(){
		return "</html>";
	}
	
	private String html_print_data(Partition partition){
		StringBuilder contents = new StringBuilder();
		contents.append("<h2>Program: "+partition.get_project_name()+"</h2>");
		contents.append("<h2>Feature Model:&nbsp;&nbsp; "+partition.get_feature_model()+"</h2>");
		contents.append("<h2>#Elements: " + partition.get_elements().size() +"  #Facts: "+partition.get_facts().size()+"   #Inferences: "+ (partition.get_elements().size() - partition.get_facts().size()) +"</h2>");
		contents.append("<h2>&nbsp;</h2>");
		contents.append("<table><thead><tr><th>#</th><th>Type</th><th>Name</th><th>Feature</th><th>Label</th><th>Is Hook?</th><th>Inferences</th></tr></thead>"
				+ "<tfoot><tr><td>&nbsp;</td><td></td><td></td><td></td><td></td><td></td><td></td></tr></tfoot>"
				+ "<tbody>");
		
		Collections.sort(partition.get_elements());
		int counter = 1;
		for(Element e : partition.get_elements()){
			contents.append("<tr>"
					+ "<td>"+ counter +"</td>"
					+ "<td>"+ e.getElement_type().get_element_type() +"</td>"
					+ "<td>"+ e.getIdentifier() +"</td>"
					+ "<td>"+ e.getFeature().getFeature_name() +"</td>");
			
			if(e.isIs_fPublic()){contents.append("<td>fpublic</td>");}		
			else if(e.isIs_fPrivate()){contents.append("<td>fprivate</td>");}
			else {contents.append("<td></td>");}
			
			if(e.isIs_hook()){contents.append("<td>x</td>");}
			else {contents.append("<td></td>");}
			
			contents.append("<td>");
			for(Fact f : partition.get_facts()){
				for(Inference i : f.getInferences()){
					if(i.getElement().equals(e)){
						contents.append(i.getInference()+"</br>");
					}
				}
			}
			contents.append("</td>");
			
			contents.append("</tr>");
			counter++;
		}
		
		contents.append("</tbody></table>");
		return contents.toString();
	}
	
}
