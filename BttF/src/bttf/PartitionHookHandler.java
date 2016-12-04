package bttf;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class PartitionHookHandler {
	/*
	 * If the element is calling something already classified in a future feature,
	 * then it is a hook
	 */
	boolean check_is_hook(Feature feature_to_assign, Element element){
		if(element.getElement_type().equals(ElementType.ELEM_TYPE_METHOD)){
			for(Element call_to : element.getRefFromThis()){
				if(call_to.getFeature() != null && call_to.getFeature().getOrder() > feature_to_assign.getOrder()){
					return true;
				}
			}
		}
		return false;
	}
	
	/*
	 * Classify hook methods that were assigned a label before all its parents got assigned
	 * Like what happens when a hook gets assigned because it calls something that is fprivate 
	 */
	void late_check_for_hooks(Element element, Fact factInf, Feature parent_feature){
		ArrayList<Element> possible_hooks = (ArrayList<Element>) element.getRefToThis().stream().filter(
				e -> e.getElement_type().equals(ElementType.ELEM_TYPE_METHOD)
				&& e.isIs_hook() == false
				&& e.getFeature() != null
			).collect(Collectors.toList());
		
		for( Element elem : possible_hooks){
			boolean is_hook = false;
			is_hook = check_is_hook(elem.getFeature(), elem);
			if (is_hook){
				elem.setIs_hook(true);
				elem.setIs_fPrivate(false);
				elem.setIs_fPublic(true);
				factInf.addInference(elem.getIdentifier() + " is hook because calls element(s) classified in future features", elem, elem.getFeature() );

				/*this line was commented on 07/22/2016, this is no longer necessary
				 * because fprivate does not propagate from fields to methods anymore -> PartitionInferencingHandler.propagate_fprivate */
				 remove_feature_hook_of_privates(elem, factInf, parent_feature); 
			}
		}
	}
	
	void recheck_hook(Element element, Fact factInf){
		ArrayList<Element> current_hooks = (ArrayList<Element>) element.getRefToThis().stream().filter( 
				e -> e.isIs_hook() == true 
				&& e.getFeature() != null
			).collect(Collectors.toList());
		
		for( Element elem : current_hooks){
			if(!check_is_hook(elem.getFeature(), elem)){
				elem.setIs_hook(false);
				factInf.addInference(elem.getIdentifier() + " is no longer hook called element(s) got reclassified", elem, elem.getFeature());
			}
		}
	}
	
	/*
	 * if the hook method calls fprivate 
	 * then the programmer needs to specify its correct feature
	 * because the current assignment was done for its relation with an fprivate element
	 */
	private void remove_feature_hook_of_privates(Element elem, Fact factInf, Feature parent_feature){
		if(elem.isIs_hook() == true
				&& elem.getFeature() != parent_feature
				&& elem.getRefFromThis().stream().filter(c -> c.isIs_fPrivate() == true).collect(Collectors.toList()).size() > 0){
			Feature feature = elem.getFeature();
			feature.removeElement(elem); //removes element from feature, and resets the element
			elem.setIs_hook(true); //reassign that it is hook (this is a fact)
			elem.setIs_fPrivate(false);
			elem.setIs_fPublic(true);
			factInf.addInference("Hook " + elem.getIdentifier() + " calls fprivate elements information about its feature to be determined.", elem, null);
		}
	}
}
