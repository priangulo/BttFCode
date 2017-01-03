package bttf;

import java.util.ArrayList;

public class FeatureBoundsWExp {
	private ArrayList<Feature> featureRange = new ArrayList<Feature>();
	private String eb_explanation = "";
	private String lb_explanation = "";
	
	public FeatureBoundsWExp(ArrayList<Feature> featureRange, String eb_explanation, String lb_explanation) {
		super();
		this.featureRange = featureRange;
		this.eb_explanation = eb_explanation;
		this.lb_explanation = lb_explanation;
	}

	public FeatureBoundsWExp(ArrayList<Feature> featureRange, FeatureBound eb, FeatureBound lb) {
		super();
		this.featureRange = featureRange;
		if(eb != null & eb.getExplanation() != null){
			this.eb_explanation = eb.getExplanation();
		}
		if(lb != null & lb.getExplanation() != null){
			this.lb_explanation = lb.getExplanation();
		}
		
	}
	
	public ArrayList<Feature> getFeatureRange() {
		return featureRange;
	}

	public String getEb_explanation() {
		return eb_explanation;
	}

	public String getLb_explanation() {
		return lb_explanation;
	}
	
	
}
