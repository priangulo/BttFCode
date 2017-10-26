package adapter;

import bttf.ElementType;

public class GAML_Adapter {
	public static final String S_SPECIES = "S_Species";
	public static final String S_DECLARATION = "S_Declaration";
	public static final String S_ACTION = "S_Action";
	public static final String S_EXPERIMENT = "S_Experiment";
	public static final String S_REFLEX = "S_Reflex";

	
	public ElementType mapGAMLTypeToJavaType(String type){
		switch (type){
			case S_SPECIES: return ElementType.ELEM_TYPE_CLASS;
			case S_DECLARATION: return ElementType.ELEM_TYPE_FIELD;
			case S_ACTION: return ElementType.ELEM_TYPE_METHOD;
			case S_EXPERIMENT: return ElementType.ELEM_TYPE_METHOD;
			case S_REFLEX: return ElementType.ELEM_TYPE_METHOD;
		}
		
		return null;
	}
}
