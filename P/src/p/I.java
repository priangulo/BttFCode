package p;

@R4Feature(p.R4Feature.blue && R4Feature.red)
public interface I {
	@R4Feature(p.R4Feature.green)
	public void m();
}
