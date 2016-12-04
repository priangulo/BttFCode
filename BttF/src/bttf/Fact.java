package bttf;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class Fact {
	private String fact;
	private Element element;
	private Boolean element_isfprivate;
	private Boolean element_isfpublic;
	private Boolean element_ishook;
	private Feature feature;
	private ArrayList<Inference> inferences = new ArrayList<Inference>();

	public String getFact() {
		return fact;
	}
	public void setFact(String fact, Element element, Boolean element_isfprivate, Boolean element_isfpublic, Boolean element_ishook, Feature feature) {
		this.fact = fact;
		this.element = element;
		this.element_isfprivate = element_isfprivate;
		this.element_isfpublic = element_isfpublic;
		this.element_ishook = element_ishook;
		this.feature = feature;
	}
	public ArrayList<String> getInferences_text() {
		return (ArrayList<String>) inferences.stream().map(i -> i.getInference()).collect(Collectors.toList());
	}
	
	public void addInference(String inference, Element element, Boolean element_isfprivate, Boolean element_isfpublic,
			Boolean element_ishook, Feature feature){
		Inference inf = new Inference(inference, element, element_isfprivate, element_isfpublic, element_ishook, feature);
		if(!inferences.contains(inf)){
			inferences.add(inf);
		}
	}
	
	public void addInference(String inference, Element element, Feature feature){
		Inference inf = new Inference(inference, element, element.isIs_fPrivate(), element.isIs_fPublic(), element.isIs_hook(), feature);
		if(!inferences.contains(inf)){
			inferences.add(inf);
		}
	}
	
	public ArrayList<Element> getElements_Inf() {
		return (ArrayList<Element>) inferences.stream().map(i -> i.getElement()).filter(e -> e != null).collect(Collectors.toList());
	}
	
	public void retractFact(){
		System.out.println("retract fact and inferences...");
		
		for(Inference inf : inferences){
			if(inf.getElement() != null){
				System.out.println("\tinferred element: " + inf.getElement().toString());
				Feature feature = inf.getElement().getFeature();
				if(feature != null){
					feature.removeElement(inf.getElement());
				}
			}
		}
		System.out.println("\tfact element: " + element.toString());
		Feature feature = element.getFeature();
		if(feature != null){
			feature.removeElement(element);
		}
	}
	@Override
	public boolean equals(Object object) {
		if (object != null && object instanceof Fact)
        {
            if( this.element.equals( ((Fact)object).element)
            		&& this.feature.equals( ((Fact)object).feature)
            		&& this.element_isfprivate.equals( ((Fact)object).element_isfprivate)
            		){
            	return true;
            }
        }
        return false;
	}
	public Element getElement() {
		return element;
	}
	public void setElement(Element element) {
		this.element = element;
	}
	public Boolean getElement_isfprivate() {
		return element_isfprivate;
	}
	
	public Boolean getElement_isfpublic() {
		return element_isfpublic;
	}
	
	public Boolean getElement_ishook() {
		return element_ishook;
	}
	
	public Feature getFeature() {
		return feature;
	}
	public void setFeature(Feature feature) {
		this.feature = feature;
	}
	public ArrayList<Inference> getInferences() {
		return inferences;
	}
	public void setFact(String fact) {
		this.fact = fact;
	}
	@Override
	public String toString() {
		return "Fact [fact=" + fact + ", element=" + element + ", feature=" + feature + "]\n";
	}
	
	
}
