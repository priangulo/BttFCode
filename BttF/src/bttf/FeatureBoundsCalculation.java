package bttf;

import java.util.ArrayList;
import java.util.stream.Collectors;

import errors.InvalidFeatureBounds;

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
}
