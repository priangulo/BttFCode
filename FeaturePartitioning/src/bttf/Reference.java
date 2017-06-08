package bttf;

public class Reference {
	String call_from;
	String call_to;
	ElementType call_from_type;
	ElementType call_to_type;
	
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

	public Reference(String call_from, String call_to, ElementType call_from_type, ElementType call_to_type) {
		super();
		this.call_from = call_from;
		this.call_to = call_to;
		this.call_from_type = call_from_type;
		this.call_to_type = call_to_type;
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
