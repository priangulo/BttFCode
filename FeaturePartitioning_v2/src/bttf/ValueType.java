package bttf;

public enum ValueType{
	TYPE_INT("INT"), 
	TYPE_STRING("STRING"),
	TYPE_DATE("DATE"),
	TYPE_BOOLEAN("BOOLEAN");

	private String type;
	
	private ValueType(String type){
		this.type = type;
	}
	
	public String get_type(){
		return this.type;
	}
	
	public String toString(){
		return this.type;
	}
}
