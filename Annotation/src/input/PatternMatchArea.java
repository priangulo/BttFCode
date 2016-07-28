package input;

public class PatternMatchArea {
	public String contents;
	public int start_index;
	public int end_index;
	
	public PatternMatchArea(String contents, int start_index, int end_index) {
		super();
		this.contents = contents;
		this.start_index = start_index;
		this.end_index = end_index;
	}

	@Override
	public String toString() {
		return "PatternMatchArea [contents=" + contents + ", start_index=" + start_index + ", end_index=" + end_index
				+ "]\n";
	}
	
	
}
