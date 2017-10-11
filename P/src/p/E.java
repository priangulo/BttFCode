package p;

@R4Feature(p.R4Feature.blue && R4Feature.red)
public enum E {
	@R4Feature(p.R4Feature.green)
	MERCURY(3.303e+23, 2.4397e6),
	@R4Feature(p.R4Feature.blue)
	VENUS(4.869e+24, 6.0518e6), 
	EARTH(5.976e+24, 6.37814e6), 
	MARS(6.421e+23,	3.3972e6), 
	JUPITER(1.9e+27, 7.1492e7), 
	SATURN(5.688e+26, 6.0268e7), 
	URANUS(8.686e+25, 2.5559e7), 
	NEPTUNE(1.024e+26, 2.4746e7);

	@R4Feature(p.R4Feature.green)
	private final double mass; // in kilograms
	private final double radius; // in meters

	E(double mass, @R4Feature(p.R4Feature.green)double radius) {
		this.mass = mass;
		this.radius = radius;
	}

	private double mass() {
		return mass;
	}

	private double radius() {
		return radius;
	}

	// universal gravitational constant (m3 kg-1 s-2)
	public static final double G = 6.67300E-11;

	double surfaceGravity() {
		return G * mass / (radius * radius);
	}

	double surfaceWeight(double otherMass) {
		return otherMass * surfaceGravity();
	}

	public static void main(String[] args) {
		if (args.length != 1) {
			System.err.println("Usage: java Planet <earth_weight>");
			System.exit(-1);
		}
		@R4Feature(p.R4Feature.green)
		double earthWeight = Double.parseDouble(args[0]);
		double mass = earthWeight / EARTH.surfaceGravity();
		for (E p : E.values())
			System.out.printf("Your weight on %s is %f%n", p, p.surfaceWeight(mass));
	}
}
