package bttf;

public class Reference {
	private String call_from;
	private String call_to;
	private ElementType call_from_type;
	private ElementType call_to_type;
	private String call_from_mod;
	private String call_to_mod;
	private String call_from_code;
	private String call_to_code;
	private boolean call_from_isterminal;
	private boolean call_to_isterminal;
	private String call_from_signature;
	private String call_to_signature;
	private String call_from_annotationtext;
	private String call_to_annotationtext;
	private int call_from_LOC;
	private int call_to_LOC;
	private String call_from_origType;
	private String call_to_origType;
	private String call_from_returnType;
	private String call_to_returnType;
	
	public String getCall_from() {
		return call_from;
	}
	
	public void setCall_from(String call_from) {
		this.call_from = call_from;
	}
	
	public String getCall_to() {
		return call_to;
	}
	
	public void setCall_to(String call_to) {
		this.call_to = call_to;
	}
	
	public ElementType getCall_from_type() {
		return call_from_type;
	}

	public void setCall_from_type(ElementType call_from_type) {
		this.call_from_type = call_from_type;
	}

	public ElementType getCall_to_type() {
		return call_to_type;
	}

	public void setCall_to_type(ElementType call_to_type) {
		this.call_to_type = call_to_type;
	}
	
	public String getCall_from_mod() {
		return call_from_mod;
	}

	public void setCall_from_mod(String call_from_mod) {
		this.call_from_mod = call_from_mod;
	}

	public String getCall_to_mod() {
		return call_to_mod;
	}

	public void setCall_to_mod(String call_to_mod) {
		this.call_to_mod = call_to_mod;
	}
	
	public String getCall_from_code() {
		return call_from_code;
	}

	public void setCall_from_code(String call_from_code) {
		this.call_from_code = call_from_code;
	}

	public String getCall_to_code() {
		return call_to_code;
	}

	public void setCall_to_code(String call_to_code) {
		this.call_to_code = call_to_code;
	}
	
	public boolean isCall_from_isterminal() {
		return call_from_isterminal;
	}

	public void setCall_from_isterminal(boolean call_from_isterminal) {
		this.call_from_isterminal = call_from_isterminal;
	}

	public boolean isCall_to_isterminal() {
		return call_to_isterminal;
	}

	public void setCall_to_isterminal(boolean call_to_isterminal) {
		this.call_to_isterminal = call_to_isterminal;
	}
	
	public String getCall_from_signature() {
		return call_from_signature;
	}

	public void setCall_from_signature(String call_from_signature) {
		this.call_from_signature = call_from_signature;
	}

	public String getCall_to_signature() {
		return call_to_signature;
	}

	public void setCall_to_signature(String call_to_signature) {
		this.call_to_signature = call_to_signature;
	}
	
	public String getCall_from_annotationtext() {
		return call_from_annotationtext;
	}

	public void setCall_from_annotationtext(String call_from_annotationtext) {
		this.call_from_annotationtext = call_from_annotationtext;
	}

	public String getCall_to_annotationtext() {
		return call_to_annotationtext;
	}

	public void setCall_to_annotationtext(String call_to_annotationtext) {
		this.call_to_annotationtext = call_to_annotationtext;
	}
	
	public int getCall_from_LOC() {
		return call_from_LOC;
	}

	public void setCall_from_LOC(int call_from_LOC) {
		this.call_from_LOC = call_from_LOC;
	}

	public int getCall_to_LOC() {
		return call_to_LOC;
	}

	public void setCall_to_LOC(int call_to_LOC) {
		this.call_to_LOC = call_to_LOC;
	}
	
	

	public String getCall_from_origType() {
		return call_from_origType;
	}

	public void setCall_from_origType(String call_from_origType) {
		this.call_from_origType = call_from_origType;
	}

	public String getCall_to_origType() {
		return call_to_origType;
	}

	public void setCall_to_origType(String call_to_origType) {
		this.call_to_origType = call_to_origType;
	}

	public String getCall_from_returnType() {
		return call_from_returnType;
	}

	public void setCall_from_returnType(String call_from_returnType) {
		this.call_from_returnType = call_from_returnType;
	}

	public String getCall_to_returnType() {
		return call_to_returnType;
	}

	public void setCall_to_returnType(String call_to_returnType) {
		this.call_to_returnType = call_to_returnType;
	}

	public Reference(String call_from, String call_to, ElementType call_from_type, ElementType call_to_type,
			String call_from_mod, String call_to_mod, String call_from_code, String call_to_code,
			boolean call_from_isterminal, boolean call_to_isterminal, String call_from_signature,
			String call_to_signature, String call_from_annotationtext, String call_to_annotationtext, int call_from_LOC,
			int call_to_LOC, String call_from_origType, String call_to_origType, String call_from_returnType,
			String call_to_returnType) {
		super();
		this.call_from = call_from;
		this.call_to = call_to;
		this.call_from_type = call_from_type;
		this.call_to_type = call_to_type;
		this.call_from_mod = call_from_mod;
		this.call_to_mod = call_to_mod;
		this.call_from_code = call_from_code;
		this.call_to_code = call_to_code;
		this.call_from_isterminal = call_from_isterminal;
		this.call_to_isterminal = call_to_isterminal;
		this.call_from_signature = call_from_signature;
		this.call_to_signature = call_to_signature;
		this.call_from_annotationtext = call_from_annotationtext;
		this.call_to_annotationtext = call_to_annotationtext;
		this.call_from_LOC = call_from_LOC;
		this.call_to_LOC = call_to_LOC;
		this.call_from_origType = call_from_origType;
		this.call_to_origType = call_to_origType;
		this.call_from_returnType = call_from_returnType;
		this.call_to_returnType = call_to_returnType;
	}

	public Reference(String call_from, String call_to, ElementType call_from_type, ElementType call_to_type,
			String call_from_mod, String call_to_mod, String call_from_code, String call_to_code,
			boolean call_from_isterminal, boolean call_to_isterminal, String call_from_signature,
			String call_to_signature, String call_from_annotationtext, String call_to_annotationtext, int call_from_LOC,
			int call_to_LOC, String call_from_origType, String call_to_origType) {
		super();
		this.call_from = call_from;
		this.call_to = call_to;
		this.call_from_type = call_from_type;
		this.call_to_type = call_to_type;
		this.call_from_mod = call_from_mod;
		this.call_to_mod = call_to_mod;
		this.call_from_code = call_from_code;
		this.call_to_code = call_to_code;
		this.call_from_isterminal = call_from_isterminal;
		this.call_to_isterminal = call_to_isterminal;
		this.call_from_signature = call_from_signature;
		this.call_to_signature = call_to_signature;
		this.call_from_annotationtext = call_from_annotationtext;
		this.call_to_annotationtext = call_to_annotationtext;
		this.call_from_LOC = call_from_LOC;
		this.call_to_LOC = call_to_LOC;
		this.call_from_origType = call_from_origType;
		this.call_to_origType = call_to_origType;
	}

	public Reference(String call_from, String call_to){
		this.call_from = call_from;
		this.call_to = call_to;
	}
	
	public String toString_v2() {
		//return call_from + "#" + call_to + "\r\n";
		return call_from.replace(",", ";") + "," + call_to.replace(",", ";") + "\r\n";
	}
	
	public boolean isThisValid(){
		return (this.call_from != null && this.call_to != null 
				&& this.call_from_type != null && this.call_to_type != null);
	}
			 
	@Override
	public String toString() {
		return "Reference [call_from=" + call_from + ", call_to=" + call_to + "] \n";
	}
	
	@Override
    public boolean equals(Object object)
    {
        if (object != null && object instanceof Reference)
        {
            if( this.call_from.equals( ((Reference)object).call_from) && this.call_to.equals( ((Reference)object).call_to) ){
            	return true;
            }
        }

        return false;
    }
}
