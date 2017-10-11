package bttf;

import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import javax.swing.JOptionPane;

import errors.InvalidFeatureBounds;
import gui.OptionButton;

public class FeatureBoundsCalculation {
	private Partition partition;
	private PartitionHelper partHelper; 
	
	public FeatureBoundsCalculation(Partition partition) {
		super();
		this.partition = partition;
		this.partHelper = new PartitionHelper(this.partition);
	}

	public FeatureBoundsWExp get_element_feature_bounds(Element element, Feature parent_feature){
		//default value all features
		ArrayList<Feature> feature_options = this.partition.partitionHelper.get_features_in_task();
		
		FeatureBound earliest_bound = get_earliest_bound(element, feature_options, parent_feature);
		FeatureBound latest_bound = get_latest_bound(element, feature_options, parent_feature);
		
		if(earliest_bound != null && latest_bound != null && earliest_bound.getFeature() != null && latest_bound.getFeature() != null){
			if(earliest_bound.getFeature().getOrder() > latest_bound.getFeature().getOrder()){
				throw new InvalidFeatureBounds(earliest_bound.getFeature().getFeature_name() + " > " + latest_bound.getFeature().getFeature_name());
			}
			
			feature_options = (ArrayList<Feature>) this.partition.get_all_features().stream()
					.filter(f -> f.getOrder() >= earliest_bound.getFeature().getOrder() && f.getOrder() <= latest_bound.getFeature().getOrder())
					.collect(Collectors.toList());
		}
		
		feature_options = (ArrayList<Feature>) feature_options
			.stream()
			.distinct()
			.filter(f -> f.getIn_current_task() == true)
			.collect(Collectors.toList());
		
		//System.out.println("Feature in bounds for: " + element.getIdentifier());
		//System.out.println(feature_options.toString());
		
		return new FeatureBoundsWExp(feature_options, earliest_bound, latest_bound);
	}
	
	/*
	 * obtain earliest_bound eb(e)
	 * eb(m) = F(parent(m)), otherwise alpha
	 * eb(d) = latest{F(r) : r in RefFrom(e)}, otherwise alpha
	 */
	public FeatureBound get_earliest_bound(Element element, ArrayList<Feature> feature_options, Feature parent_feature){
		Feature earliest_bound = null;
		String explanation = null;
		
		//features in refFromElem
		ArrayList<Feature> refFromElem = (ArrayList<Feature>) element.getRefFromThis().stream()
				.map(e -> e.getFeature())
				.collect(Collectors.toList());
		
		//if element is a method:   eb(m) = F(parent(m)), otherwise alpha
		if(element.getElement_type().equals(ElementType.ELEM_TYPE_METHOD)){
			if(element.getParentName() != null && !element.getParentName().isEmpty()){
				Element parent = partition.partitionHelper.get_element_from_string(element.getParentName());
				if(parent != null && parent.getFeature() != null && parent.getFeature().getIn_current_task()){
					earliest_bound = parent.getFeature();
					explanation = "Earliest bound set by method's parent " + parent.getIdentifier();
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
			
			ArrayList<Element> blame = get_bound_blame(element.getRefFromThis(), earliest_bound, parent_feature);
			
			if(blame.size() > 0){
				explanation = "Earliest bound (" + earliest_bound.getFeature_name() +") set by " + blame.size() + " declaration(s), one of them is: " + blame.get(0).getIdentifier();
			}
		}
		
		if(earliest_bound == null){
			earliest_bound = feature_options.stream()
					.filter(f -> f != parent_feature)
					.min((f1, f2) -> Integer.compare(f1.getOrder(), f2.getOrder()))
					.get();
				
				explanation = "Earliest bound is alpha";
		}
		
		return new FeatureBound(earliest_bound, explanation);
		
	}
	
	private ArrayList<Element> get_bound_blame(ArrayList<Element> listElements, Feature bound, Feature parent_feature){
		
		return (ArrayList<Element>) listElements.stream()
				.filter(e -> e != null && e.getFeature() != null && !e.getFeature().equals(parent_feature) && e.getFeature().equals(bound))
				.collect(Collectors.toList());
	}
	
	/*
	 * obtain latest_bound lb(e)
	 * lb(e) = earliest{F(r) :  r in RefTo(e)}, otherwise omega
	 */
	public FeatureBound get_latest_bound(Element element, ArrayList<Feature> feature_options, Feature parent_feature){
		Feature latest_bound = null;
		String explanation = "";
		ArrayList<Element> blame_refTo = new ArrayList<Element>();
		ArrayList<Feature> refToElem = element.getLayeredRefToFeatures();
		
		
		/*
		 * In addition, since e might be a hook, it could reference fprivate declarations in the present
		 * or future, but not it the past. In other words, e must be introduced at or before the earliest
		 * of the fprivate declarations that it references.
		
		 * an exception occurs when doing fw+pl partitioning, if a methods is member of
		 * a non-terminal, it cannot be hook, so all the things it references should be introduced
		 * before or at the same time than itself
		 */
		if( ( !partition.is_fwpi && element.getElement_type().equals(ElementType.ELEM_TYPE_METHOD) )
			|| ( partition.is_fwpi && element.getElement_type().equals(ElementType.ELEM_TYPE_METHOD) && element.getParentDeclaration().isIs_terminal())
			|| ( partition.is_fwpi && element.getElement_type().equals(ElementType.ELEM_TYPE_METHOD) 
					&& !element.getRefToThis().stream().map(e -> e.isIs_terminal()).collect(Collectors.toList()).contains(true)
					&& !element.getRefToThis().stream().map(e -> e.getParentDeclaration()).map(e -> e.isIs_terminal()).collect(Collectors.toList()).contains(true)
					)
					
		){
			refToElem.addAll(element.getFprivateRefFromFeatures());
		}
		
		if(partition.is_fwpi){
			Feature fwFeature = partHelper.get_feature_by_name(Feature.FW_FEATURE_NAME);
			ArrayList<Element> nonTerminalFwElements = new ArrayList<Element>();
			nonTerminalFwElements.addAll(
					element.getRefToThis().stream()
					.filter(e -> !e.isIs_terminal() && e.getFeature() != null && e.getFeature().equals(fwFeature))
					.collect(Collectors.toList()));
			
			if(element.getIdentifier().equals("com.example.expressiontree.State.visitorFactory")){
				System.out.println("here!");
			}
			
			nonTerminalFwElements.addAll(
					element.getRefToThis().stream().map(e -> e.getParentDeclaration())
					.filter(e -> 
						e != null
						&& !e.getIdentifier().equals( (element.getParentDeclaration() == null ? "" : element.getParentDeclaration().getIdentifier()) )
						&& !e.isIs_terminal() 
						&& e.getFeature() != null 
						&& e.getFeature().equals(fwFeature)
					)
					.collect(Collectors.toList()));
			/*
			 * if this declaration is referenced by a declaration of a non-terminal fw class
			 * then the latest bound has to be framework
			 */
			if( nonTerminalFwElements != null && nonTerminalFwElements.size() > 0){
				refToElem = new ArrayList<Feature>();
				refToElem.add(fwFeature);
				blame_refTo = nonTerminalFwElements;
			}
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
			
			if(blame_refTo == null || (blame_refTo != null && blame_refTo.isEmpty()) ){
				blame_refTo = get_bound_blame(element.getLayeredRefToThis(), latest_bound, parent_feature);
			}
					
			if(element.getElement_type().equals(ElementType.ELEM_TYPE_METHOD) && ( blame_refTo == null || (blame_refTo != null && blame_refTo.isEmpty()) ) ){
				ArrayList<Element> blame_fpriv = get_bound_blame(element.getFprivateRefFrom(), latest_bound, parent_feature);
				if(blame_fpriv.size() > 0){
					explanation = "Latest bound (" + latest_bound.getFeature_name() + ") set by refence(s) to " + blame_fpriv.size() + " fprivate declaration(s), one of them is: " + blame_fpriv.get(0).getIdentifier();
				}
				
			}
			else{
				explanation = "Latest bound (" + latest_bound.getFeature_name() + ") set by " + blame_refTo.size() + " declaration(s), one of them is: " + blame_refTo.get(0).getIdentifier();
			}
		}
		else{
			latest_bound = feature_options.stream()
					.filter(f -> f != parent_feature)
					.max((f1, f2) -> Integer.compare(f1.getOrder(), f2.getOrder()))
					.get();
			explanation = "Latest bound is omega";
		}
		
		return new FeatureBound(latest_bound, explanation);
	}
	
	
	public FeaturePossibility get_element_possibilities(Element current_element, Feature parent_feature, boolean recursive_partition)
	throws InvalidFeatureBounds{
		String explanations_text = "";

		//get feature bounds
		FeatureBoundsWExp bounds = this.get_element_feature_bounds(current_element, parent_feature);
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
					.filter(e -> e.getFeature() != null && e.getFeature() != parent_feature)
					.collect(Collectors.toList());
			
			ArrayList<Element> assignedNonLayered = (ArrayList<Element>) current_element.getNonLayeredRefToThis().stream()
					.filter(e -> e.getFeature() != null && e.getFeature() != parent_feature)
					.collect(Collectors.toList());
			
			ArrayList<Element> assignedRefTo = (ArrayList<Element>) current_element.getRefToThis().stream()
					.filter(e -> e.getFeature() != null && e.getFeature() != parent_feature)
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
				
				explanations_text = explanations_text + "Since this is a RECURSIVE TASK, the original modifier"
						+ " fprivate/fpublic remains. ";
				return new FeaturePossibility(explanations_text, button_options, feature_options);
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
									.filter(f -> f != null && f != parent_feature)
									.distinct().collect(Collectors.toList()).size() == 1){
						Feature shared = layeredRefToFeatures.stream()
								.filter(f -> f != null && f != parent_feature)
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
									.filter(f -> f != null && f != parent_feature)
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
								.filter(f -> f != null && f != parent_feature)
								.distinct().collect(Collectors.toList()).size() == 1
							){
						Feature shared = layeredRefToFeatures.stream()
								.filter(f -> f != null && f != parent_feature)
								.distinct().collect(Collectors.toList()).get(0);
						Feature latest_of_nonlay = null;
						try{
							latest_of_nonlay = nonLayeredRefToFeatures.stream()
									.filter(f -> f != null && f != parent_feature)
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
				return new FeaturePossibility(explanations_text, button_options, feature_options);
			}

		}
		return null;
	}
	
}
