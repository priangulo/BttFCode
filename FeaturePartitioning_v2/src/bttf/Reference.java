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

	public Reference(String call_from, String call_to, ElementType call_from_type, ElementType call_to_type,
			String call_from_mod, String call_to_mod, String call_from_code, String call_to_code,
			boolean call_from_isterminal, boolean call_to_isterminal) {
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
	}

	public Reference(String call_from, String call_to){
		this.call_from = call_from;
		this.call_to = call_to;
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
