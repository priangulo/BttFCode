package bttf;

public class FactInference {
	private Element element;
	private boolean isFact;
	private String text;
	private Feature feature;
	
	public FactInference(Fact fact){
		if(fact != null){
			this.element = fact.getElement();
			this.isFact = true;
			this.text = fact.getFact();
			this.feature = fact.getFeature();
		}
	}
	
	public FactInference(Inference inference){
		if(inference != null){
			this.element = inference.getElement();
			this.isFact = false;
			this.text = inference.getInference();
			this.feature = inference.getFeature();
		}
	}

	public Element getElement() {
		return element;
	}

	public boolean isFact() {
		return isFact;
	}

	public String getText() {
		return text;
	}
	
	public Feature getFeature(){
		return feature;
	}
	
	
}
