package bttf;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class PartitionInferencingHandler {
	private PartitionHookHandler hookHandler = new PartitionHookHandler();
	private PartitionCycleHandler cycleHandler = new PartitionCycleHandler();
	private PartitionHelper partHelper; 
	private boolean cycle_stuff_on;
	private ArrayList<Cycle> cycle_list;
	private boolean is_fwpi;
	private Partition partition;
	private int orderOfAssignment;
	
	public PartitionInferencingHandler(boolean cycle_stuff_on, ArrayList<Cycle> cycle_list, boolean is_fwpi, Partition partition){
		this.cycle_stuff_on = cycle_stuff_on;
		this.cycle_list = cycle_list;
		this.is_fwpi = is_fwpi;
		this.partition = partition;
		this.partHelper = new PartitionHelper(this.partition);
		orderOfAssignment = 1;
	}
	
	/*
	 * adds an element to a feature
	 * generates inferences
	 */
	void add_element_to_feature(Fact factInf, Feature feature_to_assign, Element element, boolean is_fprivate, boolean is_fpublic, Feature parent_feature, boolean by_inference){
		FeatureBound eb = this.partition.boundsCalc.get_earliest_bound(element, partition.get_all_features(), parent_feature);
		FeatureBound lb = this.partition.boundsCalc.get_latest_bound(element, partition.get_all_features(), parent_feature);
		FeaturePossibility poss = this.partition.boundsCalc.get_element_possibilities(element, parent_feature, false);
		
		if(eb != null && eb.getFeature() != null){
			element.setEarliest_bound(eb.getFeature());
		}
		if(lb != null && lb.getFeature() != null){
			element.setLatest_bound(lb.getFeature());
		}
		
		if(poss != null & poss.getButton_options() != null){
			element.setNumber_possibilities(poss.getButton_options().size());
		}
		
		boolean already_hook = element.isIs_hook();
		boolean is_hook = hookHandler.check_is_hook(feature_to_assign, element);
		
		if(is_hook){
			is_fprivate = false;
			is_fpublic = true;
		}
		
		if(!already_hook && is_hook){
			factInf.addInference(element.getIdentifier() + ", BttF says it's a hook because it references declaration(s) classified in future features", element, is_fprivate, is_fpublic, is_hook, feature_to_assign);
		}
		
		if(already_hook && !is_hook){
			factInf.addInference(element.getIdentifier() + ", BttF says it's no longer a hook, referenced declaration(s) got reclassified", element, is_fprivate, is_fpublic, is_hook, feature_to_assign);
		}
		
		if(element.getFeature()!= null){
			Feature orig_feature = element.getFeature();
			orig_feature.removeElement(element);
		}
		
		feature_to_assign.addElement(element, is_fprivate, is_fpublic, is_hook, by_inference);
		element.setOrderOfAssignment(orderOfAssignment);
		orderOfAssignment++;
		
		if(is_fprivate){
			propagate_childs_of_fprivate_containers(element, feature_to_assign, factInf, parent_feature);
			propagate_fprivate(element, feature_to_assign, factInf, parent_feature);
		}
		
		propagate_childs_with_no_calls(element, feature_to_assign, factInf, parent_feature);

		if(cycle_stuff_on){
			cycleHandler.propagate_elems_in_cycle(cycle_list, element, feature_to_assign, factInf);
		}
		
		//if we are partitioning into fw+pi and we have a non terminal class, 
		//all of its members go to its same feature
		if(is_fwpi){
			if(element.getElement_type().equals(ElementType.ELEM_TYPE_CLASS)){
				//check if it not should be pulled apart and add all of its members go to its same feature
				propagate_members_of_unbreakable_fwpi(element, feature_to_assign, factInf, parent_feature);
				//propagate_members_of_nonterminal(element, feature_to_assign, factInf, parent_feature);
			}
			if(element.getElement_type().equals(ElementType.ELEM_TYPE_METHOD)){
				propagate_reffrom_cannotbehook_fwpi(element, feature_to_assign, factInf, parent_feature);
				propagate_reffrom_staticmethodmember_fwpi(element, feature_to_assign, factInf, parent_feature);
			}
		}
		hookHandler.late_check_for_hooks(element, factInf, parent_feature);
		hookHandler.recheck_hook(element, factInf);
		
	}
	
	/*
	 * when an element is assigned private of a feature, 
	 * the elements that reference it are assigned also to that same feature
	 */
	void propagate_fprivate(Element private_element, Feature feature_to_assign, Fact factInf, Feature parent_feature){
		for(Element elem : private_element.getRefToThis().stream()
				.filter(e -> e.getFeature() == parent_feature && e.isIs_hook() == false)
				.collect(Collectors.toList())){
			//if(private_element.getElement_type().equals(ElementType.ELEM_TYPE_FIELD) && elem.getElement_type().equals(ElementType.ELEM_TYPE_METHOD)){
				//do not propagate fprivate of fields to methods, they may be hooks
			//}
			//do not propagate fprivate of fields to methods, they may be hooks
			//if(!elem.getElement_type().equals(ElementType.ELEM_TYPE_METHOD))
			//{
				factInf.addInference(elem.getIdentifier() + ", BttF says since it calls fprivate " + private_element.getIdentifier() + 
						" THEN it also belongs to " + feature_to_assign.getFeature_name(), elem, feature_to_assign);
				if(feature_to_assign.getIs_last_feature()){
					add_element_to_feature(factInf, feature_to_assign, elem, true, false, parent_feature, true);
				}
				else{
					add_element_to_feature(factInf, feature_to_assign, elem, false, false, parent_feature, true);
				}
				
			//}
			
			
			
			
		}
	}
	
	/*
	 * Assigns the same feature to child elements that are not called from anynone
	 * And only call its parent
	 */
	void propagate_childs_with_no_calls(Element element, Feature feature_to_assign, Fact factInf, Feature parent_feature){
		for(Element elem : element.getRefToThis()){
			if( elem.getParentName()!= null 
					&& elem.getParentName().equals(element.getIdentifier()) 
					&& elem.getRefToThis().size() == 0
					&& elem.getRefFromThis().size() == 1
					&& elem.getRefFromThis().get(0).equals(elem.getParentName())
					&& elem.getFeature() == parent_feature){
				factInf.addInference(elem.getIdentifier() + ", BttF says it's a child of " + element.getIdentifier() + 
						" with no callers and only calls its parent THEN it also belongs to " + feature_to_assign.getFeature_name(), elem, feature_to_assign);
				add_element_to_feature(factInf, feature_to_assign, elem, false, false, parent_feature, true);
			}
		}
	}
	
	/* DEPRECATED
	 * Assigns the same feature to child of a container that only calls siblings
	 * this recursive method is triggered by adding a container as fprivate
	 */
	void propagate_childs_of_fprivate_that_only_call_relatives(Element element, Feature feature_to_assign, Fact factInf, Feature parent_feature){
		for(Element child : element.getRefToThis()){
			if(child != element && check_only_call_relatives(child) && child.getFeature() == parent_feature){
				factInf.addInference(child.getIdentifier() + ", BttF says it's a child of " + element.getIdentifier() + 
						" that only calls relatives THEN it also belongs to " + feature_to_assign.getFeature_name(), child, feature_to_assign);
				add_element_to_feature(factInf, feature_to_assign, child, false, false, parent_feature, true);
				propagate_childs_of_fprivate_that_only_call_relatives(child, feature_to_assign, factInf, parent_feature);
			}
		}
	}
	
	/*
	 * assigns as fprivate all the childs on a fprivate package or class
	 */
	void propagate_childs_of_fprivate_containers(Element element, Feature feature_to_assign, Fact factInf, Feature parent_feature){
		for(Element child : element.getRefToThis()){
			if(child.getParentName().equals(element.getIdentifier()) && child.getFeature() == parent_feature){
				if(child.getElement_type().equals(ElementType.ELEM_TYPE_METHOD) && child.isIs_hook()){
					factInf.addInference("Hook " + child.getIdentifier() + ", BttF says it's a child of " + element.getIdentifier() + 
							" THEN it also belongs to " + feature_to_assign.getFeature_name(), child, feature_to_assign);
					add_element_to_feature(factInf, feature_to_assign, child, false, true, parent_feature, true);
				}
				else{
					factInf.addInference(child.getIdentifier() + ", BttF says it's a child of " + element.getIdentifier() + 
							" THEN it also is fprivate of " + feature_to_assign.getFeature_name(), child, feature_to_assign);
					add_element_to_feature(factInf, feature_to_assign, child, true, false, parent_feature, true);
					propagate_childs_of_fprivate_containers(child, feature_to_assign, factInf, parent_feature);
				}
			}
		}
	}
	
	/* DEPRECATED
	 * Checks if an element only calls to brother or father elements 
	 */
	private boolean check_only_call_relatives(Element element){
		ArrayList<Element> relatives = new ArrayList<Element>();
		for(Element elem_called : element.getRefFromThis()){
			//if they are brothers
			if(element.getParentName() != null && elem_called.getParentName() != null && element.getParentName().equals(elem_called.getParentName())){
				relatives.add(elem_called);
			}
			//if they are son - father
			if(element.getParentName() != null && element.getParentName().equals(elem_called.getIdentifier())){
				relatives.add(elem_called);
			}			
		}
		if (relatives.size() == element.getRefFromThis().size()){
			return true;
		}
		return false;
	}
	
	void propagate_members_of_nonterminal(Element element, Feature feature_to_assign, Fact factInf, Feature parent_feature){
		if(element.getElement_type().equals(ElementType.ELEM_TYPE_CLASS) && !element.isIs_terminal()){
			String reason = ", BttF says it's a public/protected member of non-terminal class ";
			for(Element member : element.getChildrenDeclarations()){
				if(!member.getModifier().contains("private")){
					if(member.getFeature() != null){
						Feature prevFeature = member.getFeature();
						prevFeature.removeElement(member);
					}
					boolean is_hook = false;
					if(member.getElement_type().equals(ElementType.ELEM_TYPE_METHOD)){
						is_hook = hookHandler.check_is_hook(feature_to_assign, element);
					}
					factInf.addInference(member.getIdentifier() + reason + element.getIdentifier() + 
							" THEN it also has to belong to " + feature_to_assign.getFeature_name(), member, feature_to_assign);
					//feature_to_assign.addElement(member, element.isIs_fPrivate(), element.isIs_fPublic(), is_hook, true);
					add_element_to_feature(factInf, feature_to_assign, member, element.isIs_fPrivate(), element.isIs_fPublic(), parent_feature, true);
					
					if(member.getElement_type().equals(ElementType.ELEM_TYPE_METHOD)){
						propagate_reffrom_cannotbehook_fwpi(member, feature_to_assign, factInf, parent_feature);
					}
				}
			}
		}
	}
	
	void propagate_members_of_unbreakable_fwpi(Element element, Feature feature_to_assign, Fact factInf, Feature parent_feature){
		if(element.getElement_type().equals(ElementType.ELEM_TYPE_CLASS) && 
				( element.getOrigType().equals(Element.ORIGTYPE_ANNOTATION) || element.getOrigType().equals(Element.ORIGTYPE_ENUM))
			){
			String reason = "";
			if(element.getOrigType().equals(Element.ORIGTYPE_ANNOTATION)) {reason = ", BttF says it's a member of an Annotation ";}
			else if(element.getOrigType().equals(Element.ORIGTYPE_ENUM)) {reason = ", BttF says it's a member of an Enum ";}
			
			//get members of the class
			for(Element member : element.getChildrenDeclarations()){
				if(member.getFeature() != null){
					Feature prevFeature = member.getFeature();
					prevFeature.removeElement(member);
				}
				boolean is_hook = false;
				if(member.getElement_type().equals(ElementType.ELEM_TYPE_METHOD)){
					is_hook = hookHandler.check_is_hook(feature_to_assign, element);
				}
				factInf.addInference(member.getIdentifier() + reason + element.getIdentifier() + 
						" THEN it also has to belong to " + feature_to_assign.getFeature_name(), member, feature_to_assign);
				feature_to_assign.addElement(member, element.isIs_fPrivate(), element.isIs_fPublic(), is_hook, true);
				//add_element_to_feature(factInf, feature_to_assign, member, element.isIs_fPrivate(), element.isIs_fPublic(), parent_feature, true);
			}
		}	
	}
	
	/*
	 * A method on a fw non-terminal class CANNOT be a hook
	 * Change the declarations it references to be part of the fw too
	*/
	void propagate_reffrom_cannotbehook_fwpi(Element element, Feature feature_to_assign, Fact factInf, Feature parent_feature){
		Feature fwFeature = partHelper.get_feature_by_name(Feature.FW_FEATURE_NAME);
		
		if( feature_to_assign.equals(fwFeature)
				&& element.getElement_type().equals(ElementType.ELEM_TYPE_METHOD) 
				&& ( !element.getParentDeclaration().isIs_terminal()
						|| element.getModifier().contains(PartitioningConstants.STATIC) )
		){
			String reason = "";
			if(element.getModifier().contains(PartitioningConstants.STATIC)){
				reason = ", BttF says a static method of a framework class references it, this method cannot be hook, ";
			}
			else{
				reason = ", BttF says a method of a non-terminal class references it, this method cannot be hook,  ";
			}
			
			for(Element ref : element.getRefFromThis()){
				if( 
					(element.getModifier().contains(PartitioningConstants.STATIC)
					&& !ref.getElement_type().equals(ElementType.ELEM_TYPE_PACKAGE)
					&& !ref.getModifier().toLowerCase().contains("private")	)
				||
					(!element.getModifier().contains(PartitioningConstants.STATIC)
					&& !ref.getElement_type().equals(ElementType.ELEM_TYPE_PACKAGE))
				){
					if(ref.getFeature() != null && !ref.getFeature().equals(fwFeature)){
						Feature prevFeature = ref.getFeature();
						prevFeature.removeElement(ref);
					}
					boolean is_hook = false;
					if(ref.getElement_type().equals(ElementType.ELEM_TYPE_METHOD)){
						is_hook = hookHandler.check_is_hook(feature_to_assign, ref);
					}
					factInf.addInference(ref.getIdentifier() + reason + 
							" THEN it also has to belong to " + feature_to_assign.getFeature_name(), ref, feature_to_assign);
					//add_element_to_feature(factInf, feature_to_assign, ref, ref.isIs_fPrivate(), ref.isIs_fPublic(), parent_feature, true);
					feature_to_assign.addElement(ref, ref.isIs_fPrivate(), ref.isIs_fPublic(), is_hook, true);
					propagate_members_of_unbreakable_fwpi(ref, feature_to_assign, factInf, parent_feature);
					propagate_reffrom_staticmethodmember_fwpi(ref, feature_to_assign, factInf, parent_feature);
				}
			}
			element.setIs_hook(false);
			element.setIs_fPrivate(false);
			element.setIs_fPublic(true);
		}	
	}
	
	
	/*
	 * A static method on a fw class CANNOT be a hook
	 * Change the declarations it references to be part of the fw too
	*/
	void propagate_reffrom_staticmethodmember_fwpi(Element element, Feature feature_to_assign, Fact factInf, Feature parent_feature){
		Feature fwFeature = partHelper.get_feature_by_name(Feature.FW_FEATURE_NAME);
		
		if( feature_to_assign.equals(fwFeature)
				&& element.getElement_type().equals(ElementType.ELEM_TYPE_METHOD) 
				&& element.getModifier().contains(PartitioningConstants.STATIC)
			){
			String reason = ", BttF says a static method of a framework class references it ";
			
			for(Element ref : element.getRefFromThis()){
				if(!ref.getElement_type().equals(ElementType.ELEM_TYPE_PACKAGE) 
						&& !ref.getModifier().toLowerCase().contains("private")
				){
					if(ref.getFeature() != null && !ref.getFeature().equals(fwFeature)){
						Feature prevFeature = ref.getFeature();
						prevFeature.removeElement(ref);
					}
					boolean is_hook = false;
					if(ref.getElement_type().equals(ElementType.ELEM_TYPE_METHOD)){
						is_hook = hookHandler.check_is_hook(feature_to_assign, ref);
					}
					factInf.addInference(ref.getIdentifier() + reason + ref.getIdentifier() + 
							" THEN it also has to belong to " + feature_to_assign.getFeature_name(), ref, feature_to_assign);
					//add_element_to_feature(factInf, feature_to_assign, ref, ref.isIs_fPrivate(), ref.isIs_fPublic(), parent_feature, true);
					feature_to_assign.addElement(ref, ref.isIs_fPrivate(), ref.isIs_fPublic(), is_hook, true);
					propagate_members_of_unbreakable_fwpi(ref, feature_to_assign, factInf, parent_feature);
				}
			}
			element.setIs_hook(false);
			element.setIs_fPrivate(false);
			element.setIs_fPublic(true);
		}	
	}
}
