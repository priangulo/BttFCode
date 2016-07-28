package bttf;

import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class DBStorage {
	private Connection conn = null;
	private Statement statement = null;
	private PreparedStatement preparedStatement = null;
	
	public DBStorage(){
	    try {
	    	Properties prop = load_properties_file();
			 
			String driverClass = prop.getProperty("driver");
			String url = prop.getProperty("url");
			String username = prop.getProperty("username");
			String password = prop.getProperty("password");
			 
			Class.forName(driverClass);
			 
			this.conn = DriverManager.getConnection(url, username, password);
	        this.statement = conn.createStatement();
	    } catch (SQLException ex) {
	    	System.out.println("SQLException: " + ex.getMessage());
            System.out.println("SQLState: " + ex.getSQLState());
            System.out.println("VendorError: " + ex.getErrorCode());
	    } catch (Exception ex) {
	        System.out.println(ex.getMessage());
	    }
	}
	
	public Properties load_properties_file() throws Exception {
		Properties properties = new Properties();
		InputStream in = this.getClass().getResourceAsStream("/conf/db.properties"); 
		properties.load(in);
		in.close();
		return properties;
	}
	
	/*
	 * if the feature model already exists deletes it and all its inner elements
	 * saves new info of feature model
	 * if no data exists, it starts creating the project 
	 */
	public boolean save_state(Partition partition) throws SQLException{
		boolean error = false;
		boolean project_already_exists = false;
		DBValue id_project = new DBValue(-1);
		DBValue id_featuremodel = new DBValue(-1);
		try {
			ArrayList<DBValue> project_values = new ArrayList<DBValue>();
			project_values.add(new DBValue(partition.get_project_name()));
			project_values.add(new DBValue("Java"));
			
			ResultSet rs = sql_select(get_idproject, project_values);
			if (rs.next()) {
				id_project.setInt_val(rs.getInt(1));
			}
			
			DBValue date = new DBValue(new java.sql.Date(new Date().getTime()));
			
			if(id_project.getInt_val() != -1){ //means it already exists
				project_already_exists = true;
				//update project date
				ArrayList<DBValue> newdate_project_values = new ArrayList<DBValue>();
				newdate_project_values.add(date);
				newdate_project_values.add(id_project);
				sql_update(update_projectdate, newdate_project_values);
			}
			else{
				//insert project
				project_values.add(date);	
				id_project.setInt_val(sql_update(insert_project, project_values));
			}

			if(id_project.getInt_val() != -1){ //no error, continue
				ArrayList<DBValue> featuremodel_values = new ArrayList<DBValue>();
				featuremodel_values.add(id_project);
				featuremodel_values.add(new DBValue(partition.get_feature_model()));
				rs = sql_select(get_idfeaturemodel, featuremodel_values);
				if(rs.next()){
					id_featuremodel = new DBValue(rs.getInt(1));
					//if feature model exists, delete it
					if(id_featuremodel.getInt_val() != -1){
						//cascade deleting configured in db
						sql_update(delete_featuremodel, new ArrayList<DBValue>(Arrays.asList(id_featuremodel)));
					}
				}
				
				featuremodel_values = new ArrayList<DBValue>();
				featuremodel_values.add(new DBValue(partition.get_feature_model()));
				featuremodel_values.add(id_project);
				
				//insert feature model
				id_featuremodel = new DBValue(sql_update(insert_featuremodel, featuremodel_values));
				if(id_featuremodel.getInt_val() != -1){
					
					//insert features
					Map<String,Integer> feature_list = new HashMap<String,Integer>();
					for(Feature f : partition.get_all_features()){
						ArrayList<DBValue> feature_values = new ArrayList<DBValue>();
						feature_values.add(new DBValue(f.getFeature_name()));
						feature_values.add(new DBValue(f.getOrder()));
						feature_values.add(id_featuremodel);
						DBValue id_feature = new DBValue(sql_update(insert_feature, feature_values));
						if(id_feature.getInt_val() != -1){ feature_list.put(f.getFeature_name(), id_feature.getInt_val()); }
						else{error = true;}
					}
					
					//insert/update elements
					Map<String,Integer> elements_list = new HashMap<String,Integer>();
					for(Element e : partition.get_elements()){
						ArrayList<DBValue> element_values = new ArrayList<DBValue>();
						element_values.add(new DBValue(e.getIdentifier()));
						element_values.add(new DBValue(e.getElement_type().get_element_type()));
						element_values.add(new DBValue(e.getCode()));
						element_values.add(new DBValue(e.isIs_hook()));
						element_values.add(new DBValue(e.isIn_cycle()));
						element_values.add(new DBValue(e.isIs_fPrivate()));
						element_values.add(new DBValue(e.isIs_fPublic()));
						element_values.add(id_project);
						if(e.getFeature() != null){
							element_values.add(new DBValue(feature_list.get(e.getFeature().getFeature_name())));
						}
						else{
							element_values.add(new DBValue());
						}
						
						DBValue id_element = new DBValue(-1);
						//update element
						if(project_already_exists){
							ArrayList<DBValue> getidelem_values = new ArrayList<DBValue>();
							getidelem_values.add(new DBValue(e.getIdentifier()));
							getidelem_values.add(id_project);
							ResultSet rs_element = sql_select(get_idelement, getidelem_values);
							if(rs_element.next()){
								id_element.setInt_val(rs_element.getInt(1));
								element_values.add(id_element);
								sql_update(update_element, element_values);
							}
						}
						//insert element
						else{
							id_element = new DBValue(sql_update(insert_element, element_values));
						}
						
						if(id_element.getInt_val() != -1){ elements_list.put(e.getIdentifier(), id_element.getInt_val()); }
						else{error = true;}
					}
					

					//insert references
					if(!project_already_exists){
						for(Reference r : partition.get_references_list()){
							ArrayList<DBValue> reference_values = new ArrayList<DBValue>();
							reference_values.add(new DBValue(elements_list.get(r.getCall_from())));
							reference_values.add(new DBValue(elements_list.get(r.getCall_to())));
							reference_values.add(id_project);
							DBValue id_reference = new DBValue(sql_update(insert_reference, reference_values));
							if(id_reference.getInt_val() == -1){ error = true;}
						}
					}
					
					//insert cycles
					if(!project_already_exists){
						for(Cycle c : partition.getCycle_list()){
							ArrayList<DBValue> cycle_values = new ArrayList<DBValue>();
							cycle_values.add(id_project);
							DBValue id_cycle = new DBValue(sql_update(insert_cycle, cycle_values));
							if(id_cycle.getInt_val() != -1){
								//insert cycle elements
								for(Element e : c.getElements()){
									ArrayList<DBValue> cycleelem_values = new ArrayList<DBValue>();
									cycleelem_values.add(new DBValue(elements_list.get(e.getIdentifier())));
									cycleelem_values.add(id_cycle);
									DBValue id_cycleelement = new DBValue(sql_update(insert_cycleelement, cycleelem_values));
									if(id_cycleelement.getInt_val() == -1){ error = true;}
								}
							}
							else{ error = true;}
						}
					}
					
					//insert facts
					for(Fact fi : partition.get_facts()){
						ArrayList<DBValue> fact_values = new ArrayList<DBValue>();
						fact_values.add(new DBValue(fi.getFact()));
						fact_values.add(new DBValue(elements_list.get(fi.getElement().getIdentifier())));
						fact_values.add(new DBValue(fi.getElement_isfprivate()));
						fact_values.add(new DBValue(feature_list.get(fi.getFeature().getFeature_name())));
						DBValue id_fact= new DBValue(sql_update(insert_fact, fact_values));
						if(id_fact.getInt_val() != -1){
							//insert inferences
							for(Inference inf : fi.getInferences()){
								ArrayList<DBValue> inf_values = new ArrayList<DBValue>();
								inf_values.add(id_fact);
								inf_values.add(new DBValue(inf.getInference()));
								if(inf.getElement() == null){
									inf_values.add(new DBValue());
								}
								else{
									inf_values.add(new DBValue(elements_list.get(inf.getElement().getIdentifier())));
								}
								
								DBValue id_inf= new DBValue(sql_update(insert_inference, inf_values));
								if(id_inf.getInt_val() == -1){error = true;}
							}
						}else{ error = true;}
					}
					
					
				}else{error = true;}
			}else{error = true;}
			
			if (error){
				System.out.println("Error: generated key not retrieved");
				//cascade deleting configured in db
				if(project_already_exists){ //if the project existed before this feature model, only delete the feature model
					sql_update(delete_featuremodel, new ArrayList<DBValue>(Arrays.asList(id_featuremodel)));
				}
				else{//if everything is new, delete everything
					sql_update(delete_project, new ArrayList<DBValue>(Arrays.asList(id_project)));
				}
				
				return false;
			}
			return true;
			
		} catch (Exception e) {
			e.printStackTrace();
			//cascade deleting configured in db
			if(project_already_exists){ //if the project existed before this feature model, only delete the feature model
				sql_update(delete_featuremodel, new ArrayList<DBValue>(Arrays.asList(id_featuremodel)));
			}
			else{//if everything is new, delete everything
				sql_update(delete_project, new ArrayList<DBValue>(Arrays.asList(id_project)));
			}
			return false;
		} 
	}
	
	/*
	 * 
	 * if the project exists, but the data is no longer valid, deletes the project, returns null
	 * if the project and feature model exist, and their data is still valid (code hasn't changed) returns the date of the saved data
	 * if the project or feature model doesn't exist, returns null 
	 */
	public Date check_feature_model_exists(Partition partition){
		boolean valid_project = true;
		ArrayList<DBValue> project_values = new ArrayList<DBValue>();
		project_values.add(new DBValue(partition.get_project_name()));
		project_values.add(new DBValue("Java"));
		
		try{
			//1. check if project exists
			ResultSet rs = sql_select(get_project, project_values);
			if (rs.next()) {
				DBValue id_project = new DBValue(rs.getInt(1));
				DBValue project_date = new DBValue(rs.getDate(4));
				if(id_project.getInt_val() != -1){ //it means it exists
					rs = sql_select(get_references_count, new ArrayList<DBValue>(Arrays.asList(id_project)));
					if (rs.next()) {
						int ref_count = rs.getInt(1);
						//2. check there is the same amount of references
						System.out.println("db_ref_count: " + ref_count + ", ref_list_size: " + partition.get_references_list().size() );
						if(ref_count == partition.get_references_list().size()){
							rs = sql_select(get_reference_list, new ArrayList<DBValue>(Arrays.asList(id_project)));
							ArrayList<Reference> list_ref_db = new ArrayList<Reference>();
							while(rs.next()){
								Reference ref = new Reference(rs.getString(1), rs.getString(2));
								list_ref_db.add(ref);
							}
							//3. check if they are the same references
							if(partition.get_references_list().containsAll(list_ref_db) && list_ref_db.containsAll(partition.get_references_list())){
								rs = sql_select(get_featuremodelmodel_by_idproject, new ArrayList<DBValue>(Arrays.asList(id_project)));
								if (rs.next()) {
									String feature_model = rs.getString(1);
									//4. check if it is the same feature model
									if(feature_model.equals(partition.get_feature_model())){
										//5. successful case!
										return (new java.util.Date(project_date.getDate_val().getTime()));
									}
								}
							}
							//3.1 references have changed, the stored info is not longer valid, delete project
							else{
								System.out.println("Project data not valid, references have changed");
								valid_project = false;
							}
						}
						//2.1 amount of references has changed, the stored info is not longer valid, delete project
						else{
							System.out.println("Project data not valid, amount of references has changed");
							valid_project = false;
						}
					}
					
					if(valid_project == false){
						sql_update(delete_project, new ArrayList<DBValue>(Arrays.asList(id_project)));
						System.out.println("Project data not valid, project deleted");
					}
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}		
		return null;
	}
	
	/*
	 * before this partition was already created and initialized, look at bttf.Partition.Partition(ArrayList<Reference>, String), and bttf.Partition.init(ArrayList<String>, String) 
	 * this means that it already has:
	 *  - list of references
	 *  - project name
	 *  - list of elements
	 *  - list of cycles
	 *  - feature model
	 *  - feature list
	 *  what we need to load is:
	 *  - updates to elements attributes
	 *  - facts
	 *  - inferences
	 */
	public boolean load_partition_data(Partition partition){	
		try {
			DBValue id_project = new DBValue(-1);
			DBValue id_featuremodel = new DBValue(-1);
			
			ArrayList<DBValue> project_values = new ArrayList<DBValue>();
			project_values.add(new DBValue(partition.get_project_name()));
			project_values.add(new DBValue("Java"));
			
			ResultSet rs = sql_select(get_idproject, project_values);
			if (rs.next()) {
				id_project.setInt_val(rs.getInt(1));
				//1. get project
				if(id_project.getInt_val() != -1){
					ArrayList<DBValue> featuremodel_values = new ArrayList<DBValue>();
					featuremodel_values.add(id_project);
					featuremodel_values.add(new DBValue(partition.get_feature_model()));
					rs = sql_select(get_idfeaturemodel, featuremodel_values);
					if(rs.next()){
						id_featuremodel = new DBValue(rs.getInt(1));
						//2. get feature model
						if(id_featuremodel.getInt_val() != -1){
							//3. retrieve elements, and add attributes
							rs = sql_select(get_element_list, new ArrayList<DBValue>(Arrays.asList(id_featuremodel)));
							while(rs.next()){
								Element elem = partition.get_element_from_string(rs.getString("identifier"));
								if(elem != null){
									String feature_name = rs.getString("feature_name");
									Boolean is_fprivate = rs.getBoolean("is_fprivate");
									Boolean is_fpublic = rs.getBoolean("is_fpublic");
									Boolean is_hook = rs.getBoolean("is_hook");
									if(feature_name != null){
										Feature feature = partition.get_feature_by_name(feature_name);
										if(feature != null){
											feature.addElement(elem, is_fprivate, is_fpublic, is_hook);
										}else{ return false; } //error: inconsistent features
									}
									else{
										elem.setIs_fPrivate(is_fprivate);
										elem.setIs_fPublic(is_fpublic);
										elem.setIs_hook(is_hook);
									}
								}else{ return false; } //error: inconsistent elements
							}
							//4. retrieve facts
							rs = sql_select(get_fact_list, new ArrayList<DBValue>(Arrays.asList(id_featuremodel)));
							while(rs.next()){
								DBValue id_fact = new DBValue(rs.getInt("id_fact"));
								Fact fact = new Fact();
								fact.setFact(rs.getString("fact"),
										partition.get_element_from_string(rs.getString("identifier")), 
										rs.getBoolean("elem_fact_isfprivate"),
										false,
										false,
										partition.get_feature_by_name(rs.getString("feature_name")));
								//4.1 retrieve inferences
								ResultSet rs_inferences = sql_select(get_inference_list, new ArrayList<DBValue>(Arrays.asList(id_fact)));
								while(rs_inferences.next()){
									String inference_text = rs_inferences.getString("inference");
									String identifier = rs_inferences.getString("identifier");
									fact.addInference(inference_text, partition.get_element_from_string(identifier), partition.get_element_from_string(identifier).getFeature());
								}
								partition.get_facts().add(fact);
							}
						}else{ return false; } //error: no id_featuremodel
					}else{ return false; } //error: no id_featuremodel
				}else{ return false; } //error: no id_project
			}else { return false; } //error: no id_project
			
			return true; //finished without returning before!
		}catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	private ResultSet sql_select(String sql, ArrayList<DBValue> values){
		try {
			preparedStatement = conn.prepareStatement(sql);
			set_parameters(preparedStatement, values);
			ResultSet rs = preparedStatement.executeQuery();
			return rs;
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private int sql_update(String sql, ArrayList<DBValue> values){
		try {
			preparedStatement = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
			set_parameters(preparedStatement, values);
			preparedStatement.executeUpdate();
			ResultSet rs = preparedStatement.getGeneratedKeys();
			if (rs.next()){
				return rs.getInt(1);
			}
			return 0;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return -1;
	}
	
	private void set_parameters(PreparedStatement preparedStatement, ArrayList<DBValue> values){
		int i = 1;
		try {
			for(DBValue val : values){
				if(val.getNull_val() == true){
					preparedStatement.setNull(i, java.sql.Types.INTEGER);
				}
				else if(val.getType().equals(ValueType.TYPE_INT)){
					preparedStatement.setInt(i, val.getInt_val());
				}
				else if (val.getType().equals(ValueType.TYPE_STRING)){
					preparedStatement.setString(i, val.getStr_val());
				}
				else if (val.getType().equals(ValueType.TYPE_DATE)){
					preparedStatement.setDate(i, val.getDate_val());
				}
				else if (val.getType().equals(ValueType.TYPE_BOOLEAN)){
					preparedStatement.setBoolean(i, val.getBool_val());
				}
				i++;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void close_connection(){
		try {
			if(!statement.isClosed()){
					statement.close();
			}
			if(!conn.isClosed()){
				conn.close();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	private String insert_project = "INSERT INTO project(project_name, language, date) VALUES (?,?,?)";
	private String insert_featuremodel = "INSERT INTO feature_model(feature_model,id_project) VALUES(?,?);";
	private String insert_feature = "INSERT INTO feature(feature_name,feature_order,id_feature_model) VALUES(?,?,?);";
	private String insert_element = "INSERT INTO element(identifier,type,code,is_hook,in_cycle,is_fprivate,is_fpublic,id_project,id_feature) VALUES(?,?,?,?,?,?,?,?,?);";
	private String insert_reference = "INSERT INTO reference(from_id_elem,to_id_elem,id_project) VALUES(?,?,?);";
	private String insert_cycle = "INSERT INTO cycle(id_project) VALUES(?);";
	private String insert_cycleelement = "INSERT INTO cycle_elements(id_cycle,id_element) VALUES(?,?);";				
	private String insert_fact = "INSERT INTO fact(fact,id_element_fact,elem_fact_isfprivate,id_feature) VALUES (?,?,?,?);";
	private String insert_inference = "INSERT INTO inference(id_fact,inference,id_element) VALUES(?,?,?);";
	//private String insert_inferenceelement = "INSERT INTO inference_element(id_fact,id_element) VALUES(?,?);";
	
	private String update_projectdate = "UPDATE project SET date = ? WHERE id_project = ?;";
	private String update_element = "UPDATE element SET identifier = ?, type = ?, code = ?, is_hook = ?, in_cycle = ?, is_fprivate = ?, is_fpublic = ?, id_project = ?, id_feature = ? WHERE id_element = ?;";
	
	private String delete_project = "DELETE FROM project WHERE id_project = ?;";
	private String delete_featuremodel = "DELETE FROM feature_model WHERE id_feature_model = ?;";
	
	private String get_idproject = "SELECT id_project FROM project WHERE project_name LIKE ? AND language LIKE ?;";
	private String get_project = "SELECT id_project, project_name, language, date FROM project WHERE project_name LIKE ? AND language LIKE ?;";
	private String get_idfeaturemodel = "SELECT id_feature_model FROM feature_model WHERE id_project = ? AND feature_model LIKE ?;";
	private String get_featuremodelmodel_by_idproject = "SELECT feature_model FROM feature_model WHERE id_project = ?;";
	private String get_references_count = "SELECT count(id_reference) count_references FROM reference WHERE id_project = ?;";
	private String get_reference_list = "SELECT e_from.identifier from_elem, e_to.identifier to_elem FROM reference r INNER JOIN element e_from ON r.from_id_elem = e_from.id_element INNER JOIN element e_to ON r.to_id_elem = e_to.id_element WHERE r.id_project = ?;";
	private String get_element_list = "SELECT e.identifier, e.is_hook, e.is_fprivate, e.is_fpublic, f.feature_name FROM element e INNER JOIN feature f ON e.id_feature = f.id_feature WHERE f.id_feature_model = ?;";
	private String get_fact_list = "SELECT f.id_fact, f.fact, e.identifier, f.elem_fact_isfprivate, fe.feature_name FROM fact f INNER JOIN element e ON f.id_element_fact = e.id_element INNER JOIN feature fe ON f.id_feature = fe.id_feature WHERE fe.id_feature_model = ? ORDER BY f.id_fact;";
	private String get_inference_list = "SELECT i.inference, e.identifier FROM inference i LEFT JOIN element e ON i.id_element = e.id_element WHERE id_fact = ? ORDER BY id_inference;";
	//private String get_inference_list = "SELECT inference from inference WHERE id_fact = ? ORDER BY id_inference;";
	//private String get_inferredelements = "SELECT e.identifier FROM inference_element ie INNER JOIN element e ON ie.id_element = e.id_element  WHERE id_fact = ?;";
	private String get_idelement = "SELECT id_element FROM element WHERE identifier = ? AND id_project = ?;";
}



