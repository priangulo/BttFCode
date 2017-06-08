package bttf;

public class Element {
	String identificator;
	ElementType element_type;
	int earliest_time;
	int latest_time;
	boolean is_hook;
	boolean in_cycle;
	boolean is_fPrivate;
	boolean is_fPublic;
	
	public String getIdentificator() {
		return identificator;
	}
	public void setIdentificator(String identificator) {
		this.identificator = identificator;
	}
	public ElementType getElement_type() {
		return element_type;
	}
	public void setElement_type(ElementType element_type) {
		this.element_type = element_type;
	}
	public int getEarliest_time() {
		return earliest_time;
	}
	public void setEarliest_time(int earliest_time) {
		this.earliest_time = earliest_time;
	}
	public int getLatest_time() {
		return latest_time;
	}
	public void setLatest_time(int latest_time) {
		this.latest_time = latest_time;
	}
	public boolean isIs_hook() {
		return is_hook;
	}
	public void setIs_hook(boolean is_hook) {
		this.is_hook = is_hook;
	}
	public boolean isIn_cycle() {
		return in_cycle;
	}
	public void setIn_cycle(boolean in_cycle) {
		this.in_cycle = in_cycle;
	}	
	public boolean isIs_fPrivate() {
		return is_fPrivate;
	}
	public void setIs_fPrivate(boolean is_fPrivate) {
		this.is_fPrivate = is_fPrivate;
	}
	public boolean isIs_fPublic() {
		return is_fPublic;
	}
	public void setIs_fPublic(boolean is_fPublic) {
		this.is_fPublic = is_fPublic;
	}
	public Element(String identificator, ElementType element_type) {
		super();
		this.identificator = identificator;
		this.element_type = element_type;
	}
	
	@Override
	public String toString() {
		return "Element [identificator=" + identificator + ", element_type=" + element_type + ", earliest_time="
				+ earliest_time + ", latest_time=" + latest_time + ", is_hook=" + is_hook + ", in_cycle=" + in_cycle
				+ ", is_fPrivate=" + is_fPrivate + ", is_fPublic=" + is_fPublic + "]\n";
	}
	
	@Override
    public boolean equals(Object object)
    {
        if (object != null && object instanceof Element)
        {
            if( this.identificator.equals( ((Element)object).identificator)){
            	return true;
            }
        }

        return false;
    }
}
