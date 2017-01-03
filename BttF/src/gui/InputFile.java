package gui;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.stream.Collectors;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import adapter.GAML_Adapter;
import bttf.Element;
import bttf.ElementType;
import bttf.Feature;
import bttf.Partition;
import bttf.Reference;

public class InputFile {
	public final static String container_feature_name = "^";
	public final static String GAML_LANGUAGE = "GAML";
	
	private Partition partition;
	private JFrame main_window;
	private OutputFile output;
	private String project_name;
	
	private final String comma = ",";
	
	private final int identifier_column = 0;
	private final int type_column = 2;
	private final int package_column = 3;
	private final int class_column = 4;
	private final int member_column = 5;		
	private final int feature_column = 6;
	private final int modifier_column = 7;
	private final int inferred_column = 8;
	private final int parentfeatures_column = 9;
	private final int isterminal_column = 10;
	private final int usercomment_column = 13;
	private final Feature container_feature = new Feature(container_feature_name, -1, true, null, false);
	
	private final int call_from_column = 0;
	private final int call_to_column = 1;
	private final int call_from_type_column = 2;
	private final int call_to_type_column = 3;
	private final int call_from_mod_column = 4;
	private final int call_to_mod_column = 5;
	private final int call_from_code_column = 6;
	private final int call_to_code_column = 7;
	private final int call_from_isterminal_column = 8;
	private final int call_to_isterminal_column = 9;
	private final int number_fields_crg_file = 10;
	private final String call_from_name	= "call_from";
	private final String call_to_name = "call_to";
	private final String call_from_type_name = "call_from_type";
	private final String call_to_type_name = "call_to_type"; 
	private final String call_from_mod_name = "call_from_mod";
	private final String call_to_mod_name = "call_to_mod";
	private final String call_from_code_name = "call_from_code";
	private final String call_to_code_name = "call_to_code";
	private final String call_from_isterminal_name = "call_from_isterminal";
	private final String call_to_isterminal_name = "call_to_isterminal";

	
	
	public InputFile(JFrame main_window, Partition partition, String project_name) {
		this.partition = partition;
		this.main_window = main_window;
		this.project_name = project_name;
		this.output = OutputFile.OutputFileInstance(main_window, this.project_name, this.partition);
	}
	
	public void set_partition(Partition partition){
		this.partition = partition;
	}
	
	public ArrayList<String> get_featuremodel_from_bttffile(String file_name){
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
		int latest_feature_order = partition.partitionHelper.get_latest_feature_in_task();
		boolean sanity_check = true;
		BufferedReader reader = null;
		String line = "";
			
		ArrayList<Element> file_elements = new ArrayList<Element>();
		ArrayList<InvalidFileFact> invalid_facts = new ArrayList<InvalidFileFact>();
		
		try {
			reader = new BufferedReader(new FileReader(file_name));
			int line_number = 1;
			reader.readLine(); //ignore first line, it is the header
			while ((line = reader.readLine()) != null) {
				String[] fields = line.split(comma);
				sanity_check = sanity_check(fields);
				if(sanity_check){
					if(fields.length > feature_column 
							&& fields[identifier_column] != null
							&& !fields[identifier_column].trim().isEmpty()
							&& fields[type_column] != null
							&& !fields[type_column].trim().isEmpty()
							//&& fields[feature_column] != null 
							//&& !fields[feature_column].trim().isEmpty()
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
						if(feature_name.startsWith(container_feature_name)){
							feature = container_feature;
						}
						else{
							feature = partition.partitionHelper.get_feature_by_name(feature_name);
						}
						
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
									parent_feature = partition.partitionHelper.get_feature_by_name(parent_features[i].trim());
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
									if(partition.partitionHelper.get_feature_by_name(parent_features[i].trim()) != null){
										feature = partition.partitionHelper.get_feature_by_name(parent_features[i].trim());
										break;
									}
								}
							}
						}
						
						//GET ELEMENT TYPE
						element_type = get_type(type);
						if( element_type != null 
								&& ( 
									(feature != null && ((ignore_inferred && !is_inferred) || !ignore_inferred) )
									|| (fields.length > usercomment_column && !fields[usercomment_column].trim().isEmpty())
								)
							){
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
							
							if( fields.length > usercomment_column){
								file_elem.setUser_comment(fields[usercomment_column]);
							}
							
							file_elements.add(file_elem);
						}
						else{
							//element type doesn't exist
							if(element_type == null){
								invalid_facts.add(new InvalidFileFact(identifier, assignment_text, "Line#" + line_number + " " + InvalidFileFact.ELEMENTTYPE_DOESNT_EXIST));
							}
							//feature doesn't exist
							if(feature == null && feature_name != null && !feature_name.isEmpty()){
								invalid_facts.add(new InvalidFileFact(identifier, assignment_text, "Line#" + line_number + " " + InvalidFileFact.FEATURE_DOESNT_EXIST));
							}
							//supposed to be ignored, this is not an invalid fact, is for debugging purposes
							if(ignore_inferred && is_inferred){
								System.out.println("Line#" + line_number + " supposed to be ignored: " + identifier + " - " + assignment_text);
							}
						}
					}
				}
				//sanity_check is false
				else{
					JOptionPane.showMessageDialog(main_window.getContentPane(), "File contents are invalid.\nColumn identifier doesn't match package+class+member columns. Line# " + line_number +".", "Invalid file contents.", JOptionPane.ERROR_MESSAGE);
					break;
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
		
		if(sanity_check){
			sort_elements_by_type(file_elements);
			invalid_facts.addAll(partition.add_elements_from_facts_file(file_elements, reload));
			if(invalid_facts.size() > 0){
				System.out.println("These facts where not valid: \n");
				ArrayList<String> error_log = new ArrayList<String>();
				
				for(InvalidFileFact invfact : invalid_facts){
					String error = "You said: " + invfact.getElement_identifier() + " " + invfact.getFact() + ", " + invfact.getExplanation();
					error_log.add(error);
					System.out.println(error);
				}		
				
				output.output_log_to_txt_file(error_log, "Errors_ReadingCSV", "There were invalid facts", JOptionPane.WARNING_MESSAGE);
			}
		}
	}
	
	private void sort_elements_by_type(ArrayList<Element> file_elements){
		Collections.sort(file_elements, new Comparator<Element>() {
	        @Override
	        public int compare(Element e1, Element e2)
	        {
	        	return Integer.valueOf(e1.getElement_type().get_element_granularity()).compareTo(e2.getElement_type().get_element_granularity());
	            
	        }
	    });
	}
	
	private boolean sanity_check(String[] fields){
		String identifier = fields[identifier_column].trim().replace(";", ",");
		String type = fields[type_column].trim().toUpperCase();
		ElementType elementType = get_type(type);
		if(elementType.equals(ElementType.ELEM_TYPE_PACKAGE)){
			if(identifier.toUpperCase().equals(fields[package_column].toUpperCase())){
				return true;
			}
		}
		else if(elementType.equals(ElementType.ELEM_TYPE_CLASS)){
			String name = fields[package_column] + "." + fields[class_column];
			if(identifier.toUpperCase().equals(name.toUpperCase())){
				return true;
			}
		}
		else if(elementType.equals(ElementType.ELEM_TYPE_FIELD) || elementType.equals(ElementType.ELEM_TYPE_METHOD) ){
			String name = fields[package_column] + "." + fields[class_column] + "." + fields[member_column];
			name = name.replace(";", ",");
			if(identifier.toUpperCase().equals(name.toUpperCase())){
				return true;
			}
		}
		/*else if(elementType.equals(ElementType.ELEM_TYPE_METHOD)){
			String name = fields[package_column] + "." + fields[class_column] + "." + fields[member_column];
			if(identifier.substring(0,identifier.indexOf("(")).toUpperCase().equals(name.toUpperCase())){
				return true;
			}
		}*/
		return false;
	}
	
	private ElementType get_type(String type){
		if(type.equals("FIELD")){return ElementType.ELEM_TYPE_FIELD; }
		else if(type.equals("METHOD") || type.equals("CONSTRUCTOR") || type.equals("INITIALIZER")){return ElementType.ELEM_TYPE_METHOD; }
		else if(type.equals("CLASS")){return ElementType.ELEM_TYPE_CLASS; }
		else if(type.equals("PACKAGE")){return ElementType.ELEM_TYPE_PACKAGE; }
		else{ return null;}
	}
	
	public ArrayList<Reference> get_crg_from_csv(String file_name, String language){
		BufferedReader reader = null;
		String comma = ",";
		String line = "";
		ArrayList<Reference> ref_list = new ArrayList<Reference>();
		ArrayList<String> error_log = new ArrayList<String>();
		
		String path = file_name.substring(0,file_name.lastIndexOf('/')+1);
		String only_file_name = file_name.substring(file_name.lastIndexOf('/')+1);
		
		try {
			reader = new BufferedReader(new FileReader(file_name));
			//send first line (header) for check
			if(crg_csv_sanity_check(reader.readLine())){
				int line_number = 1;
				while ((line = reader.readLine()) != null) {
					String[] fields = line.split(comma);
					if(fields.length == number_fields_crg_file){
						Reference ref = new Reference(
								fields[call_from_column], 
								fields[call_to_column], 
								mapElementType(fields[call_from_type_column], language), 
								mapElementType(fields[call_to_type_column], language),
								fields[call_from_mod_column], 
								fields[call_to_mod_column], 
								fields[call_from_code_column], 
								fields[call_to_code_column], 
								fields[call_from_isterminal_column].trim().toUpperCase().equals("TRUE"), 
								fields[call_to_isterminal_column].trim().toUpperCase().equals("TRUE")
							);
						if(ref.isThisValid()){
							if(!ref_list.contains(ref)){
								ref_list.add(ref);
							}
						}
						else{
							error_log.add("CRG file, invalid data in line: " + line_number);
						}
					}
					else{
						error_log.add("CRG file, not enough fields in line: " + line_number);
					}
					
					line_number++;
				}
			}else{
				error_log.add("CRG file, sanity check failed.");
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
			if(!error_log.isEmpty()){
				output.output_log_to_txt_file(error_log, "Errors_CRG", "Errors on CRG file", JOptionPane.WARNING_MESSAGE);
			}
		}
		return ref_list;
	}
	
	private ElementType mapElementType(String type, String language){
		switch(language){
			case GAML_LANGUAGE:
			GAML_Adapter gaml_adapter = new GAML_Adapter();
			return gaml_adapter.mapGAMLTypeToJavaType(type);
		}
		
		return null;
	}
	
	private boolean crg_csv_sanity_check(String header){
		String[] fields = header.split(comma);
		return(fields.length == number_fields_crg_file
				&& fields[call_from_column].trim().toLowerCase().equals(call_from_name) 
				&& fields[call_to_column].trim().toLowerCase().equals(call_to_name)
				&& fields[call_from_type_column].trim().toLowerCase().equals(call_from_type_name) 
				&& fields[call_to_type_column].trim().toLowerCase().equals(call_to_type_name)
				&& fields[call_from_mod_column].trim().toLowerCase().equals(call_from_mod_name) 
				&& fields[call_to_mod_column].trim().toLowerCase().equals(call_to_mod_name)
				&& fields[call_from_code_column].trim().toLowerCase().equals(call_from_code_name)
				&& fields[call_to_code_column].trim().toLowerCase().equals(call_to_code_name)
				&& fields[call_from_isterminal_column].trim().toLowerCase().equals(call_from_isterminal_name) 
				&& fields[call_to_isterminal_column].trim().toLowerCase().equals(call_to_isterminal_name)
			);
	}
	
}
