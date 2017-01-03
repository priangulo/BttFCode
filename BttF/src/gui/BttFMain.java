package gui;


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.SystemColor;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.LayoutStyle;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.MatteBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.eclipse.swt.widgets.DirectoryDialog;

import adapter.AnnotationElementAdapter;
import annotation.AnnotationElement;
import bttf.DBStorage;
import bttf.Element;
import bttf.ElementType;
import bttf.Fact;
import bttf.FactInference;
import bttf.Feature;
import bttf.FeatureBoundsWExp;
import bttf.Partition;
import bttf.Reference;
import errors.InvalidFeatureBounds;
import app.AnnotationMain;


/**
 * @author Priscila Angulo
 */
public class BttFMain extends JFrame {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Font buttonFont = new Font("Tahoma", Font.PLAIN, 11);
	private Font buttonFont_bold = new Font("Tahoma", Font.BOLD, 11);
	
	private final String SKIP_TEXT = "Skip";
	private final String TASK_REGEX = "\\w+ : (\\w+(( \\+ )|( ?)))+";
	//private final String BASE_TASK_REGEX = "\\w+ : (Base|BASE) (\\w+(( \\+ )|( ?)))+";
	//private final String RECURSIVE_TASK_REGEX = "\\w+ : (\\w+(( \\+ )|( ?)))+";
	private final String ERROR_TASK_FORMAT = "Error: Wrong task text format.";
	private final String ERROR_INVALID_FEATURES = "Invalid Features. Read the instructions";
	private final String PRIV_ENDFIX = "-priv";
	private final String PUB_ENDFIX = "-pub";
	private final String FWPI_ACTION = "PROGRAM : FRAMEWORK PLUGIN";
	private final String CSV_EXTENSION = ".csv"; 
	private final String BTTF_EXTENSION = ".bttf";
	
	private String start_path = System.getProperty("user.home") + "\\Desktop"; 
	
	private String csv_file = null;
	private String fm_file = null;
	
	private DefaultListModel<String> tasks_listmodel = new DefaultListModel<String>();
	private ArrayList<String> partition_names;
	private ButtonGroup bg_partitions;
	private ArrayList<JButton> bt_partitions_list = new ArrayList<JButton>();
	private ArrayList<Element> elements_skiped = new ArrayList<Element>();
	private Partition partition;
	private Element current_element;
	private ArrayList<FactInference> factsInferences = new ArrayList<FactInference>();
	private boolean cycle_same_feature;
	private boolean is_fwpi = false;
	private OutputFile outputFile;
	private InputFile inputFile;
	private DBAccess dbaccess;
	private Feature parent_feature = null;
	private boolean recursive_partition = false;
	private String project_path;
	private String project_name;
	private ProgressBar pb;
	
	/*SETTINGS*/
	private boolean usedb = false;
	private boolean cycle_stuff_on = false;
	private boolean standalone = false;
	
	
	public BttFMain() {
		JOptionPane.showMessageDialog(this.getContentPane(), "There is no open project in Project Explorer.", "No open project.", JOptionPane.ERROR_MESSAGE);		   
	}
	
	public BttFMain(boolean standalone) {
		this.standalone = standalone;
		initComponents();
		this.setTitle("Back to the Future - Standalone");
		
		//get crg file
		String file_name = (getFileFromFileDialog(CSV_EXTENSION, "CRG required. "));
		if(file_name != null){
			this.project_name = getProjectNameFromFileName(file_name);
			this.outputFile = OutputFile.OutputFileInstance(this, this.project_name, this.partition);
			this.inputFile = new InputFile(this, null, this.project_name);
			this.set_StartPathDir();
			//get references from file
			ArrayList<Reference> ref_list = inputFile.get_crg_from_csv(file_name, inputFile.GAML_LANGUAGE);
			if(ref_list != null && !ref_list.isEmpty()){
				Partition partition = new Partition(ref_list, getProjectNameFromFileName(file_name));
				start_partitioning(partition);
			}
		}
		else{
			JOptionPane.showMessageDialog(this.getContentPane(), "Invalid CRG file.", "BttF. Invalid CRG file.", JOptionPane.ERROR_MESSAGE);
		}
		
		
	}
	
	public BttFMain(String project_path, String project_name) {
		this.project_path = project_path;
		this.project_name = project_name;
		this.outputFile = OutputFile.OutputFileInstance(this, this.project_name, this.partition);
		this.set_StartPathDir();
		initComponents();
		pb = ProgressBar.StartProgressBar(this);   
	}
	
	public void close(){
		this.dispose();
	}
	
	public void update_progress(int howFar, String progress_text, Boolean error){
		if(error){
			ArrayList<String> error_log = new ArrayList<String>();
			error_log.add(progress_text);
			outputFile.output_log_to_txt_file(error_log, "Error_AST", "Error getting CRG from ASTs", JOptionPane.ERROR_MESSAGE);
			pb.dispose();
			this.dispose();
		}else{
			pb.setProgress(howFar);
			pb.setProgressText(progress_text);
		}
	}
	
	private void set_StartPathDir(){
		String temp_start_path = this.start_path + "\\BttF";
		File start_path_dir = new File(this.start_path);
		if(start_path_dir.exists() && start_path_dir.isDirectory()){
			this.start_path = temp_start_path;
			
			temp_start_path = this.start_path + "\\" + this.project_name;
			start_path_dir = new File(this.start_path);
			if(start_path_dir.exists() && start_path_dir.isDirectory()){
				this.start_path = temp_start_path;
			}
		}
	}
	
	public void start_partitioning(Partition partition){
		bt_uploadcvs.setEnabled(false);
		this.partition = partition;
		this.project_name = partition.get_project_name();
		inputFile = new InputFile(this, this.partition, this.project_name);
		dbaccess = new DBAccess(this);
		
		//ask for Framework-Plugin refactoring
		int answer = JOptionPane.showConfirmDialog(this.getContentPane(), "Do you want to partition into Framework and Plugin?", "Partition type.", JOptionPane.YES_NO_OPTION);
		if(answer == JOptionPane.YES_OPTION){
			is_fwpi = true;
			tabbedPane.remove(tabbedPane.indexOfTab("Tasks"));
			load_partitionpanel(FWPI_ACTION, false, null);
		}
		//no FW+Pi, then it is SPL, ask for feature model file
		else{
			JFileChooser fc = new JFileChooser(this.start_path);
			fc.setDialogTitle("Select BttF's state folder...");
			fc.setVisible(true);
			fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			int returnVal = fc.showOpenDialog(this);
			if(returnVal == JFileChooser.APPROVE_OPTION) {
			    File dir = fc.getSelectedFile();
			    for (final File fileEntry : dir.listFiles()) {
			        if (!fileEntry.isDirectory()) {
			            if(csv_file == null && fileEntry.getName().endsWith(CSV_EXTENSION)){
			            	csv_file = fileEntry.getAbsolutePath();
			            }
			            if(fm_file == null && fileEntry.getName().endsWith(BTTF_EXTENSION)){
			            	fm_file = fileEntry.getAbsolutePath();
			            }
			        }
			    }
			}
			
			if(fm_file != null && csv_file != null ){
				boolean correct = false;
				if(fm_file != null){
					ArrayList<String> tasks = inputFile.get_featuremodel_from_bttffile(fm_file);
					if(tasks != null){
						boolean recursive = false;
						for(String task : tasks){
							correct = validate_and_add_task(task, recursive);
							recursive = true;
						}
						if(tasks.size() > 0 && correct && csv_file != null){
							correct = validate_and_process_task(tasks.get(0), 0, csv_file);
						}
					}
				}
				if(!correct){
					JOptionPane.showMessageDialog(this.getContentPane(), "Invalid feature model contents.", "Invalid feature model.", JOptionPane.WARNING_MESSAGE);
				}
			}
			else{
				JOptionPane.showMessageDialog(this.getContentPane(), "Required files not found.", "No files to upload.", JOptionPane.WARNING_MESSAGE);
			}
		}
		
		this.setTitle(this.getTitle() + " - " + this.project_name);
		this.setVisible(true);
		bt_refactor.setVisible(!standalone && is_fwpi);
		bt_annotate.setVisible(!standalone && !is_fwpi);
	}
	
	public void setCycle_same_feature(boolean cycle_same_feature) {
		this.cycle_same_feature = cycle_same_feature;
	}
	

	private String getProjectNameFromFileName(String file_name){
		int idxSlash = file_name.lastIndexOf('\\');
		int idxDot = file_name.lastIndexOf('.');
		if (idxSlash != -1 && idxDot != -1 && idxSlash < idxDot){
			return file_name.substring(idxSlash+1, idxDot);
		}
		return "";
	}
	/*
	 * add a task button
	 */
	private void bt_addTaskMouseClicked(MouseEvent e) {
		boolean recursive = false;
		
		if(ls_tasksList.getModel().getSize() > 0){ //recursive tasks being added
			recursive = true;
		}
		System.out.println("recursive: " + recursive);
		
		boolean correct = validate_and_add_task(tx_addTask.getText(), recursive);
		if(correct){
			tx_addTask.setText("");
		}
	}
	
	private boolean validate_and_add_task(String task, boolean recursive){
		//if empty or repeated returns false
		if(!task.isEmpty() 
				&& !tasks_listmodel.contains(task.trim().toUpperCase())
				&& check_task_regex(task, recursive) ){
			if(
					(recursive && check_feature_subpartition(task, -1, false))
					|| !recursive
				){
					tasks_listmodel.addElement(task.trim().toUpperCase());
					return true;
				}
				else{JOptionPane.showMessageDialog(this.getContentPane(), ERROR_INVALID_FEATURES, "Error", JOptionPane.ERROR_MESSAGE);}
		}
		else{JOptionPane.showMessageDialog(this.getContentPane(), ERROR_TASK_FORMAT, "Error", JOptionPane.ERROR_MESSAGE);}
		return false;
	}
	
	/*
	 * process an action button
	 */
	private void bt_processActionMouseClicked(MouseEvent e){
		if(ls_tasksList.getSelectedValue() != null){
			String task = ls_tasksList.getSelectedValue().toString();
			int task_no = ls_tasksList.getSelectedIndex();
			boolean correct = validate_and_process_task(task, task_no, null);
		}
	}
	
	private boolean validate_and_process_task(String task, int task_no, String file_name){
		if(task_no == 0){ //first task ever
			if(check_task_regex(task, false)){
				recursive_partition = false;
				load_partitionpanel(task, false, file_name);
				return true;
			}
			else{JOptionPane.showMessageDialog(this.getContentPane(), ERROR_TASK_FORMAT, "Error", JOptionPane.ERROR_MESSAGE);}
		}
		else{ //a recursive partition!! D:
			//0-check partition state is finished (all elements classified)
			//1-check task regex A : X Y
			//2-check parent feature is not a parent element in existing tasks
			//3-check that parent feature exists in partition's feature model
			//4-check that new partitions don't exist as features in current feature model
			if( partition != null && partition.is_partition_finished()){
				if(check_task_regex(task, true)){
					if(check_feature_subpartition(task, task_no, true)){
						recursive_partition = true;
						load_partitionpanel(task, true, file_name);
						return true;
					}
					else{JOptionPane.showMessageDialog(this.getContentPane(), ERROR_INVALID_FEATURES, "Error", JOptionPane.ERROR_MESSAGE);}
				}
				else{JOptionPane.showMessageDialog(this.getContentPane(), ERROR_TASK_FORMAT, "Error", JOptionPane.ERROR_MESSAGE);}
			}
			else{JOptionPane.showMessageDialog(this.getContentPane(), "To process a recursive partition all the elements need to be classified first.", "Error", JOptionPane.ERROR_MESSAGE);}
		}
		return false;
	}
	
	/*
	 * annotate code method
	 */
	private void bt_annotate(MouseEvent e){
		if(bt_annotate.isEnabled()){
			AnnotationElementAdapter adapter = new AnnotationElementAdapter();
			AnnotationMain annotationMain = new AnnotationMain();
			
			ArrayList<AnnotationElement> annotation_elements = adapter.adaptElementList_to_AnnotationElementList(partition.get_elements());
		
			String result = annotationMain.annotate_java(project_path, annotation_elements);
			JOptionPane.showMessageDialog(this.getContentPane(), result, "Annotation result", JOptionPane.INFORMATION_MESSAGE);
		}
	}
	
	
	
	
	private String get_task_parent_name(String task){
		return task.substring(0,task.indexOf(":")).trim();
	}
	
	private String[] get_task_partitions(String task){
		String partitions_names = task.substring(task.indexOf(":")+2);
		return partitions_names.split(" ");
	}
	
	private boolean check_task_regex(String task, boolean recursive){
		return task.matches(TASK_REGEX);
	}
	
	/*
	 * 1 - Check parent feature is not a parent element in existing tasks
	 * 2 - Check that parent feature exists in partition's feature model
	 * 3 - Check that new partitions don't exist as features in current feature model 
	 */
	private boolean check_feature_subpartition(String task, int task_no, boolean process){
		//Boolean correct = true;
		String new_task_parent = get_task_parent_name(task.toUpperCase());
		String[] new_task_partitions = get_task_partitions(task.toUpperCase());
		
		//1 - Check parent feature is not a parent element in existing tasks, a feature can be subpartitioned only once
		for(int i = 0; i < tasks_listmodel.size(); i++){
			if( i != task_no && new_task_parent.equals(get_task_parent_name(tasks_listmodel.elementAt(i)))){
				return false;
			}
		}
		
		//check the real deal
		if(process){
			//2 - Check that parent feature exists in existing features
			if( partition != null 
					&& partition.get_all_features() != null
					&& partition.get_all_features().size() > 0
					&& partition.get_all_features()
						.stream().map(f -> f.getFeature_name()).collect(Collectors.toList()).contains(new_task_parent)
			){
				//3 - Check that new partitions don't exist as features in current feature model
				for(String new_partition : new_task_partitions){
					if( partition.get_all_features()
							.stream().map(f -> f.getFeature_name()).collect(Collectors.toList())
							.contains(new_partition)
					){
						return false;
					}
				}
				return true; //HITS TRUE ONLY WITH ALL CHECKS PASSED
			}
		}
		//check only syntax
		else{
			//2 - Check that parent feature exists in existing features
			boolean parent_found_in_feature_model = false;
			boolean newpartition_repeated = false;
			//3 - Check that new partitions don't exist as features in current feature model
			for(int i = 0; i < tasks_listmodel.size(); i++){
				String[] current_partitions = get_task_partitions(tasks_listmodel.elementAt(i));
				for(int j = 0; j < current_partitions.length; j++){
					if(current_partitions[j].equals(new_task_parent)){
						parent_found_in_feature_model = true;
					}
					for(int k = 0; k < new_task_partitions.length; k++){
						if(current_partitions[j].equals(new_task_partitions[k])){
							newpartition_repeated = true;
						}
					}
				}
			}
			if(parent_found_in_feature_model && !newpartition_repeated){
				return true;
			}
		}
		return false;
	}
	
	
	private void reload_partitionpanel(){
		
		String file_name = (getFileFromFileDialog(CSV_EXTENSION, ""));
		if(file_name != null){
			inputFile.get_elements_from_csv_file(file_name, partition_names, true);
			//System.out.println(partition.get_elements_from_file().toString());
		}
		
		display_next_element();
		tabbedPane.setSelectedIndex(tabbedPane.indexOfTab("Partitions"));
	}
	
	private ArrayList<String> getJListElements(JList jlist){
		ArrayList<String> allElements = new ArrayList<String>();
		
		if(jlist.getModel() != null && jlist.getModel().getSize() > 0){
			for(int i = 0; i < jlist.getModel().getSize(); i++){
				allElements.add(jlist.getModel().getElementAt(i).toString());
			}
		}
		
		return allElements;
	}
	
	
	private void load_partitionpanel(String task, boolean recursive, String file_name){
		clean_current_action();
		ta_task.setText(task);
		partition_names = create_partition_buttons(task);
		System.out.println(panel4.getSize().toString());
		System.out.println(ta_task.getSize().toString());
		
		if(recursive){
			parent_feature = partition.partitionHelper.get_feature_by_name(get_task_parent_name(task));
		}
		
		partition.set_featureModel(getJListElements(ls_tasksList), partition_names, task, recursive, parent_feature, is_fwpi, cycle_stuff_on);
		
		if(cycle_stuff_on && partition.getCycle_list() != null && partition.getCycle_list().size() > 0){
			CycleElements cycle_elem_frame = new CycleElements(this, partition.getCycle_list());
			this.setVisible(false);
			cycle_elem_frame.setVisible(true);
		}
		
		if(usedb){
			this.partition = dbaccess.getPartitionDataFromDB(this.partition);
		}
		
		//ask for input file
		if(file_name == null){
			int answer = JOptionPane.showConfirmDialog(this.getContentPane(), "Do you want to upload a csv facts file?", "Input file.", JOptionPane.YES_NO_OPTION);
			if(answer == JOptionPane.YES_OPTION){
				file_name = (getFileFromFileDialog(CSV_EXTENSION, ""));			
			}
		}
		
		if(file_name != null){
			inputFile.get_elements_from_csv_file(file_name, partition_names, false);
			//System.out.println(partition.get_elements_from_file().toString());
		}
		
		display_next_element();
		tabbedPane.setSelectedIndex(tabbedPane.indexOfTab("Partitions"));
		bt_uploadcvs.setEnabled(true);
	}
		
	private String getFileFromFileDialog(String extension, String message){
		FileDialog fd = new FileDialog(this, message + "Choose a " + extension + " file", FileDialog.LOAD);
		fd.setDirectory(this.start_path);
		fd.setFile("*" + extension);
		fd.setVisible(true);
		String file_name = fd.getDirectory() + "\\" + fd.getFile();
		if (file_name != null && file_name.endsWith(extension)){
			return file_name;
		}
		return null;
	}
	
	
	/*
	 * delete an action button
	 */
	private void bt_deleteActionMouseClicked(MouseEvent e){
		if(!ls_tasksList.isSelectionEmpty()){
			if(ta_task.getText().equals(ls_tasksList.getSelectedValue())){
				clean_current_action();
			}
			tasks_listmodel.remove(ls_tasksList.getSelectedIndex());
		}
	}
	
	/* 
	 * click partition button  
	 */	
	private void partitionButtonClicked(MouseEvent e, JButton button, String part_name, Boolean is_fprivate){
		if(button.isEnabled()){
			if(part_name.equals(SKIP_TEXT)){
				elements_skiped.add(current_element);
			}
			
			else{
				Feature feature = partition.partitionHelper.get_feature_by_name(part_name);			
				if(elements_skiped.contains(current_element)){
					elements_skiped.remove(current_element);
				}
				
				partition.add_element_to_feature_gui(feature, current_element, is_fprivate, this.parent_feature);
				refresh_facts();
			}
			display_next_element();
			
		}
	}
	
	/*
	 * Enable delete fact button only when it is a fact
	 */
	private void ls_factsValueChanged(ListSelectionEvent e){
		String fact = ls_facts.getSelectedValue();
		if (fact != null && !fact.isEmpty()){
			FactInference fi = getFactInfwithText(fact);
			if(fi.isFact()){
				bt_deleteFact.setEnabled(true);
			}
			else{
				bt_deleteFact.setEnabled(false);
			}
		}
	}
	
	/*
	 * Deletes a fact and consequent facts
	 */
	private void bt_deleteFactMouseClicked(MouseEvent e){
		if(bt_deleteFact.isEnabled()){
			String fact = ls_facts.getSelectedValue();
			
			if (fact != null && !fact.isEmpty()){
				FactInference fi = getFactInfwithText(fact);
				if(fi.isFact()){
					int answer = JOptionPane.showConfirmDialog(this.getContentPane(), "Deleting a fact will also delete consequent facts, do you want to continue?", "Are you sure?", JOptionPane.YES_NO_OPTION);
					if(answer == JOptionPane.YES_OPTION){
						partition.delete_fact(fact);
						refresh_facts();
						display_next_element();
					}
				}
			}
		}
	}
	
	
	/*
	 * Sorts the list of facts
	 */
	
	private void bt_sortMouseClicked(MouseEvent e){
		factsInferences = partition.partitionHelper.get_flatFacts();
		Collections.sort(factsInferences, new Comparator<FactInference>() {
		    @Override
		    public int compare(FactInference f1, FactInference f2) {
		    	if(f1 != null && f2 != null && f1.getElement() != null && f2.getElement() != null){
		    		return f1.getElement().getIdentifier().compareTo(f2.getElement().getIdentifier());
		    	}
		    	return 0;
		    }
		});
		
		List<String> facts_list = factsInferences.stream().map(f -> f.getText()).collect(Collectors.toList());
		ls_facts.setListData((String[]) facts_list.toArray(new String[0]));
		//ls_inferences.setListData(new String[0]);
	}
	
	/*
	 * Opens the more info window for an element
	 */
	private void bt_moreInfoMouseClicked(MouseEvent e, String fact_text){
		if(fact_text != null){
			FactInference fact = getFactInfwithText(fact_text);
			System.out.println(fact.toString());
			if(fact != null && fact.getElement() != null){
				DeclarationMoreInfo more_info_fram = new DeclarationMoreInfo(fact.getElement());
				more_info_fram.setVisible(true);
			}
			else{
				JOptionPane.showMessageDialog(this.getContentPane(), "Declaration in fact not found.", "No declaration found.", JOptionPane.WARNING_MESSAGE);
			}
		}
		else if(current_element != null){
			DeclarationMoreInfo more_info_fram = new DeclarationMoreInfo(current_element);
			more_info_fram.setVisible(true);
		}
	}
	
	
	private FactInference getFactInfwithText(String fact_text){
		if(factsInferences != null && factsInferences.size() > 0){
			try{
				return factsInferences.stream()
					.filter(f -> f.getText().equals(fact_text))
					.collect(Collectors.toList())
					.get(0);
			}catch(IndexOutOfBoundsException ex){
				return null;
			}
		}
		return null;
	}
	
	
	/*
	 * Saves current state in DB
	 */
	private void bt_saveindbMouseClicked(MouseEvent e){
		if(usedb && factsInferences != null && factsInferences.size() > 0){
			boolean correct = false;
			try {
				correct = dbaccess.saveStateInDB(this.partition);
				if(correct){
					JOptionPane.showMessageDialog(this.getContentPane(), "Current status saved.", "Status Saved", JOptionPane.INFORMATION_MESSAGE);
				}
				else{
					JOptionPane.showMessageDialog(this.getContentPane(), "Error saving current status.", "Error on saving", JOptionPane.ERROR_MESSAGE);
				}
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
		}
		else{
			JOptionPane.showMessageDialog(this.getContentPane(), "No declarations have been classified yet. There is nothing to save.", "Nothing to save", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	/*
	 * Saves current state in file
	 */
	private void bt_saveinfileMouseClicked(MouseEvent e){
		outputFile.save_elements_file(partition);
	}
	
	/*
	 * Re-uploads csv file
	 */
	private void bt_uploadcvsMouseClicked (MouseEvent e){
		if(bt_uploadcvs.isEnabled()){
			reload_partitionpanel();
		}
	}
		
	/*
	 * Resets partitions tab 
	 */
	private void clean_current_action(){
		ta_task.setText("");
		bt_partitions_list = new ArrayList<JButton>();
		bg_partitions = new ButtonGroup();
		pn_partitionButtons.removeAll();
	}
	
	/*
	 * Displays buttons for partitions
	 */
	private ArrayList<String> create_partition_buttons(String task){
		String[] partitions = get_task_partitions(task);
		ArrayList<String> no_duplicate_parts = new ArrayList<String>();
		
		int i = 0;
		for (String part : partitions){
			if(!no_duplicate_parts.contains(part)){
				no_duplicate_parts.add(part);
				
				//create feature private button
				create_partition_button("bt_partition_priv_" + part, i + "-" + part + PRIV_ENDFIX, part, true);
				//create feature public button
				create_partition_button("bt_partition_pub_" + part, i + "-" + part + PUB_ENDFIX, part, false);
				
				i++;
			}
		}
		//create skip button
		create_partition_button("bt_skip", SKIP_TEXT, SKIP_TEXT, false);
		
		for(Component c : pn_partitionButtons.getComponents()){
			/*if(!pn_partitionButtons.contains(c.getLocationOnScreen())){
				Dimension newdim = new Dimension(pn_partitionButtons.getPreferredSize().width, pn_partitionButtons.getPreferredSize().height + 35);
				pn_partitionButtons.setPreferredSize(newdim);
				scrollPane5.revalidate();
			}*/
			
			if(!c.isShowing()){
				Dimension newdim = new Dimension(pn_partitionButtons.getPreferredSize().width, pn_partitionButtons.getPreferredSize().height + 15);
				pn_partitionButtons.setPreferredSize(newdim);
				pn_partitionButtons.revalidate();
				scrollPane5.revalidate();
			}
		}
		
		return no_duplicate_parts;
	}	
	
	/*
	 * creates a button for the partition buttons area 
	 */
	private void create_partition_button(String name, String text, String partition_name, Boolean fprivate){
		JButton button = new JButton();
		button.setFont(buttonFont);
		button.setPreferredSize(new Dimension(180, 35));
		button.setName(name);
		button.setText(text);				
		button.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				partitionButtonClicked(e, button, partition_name, fprivate);
			}
		});
		bt_partitions_list.add(button);
		bg_partitions.add(button);
		pn_partitionButtons.add(button);

	}
	
	/*
	 * Displays next element to be classified
	 * Simplified version
	 */
	private void display_next_element(){
		String explanations_text = "";
		ArrayList<Element> elements_to_classify = partition.get_next_elems_to_classify(this.parent_feature);
		
		int count_pendingelems = partition.get_elements().stream().filter(e -> e.getFeature() == parent_feature).collect(Collectors.toList()).size();
		//int count_facts = partition.get_facts().size();
		long count_facts = partition.get_facts().stream().map(f -> f.getElement()).distinct().count();
		long count_inferences = partition.get_elements().size() - count_pendingelems - count_facts; 
		lb_countelements.setText("#Declarations: " + partition.get_elements().size() + "  #Pending: " + count_pendingelems + "  #Facts: " + count_facts + "  #Inferences: " + count_inferences);
		
		//no more elements to classify
		if(elements_to_classify.isEmpty()){
			bt_annotate.setEnabled(true);
			current_element = null;
			ta_element.setText("");
			JOptionPane.showMessageDialog(this.getContentPane(), "No more elements to classify.", "Done!", JOptionPane.INFORMATION_MESSAGE);
			refresh_buttons(get_allOff_option_buttons(), false);
		}
		//we still have elements to classify
		else{
			bt_annotate.setEnabled(false);
			
			if (elements_skiped.containsAll(elements_to_classify)){
				JOptionPane.showMessageDialog(this.getContentPane(), "No more elements to skip.", "Make a decision...", JOptionPane.INFORMATION_MESSAGE);
				elements_skiped.clear();
			}
			
			for(Element elem : elements_to_classify){
				if(!elements_skiped.contains(elem)){
					current_element = elem;
					ta_element.setText(current_element.displayElement());
					explanations_text = set_element_options(current_element);
					break;
				}
			}
		}
		ta_explanations.setText(explanations_text);
		ta_explanations.revalidate();
		refresh_facts();
	}
	
	
	private String set_element_options(Element current_element){
		String explanations_text = "";
		
		try{
			//get feature bounds
			FeatureBoundsWExp bounds = partition.boundsCalc.get_element_feature_bounds(current_element, this.parent_feature);
			if (bounds != null && bounds.getFeatureRange() != null){
				ArrayList<Feature> feature_options = (ArrayList<Feature>) bounds.getFeatureRange();
			
				boolean reduced_options = (feature_options.size() < partition.partitionHelper.get_features_in_task().size());
				if(reduced_options){
					explanations_text = explanations_text + "Available feature options are calculated "
							+ "based on this declaration's feature bounds. ";
				}
				
				if(bounds.getEb_explanation() != null && !bounds.getEb_explanation().isEmpty()){
					explanations_text = explanations_text + bounds.getEb_explanation() + ". ";
					//System.out.println(bounds.getEb_explanation());
				}
				
				if(bounds.getLb_explanation()!= null && !bounds.getLb_explanation().isEmpty()){
					explanations_text = explanations_text + bounds.getLb_explanation() + ". ";
					//System.out.println(bounds.getLb_explanation());
				}
				
				//get RefTo
				ArrayList<Feature> refToFeatures = current_element.getRefToFeatures();
				ArrayList<Feature> layeredRefToFeatures = current_element.getLayeredRefToFeatures();
				ArrayList<Feature> nonLayeredRefToFeatures = current_element.getNonLayeredRefToFeatures();
				
				ArrayList<Element> assignedLayered = (ArrayList<Element>) current_element.getLayeredRefToThis().stream()
						.filter(e -> e.getFeature() != null && e.getFeature() != this.parent_feature)
						.collect(Collectors.toList());
				
				ArrayList<Element> assignedNonLayered = (ArrayList<Element>) current_element.getNonLayeredRefToThis().stream()
						.filter(e -> e.getFeature() != null && e.getFeature() != this.parent_feature)
						.collect(Collectors.toList());
				
				ArrayList<Element> assignedRefTo = (ArrayList<Element>) current_element.getRefToThis().stream()
						.filter(e -> e.getFeature() != null && e.getFeature() != this.parent_feature)
						.collect(Collectors.toList());
				
				
				/*
				 * is a recursive task
				 * and the declaration has a modifier
				 * and the declaration DOES NOT belong a fake latest feature
				 */
				//SCENARIO#RECURSIVE
				if(recursive_partition 
					&& (current_element.isIs_fPrivate() || current_element.isIs_fPublic())
					&& (current_element.getFeature() == null 
						|| (current_element.getFeature() != null && !current_element.getFeature().getWas_last_feature()) 
						)
					){
					ArrayList<OptionButton> button_options = new ArrayList<OptionButton>();
					for(Feature f : feature_options){
						button_options.add(new OptionButton(f.getFeature_name(), current_element.isIs_fPrivate(), current_element.isIs_fPublic(), partition.partitionHelper.is_same_than_container_feature(current_element,f)));
					}
					refresh_buttons(button_options, true);
					explanations_text = explanations_text + "Since this is a RECURSIVE TASK, the original modifier"
							+ " fprivate/fpublic remains. ";
				}
				//NORMAL CASES
				else 
				{
					ArrayList<OptionButton> button_options = new ArrayList<OptionButton>();
					//latest_feature is omega. Omega is the last feature on the first partition task,
					//or the last child of the last feature
					int latest_feature_order = partition.partitionHelper.get_latest_feature_in_task();
	
					//CASE#0 if omega is in bounds then e can only be fprivate of it, not public
					//CASE#A a declaration can be fpublic of any feature in bounds
					for(Feature f : feature_options){
						if(f.getOrder() == latest_feature_order){
							button_options.add(new OptionButton(f.getFeature_name(), true, false, partition.partitionHelper.is_same_than_container_feature(current_element,f)));
						}
						else{	
							button_options.add(new OptionButton(f.getFeature_name(), false, true, partition.partitionHelper.is_same_than_container_feature(current_element,f)));
						}
					}
					
					//fprivate possibilities calculation only for non-hooks
					//if a method has been already identified as hook but its actual feature is pending
					//only offer fpublic opions, a hook cannot be fprivate
					if(!current_element.getElement_type().equals(ElementType.ELEM_TYPE_METHOD) ||
							(current_element.getElement_type().equals(ElementType.ELEM_TYPE_METHOD) && !current_element.isIs_hook())
						){
					
						//CASE#B1 If none of the declarations in RefTo(e) has been assigned,
						// e can be fprivate of any feature in bounds
						if( assignedRefTo == null || ( assignedRefTo != null && assignedRefTo.size() == 0) ){
							for(Feature f : feature_options){
								OptionButton op = new OptionButton(f.getFeature_name(), true, false, partition.partitionHelper.is_same_than_container_feature(current_element,f));
								if(!button_options.contains(op)){
									button_options.add(op);
								}
							}
						}
						
						//CASE#B2 If only layered decs in RefTo(e) have been assigned, and all of them are assigned to the
						//same feature, and it is in bounds, then e can be fprivate of it
						if(assignedLayered != null && assignedLayered.containsAll(assignedRefTo)
								&& layeredRefToFeatures != null && layeredRefToFeatures.size() > 0
								&& layeredRefToFeatures.stream()
										.filter(f -> f != null && f != this.parent_feature)
										.distinct().collect(Collectors.toList()).size() == 1){
							Feature shared = layeredRefToFeatures.stream()
									.filter(f -> f != null && f != this.parent_feature)
									.distinct().collect(Collectors.toList()).get(0);
							if(shared != null && feature_options.contains(shared)){
								OptionButton op = new OptionButton(shared.getFeature_name(), true, false, partition.partitionHelper.is_same_than_container_feature(current_element,shared));
								if(!button_options.contains(op)){
									button_options.add(op);
								}
							}
						}
						
						//CASE#B3 If only methods in RefTo(e) have been assigned, and the latest of their features
						// is in bounds for e, then e can be fprivate of the range of features in bounds that are larger
						//or equal than it.
						if(assignedNonLayered != null && assignedNonLayered.containsAll(assignedRefTo) 
								&& nonLayeredRefToFeatures != null && nonLayeredRefToFeatures.size() > 0){
							Feature latest_of_nonlay = null;
							try{
								latest_of_nonlay = nonLayeredRefToFeatures.stream()
										.filter(f -> f != null && f != this.parent_feature)
										.max((f1, f2) -> Integer.compare(f1.getOrder(), f2.getOrder()))
										.get();
							}catch(NoSuchElementException ex){}
							if(latest_of_nonlay != null && feature_options.contains(latest_of_nonlay)){
								for(Feature f : feature_options ){
									if(f.getOrder() >= latest_of_nonlay.getOrder()){
										OptionButton op = new OptionButton(latest_of_nonlay.getFeature_name(), true, false, partition.partitionHelper.is_same_than_container_feature(current_element,latest_of_nonlay));
										if(!button_options.contains(op)){
											button_options.add(op);
										}
									}
								}
							}
						}
						
						//CASE#B4 There is a mixture of nonLay and lay declarations assigned. 
						// If all assigned lay decs share the same feature,
						// and that feature is at or after the latest of nonLay assigned decs
						// e can be fprivate of it
						if( assignedLayered != null && assignedLayered.size() > 0 
								&& assignedNonLayered != null && assignedNonLayered.size() > 0
								&& layeredRefToFeatures.stream()
									.filter(f -> f != null && f != this.parent_feature)
									.distinct().collect(Collectors.toList()).size() == 1
								){
							Feature shared = layeredRefToFeatures.stream()
									.filter(f -> f != null && f != this.parent_feature)
									.distinct().collect(Collectors.toList()).get(0);
							Feature latest_of_nonlay = null;
							try{
								latest_of_nonlay = nonLayeredRefToFeatures.stream()
										.filter(f -> f != null && f != this.parent_feature)
										.max((f1, f2) -> Integer.compare(f1.getOrder(), f2.getOrder()))
										.get();
							}catch(NoSuchElementException ex){}
							if(shared != null && latest_of_nonlay != null && shared.getOrder() >= latest_of_nonlay.getOrder() && feature_options.contains(shared)){
								OptionButton op = new OptionButton(shared.getFeature_name(), true, false, partition.partitionHelper.is_same_than_container_feature(current_element,shared));
								if(!button_options.contains(op)){
									button_options.add(op);
								}
							}
						}
					}
					refresh_buttons(button_options, true);
				}
			}
		}
		catch(InvalidFeatureBounds ex){
			JOptionPane.showMessageDialog(getContentPane(), "A contradiction was found in feature bounds for this declaration:\n" + ex.getMessage(), "Contradiction in feature bounds.", JOptionPane.WARNING_MESSAGE);
		}
		
		return explanations_text;
	}
	
	/*
	 * updates the list of facts on screen 
	 */
	private void refresh_facts(){
		factsInferences = partition.partitionHelper.get_flatFacts();
		List<String> facts_list = factsInferences.stream().map(f -> f.getText()).collect(Collectors.toList());
		Collections.reverse(facts_list);
		ls_facts.setListData((String[]) facts_list.toArray(new String[0]));
		//ls_inferences.setListData(new String[0]);
	}
	
	private void refresh_buttons(ArrayList<OptionButton> options, Boolean show_skip){
		for(JButton button : bt_partitions_list){
			button.setEnabled(false);
			button.setFont(buttonFont);
			
			if(button.getText().equals(SKIP_TEXT) && show_skip){
				button.setEnabled(true);
			}
		}
		
		//System.out.println("OPTIONS:\n" + options.toString());
		
		for(OptionButton option : options){
			for(JButton button : bt_partitions_list){
				if( 
					( option.getFpublic() && button.getText().endsWith("-" + option.getPartition_name() + PUB_ENDFIX))
					|| ( option.getFprivate() && button.getText().endsWith("-" + option.getPartition_name() + PRIV_ENDFIX))
					|| ( button.getText().equals(SKIP_TEXT) && option.getPartition_name().equals(SKIP_TEXT) ) 
				){
					button.setEnabled(true);
					if(option.getIs_container_feature()){
						//System.out.println(option.toString());
						button.setFont(buttonFont_bold);
					}
				}
				
			}
		}
	}
	
	private ArrayList<OptionButton> get_allOff_option_buttons(){
		ArrayList<OptionButton> options = new ArrayList<OptionButton>();
		for(String part : partition_names){
			options.add(new OptionButton(part, false, false, false));
		}
		return options;
	}
	
	private boolean less_option_buttons(){
		for(JButton button : bt_partitions_list){
			if( (button.getText().endsWith(PUB_ENDFIX) || button.getText().endsWith(PRIV_ENDFIX))
				&& !button.isEnabled() ){
				return true;
			}
		}
		return false;
	}
	
	private void initComponents() {
		tabbedPane = new JTabbedPane();
		pn_actions = new JPanel();
		lb_addAction = new JLabel();
		tx_addTask = new JTextField();
		bt_addAction = new JButton();
		lb_actionList = new JLabel();
		scrollPane1 = new JScrollPane();
		ls_tasksList = new JList();
		bt_deleteAction = new JButton();
		bt_processAction = new JButton();
		ta_instructions = new JTextArea();
		pn_partitions = new JPanel();
		panel2 = new JPanel();
		panel5 = new JPanel();
		panel4 = new JScrollPane();
		paneltasklabel = new JPanel();
		lb_currentAction = new JLabel();
		ta_task = new JTextPane();
		panel_countelements = new JPanel();
		lb_countelements = new JLabel();
		panel6 = new JPanel();
		lb_element = new JLabel();
		bt_sort = new JButton(); 
		bt_moreinfo = new JButton();
		scrollPane2 = new JScrollPane();
		ta_element = new JTextArea();
		panel3 = new JPanel();
		lb_facts = new JLabel();
		bt_deleteFact = new JButton();
		scrollPane3 = new JScrollPane();
		ls_facts = new JList();
		hSpacer1 = new JPanel(null);
		panel1 = new JPanel();
		panel8 = new JPanel();
		scrollPane5 = new JScrollPane();
		lb_partition = new JLabel();
		pn_partitionButtons = new JPanel();
		pn_explanations = new JPanel();
		ta_explanations = new JTextArea();
		panel9 = new JPanel();
		bt_uploadcvs = new JButton();
		bt_saveincvs = new JButton();
		bt_saveindb = new JButton();
		bt_annotate = new JButton();
		bt_refactor = new JButton();

		//======== this ========
		setTitle("Back to the Future");
		Container contentPane = getContentPane();

		//======== tabbedPane ========
		{

			//======== pn_actions ========
			{				
				//---- lb_addAction ----
				lb_addAction.setText("Add task:");

				//---- bt_addAction ----
				bt_addAction.setText("Add");

				//---- lb_actionList ----
				lb_actionList.setText("Tasks:");

				//======== scrollPane1 ========
				{

					//---- ls_actionList ----
					ls_tasksList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
					scrollPane1.setViewportView(ls_tasksList);
				}

				//---- bt_deleteAction ----
				bt_deleteAction.setText("Delete");

				//---- bt_processAction ----
				bt_processAction.setText("Process");

				//---- ta_instructions ----
				ta_instructions.setText(
						"\nInstructions for first/base task:"
						+ "\n\t* The expected format is P : A B C"
						+ "\n\t* Order the features as you would do if they were layers"
						+ "\n\n"
						+ "\nInstructions for recursive tasks:"
						+ "\n\t* The program has to be fully partitioned"
						+ "\n\t* The expected format is A : X Y"
						+ "\n\t* The feature to sub-partition should exist in the current feature model"
						+ "\n\t* A feature can be sub-partitioned only once"
						+ "\n\t* The sub-features should not be named as any existing feature"
						+ "\n\t* Order the sub-features as you would do if they were layers\n");
				ta_instructions.setFont(new Font("Tahoma", Font.PLAIN, 11));
				ta_instructions.setBackground(SystemColor.menu);
				ta_instructions.setEditable(false);

				GroupLayout pn_actionsLayout = new GroupLayout(pn_actions);
				pn_actions.setLayout(pn_actionsLayout);
				pn_actionsLayout.setHorizontalGroup(
					pn_actionsLayout.createParallelGroup()
						.addGroup(pn_actionsLayout.createSequentialGroup()
							.addContainerGap()
							.addGroup(pn_actionsLayout.createParallelGroup()
								.addGroup(pn_actionsLayout.createSequentialGroup()
									.addComponent(lb_actionList)
									.addContainerGap(1000, Short.MAX_VALUE))
								.addGroup(GroupLayout.Alignment.TRAILING, pn_actionsLayout.createSequentialGroup()
									.addGroup(pn_actionsLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
										.addGroup(pn_actionsLayout.createSequentialGroup()
											.addComponent(lb_addAction)
											.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
											.addComponent(tx_addTask, GroupLayout.DEFAULT_SIZE, 854, Short.MAX_VALUE)
											.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
											.addComponent(bt_addAction, GroupLayout.PREFERRED_SIZE, 85, GroupLayout.PREFERRED_SIZE))
										.addComponent(scrollPane1, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 1014, Short.MAX_VALUE)
										.addGroup(GroupLayout.Alignment.LEADING, pn_actionsLayout.createSequentialGroup()
											.addComponent(ta_instructions, GroupLayout.DEFAULT_SIZE, 862, Short.MAX_VALUE)
											.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
											.addComponent(bt_deleteAction)
											.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
											.addComponent(bt_processAction)))
									.addGap(25, 25, 25))))
				);
				pn_actionsLayout.setVerticalGroup(
					pn_actionsLayout.createParallelGroup()
						.addGroup(pn_actionsLayout.createSequentialGroup()
							.addContainerGap()
							.addGroup(pn_actionsLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
								.addComponent(lb_addAction)
								.addComponent(bt_addAction)
								.addComponent(tx_addTask, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
							.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
							.addComponent(lb_actionList)
							.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
							.addComponent(scrollPane1, GroupLayout.PREFERRED_SIZE, 251, GroupLayout.PREFERRED_SIZE)
							.addGap(18, 18, 18)
							.addGroup(pn_actionsLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
								.addComponent(ta_instructions)
								.addComponent(bt_deleteAction)
								.addComponent(bt_processAction))
							.addContainerGap(190, Short.MAX_VALUE))
				);
			}
			tabbedPane.addTab("Tasks", pn_actions);

			//======== pn_partitions ========
			{
				pn_partitions.setLayout(new BoxLayout(pn_partitions, BoxLayout.X_AXIS));

				//======== panel2 ========
				{
					panel2.setLayout(new BoxLayout(panel2, BoxLayout.Y_AXIS));

					//======== panel5 ========
					{
						panel5.setMinimumSize(new Dimension(145, 22));
						panel5.setLayout(new BoxLayout(panel5, BoxLayout.Y_AXIS));

						//======== paneltasklabel ========
						{
							paneltasklabel.setLayout(new FlowLayout(FlowLayout.LEFT));
							
							//---- lb_currentAction ----
							lb_currentAction.setAlignmentX(LEFT_ALIGNMENT);
							lb_currentAction.setText("Current Task:");
							paneltasklabel.add(lb_currentAction);
						}
						panel5.add(paneltasklabel);
						
						//======== panel4 ========
						{							
							//---- lb_action ----
							ta_task.setText(" ");
							ta_task.setFont(new Font("Tahoma", Font.BOLD, 11));
							ta_task.setEditable(false);
							ta_task.setMinimumSize(new Dimension(150, 5));
							ta_task.setPreferredSize(new Dimension(150, 10));
							ta_task.setLayout(new BoxLayout(ta_task, BoxLayout.X_AXIS));
							
							panel4.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
							panel4.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
							panel4.setViewportView(ta_task);
						}
						panel5.add(panel4);
						
						//======== panel_countelements ========
						{
							panel_countelements.setPreferredSize(new Dimension(105, 20));
							panel_countelements.setLayout(new FlowLayout(FlowLayout.LEFT));

							//---- lb_countelements ----
							lb_countelements.setText("");
							lb_countelements.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
							lb_countelements.setMinimumSize(new Dimension(200, 14));
							panel_countelements.add(lb_countelements);
						}
						panel5.add(panel_countelements);

						//======== panel6 ========
						{
							panel6.setPreferredSize(new Dimension(105, 35));
							panel6.setLayout(new FlowLayout(FlowLayout.LEFT, 2, 5));

							//---- lb_element ----
							lb_element.setText("Declaration to Assign:     ");
							lb_element.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
							lb_element.setPreferredSize(new Dimension(200, 35));
							panel6.add(lb_element);
							
							//---- bt_moreinfo ----
							bt_moreinfo.setText("More information");
							panel6.add(bt_moreinfo);
						}
						panel5.add(panel6);

						//======== scrollPane2 ========
						{
							scrollPane2.setPreferredSize(new Dimension(550, 100));

							//---- ta_element ----
							ta_element.setEditable(false);
							ta_element.setMinimumSize(new Dimension(4, 44));
							scrollPane2.setViewportView(ta_element);
						}
						panel5.add(scrollPane2);

						//======== panel3 ========
						{
							panel3.setMinimumSize(new Dimension(120, 30));
							panel3.setPreferredSize(new Dimension(170, 35));
							panel3.setOpaque(false);
							panel3.setLayout(new FlowLayout(FlowLayout.LEFT, 2, 5));

							//---- lb_facts ----
							lb_facts.setText("Assignments log:  ");
							lb_facts.setMinimumSize(new Dimension(105, 30));
							lb_facts.setPreferredSize(new Dimension(105, 35));
							lb_facts.setOpaque(true);
							panel3.add(lb_facts);
							
							//---- bt_sort ----
							bt_sort.setText("Sort");
							panel3.add(bt_sort);
							
							//---- bt_deleteFact ----
							bt_deleteFact.setText("Delete Fact");
							panel3.add(bt_deleteFact);
						}
						panel5.add(panel3);

						//======== scrollPane3 ========
						{
							scrollPane3.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
							scrollPane3.setPreferredSize(new Dimension(600, 340));
							scrollPane3.setViewportView(ls_facts);
						}
						panel5.add(scrollPane3);
					}
					panel2.add(panel5);
				}
				pn_partitions.add(panel2);
				pn_partitions.add(hSpacer1);

				//======== panel1 ========
				{
					panel1.setPreferredSize(new Dimension(500, 237));
					panel1.setRequestFocusEnabled(false);
					panel1.setLayout(new BoxLayout(panel1, BoxLayout.Y_AXIS));

					//======== panel8 ========
					{
						panel8.setOpaque(false);
						panel8.setPreferredSize(new Dimension(140, 18));
						panel8.setLayout(new FlowLayout());

						//---- lb_partition ----
						lb_partition.setText("To which partition belongs?");
						panel8.add(lb_partition);
					}
					panel1.add(panel8);

					
					//======== scrollPane5 ========
					{
						//======== pn_partitionButtons ========
						{
							pn_partitionButtons.setPreferredSize(new Dimension(400, 450));
							pn_partitionButtons.setMinimumSize(new Dimension(300, 300));
							pn_partitionButtons.setMaximumSize(new Dimension(450, 1500));
							
							pn_partitionButtons.setOpaque(false);
							pn_partitionButtons.setBorder(new MatteBorder(1, 1, 1, 1, Color.black));
							pn_partitionButtons.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 20));
						}
												
						scrollPane5.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
						scrollPane5.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
						scrollPane5.setViewportView(pn_partitionButtons);
					}
					panel1.add(scrollPane5);
					
					//======== pn_explanations ========
					{
						pn_explanations.setPreferredSize(new Dimension(400, 50));
						pn_explanations.setMinimumSize(new Dimension(300, 50));
						pn_explanations.setLayout(new BoxLayout(pn_explanations, BoxLayout.Y_AXIS));
						//pn_explanations.setOpaque(false);
						
						pn_explanations.setBackground(SystemColor.WHITE);
						//pn_explanations.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

						//---- ta_explanations ----
						ta_explanations.setText("");
						ta_explanations.setBackground(SystemColor.menu);
						ta_explanations.setEditable(false);
						ta_explanations.setLineWrap(true);
						ta_explanations.setWrapStyleWord(true);
						pn_explanations.add(ta_explanations);
					}
					panel1.add(pn_explanations);
					

					//======== panel9 ========
					{
						panel9.setPreferredSize(new Dimension(147, 70));
						panel9.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 30));
						
						//---- bt_uploadcvs ----
						bt_uploadcvs.setText("Upload State");
						panel9.add(bt_uploadcvs);
						
						//---- bt_saveincvs ----
						bt_saveincvs.setText("Save State");
						panel9.add(bt_saveincvs);
						
						//---- bt_annotate ----
						bt_annotate.setText("Annotate code");
						bt_annotate.setEnabled(false);
						panel9.add(bt_annotate);
						
						//---- bt_refactor ----
						bt_refactor.setText("Refactor code");
						bt_refactor.setEnabled(false);
						panel9.add(bt_refactor);
					}
					panel1.add(panel9);
				}
				pn_partitions.add(panel1);
			}
			tabbedPane.addTab("Partitions", pn_partitions);
		}

		GroupLayout contentPaneLayout = new GroupLayout(contentPane);
		contentPane.setLayout(contentPaneLayout);
		contentPaneLayout.setHorizontalGroup(
			contentPaneLayout.createParallelGroup()
				.addGroup(contentPaneLayout.createSequentialGroup()
					.addContainerGap()
					.addComponent(tabbedPane, GroupLayout.DEFAULT_SIZE, 1054, Short.MAX_VALUE)
					.addContainerGap())
		);
		contentPaneLayout.setVerticalGroup(
			contentPaneLayout.createParallelGroup()
				.addGroup(contentPaneLayout.createSequentialGroup()
					.addContainerGap()
					.addComponent(tabbedPane, GroupLayout.DEFAULT_SIZE, 660, Short.MAX_VALUE)
					.addContainerGap())
		);
		setSize(1090, 720);
		setLocationRelativeTo(getOwner());
		
		register_actions();
	}
	
	private void register_actions(){
		ls_tasksList.setModel(tasks_listmodel);
		
		tx_addTask.addKeyListener(new KeyAdapter(){
			public void keyPressed(KeyEvent e)
			{
				int key = e.getKeyCode();
				if (key == KeyEvent.VK_ENTER){
					bt_addTaskMouseClicked(null);
				}
			}
		});
		
		bt_addAction.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				bt_addTaskMouseClicked(e);
			}
		});
		
		ls_tasksList.addMouseListener(new MouseAdapter() {
		    public void mouseClicked(MouseEvent evt) {
		        if (evt.getClickCount() == 2) {
		        	bt_processActionMouseClicked(null);
		        }
		    }
		});
		
		bt_processAction.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				bt_processActionMouseClicked(e);
			}
		});
		
		bt_deleteAction.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				bt_deleteActionMouseClicked(e);
			}
		});
		
		ls_facts.addListSelectionListener(new ListSelectionListener() {
			
			@Override
			public void valueChanged(ListSelectionEvent e) {
				ls_factsValueChanged(e);
			}
		});
		
		ls_facts.addMouseListener(new MouseAdapter() {
		    public void mouseClicked(MouseEvent evt) {
		        if (evt.getClickCount() == 2) {
		        	bt_moreInfoMouseClicked(null, ls_facts.getSelectedValue());
		        }
		    }
		});
		
		bt_deleteFact.addMouseListener(new MouseAdapter(){
			@Override
			public void mouseClicked(MouseEvent e) {
				bt_deleteFactMouseClicked(e);
				
			}
		});
		
		bt_sort.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				bt_sortMouseClicked(e);
				
			}
		});
		
		bt_moreinfo.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				bt_moreInfoMouseClicked(e, null);
				
			}
		});
		
		bt_saveindb.addMouseListener(new MouseAdapter(){
			@Override
			public void mouseClicked(MouseEvent e){
				bt_saveindbMouseClicked(e);
			}
		});
		
		bt_saveincvs.addMouseListener(new MouseAdapter(){
			@Override
			public void mouseClicked(MouseEvent e){
				bt_saveinfileMouseClicked(e);
			}
		});
		
		bt_uploadcvs.addMouseListener(new MouseAdapter(){
			@Override
			public void mouseClicked(MouseEvent e){
				bt_uploadcvsMouseClicked(e);
			}
		});
		bt_annotate.addMouseListener(new MouseAdapter(){
			@Override
			public void mouseClicked(MouseEvent e){
				bt_annotate(e);
			}
		});
		
		this.addWindowListener(new java.awt.event.WindowAdapter() {
		    @Override
		    public void windowClosing(java.awt.event.WindowEvent windowEvent) {
		        if(usedb){
		        	dbaccess.closeConnection();
		        }
		        windowEvent.getWindow().dispose();
		    }
		});
	}
	
	private JTabbedPane tabbedPane;
	private JPanel pn_actions;
	private JLabel lb_addAction;
	private JTextField tx_addTask;
	private JButton bt_addAction;
	private JLabel lb_actionList;
	private JScrollPane scrollPane1;
	private JList ls_tasksList;
	private JButton bt_deleteAction;
	private JButton bt_processAction;
	private JTextArea ta_instructions;
	private JPanel pn_partitions;
	private JPanel panel2;
	private JPanel panel5;
	private JScrollPane panel4;
	private JPanel paneltasklabel;
	private JLabel lb_currentAction;
	private JTextPane ta_task;
	private JPanel panel_countelements;
	private JLabel lb_countelements;
	private JPanel panel6;
	private JLabel lb_element;
	private JButton bt_sort;
	private JButton bt_moreinfo;
	private JScrollPane scrollPane2;
	private JTextArea ta_element;
	private JPanel panel3;
	private JLabel lb_facts;
	private JButton bt_deleteFact;
	private JScrollPane scrollPane3;
	private JList<String> ls_facts;
	private JPanel hSpacer1;
	private JPanel panel1;
	private JPanel panel8;
	private JScrollPane scrollPane5;
	private JLabel lb_partition;
	private JPanel pn_partitionButtons;
	private JPanel pn_explanations;
	private JTextArea ta_explanations;
	private JPanel panel9;
	private JButton bt_uploadcvs;
	private JButton bt_saveincvs;
	private JButton bt_saveindb;
	private JButton bt_annotate;
	private JButton bt_refactor;
	
}

