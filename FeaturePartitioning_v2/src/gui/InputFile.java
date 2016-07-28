package gui;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.stream.Collectors;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import bttf.Element;
import bttf.ElementType;
import bttf.Feature;
import bttf.Partition;

public class InputFile {
	private Partition partition;
	private JFrame main_window;
	private OutputFile output;
	
	public InputFile(JFrame main_window, Partition partition) {
		this.partition = partition;
		this.main_window = main_window;
		this.output = new OutputFile(main_window);
	}
	
	public ArrayList<String> get_featuremodel_from_tagfile(String file_name){
		BufferedReader reader = null;
		ArrayList<String> tasks = new ArrayList<String>();
		try {
			reader = new BufferedReader(new FileReader(file_name));
			String line;
			while ((line = reader.readLine()) != null && !line.trim().isEmpty()) {
				tasks.add(line);
			}
			return tasks;
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(main_window.getContentPane(), "Error uploading file: " + file_name, "File does not exist.", JOptionPane.ERROR_MESSAGE);
		} catch (IOException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(main_window.getContentPane(), "Error uploading file: " + file_name, "IOException.", JOptionPane.ERROR_MESSAGE);
		} catch (Exception e){
			e.printStackTrace();
			JOptionPane.showMessageDialog(main_window.getContentPane(), "Error uploading file: " + file_name + " " + e.getMessage(), "Exception.", JOptionPane.ERROR_MESSAGE);
			
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return null;
	}

	public void get_elements_from_csv_file(String file_name, ArrayList<String> features, Boolean reload){
		int latest_feature_order = partition.get_latest_feature_in_task();
		
		BufferedReader reader = null;
		String comma = ",";
		String line = "";
		int identifier_column = 0;
		int type_column = 2;
		int feature_column = 6;
		int modifier_column = 7;
		int inferred_column = 8;
		int parentfeatures_column = 9;
		int isterminal_column = 10;
			
		ArrayList<Element> file_elements = new ArrayList<Element>();
		ArrayList<InvalidFileFact> invalid_facts = new ArrayList<InvalidFileFact>();
		
		try {
			reader = new BufferedReader(new FileReader(file_name));
			int line_number = 1;
			reader.readLine(); //ignore first line, it is the header
			while ((line = reader.readLine()) != null) {
				String[] fields = line.split(comma);
				if(fields.length > feature_column 
						&& fields[identifier_column] != null
						&& !fields[identifier_column].trim().isEmpty()
						&& fields[type_column] != null
						&& !fields[type_column].trim().isEmpty()
						&& fields[feature_column] != null 
						&& !fields[feature_column].trim().isEmpty() 
				){
					String identifier = fields[identifier_column].trim().replace(";", ",");
					String type = fields[type_column].trim().toUpperCase();
					String feature_name = fields[feature_column].trim().toUpperCase();
					String parent_features_names = fields[parentfeatures_column].trim().toUpperCase();
					String[] parent_features = parent_features_names.split(" ");
					String assignment_text = identifier + " assigned to " + feature_name;
					Boolean is_inferred = !fields[inferred_column].trim().isEmpty() && fields[inferred_column].toUpperCase().trim().equals("TRUE");
					Boolean is_terminal = !fields[isterminal_column].trim().isEmpty() && fields[isterminal_column].toUpperCase().trim().equals("TRUE");
					
					ElementType element_type = null;
					Feature feature = null;
					Boolean ignore_inferred = false;
					
					//GET FEATURE
					feature = partition.get_feature_by_name(feature_name);
					
					//feature is in task, everything is cool
					//ignore inferred
					if(feature != null && feature.getIn_current_task()){
						ignore_inferred = true;
					}
					//feature not in task or feature inexistent, look for active parent
					else if( (feature != null && !feature.getIn_current_task()) || feature == null){
						Feature parent_feature = null;
						Boolean found_active_parent = false;
						for(int i=0; i<parent_features.length; i++){
							if(parent_features[i] != null && !parent_features[i].trim().isEmpty()){
								parent_feature = partition.get_feature_by_name(parent_features[i].trim());
								if(parent_feature != null && parent_feature.getIn_current_task()){
									found_active_parent = true;
									break;
								}
							}
						}
						//there is an active parent!! move child to its parent
						if(found_active_parent && parent_feature != null){
							feature = parent_feature;
						}
					}
					
					//if feature still non-existing try to move to first existing parent
					if(feature == null){
						for(int i=0; i<parent_features.length; i++){
							if(parent_features[i] != null && !parent_features[i].trim().isEmpty()){
								if(partition.get_feature_by_name(parent_features[i].trim()) != null){
									feature = partition.get_feature_by_name(parent_features[i].trim());
									break;
								}
							}
						}
					}
					
					//GET ELEMENT TYPE
					if(type.equals("FIELD")){element_type = ElementType.ELEM_TYPE_FIELD; }
					else if(type.equals("METHOD") || type.equals("CONSTRUCTOR") || type.equals("INITIALIZER")){element_type = ElementType.ELEM_TYPE_METHOD; }
					else if(type.equals("CLASS")){element_type = ElementType.ELEM_TYPE_CLASS; }
					else if(type.equals("PACKAGE")){element_type = ElementType.ELEM_TYPE_PACKAGE; }
					
					if(element_type != null && feature != null && ((ignore_inferred && !is_inferred) || !ignore_inferred)){
						Element file_elem = new Element(identifier, element_type, null, null, is_terminal);
						file_elem.setFeature(feature);
						
						if( ( fields.length > modifier_column
								&& fields[modifier_column] != null
								&& !fields[modifier_column].trim().isEmpty()
								&& fields[modifier_column].toUpperCase().trim().equals("TRUE") )
							|| (feature != null && feature.getOrder() == latest_feature_order)
						){
							file_elem.setIs_fPrivate(true);
						}
						else{
							file_elem.setIs_fPublic(true);
						}
						
						file_elements.add(file_elem);
					}
					else{
						//element type doesn't exist
						if(element_type == null){
							invalid_facts.add(new InvalidFileFact(identifier, assignment_text, "Line#" + line_number + " " + InvalidFileFact.ELEMENTTYPE_DOESNT_EXIST));
						}
						//feature doesn't exist
						if(feature == null){
							invalid_facts.add(new InvalidFileFact(identifier, assignment_text, "Line#" + line_number + " " + InvalidFileFact.FEATURE_DOESNT_EXIST));
						}
						//supposed to be ignored, this is not an invalid fact, is for debugging purposes
						if(ignore_inferred && is_inferred){
							System.out.println("Line#" + line_number + " supposed to be ignored: " + identifier + " - " + assignment_text);
						}
					}
				}
				line_number++;
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(main_window.getContentPane(), "Error uploading file: " + file_name, "File does not exist.", JOptionPane.WARNING_MESSAGE);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		invalid_facts.addAll(partition.add_elements_from_facts_file(file_elements, reload));
		if(invalid_facts.size() > 0){
			System.out.println("These facts where not valid: \n");
			ArrayList<String> error_log = new ArrayList<String>();
			
			for(InvalidFileFact invfact : invalid_facts){
				String error = "You said: " + invfact.getElement_identifier() + " " + invfact.getFact() + ", " + invfact.getExplanation();
				error_log.add(error);
				System.out.println(error);
			}
			
			/*
			if(invalid_facts.size() <= 20){
				JOptionPane.showMessageDialog(
						main_window.getContentPane(), 
						"These facts where not valid: \n" 
								+ invalid_facts.stream()
								.map(f -> f.displayInvalidFact())
								.collect(Collectors.toList())
								.toString()
						, "List of invalid facts."
						, JOptionPane.ERROR_MESSAGE
					);
			}*/
			
			output.output_log_to_txt_file(error_log, file_name + "_ErrorLog", "There were invalid facts", JOptionPane.WARNING_MESSAGE);
		}
	}
	
	
}
