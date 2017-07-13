package bttf;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import adapter.FactInferenceAdapter;

public class PartitionHelper {
	private Partition partition;
	
	public PartitionHelper(Partition partition) {
		super();
		this.partition = partition;
	}


	/*
	 * Gets the Element instance using its identifier
	 * */
	public Element get_element_from_string(String elem_identifier){
		for(Element elem : this.partition.getElements_list()){
			if(elem.getIdentifier().equals(elem_identifier)){
				return elem;
			}
		}
		return null;
	}
	
	public static Element get_element_from_string(ArrayList<Element> allelems, String elem_identifier){
		for(Element elem : allelems){
			if(elem.getIdentifier().equals(elem_identifier)){
				return elem;
			}
		}
		return null;
	}
	
	/*public static Element get_element_from_string_flexMethod(ArrayList<Element> allelems, String elem_identifier){
		Element found = null;
		for(Element elem : allelems){
			if(elem.getIdentifier().equals(elem_identifier)){
				found = elem;
			}
		}
		if(found == null){
			
		}
		return found;
	}*/
	
	/*
	 * Search feature using name 
	 */
	public Feature get_feature_by_name(String name){
		//return (feature_list.stream().filter(f -> f.getFeature_name().equals(name)).collect(Collectors.toList())).get(0);
		List<Feature> feature_list_by_name = this.partition.get_all_features().stream().filter(f -> f.getFeature_name().equals(name)).collect(Collectors.toList());
		if(!feature_list_by_name.isEmpty()){
			return feature_list_by_name.get(0);
		}
		return null;
	}
	
	public ArrayList<Feature> get_features_in_task(){
		return (ArrayList<Feature>) this.partition.get_all_features()
				.stream()
				.filter(f -> f.getIn_current_task() == true)
				.collect(Collectors.toList());
	}
	
	public ArrayList<FactInference> get_flatFacts(){
		ArrayList<FactInference> facts = FactInferenceAdapter.getFlatFacts(this.partition.factsAndInferences);
		if(facts != null && !facts.isEmpty()){
			return (ArrayList<FactInference>) facts
					.stream()
					.filter( fact -> (fact.getFeature() != null && fact.getFeature().getIn_current_task() == true) || fact.getFeature() == null)
					.collect(Collectors.toList());
		}
		
		return facts;
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
	
	public void get_better_attribute(Element orig, Element neww){
		if(orig != null && neww != null){
			if( (orig.getCode() == null && neww.getCode() != null)
					|| (orig.getCode() != null && neww.getCode() != null && orig.getCode().length() < neww.getCode().length()) 
				){
					orig.setCode(neww.getCode());
				}
				
			if( (orig.getAnnotation_text() == null && neww.getAnnotation_text() != null)
					|| (orig.getAnnotation_text() != null && neww.getAnnotation_text() != null && orig.getAnnotation_text().length() < neww.getAnnotation_text().length()) 
				){
					orig.setAnnotation_text(neww.getAnnotation_text());
			}
			
			if(neww.getLOC() > orig.getLOC()){
				orig.setLOC(neww.getLOC());
			}
		}
	}
}
