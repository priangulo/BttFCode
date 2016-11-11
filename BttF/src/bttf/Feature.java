package bttf;

import java.util.ArrayList;

public class Feature {
	private String feature_name;
	private ArrayList<Element> feature_elements = new ArrayList<Element>();
	private int order;
	private ArrayList<Element> dont_belong = new ArrayList<Element>();
	private Boolean in_current_task;
	private Feature parent_feature;
	private boolean is_last_feature;
	private boolean was_last_feature;
	
	public Feature(String feature_name, int order, Boolean in_current_task, Feature parent_feature, boolean is_last_feature) {
		this.feature_name = feature_name;
		this.order = order;
		this.in_current_task = in_current_task;
		this.parent_feature = parent_feature;
		this.is_last_feature = is_last_feature;
		this.was_last_feature = is_last_feature;
	}
	
	void resetFeature(){
		this.feature_elements = new ArrayList<Element>();
		this.dont_belong = new ArrayList<Element>();
	}
	
	public String getFeature_name() {
		return feature_name;
	}
	public ArrayList<Element> getFeature_elements() {
		return feature_elements;
	}
	public void setFeature_elements(ArrayList<Element> feature_elements) {
		this.feature_elements = feature_elements;
	}
	public int getOrder() {
		return order;
	}
	public void setOrder(int order) {
		this.order = order;
	}
	public ArrayList<Element> getDont_belong() {
		return dont_belong;
	}
	public void setDont_belong(ArrayList<Element> dont_belong) {
		this.dont_belong = dont_belong;
	}	
	public Boolean getIn_current_task() {
		return in_current_task;
	}
	public void setIn_current_task(Boolean in_current_task) {
		this.in_current_task = in_current_task;
	}
	public Feature getParent_feature() {
		return parent_feature;
	}
	public boolean getIs_last_feature(){
		return is_last_feature;
	}
	public void setIs_last_feature(boolean is_last_feature){
		this.is_last_feature = is_last_feature;
	}
	public boolean getWas_last_feature() {
		return was_last_feature;
	}
	public void setWas_last_feature(boolean was_last_feature) {
		this.was_last_feature = was_last_feature;
	}

	public void addElement(Element element, boolean is_fPrivate, boolean is_fPublic, boolean is_hook){
		element.setFeature(this);
		element.setIs_fPrivate(is_fPrivate);
		element.setIs_fPublic(is_fPublic);
		element.setIs_hook(is_hook);
		if(!feature_elements.contains(element)){
			feature_elements.add(element);
		}
	}
	public void removeElement(Element element){
		element.setFeature(null);
		element.setIs_fPrivate(false);
		element.setIs_fPublic(false);
		element.setIs_hook(false);
		if(feature_elements.contains(element)){
			feature_elements.remove(element);
		}
	}
	
	public Feature get_feature_or_parent_in_task(){
		Feature feature_in_task = this;
		while(feature_in_task != null && !in_current_task){
			feature_in_task = this.parent_feature;
		}
		return feature_in_task; 
	}
	
	public void addDontBelongElement(Element element){
		if(!dont_belong.contains(element)){
			dont_belong.add(element);
		}
	}
	public Boolean isChildOf(Feature feature){
		Feature parent = this.parent_feature;
		if(parent != null && parent.equals(feature)){
			return true;
		}
		else if (parent != null ){
			return parent.isChildOf(feature);
		}
		else{
			return false;
		}
	}
	
	@Override
	public String toString() {
		return "Feature [feature_name=" + feature_name + ", order=" + order + " islast=" + is_last_feature + "]\n";
	}
	@Override
	public boolean equals(Object object) {
		if (object != null && object instanceof Feature)
        {
            if( this.feature_name.equals( ((Feature)object).feature_name)){
            	return true;
            }
        }
        return false;
	}

	

}
