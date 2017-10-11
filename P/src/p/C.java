package p;

@R4Feature(p.R4Feature.blue && R4Feature.red)
public class C {
	@R4Feature(p.R4Feature.green)
	public int i = 0;
	
	@R4Feature(p.R4Feature.green)
	public C() {
		
	}

	@R4Feature(R4Feature.red)
	public void m(@R4Feature(R4Feature.blue)int j) {
		@R4Feature(R4Feature.blue)
		int k = 0;
		
		if(p.R4Feature.green) {
			
		}
	}
}
