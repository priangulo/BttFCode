package gui;

public class InvalidFileFact {
	public final static String ELEMENT_DOESNT_EXIST = "BttF says: Declaration's identifier doesn't exist.";
	public final static String ELEMENTTYPE_DOESNT_EXIST = "BttF says: Element Type doesn't exist.";
	public final static String FEATURE_DOESNT_EXIST = "BttF says: Feature doesn't exist.";
	public final static String FEATURE_ASSIGNED = "BttF insists it belongs to";
	public final static String FEATURE_INVALID = "BttF says the selected feature is not valid";
	
	private String element_identifier;
	private String fact;
	private String explanation;
	
	public InvalidFileFact(String element_identifier, String fact, String explanation) {
		this.element_identifier = element_identifier;
		this.fact = fact;
		this.explanation = explanation;
	}

	public String getElement_identifier() {
		return element_identifier;
	}

	public String getFact() {
		return fact;
	}

	public String getExplanation() {
		return explanation;
	}

	public String displayInvalidFact() {
		return "Element: " + element_identifier + ", Provided fact: " + fact + ", Reason: "
				+ explanation + "\n";
	}
	
	@Override
	public String toString() {
		return "InvalidFileFacts [element_identifier=" + element_identifier + ", fact=" + fact + ", explanation="
				+ explanation + "]";
	}
	
}
