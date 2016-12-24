package bttf;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import adapter.FactInferenceAdapter;
import errors.InvalidFeatureBounds;
import gui.InputFile;
import gui.InvalidFileFact;

public class Partition {
	private boolean cycle_stuff_on = false;
	private String project_name;
	private String feature_model;
	public String current_task;
	private boolean is_fwpi;
	
	private ArrayList<Reference> references_list;
	private ArrayList<Element> elements_list = new ArrayList<Element>();
	private ArrayList<Cycle> cycle_list = new ArrayList<Cycle>();
	private ArrayList<Feature> feature_list = new ArrayList<Feature>();
	private ArrayList<Fact> factsAndInferences = new ArrayList<Fact>();
	private ArrayList<Fact> factsAndInferences_retracted = new ArrayList<Fact>();
	private ArrayList<Element> elements_from_file = new ArrayList<Element>();
	private PartitionInferencingHandler partitionInferencing; 
	private PartitionCycleHandler cycleHandler; 
	final String private_modifier = "PRIVATE";
	private ArrayList<String> allFeatures = new ArrayList<String>();
	public ArrayList<String> featuremodel_alllines;
	
	public Partition(ArrayList<Reference> references_list, String project_name) {
		this.references_list = references_list;
		this.project_name = project_name;
		get_list_of_elements();
		get_toposort();
		
		if(cycle_stuff_on){
			this.cycle_list = cycleHandler.get_cycles(this.elements_list);
			if(cycle_list.isEmpty()){
				print_toposort(null, "");
			}
			else{
				System.out.println(cycle_list.toString());
			}
		}
		
		
		this.cycleHandler = new PartitionCycleHandler();
	}

	private void reset_partition(){
		this.factsAndInferences = new ArrayList<Fact>();
		this.factsAndInferences_retracted = new ArrayList<Fact>();
		for(Feature f : get_features_in_task()){
			for(Element e : f.getFeature_elements()){
				e.resetElement();
			}
			f.resetFeature();
		}
		
	}
	
	/*
	 * Public method that calculates the final feature model
	 * */
	public void set_featureModel(ArrayList<String> fm_lines, ArrayList<String> partition_names, String task, boolean recursive, Feature parent_feature, boolean is_fwpi, boolean cycle_stuff_on){
		this.is_fwpi = is_fwpi;
		this.cycle_stuff_on = cycle_stuff_on;
		this.partitionInferencing = new PartitionInferencingHandler(cycle_stuff_on, cycle_list, this.is_fwpi);
		this.current_task = task;
		this.featuremodel_alllines = fm_lines;
		this.allFeatures = get_allfeaturesnames(fm_lines);
		
		if(recursive && parent_feature != null){
			this.feature_model = this.feature_model.replace(
					parent_feature.getFeature_name(), 
					task.substring(task.indexOf(":")+2));
		}
		else{
			this.feature_model = task;
		}
		
		create_features(partition_names, recursive, parent_feature);
		
		System.out.println("Nodes: " + elements_list.size() + " Edges: " + references_list.size());
	}
	
	
	private ArrayList<String> get_allfeaturesnames(ArrayList<String> fm_lines){
		ArrayList<String> allFeatures = new ArrayList<String>();
		if(fm_lines != null){
			for(String line : fm_lines){
				String parent = line.substring(0, line.indexOf(":")).trim();
				String content = line.substring(line.indexOf(":")+2);
				String[] features = content.split(" ");
				
				if (allFeatures.contains(parent)){
					int newIndex = allFeatures.indexOf(parent) + 1;
					for(String f : features){
						if(newIndex >= allFeatures.size()){
							allFeatures.add(f);
						}
						else{
							allFeatures.add(newIndex, f);
						}
						newIndex++;
					}
				}
				else{
					allFeatures.add(parent);
					for(String f : features){
						allFeatures.add(f);
					}
				}
			}
		}
		System.out.println(allFeatures.toString());
		return allFeatures;
	}
	
	
	/*
	 * says whether this partition has been finished aka all elements have been classified
	 */
	public boolean is_partition_finished(){
		if(elements_list != null && elements_list.size() > 0 && !elements_list.stream().map(e -> e.getFeature()).collect(Collectors.toList()).contains(null)){
			return true;
		}
		else{
			return false;
		}
	}
	
	
	/*return list of cycles*/
	public ArrayList<Cycle> getCycle_list() {
		return cycle_list;
	}
	
	/*
	 * Gets the Element instance using its identifier
	 * */
	public Element get_element_from_string(String elem_identifier){
		for(Element elem : elements_list){
			if(elem.getIdentifier().equals(elem_identifier)){
				return elem;
			}
		}
		return null;
	}

	/*
	 * From the references list gets the individual elements
	 * */
	private void get_list_of_elements(){
		for(Reference ref : references_list){
			Element call_from = new Element(ref.getCall_from(), ref.getCall_from_type(), ref.getCall_from_mod(), ref.getCall_from_code(), ref.isCall_from_isterminal());
			Element call_to = new Element(ref.getCall_to(), ref.getCall_to_type(), ref.getCall_to_mod(), ref.getCall_to_code(), ref.isCall_to_isterminal());
			if(!elements_list.contains(call_from)){
				elements_list.add(call_from);
			}
			if(!elements_list.contains(call_to)){
				elements_list.add(call_to);
			}
		}
	}
	
	/*
	 * Gets topological ordering, or partial ordering
	 * Each element has the association with elements that call it and elements it calls
	 * */
	private void get_toposort(){
		for(Element elem : elements_list){
			for(Reference ref : references_list){
				if(ref.getCall_from().equals(elem.getIdentifier())){
					Element to = get_element_from_string(ref.getCall_to());
					elem.addCallTo(to);
				}
				if(ref.getCall_to().equals(elem.getIdentifier())){
					Element from = get_element_from_string(ref.getCall_from());
					elem.addCallFrom(from);
				}
			}
		}
	}
	
	/*
	 * Print the dependency chains
	 * Will not work if there is a cycle
	 * */
	private void print_toposort(Element current_elem, String chain_text){
		if(current_elem == null){
			ArrayList<Element> elems_with_no_callers =(ArrayList<Element>)(elements_list.stream().filter(e -> e.getRefToThis().size() == 0).collect(Collectors.toList())); 
			for(Element elem : elems_with_no_callers){
				print_toposort(elem, elem.getIdentifier());
			}
		}
		else{
			if(current_elem.getRefFromThis() == null || current_elem.getRefFromThis().size() == 0){
				System.out.println(chain_text);
			}
			else{
				for(Element elem : current_elem.getRefFromThis()){
					print_toposort(elem, chain_text + " -> " + elem.getIdentifier());
				}
			}
		}		
	}
	
	
	
	/*
	 * Creates features based on provided feature model in GUI
	 */
	private void create_features(ArrayList<String> partition_names, boolean recursive, Feature parent_feature) {
		int i;
		if(recursive && feature_list != null && feature_list.size() > 0){
			int count_new_inner_features = partition_names.size();
			//reorder consequent features of subpartitioned one
			for(Feature feat_to_reorder : 
				feature_list
				.stream()
				.filter(f -> f.getOrder() > parent_feature.getOrder())
				.collect(Collectors.toList())
			){
				feat_to_reorder.setOrder(feat_to_reorder.getOrder() + count_new_inner_features);
			}
			i = parent_feature.getOrder() + 1;
			
			//set all features in false for current task
			for(Feature f : feature_list ){
				f.setIn_current_task(false);
			}
			
			//clean feature assignment of elements in paren_feature
			/*for(Element elem : elements_list.stream().filter(e -> e.getFeature().equals(parent_feature)).collect(Collectors.toList())){
				elem.setFeature(null);
			}*/
		}
		else{
			i = 0;
			feature_list = new ArrayList<Feature>();
		}
		
		//is last when is the last of the last, the actual last
		String last_feature_name = (allFeatures != null && allFeatures.size() > 0) 
				? allFeatures.get(allFeatures.size()-1).toUpperCase().trim()
						: "";
		
		for(String part : partition_names){
			
			boolean is_last = part.toUpperCase().equals(last_feature_name);
			
			//is_last? if the feature has no parent and it is the last in this list
			//or, its parent is the last, and it is also the last in this list (the last child of the last feature)
			//boolean is_last = (parent_feature == null && count == partition_names.size()-1) 
			//		|| (parent_feature != null  && parent_feature.getIs_last_feature() && count == partition_names.size()-1);
			
			Feature feature = new Feature(part, i, true, parent_feature, is_last);
			feature_list.add(feature);
			i++;
		}
		
		if(!last_feature_name.isEmpty()){
			ArrayList<Feature> wereLast = (ArrayList<Feature>) feature_list.stream()
					.filter(
						f -> f.getIs_last_feature() 
						&& !f.getFeature_name().equals(last_feature_name)
					).collect(Collectors.toList());
			if(wereLast != null && wereLast.size() > 0){
				for(Feature f : wereLast){
					f.setIs_last_feature(false);
					f.setWas_last_feature(true);
				}
			}
		}
		
		System.out.println(feature_list.toString());
	}
	
	/*
	 * Search feature using name 
	 */
	public Feature get_feature_by_name(String name){
		//return (feature_list.stream().filter(f -> f.getFeature_name().equals(name)).collect(Collectors.toList())).get(0);
		List<Feature> feature_list_by_name = feature_list.stream().filter(f -> f.getFeature_name().equals(name)).collect(Collectors.toList());
		if(!feature_list_by_name.isEmpty()){
			return feature_list_by_name.get(0);
		}
		return null;
	}
	
	
	public ArrayList<Feature> get_all_features(){
		return feature_list;
	}
	
	public ArrayList<Feature> get_features_in_task(){
		return (ArrayList<Feature>) feature_list
				.stream()
				.filter(f -> f.getIn_current_task() == true)
				.collect(Collectors.toList());
	}
	
	/*
	 * gets not classified elements, that their predecessors have been already classified
	 */
	public ArrayList<Element> get_next_elems_to_classify(Feature parent_feature){
		//get initial list
		ArrayList<Element> elements_to_classify = get_elems_to_classify(parent_feature);
		
		/*
		 * recall those facts that were retracted but not removed
		 * if they still are valid they will get applied
		 */
		if(factsAndInferences_retracted.size() > 0){
			for(Element elem : elements_to_classify){
				Fact unretract_fi = null;
				for(Fact fi : factsAndInferences_retracted){
					if(fi.getElement().equals(elem)){
						unretract_fi = fi;
						break;
					}
				}
				if(unretract_fi != null){
					factsAndInferences_retracted.remove(unretract_fi);
					add_element_to_feature_gui(unretract_fi.getFeature(), unretract_fi.getElement(), unretract_fi.getElement_isfprivate(), parent_feature);
				}
			}
		}
		
		//refresh after recalling past facts
		elements_to_classify = get_elems_to_classify(parent_feature);
		
		//System.out.println("elements to classify: " + elements_to_classify.toString());
		return elements_to_classify;
	}
	
	private ArrayList<Element> get_elems_to_classify(Feature parent_feature){
		ArrayList<Element> elements_to_classify = (ArrayList<Element>) elements_list.stream().filter(
				e -> e.getFeature() == parent_feature).collect(Collectors.toList());
		
		//filter those which the elements they call have been all classified
		elements_to_classify = (ArrayList<Element>) elements_to_classify.stream().filter(
				e -> e.getRefFromThis().stream().filter(
						p -> p.getFeature() != null).collect(Collectors.toList()).size() 
				== e.getRefFromThis().size()
				).collect(Collectors.toList());
		
		boolean change = false;
		for(Element elem : elements_to_classify){
			//System.out.println("try to assign: " + elem.getIdentifier());
			boolean temp_change = assign_elements_with_bounds_in_same_feature(elem, parent_feature);
			if(!change && temp_change){
				change = true;
			}
		}
		
		if(change){
			return get_elems_to_classify(parent_feature);
		}
		
		return elements_to_classify;
	}
	
	/*
	 * method for adding an element to a feature from gui
	 * adds fact
	 * calls the actual method that adds the element and generates inferences
	 */
	public void add_element_to_feature_gui(Feature feature, Element element, boolean is_fprivate, Feature parent_feature){
		Fact factInf = new Fact();
		
		partitionInferencing.add_element_to_feature(factInf, feature, element, is_fprivate, !is_fprivate, parent_feature);
		
		String feat_mod = " is " + (element.isIs_fPrivate() ? "fprivate" : "fpublic") + (element.isIs_hook()? " and hook" : "" ) + " of ";
		factInf.setFact(element.getIdentifier() + feat_mod + feature.getFeature_name(), element, element.isIs_fPrivate(), element.isIs_fPublic(), element.isIs_hook(), feature);
		
		factsAndInferences.add(factInf);
	}
	
	
	/*
	 * If callers and calls-to are all classified in the same feature (left and right bounds in same feature)
	 * Add element to that feature too
	 */
	private boolean assign_elements_with_bounds_in_same_feature(Element element, Feature parent_feature){
		try{
			ArrayList<Feature> options = (ArrayList<Feature>) get_element_feature_bounds(element, parent_feature)
					.stream()
					.filter(f -> f.getIn_current_task() == true)
					.collect(Collectors.toList());
			
			if(options.size() == 1){
				Feature feature = options.get(0);
				if(feature.getOrder() == get_latest_feature_in_task()){
					element.setIs_fPrivate(true);
					element.setIs_fPublic(false);
				}
				feature.addElement(element, element.isIs_fPrivate(), element.isIs_fPublic(), element.isIs_hook());
				//Add inference to last fact
				factsAndInferences.get(factsAndInferences.size()-1).addInference(element.getIdentifier() + " belongs to " + feature.getFeature_name() + " because of its bounds.", element, feature);
				return true;
			}
		}
		catch(InvalidFeatureBounds ex){
			System.out.println("Invalid feature bounds found for " + element.getIdentifier() + "\n" + ex.getMessage());
		}		
		return false;
	}
	
	/*
	 * get the features an element could belong to
	 */
	public ArrayList<Feature> get_element_feature_bounds(Element element, Feature parent_feature){
		//default value all features
		ArrayList<Feature> feature_options = get_features_in_task();
		
		Feature earliest_bound = get_earliest_bound(element, feature_options, parent_feature);
		Feature latest_bound = get_latest_bound(element, feature_options, parent_feature);
		
		if(earliest_bound != null && latest_bound != null){
			if(earliest_bound.getOrder() > latest_bound.getOrder()){
				throw new InvalidFeatureBounds(earliest_bound.getFeature_name() + " > " + latest_bound.getFeature_name());
			}
			
			feature_options = (ArrayList<Feature>) feature_list.stream()
					.filter(f -> f.getOrder() >= earliest_bound.getOrder() && f.getOrder() <= latest_bound.getOrder())
					.collect(Collectors.toList());
		}
		
		feature_options = (ArrayList<Feature>) feature_options
			.stream()
			.distinct()
			.filter(f -> f.getIn_current_task() == true)
			.collect(Collectors.toList());
		
		return feature_options;
	}
	
	/*
	 * obtain earliest_bound eb(e)
	 * eb(m) = F(parent(m)), otherwise alpha
	 * eb(d) = latest{F(r) : r in RefFrom(e)}, otherwise alpha
	 */
	public Feature get_earliest_bound(Element element, ArrayList<Feature> feature_options, Feature parent_feature){
		Feature earliest_bound = null;
		
		//features in refFromElem
		ArrayList<Feature> refFromElem = (ArrayList<Feature>) element.getRefFromThis().stream()
				.map(e -> e.getFeature())
				.collect(Collectors.toList());
		
		//if element is a method:   eb(m) = F(parent(m)), otherwise alpha
		if(element.getElement_type().equals(ElementType.ELEM_TYPE_METHOD)){
			if(element.getParentName() != null && !element.getParentName().isEmpty()){
				Element parent = get_element_from_string(element.getParentName());
				if(parent != null && parent.getFeature() != null && parent.getFeature().getIn_current_task()){
					earliest_bound = parent.getFeature();
				}
			}
		}
		//else:   eb(d) = latest{F(r) : r in RefFrom(e)}, otherwise alpha
		else if(!element.getElement_type().equals(ElementType.ELEM_TYPE_METHOD) 
				&& refFromElem != null && refFromElem.size() > 0
				&& refFromElem.stream().filter(f -> f != parent_feature).collect(Collectors.toList()).size()>0){
			earliest_bound = refFromElem
				.stream()
				.filter(f -> f != parent_feature)
				.max((f1, f2) -> Integer.compare(f1.getOrder(), f2.getOrder()))
				.get();		
		}
		else{
			earliest_bound = feature_options.stream()
				.filter(f -> f != parent_feature)
				.min((f1, f2) -> Integer.compare(f1.getOrder(), f2.getOrder()))
				.get();
		}
		
		return earliest_bound;
		
	}
	
	/*
	 * obtain latest_bound lb(e)
	 * lb(e) = earliest{F(r) :  r in RefTo(e)}, otherwise omega
	 */
	public Feature get_latest_bound(Element element, ArrayList<Feature> feature_options, Feature parent_feature){
		Feature latest_bound = null;
		
		ArrayList<Feature> refToElem = element.getLayeredRefToFeatures();
		/*In addition, since e might be a hook, it could reference fprivate declarations in the present
		or future, but not it the past. In other words, e must be introduced at or before the earliest
		of the fprivate declarations that it references.*/
		if(element.getElement_type().equals(ElementType.ELEM_TYPE_METHOD)){
			refToElem.addAll(element.getFprivateRefFromFeatures());
		}
		
		if(refToElem != null 
			&& refToElem.size() > 0 
			&& refToElem.stream()
				.filter(f -> f != parent_feature)
				.collect(Collectors.toList()).size()>0){
			
			latest_bound = refToElem
					.stream()
					.filter(f -> f != parent_feature)
					.min((f1, f2) -> Integer.compare(f1.getOrder(), f2.getOrder()))
					.get();
		}
		else{
			latest_bound = feature_options.stream()
					.filter(f -> f != parent_feature)
					.max((f1, f2) -> Integer.compare(f1.getOrder(), f2.getOrder()))
					.get();
		}
		
		return latest_bound;
	}
	
	/*
	 * Remove fact and consequent facts
	 */
	public void delete_fact(String fact){
		Fact fact_to_remove = new Fact(); 
		ArrayList<Fact> facts_to_retract = new ArrayList<Fact>();
		boolean found = false;
		
		for(Fact factinf : factsAndInferences){
			if(found){
				facts_to_retract.add(factinf);
			}
			if(factinf.getFact().equals(fact)){
				found = true;
				fact_to_remove = factinf;
			}
		}
		
		fact_to_remove.retractFact();
		factsAndInferences.remove(fact_to_remove);
		
		for(Fact fact_ret : facts_to_retract){
			fact_ret.retractFact();
			factsAndInferences_retracted.add(fact_ret);
			factsAndInferences.remove(fact_ret);
		}
	}
	
	/*
	 * Add elements from fact list on file
	 * things to check: 
	 * 	- element exists
	 *  - element hasn't been assigned yet to another feature
	 *  - the specified feature is inside bounds
	 */
	public ArrayList<InvalidFileFact> add_elements_from_facts_file(ArrayList<Element> file_elements, boolean reload){
		ArrayList<InvalidFileFact> invalid_facts = new ArrayList<InvalidFileFact>();
		
		if(reload){
			reset_partition();
		}
		
		for(Element file_elem : file_elements){
			Element element = get_element_from_string(file_elem.getIdentifier());
			//the element exists
			if(element != null){
				element.setUser_comment(file_elem.getUser_comment());
			
				Feature feature_file_elem = file_elem.getFeature();
				//only take those elements that its feature is in task
				if(feature_file_elem != null){
					if(feature_file_elem.getIn_current_task()){
						//get feature
						setFileElemFeature(file_elem, element, invalid_facts);
						if(file_elem.getFeature() != null){
							//it is a fresh element, no feature assigned (happy path!)
							if (element.getFeature() == null){
								try{
									ArrayList<Feature> options = get_element_feature_bounds(element, null);
									if(options.contains(file_elem.getFeature())){
										Feature feature = get_feature_by_name(file_elem.getFeature().getFeature_name());
										add_element_to_feature_gui(feature, element, file_elem.isIs_fPrivate(), feature.getParent_feature()); 
									}
									//asked feature is outside bounds
									else{invalid_facts.add(new InvalidFileFact(file_elem.getIdentifier(), file_elem.get_assignment_text(), InvalidFileFact.FEATURE_INVALID 
											+ ", valid features are: " 
											+ options.stream().map(f -> f.getFeature_name()).collect(Collectors.toList()).toString()
											+"."));
									}
								}
								catch (InvalidFeatureBounds ex){
									invalid_facts.add(new InvalidFileFact(file_elem.getIdentifier(), file_elem.get_assignment_text(), InvalidFileFact.INVALID_BOUNDS + " " + ex.getMessage()));
								}
								
							}
							//feature already assigned
							else{
								//element assigned to same feature that is being asked
								//propagate fprivate if it is fprivate
								if(element.getFeature().equals(file_elem.getFeature())){
									if( file_elem.isIs_fPrivate() == true && element.isIs_fPrivate() != file_elem.isIs_fPrivate()){
										element.setIs_fPrivate(true);
										add_element_to_feature_gui(element.getFeature(), element, element.isIs_fPrivate(), element.getFeature().getParent_feature());
									}
								}
								//element not assigned to same feature that is being asked
								else{
									//it is ok to reassign as long as they belong to the same hierarchy, specifically:
									// a)element is currently assigned to a parent and it's being asked to move to child
									// b)element is currently assigned to a child and it's being asked to move to parent
									if(file_elem.getFeature().isChildOf(element.getFeature())
											|| element.getFeature().isChildOf(file_elem.getFeature())){
										Feature new_feature = file_elem.getFeature();
										/*Feature old_feature = element.getFeature();
										old_feature.removeElement(element);
										*/
										add_element_to_feature_gui(new_feature, element, file_elem.isIs_fPrivate(), new_feature.getParent_feature());
										//new_feature.addElement(element, element.isIs_fPrivate(), element.isIs_fPublic(), element.isIs_hook());
									}
									//it is a method, it may be a hook
									else if(file_elem.getElement_type().equals(ElementType.ELEM_TYPE_METHOD)){
										try{
											ArrayList<Feature> options = get_element_feature_bounds(element, null);
											if(options.contains(file_elem.getFeature())){
												Feature feature = get_feature_by_name(file_elem.getFeature().getFeature_name());
												add_element_to_feature_gui(feature, element, file_elem.isIs_fPrivate(), feature.getParent_feature()); 
											}
											//asked feature is outside bounds
											else{invalid_facts.add(new InvalidFileFact(file_elem.getIdentifier(), file_elem.get_assignment_text(), InvalidFileFact.FEATURE_INVALID 
													+ ", valid features are: " 
													+ options.stream().map(f -> f.getFeature_name()).collect(Collectors.toList()).toString()
													+"."));
											}
										}catch (InvalidFeatureBounds ex){
											invalid_facts.add(new InvalidFileFact(file_elem.getIdentifier(), file_elem.get_assignment_text(), InvalidFileFact.INVALID_BOUNDS + " " + ex.getMessage()));
										}
									}
									//if it is asked to be assigned to something different notify error
									else{
										invalid_facts.add(
												new InvalidFileFact(file_elem.getIdentifier(), 
														file_elem.get_assignment_text(), 
														InvalidFileFact.FEATURE_ASSIGNED + " " + element.getFeature().getFeature_name() +"."
													)
											);
									}
								}
							}
						}
					}
					//requested feature is not in game, notify in console for debugging purpose
					else{
						System.out.println("File element with feature not in task: " + file_elem.simpleDisplayElement());
					}
				}
			}
			//element doesn't exist
			else{invalid_facts.add(new InvalidFileFact(file_elem.getIdentifier(), file_elem.get_assignment_text(), InvalidFileFact.ELEMENT_DOESNT_EXIST));}
			
		}
		return invalid_facts;
	}
	
	private void setFileElemFeature(Element file_elem, Element element, ArrayList<InvalidFileFact> invalid_facts){
		if(file_elem.getFeature()!= null){
			Feature feature_file_elem = file_elem.getFeature();
			
			if(feature_file_elem.getFeature_name().equals(InputFile.container_feature_name)){
				String parent_elem_name = element.getParentName();
				if(parent_elem_name != null){
					Element parent_elem = get_element_from_string(parent_elem_name);
					if(parent_elem != null){
						if(parent_elem.getFeature() != null && parent_elem.getFeature().getIn_current_task()){
							file_elem.setFeature(parent_elem.getFeature());
						}else{
							invalid_facts.add(
									new InvalidFileFact(file_elem.getIdentifier(), 
											file_elem.get_assignment_text(), 
											InvalidFileFact.CONTAINER_UNASSIGNED
										)
								);
							file_elem.setFeature(null);
						}
					}else{
						invalid_facts.add(
								new InvalidFileFact(file_elem.getIdentifier(), 
										file_elem.get_assignment_text(), 
										InvalidFileFact.NO_CONTAINER
									)
							);
						file_elem.setFeature(null);
					}
				}else{
					invalid_facts.add(
							new InvalidFileFact(file_elem.getIdentifier(), 
									file_elem.get_assignment_text(), 
									InvalidFileFact.NO_CONTAINER
								)
						);
					file_elem.setFeature(null);
				}
			}
			else{
				file_elem.setFeature(feature_file_elem);
			}
		}
	}
	
	
	/*
	 * Get list of references
	 */
	public ArrayList<Reference> get_references_list(){
		return references_list;
	}
	
	/*
	 * Get list of facts and inferences
	 */
	public ArrayList<Fact> get_facts(){
		return (ArrayList<Fact>) factsAndInferences
				.stream()
				.filter(fact -> fact.getFeature().getIn_current_task() == true)
				.collect(Collectors.toList());
		//return factsAndInferences;
	}
	
	public ArrayList<FactInference> get_flatFacts(){
		ArrayList<FactInference> facts = FactInferenceAdapter.getFlatFacts(this.factsAndInferences);
		if(facts != null && !facts.isEmpty()){
			return (ArrayList<FactInference>) facts
					.stream()
					.filter(fact -> fact.getFeature() != null && fact.getFeature().getIn_current_task() == true)
					.collect(Collectors.toList());
		}
		
		return facts;
	}
	
	/*
	 * Get list of all elements
	 */
	public ArrayList<Element> get_elements(){
		return elements_list;
	}
	
	/*
	 * Get list of elements from file
	 */
	public ArrayList<Element> get_elements_from_file(){
		return elements_from_file;
	}
	
	/*
	 * Get project name
	 */
	public String get_project_name(){
		return project_name;
	}
	
	/*
	 * Get feature model
	 */
	public String get_feature_model(){
		return feature_model;
	}
	
	public int get_latest_feature_in_task(){
		List<Feature> last_feature = get_features_in_task().stream()
				.filter(f -> f.getIs_last_feature()).collect(Collectors.toList());
		
		if(last_feature.size() > 0){
			return last_feature.get(0).getOrder();
		}
		else{
			return -1;
		}
	}
	
	public boolean is_same_than_container_feature(Element element, Feature feature){
		if(element.getParentName() != null){
			Element parent = get_element_from_string(element.getParentName());
			if(parent != null && parent.getFeature() != null && parent.getFeature().equals(feature)){
				return true;
			}
		}
		return false;
	}
	
	/*public Fact get_fact_with_text(String fact_text){
		if(factsAndInferences != null && factsAndInferences.size() > 0){
			try{
				return factsAndInferences.stream()
					.filter(f -> f.getFact().equals(fact_text))
					.collect(Collectors.toList())
					.get(0);
			}catch(IndexOutOfBoundsException ex){
				return null;
			}
		}
		return null;
	}*/
}


