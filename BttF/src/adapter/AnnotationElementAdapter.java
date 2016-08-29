package adapter;

import java.util.ArrayList;
import annotation.AnnotationElement;
import bttf.Element;

public class AnnotationElementAdapter {
	public AnnotationElement adaptfromElementToAnnotationElement(Element element){
		if(element != null){
			AnnotationElement aelement = new AnnotationElement(
					element.getIdentifier(),
					element.getElement_type().get_element_type().toUpperCase(),
					getAnnotation(element.getFeature().getFeature_name())
				);
			return aelement;
		}
		return null;
	}
	
	public ArrayList<AnnotationElement> adaptElementList_to_AnnotationElementList(ArrayList<Element> elementList){
		if(elementList != null){
			ArrayList<AnnotationElement> aelemList = new ArrayList<AnnotationElement>();
			for(Element e : elementList){
				aelemList.add(adaptfromElementToAnnotationElement(e));
			}
			return aelemList;
		}
		return null;
	}
	
	private String getAnnotation(String feature){
		return feature;
	}
		
}
