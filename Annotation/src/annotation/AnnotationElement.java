package annotation;

public class AnnotationElement {
	public String full_identifier;	
	public String type;
	public String feature_name;
	
	public AnnotationElement(String full_identifier, String type, String feature_name) {
		super();
		this.full_identifier = full_identifier;
		this.type = type;
		this.feature_name = feature_name;
	}
	
	@Override
    public boolean equals(Object object)
    {
        if (object != null && object instanceof AnnotationElement)
        {
            if( this.full_identifier.equals( ((AnnotationElement)object).full_identifier)){
            	return true;
            }
        }

        return false;
    }

}
