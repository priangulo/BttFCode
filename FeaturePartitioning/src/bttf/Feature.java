package bttf;

import java.util.ArrayList;

public class Feature {
	String feature_name;
	ArrayList<Element> feature_elements;
	
	public String getFeature_name() {
		return feature_name;
	}
	public void setFeature_name(String feature_name) {
		this.feature_name = feature_name;
	}
	public ArrayList<Element> getFeature_elements() {
		return feature_elements;
	}
	public void setFeature_elements(ArrayList<Element> feature_elements) {
		this.feature_elements = feature_elements;
	}
	@Override
	public String toString() {
		return "Feature [feature_name=" + feature_name + ", feature_elements=" + feature_elements.toString() + "]\n";
	}
	
	
}
