package gui;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import bttf.Element;
import bttf.Fact;
import bttf.Feature;
import bttf.FeatureBound;
import bttf.Inference;
import bttf.Partition;
import bttf.Reference;

public class OutputFile {
	private JFrame main_window;
	private Partition partition;
	private BufferedWriter writer = null;
	static String file_path_base;
	static String file_path;
	private String program_name;
	private boolean references_saved = false;
	private SimpleDateFormat date_format = new SimpleDateFormat("yyyyMMdd HH.mm");
	
	private static OutputFile instance = null;
	
	public static OutputFile OutputFileInstance(JFrame main_window, String program_name, Partition partition) {
		if(instance == null) {
			instance = new OutputFile(main_window, program_name, partition);
		}
		else if(instance.partition == null){
			instance.partition = partition;
		}
		
		return instance;
	}
	
	private OutputFile(JFrame main_window, String program_name, Partition partition) {
		this.partition = partition;
		this.main_window = main_window;
		this.program_name = program_name;
		
		String datetime = date_format.format(new Date());
		this.file_path_base = System.getProperty("user.home") + "/Desktop/BttF/" + program_name;
		this.file_path = file_path_base + "/" + datetime + "/";
	}
	
	
	public void disposeInstance(){
		this.instance = null;
	}

	
	private File create_new_file(String file_name){
		File parent = new File(file_path);
		parent.mkdirs();
		parent.setWritable(true);
		parent.setReadable(true);
		
		File file = new File(file_path + "//" + file_name);
		file.setWritable(true);
		file.setReadable(true);
		return file;
	}
	
	public void save_reference_list(boolean showSaveMsg){
		if(!this.references_saved){
			try{
				if(this.partition != null && this.partition.get_references_list() != null && this.partition.get_references_list().size() > 0){
					String file_name = "ReferencesList.csv";
					
					writer = new BufferedWriter( new FileWriter(create_new_file(file_name),false));
					writer.append("from,to\r\n");
					for(Reference r : this.partition.get_references_list()){
						//writer.append(r.toString());
						writer.append(r.toString_v2());
					}
					writer.flush();
					this.references_saved = true;
					if(showSaveMsg){
						JOptionPane.showMessageDialog(main_window.getContentPane(), "List of references in CRG saved in \n"+ file_name, "CRG references saved.", JOptionPane.INFORMATION_MESSAGE);
					}
				}
				
			}catch (IOException e){
				JOptionPane.showMessageDialog(main_window.getContentPane(), "Error saving references file.", "Error", JOptionPane.ERROR_MESSAGE);
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
	}
	
	private void save_feature_model(boolean showSaveMsg){
		if(partition.featuremodel_alllines != null && partition.featuremodel_alllines.size() > 0){
			try{
				String file_name = "FeatureModel.bttf";
				writer = new BufferedWriter( new FileWriter(create_new_file(file_name),false));
				for(String line : partition.featuremodel_alllines){
					writer.append(line);
				}
				writer.flush();
				if(showSaveMsg){
					JOptionPane.showMessageDialog(main_window.getContentPane(), "Feature Model saved in \n"+ file_name, "Feature Model saved.", JOptionPane.INFORMATION_MESSAGE);
				}
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
		String datetime = date_format.format(new Date());
		this.file_path = file_path_base + "/" + datetime + "/";
		boolean useAnnotFeature = !partition.thereAreAssignedElements();
		
		if(useAnnotFeature){
			JOptionPane.showMessageDialog(main_window.getContentPane(), "No assignments have been made. "
					+ "\nFeature will be harvested from declarations' annotation text, if available.", "Harvest features from annotations.", JOptionPane.INFORMATION_MESSAGE);
		}
		
		if(partition != null && partition.get_elements() != null && partition.get_elements().size() > 0){
			
			String file_name = "FactsAndInferences.csv";
			
			try{
				this.references_saved = false;
				save_feature_model(false);
				save_reference_list(false);
				
				writer = new BufferedWriter( new FileWriter(create_new_file(file_name),false));
				writer.append("Identifier,TypeID,Type,Package,Class,Member,Feature,Is_fprivate?,"
					+ "Is_inferred?,Parent_features,Is_terminal?,Is_hook?,Inferences,User_comment,Member_modifier,"
					+ "Method_Signature,Earliest_bound,Latest_bound,CodeAnnotation,LOC,FWBelongLevel,NeedsLocalConstructor\r\n");
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
					
					Feature parent_feature = null;
					StringBuilder parent_features = new StringBuilder();
					if(e.getFeature() != null){
						parent_feature = e.getFeature().getParent_feature();
						while(parent_feature != null){
							parent_features.append(parent_feature.getFeature_name() + " ");
							parent_feature = parent_feature.getParent_feature();
						}
					}
					
					String temp_eb = "";
					String temp_lb = "";
					
					if(e.getEarliest_bound().isEmpty()){
						FeatureBound eb = this.partition.boundsCalc.get_earliest_bound(e, partition.get_all_features(), parent_feature);
						if(eb != null && eb.getFeature() != null){
							temp_eb = eb.getFeature().getFeature_name();
						}
					}
					if(e.getLatest_bound().isEmpty()){
						FeatureBound lb = this.partition.boundsCalc.get_latest_bound(e, partition.get_all_features(), parent_feature);
						if(lb != null && lb.getFeature() != null){
							temp_lb = lb.getFeature().getFeature_name();
						}
					}
					
					writer.append(
						e.getIdentifier().replace(",", ";")+ ","
						+ e.getElement_type_granularity_trick()+ ","
						+ e.getElement_type_trick()+ ","
						+ e.getPackageName()+ ","
						+ e.getClassName().replaceAll(",", ";")+ ","
						+ e.getMemberName().replaceAll(",", ";")+ ","
						+ ((e.getFeature() != null) ? e.getFeature().getFeature_name() : (useAnnotFeature ? getFeatureFromAnnotationText(e.getAnnotation_text(),partition.get_all_features()) : "")) +","
						+ ((e.getFeature() != null && e.isIs_fPrivate()) ? "TRUE" : "FALSE") +","
						+ ( (inferences.toString().isEmpty() || count_nohook_inferences == 0 ) ? "FALSE" : "TRUE" ) +","
						+ parent_features.toString() +","
						+ ((e.isIs_terminal()) ? "TRUE" : "FALSE") +","
						+ (e.isIs_hook() ? "TRUE" : "FALSE") +","
						+ inferences.toString().replace(",", ";")  +","
						+ (e.getUser_comment() == null ? "" :  e.getUser_comment()) +","
						+ e.getModifier() +","
						+ e.getMethod_signature().replace(",", ";") +","
						+ (e.getEarliest_bound().isEmpty() ? temp_eb : e.getEarliest_bound()) +","
						+ (e.getLatest_bound().isEmpty() ? temp_lb : e.getLatest_bound()) +","
						+ e.getAnnotation_text() +","
						+ e.getLOCAdjusted() +","
						+ ((e.belongLevelFW() ==  null) ? "" : e.belongLevelFW().getLevel()) +","
						+ e.needsLocalConstructor()
						+ "\r\n"
					);
				}
			    writer.flush();
			    JOptionPane.showMessageDialog(main_window.getContentPane(), "State saved on " + this.file_path , "State saved.", JOptionPane.INFORMATION_MESSAGE);
			}catch (IOException e){
				JOptionPane.showMessageDialog(main_window.getContentPane(), "Error creating file.", "Error", JOptionPane.ERROR_MESSAGE);
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
		else{
			JOptionPane.showMessageDialog(main_window.getContentPane(), "No elements to save.", "Nothing to save.", JOptionPane.INFORMATION_MESSAGE);
		}
	}
	
	public void save_results_file(Partition partition, boolean html){
		this.partition = partition;
		String file_name = "BttF-" + partition.get_project_name() + "-results";
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
			writer = new BufferedWriter( new FileWriter(create_new_file(file_name),false));
			for(String item : log){
				writer.write("\r\n" + item);
			}
		    writer.flush();
		    writer.close();
		    
		    JOptionPane.showMessageDialog(main_window.getContentPane(), message + ".\nError log saved on " + file_name, "File saved.", messageType);
		    
		    save_reference_list(true);
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
			writer = new BufferedWriter( new FileWriter(create_new_file(file_name),false));
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
			writer = new BufferedWriter( new FileWriter(create_new_file(file_name),false));
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
	
	private String getFeatureFromAnnotationText(String annotation_text, ArrayList<Feature> features){
		if(annotation_text != null){
			for(Feature feature : features){
				if(annotation_text.toUpperCase().contains(feature.getFeature_name())){
					return feature.getFeature_name();
				}
			}
		}
		return "";
		
	}
	
}
