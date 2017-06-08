package bttf;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.stream.Collectors;

public class Partition {
	ArrayList<Reference> references_list;

	public Partition(ArrayList<Reference> references_list) {
		super();
		this.references_list = references_list;
	}

	//public ArrayList<Element> get_partitions(ArrayList<Reference> references_list){
	public ArrayList<Element> get_partitions(){
		ArrayList<Feature> feature_list = new ArrayList<Feature>();
		ArrayList<Cycle> cycle_list = new ArrayList<Cycle>();
		ArrayList<Element> elements_list = new ArrayList<Element>();
		ArrayList<String> from_elems = new ArrayList<String>();
		ArrayList<String> to_elems = new ArrayList<String>();
		ArrayList<String> from_and_to_elems = new ArrayList<String>();
		ArrayList<String> classified_elems = new ArrayList<String>();
		
		for(Reference reference : references_list){
			String call_from = reference.getCall_from();
			String call_to = reference.getCall_to();
			
			if(!from_elems.contains(call_from)){
				from_elems.add(call_from);
			}
			if(!to_elems.contains(call_to)){
				to_elems.add(call_to);
			}
		}
		
		for(String from_elem : from_elems){
			if(to_elems.contains(from_elem)){
				from_and_to_elems.add(from_elem);
			}
		}

		elements_list = set_earliest_times(elements_list, classified_elems, from_elems, to_elems, 0);
		elements_list = set_latest_times(references_list, elements_list, from_elems, to_elems);
		System.out.println("**************************");
		System.out.println(elements_list.toString());
		
		cycle_list = get_cycles(elements_list, from_and_to_elems);
		System.out.println("**************************");
		System.out.println(cycle_list.toString());
		
		//test_get_cycles();
		
		System.out.println("**************************");
		feature_list = ask_feature_hint_per_granularity(feature_list, elements_list);
		
		return elements_list;
		
	}
	
	//ArrayList<Element> set_earliest_times(ArrayList<Reference> ref_list, ArrayList<Element> elems_list, ArrayList<String> classified_elems, ArrayList<String> from_elems, ArrayList<String> to_elems, int time){
	ArrayList<Element> set_earliest_times(ArrayList<Element> elems_list, ArrayList<String> classified_elems, ArrayList<String> from_elems, ArrayList<String> to_elems, int time){
		ArrayList<String> new_classified_elems = new ArrayList<String>();
		
		if(!classified_elems.containsAll(to_elems)){
			for(String call_to : to_elems){
				if (!classified_elems.contains(call_to)){
					boolean dependent = false;
					for(Reference ref : references_list){
						if( !classified_elems.contains(ref.getCall_to()) && ref.getCall_from().equals(call_to)){
							dependent = true;
							break;
						}
					}
					if(!dependent){
						ElementType call_to_type = null;
						for(Reference ref : references_list){
							if (ref.getCall_to().equals(call_to)){
								call_to_type = ref.getCall_to_type();
								break;
							}
						}
						Element elem = new Element(call_to, call_to_type);
						elem.setEarliest_time(time);
						elems_list.add(elem);
						new_classified_elems.add(call_to);
					}
				}
			}
		}
		else{
			for(String call_from : from_elems){
				if (!classified_elems.contains(call_from)){
					ElementType call_from_type = null;
					for(Reference ref : references_list){
						if (ref.getCall_from().equals(call_from)){
							call_from_type = ref.getCall_from_type();
							break;
						}
					}
					Element elem = new Element(call_from, call_from_type);
					elem.setEarliest_time(time);
					elems_list.add(elem);
					new_classified_elems.add(call_from);
				}
			}
		}
		
		classified_elems.addAll(new_classified_elems);
		
		if(classified_elems.containsAll(from_elems) && classified_elems.containsAll(to_elems)){
			return elems_list;
		}
		
		time++;
		return set_earliest_times(elems_list, classified_elems, from_elems, to_elems, time);
	}
	
	ArrayList<Element> set_latest_times(ArrayList<Reference> ref_list, ArrayList<Element> elems_list, ArrayList<String> from_elems, ArrayList<String> to_elems){
		for(String call_to : to_elems){
			int latest_time = Integer.MAX_VALUE;
			for(Reference ref : ref_list){
				if( ref.getCall_to().equals(call_to) ){
					for(Element elem : elems_list){
						if(elem.getIdentificator().equals(ref.getCall_from())){
							if(elem.getEarliest_time() < latest_time){
								latest_time = elem.getEarliest_time();
							}
						}
					}
				}
			}
			for(Element elem : elems_list){
				if(elem.getIdentificator().equals(call_to)){
					elem.setLatest_time(latest_time);
				}
			}
		}
		
		for(String call_from : from_elems){
			if(!to_elems.contains(call_from)){
				for(Element elem : elems_list){
					if(elem.getIdentificator().equals(call_from)){
						elem.setLatest_time(elem.getEarliest_time());
					}
				}
			}
		}
	
		return elems_list;
	}
	
	ArrayList<Cycle> get_cycles(ArrayList<Element> elems_list, ArrayList<String> possible_elems_cycle){
		ArrayList<Cycle> cycle_list = new ArrayList<Cycle>();
		
		for(String elem : possible_elems_cycle){
			for(Reference ref : references_list){
				if( ref.getCall_to().equals(elem) && possible_elems_cycle.contains(ref.getCall_from()) ){
					ArrayList<String> accum_elems = new ArrayList<String>();
					accum_elems.add(elem);
					ArrayList<String> cycle_elems = get_connected_elements(elem, ref.getCall_from(), references_list, possible_elems_cycle, accum_elems);
					if (cycle_elems != null){
						cycle_elems = (ArrayList<String>) cycle_elems.stream().distinct().collect(Collectors.toList());
						Collections.sort(cycle_elems);
						ArrayList<Element> elems_in_cycle = new ArrayList<Element>();
						for(String elem_name : cycle_elems){
							Element elem_to_cycle = get_element_from_string(elems_list, elem_name);
							elem_to_cycle.setIn_cycle(true);
							elems_in_cycle.add(elem_to_cycle);
						}
						Cycle cycle = new Cycle();
						cycle.setElements(elems_in_cycle);
						if(!cycle_list.contains(cycle)){
							cycle_list.add(cycle);
						}
					}
				}
			}
		}
		
		return cycle_list;
	}
	
	Element get_element_from_string(ArrayList<Element> elems_list, String elem_name){
		for(Element elem : elems_list){
			if(elem.getIdentificator().equals(elem_name)){
				return elem;
			}
		}
		return null;
	}
	
	ArrayList<String> get_connected_elements(String current_elem, String final_elem, ArrayList<Reference> ref_list, ArrayList<String> possible_elems_cycle, ArrayList<String> accum_elems ){
		for(Reference ref : ref_list){
			if(ref.getCall_from().equals(current_elem) && possible_elems_cycle.contains(ref.getCall_to())){
				accum_elems.add(ref.getCall_to());
				if(!ref.getCall_from().equals(final_elem)){
					return get_connected_elements(ref.getCall_to(), final_elem, ref_list, possible_elems_cycle, accum_elems);
				}
			}
		}
		if ( accum_elems != null && accum_elems.size() > 1 && accum_elems.get(0).equals(accum_elems.get((accum_elems.size()-1)))){
			return accum_elems;
		}
		else {
			return null;
		}
	}
	
	/*void test_get_cycles(){
		ArrayList<Reference> ref_test = new ArrayList<Reference>();
		ref_test.add(new Reference("A", "B", null, null));
		ref_test.add(new Reference("B", "C", null, null));
		ref_test.add(new Reference("C", "D", null, null));
		ref_test.add(new Reference("C", "R", null, null));
		ref_test.add(new Reference("D", "B", null, null));
		
		ArrayList<Element> elems_test = new ArrayList<Element>();
		elems_test.add(new Element("A", null));
		elems_test.add(new Element("B", null));
		elems_test.add(new Element("C", null));
		elems_test.add(new Element("D", null));
		elems_test.add(new Element("R", null));
		
		ArrayList<String> fr_to_elems_test = new ArrayList<String>();
		fr_to_elems_test.add("B");
		fr_to_elems_test.add("C");
		fr_to_elems_test.add("D");
		
		ArrayList<Cycle> cycle_list_test = get_cycles(ref_test, elems_test, fr_to_elems_test);
		System.out.println("CYCLE TEST **************************");
		System.out.println(cycle_list_test .toString());
	}	*/

	ArrayList<Feature> ask_feature_hint_per_granularity(ArrayList<Feature> feature_list, ArrayList<Element> elems_list){
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
		for(ElementType elementType : ElementType.values()){
			int granularity = elementType.get_element_granularity();
			ArrayList<Element> elems_to_classify = new ArrayList<Element>();
			elems_to_classify = (ArrayList<Element>) elems_list.stream().filter(elem -> 
				elem.isIs_fPrivate() == false && 
				elem.isIs_fPublic() == false && 
				elem.getElement_type().get_element_granularity() == granularity
				).collect(Collectors.toList());

			System.out.println("**************************");
			System.out.println("ELEMENTS TO CLASSIFY");
			System.out.println(elems_to_classify.toString());
			
			for(Element elem : elems_to_classify){
				try {
					System.out.println("**************************");
					System.out.println("Does element: <<" + elem.getIdentificator() + ">> belongs completely to a single Feature? (Y/N)");
					String answer = bufferedReader.readLine();
					if(answer.trim().toUpperCase().equals("Y")){
						boolean assigned = false;
						if(feature_list.size() > 0){
							for(Feature feature : feature_list){
								System.out.println("Does <<" + elem.getIdentificator() + ">> belong in Feature <<" + feature.getFeature_name() + ">>? (Y/N)");
								answer = bufferedReader.readLine();
								if(answer.trim().toUpperCase().equals("Y")){
									assigned = true;
									System.out.println("Is <<" + elem.getIdentificator() + ">> private of the feature? (Y/N) ... N means is public");
									answer = bufferedReader.readLine();
									if(answer.trim().toUpperCase().equals("Y")){
										elem.setIs_fPrivate(true);
										recalculate_private_elems(elems_list, elem, feature);
									}
									else {
										elem.setIs_fPublic(true);
									}
									ArrayList<Element> feature_elements = feature.getFeature_elements();
									feature_elements.add(elem);
									feature.setFeature_elements(feature_elements);
									
									//TODO : RECALCULATE GIVEN THE HINT
									
									break;
								}
							}
						}
						if(!assigned){
							if(feature_list.size() == 0){
								System.out.println("Any Feature has been created yet. Provide the name of the feature where <<" + elem.getIdentificator() + ">> will belong");
							}
							else{
								System.out.println("Provide the name of the feature where <<" + elem.getIdentificator() + ">> will belong");
							}
							answer = bufferedReader.readLine();
							Feature new_feature = new Feature();
							new_feature.setFeature_name(answer);
							ArrayList<Element> feature_elements = new ArrayList<Element>();
							new_feature.setFeature_elements(feature_elements);
							
							System.out.println("Is <<" + elem.getIdentificator() + ">> private of the feature? (Y/N) ... N means is public");
							answer = bufferedReader.readLine();
							if(answer.trim().toUpperCase().equals("Y")){
								elem.setIs_fPrivate(true);
								recalculate_private_elems(elems_list, elem, new_feature);
							}
							else {
								elem.setIs_fPublic(true);
							}
							
							feature_elements = new_feature.getFeature_elements();
							feature_elements.add(elem);
							new_feature.setFeature_elements(feature_elements);
							feature_list.add(new_feature);
							
							//TODO : RECALCULATE GIVEN THE HINT
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		System.out.println(feature_list.toString());
		
		return feature_list;
	}
	
	void recalculate_private_elems(ArrayList<Element> elems_list, Element private_elem, Feature feature){
		String private_elem_name = private_elem.getIdentificator();
		ArrayList<Reference> current_call_from = (ArrayList<Reference>) references_list.stream().filter(ref -> ref.getCall_to().equals(private_elem_name)).collect(Collectors.toList());
		for(Reference ref : current_call_from){
			Element elem = get_element_from_string(elems_list, ref.getCall_from());
			elem.setIs_fPrivate(true);
			ArrayList<Element> feature_elements = feature.getFeature_elements();
			if(!feature_elements.contains(elem)){
				feature_elements.add(elem);
				feature.setFeature_elements(feature_elements);
			}
			recalculate_private_elems(elems_list, elem, feature);
		}
	}
	
}
