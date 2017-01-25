package bttf;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class PartitionCycleHandler {
	private ArrayList<Cycle> cycle_list = new ArrayList<Cycle>(); 
	/*
	 * if an element in cycle is assigned to a feature, 
	 * the elements in the same cycle are assigned to that same feature
	 */
	/*
	 * Search cycles among the elements
	 * */
	ArrayList<Cycle> get_cycles(ArrayList<Element> elements_list){
		//get elements that are called from other, and call others
		//those are cycle element candidates
		ArrayList<Element> possible_elems_cycle = (ArrayList<Element>) elements_list.stream().filter(e -> e.getRefToThis().size() > 0 && e.getRefFromThis().size() > 0).collect(Collectors.toList());
		
		//search if eventually the starting element is found
		for(Element elem : possible_elems_cycle){
			search_cycle(possible_elems_cycle, new ArrayList<Element>(), elem, elem);
		}
		
		System.out.println("CYCLE LIST: \n" + cycle_list.toString());
		
		return this.cycle_list;
	}
	
	/*
	 * Recursive method used by get_cycles()
	 * */
	private void search_cycle(ArrayList<Element> possible_elems_cycle, ArrayList<Element> cycle_elements, Element current_element, Element searched_elem){
		cycle_elements.add(current_element);
		
		//The starting element was found, then the cycle is added to the list
		//cycle elements size check to prevent adding recursive calls
		if(current_element.getRefFromThis().contains(searched_elem) && cycle_elements.size() > 1){
			Cycle new_cycle = new Cycle();
			new_cycle.setElements(cycle_elements);
			if(!cycle_list.contains(new_cycle)){
				cycle_list.add(new_cycle);
			}
		}
		//Starting element not found yet, search in next inner level
		else{
			for(Element elem : current_element.getRefFromThis()){
				//only consider routes that haven't been explored and elements that are not a dead end 
				if(!cycle_elements.contains(elem) && possible_elems_cycle.contains(elem)){
					search_cycle(possible_elems_cycle, cycle_elements, elem, searched_elem);
				}
			}
		}
		
	}
	
	public void propagate_elems_in_cycle(ArrayList<Cycle> cycle_list, Element element, Feature feature, Fact factInf){
		if (element.isIn_cycle()){
			System.out.println(cycle_list.toString());
			for(Cycle cycle : cycle_list){
				if(cycle.getElements().contains(element)){
					for(Element e : cycle.getElements()){
						if(e != element){
							String feat_mod = " is " + (element.isIs_fPrivate() ? "fprivate" : "fpublic") + " of ";
							factInf.addInference(e.getIdentifier() + " is in a cycle with " + element.getIdentifier() + 
									" THEN it also " + feat_mod + feature.getFeature_name(), e, feature);
							feature.addElement(e, element.isIs_fPrivate(), element.isIs_fPublic(), false, true);
						}
					}
				}
			}
		}
	}
}
