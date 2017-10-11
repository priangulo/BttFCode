package p;

@R4Feature(p.R4Feature.green)
public @interface R4Feature {
	@R4Feature(p.R4Feature.green)
	public static boolean blue = false;
	public static boolean red = false;
	public static boolean green = false;
	
	@R4Feature(p.R4Feature.green)
	boolean value();	
}
