package bttf;

import java.util.ArrayList;

import gui.OptionButton;

public class FeaturePossibility {
	private String explanations_text = "";
	private ArrayList<OptionButton> button_options = new ArrayList<OptionButton>();
	private ArrayList<Feature> feature_options = new ArrayList<Feature>(); 
	
	public FeaturePossibility(String explanations_text, ArrayList<OptionButton> button_options,
			ArrayList<Feature> feature_options) {
		super();
		this.explanations_text = explanations_text;
		this.button_options = button_options;
		this.feature_options = feature_options;
	}

	public String getExplanations_text() {
		return explanations_text;
	}
	
	public ArrayList<OptionButton> getButton_options() {
		return button_options;
	}

	public ArrayList<Feature> getFeature_options() {
		return feature_options;
	}
	
	
	
}
