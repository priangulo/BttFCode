package bttf;

public enum SourceLanguage {
	LANG_JAVA("Java"), 
	LANG_GAML("GAML");

	private String language;
	
	private SourceLanguage(String language){
		this.language = language;
	}
	
	public String getSourceLanguage(){
		return this.language;
	}
}
