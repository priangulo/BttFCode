package input;

public class TextElement {
	public String identifier;
	public TextElement parent;
	public String file_path;
	public int dec_initial_char;
	public int dec_end_char;
	public String dec_contents;
	public String type;
	public String full_identifier;
	public int body_initial_char;
	public int body_end_char;
		
	public TextElement(String identifier, TextElement parent, String file_path, int dec_initial_char, int dec_end_char,
			String dec_contents, String type, String full_identifier, int body_initial_char, int body_end_char) {
		this.identifier = identifier;
		this.parent = parent;
		this.file_path = file_path;
		this.dec_initial_char = dec_initial_char;
		this.dec_end_char = dec_end_char;
		this.dec_contents = dec_contents;
		this.type = type;
		this.full_identifier = full_identifier;
		this.body_initial_char = body_initial_char;
		this.body_end_char = body_end_char;
	}


	public void printTextElement(){
		System.out.println(this.type +": "+ this.full_identifier +"\n"+ this.dec_contents +"\n");
	}
	
	
	@Override
	public String toString() {
		return "TextElement [identifier=" + identifier + ", parent=" + ((parent==null) ? "" : parent.full_identifier) + ", file_path=" + file_path
				+ ", initial_char=" + dec_initial_char + ", end_char=" + dec_end_char + ", contents=" + dec_contents + ", type="
				+ type + ", full_identifier=" + full_identifier + ", body_initial_char=" + body_initial_char
				+ ", body_end_char=" + body_end_char + "]\n";
	}
	
	@Override
    public boolean equals(Object object)
    {
        if (object != null && object instanceof TextElement)
        {
            if( this.full_identifier.equals( ((TextElement)object).full_identifier)){
            	return true;
            }
        }

        return false;
    }
	
	
	
	
}
