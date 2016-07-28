package bttf;

public class Inference {
	private String inference;
	private Element element;
	private Boolean element_isfprivate;
	private Boolean element_isfpublic;
	private Boolean element_ishook;
	private Feature feature;
	
	public String getInference() {
		return inference;
	}
	public void setInference(String inference) {
		this.inference = inference;
	}
	public Element getElement() {
		return element;
	}
	public void setElement(Element element) {
		this.element = element;
	}

	public Inference(String inference, Element element, Boolean element_isfprivate, Boolean element_isfpublic,
			Boolean element_ishook, Feature feature) {
		this.inference = inference;
		this.element = element;
		this.element_isfprivate = element_isfprivate;
		this.element_isfpublic = element_isfpublic;
		this.element_ishook = element_ishook;
		this.feature = feature;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj != null && obj instanceof Inference)
        {
            if( this.inference.equals( ((Inference)obj).inference) && this.element.equals( ((Inference)obj).element)){
            	return true;
            }
        }
        return false;
	}
	
	@Override
	public String toString() {
		if(element != null){
			return element.getIdentifier() + ": " + inference;
		}
		else return inference;
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
	
	

}
