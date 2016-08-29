package bttf;

public enum ElementType{
	 ELEM_TYPE_PACKAGE("Package", 1), 
	 ELEM_TYPE_CLASS("Class", 2),
	 ELEM_TYPE_FIELD("Field", 3),
	 ELEM_TYPE_METHOD("Method", 4);
	 //ELEM_TYPE_VARIABLE("Variable", 5);

	private String element_type;
	private int granularity;
	
	private ElementType(String element_type, int granularity){
		this.element_type = element_type;
		this.granularity = granularity;
	}
	
	public String get_element_type(){
		return this.element_type;
	}

	public int get_element_granularity(){
		return this.granularity;
	}
	
	public String toString(){
		return this.element_type;
	}
}