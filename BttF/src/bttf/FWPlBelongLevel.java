package bttf;

public enum FWPlBelongLevel {
	FULLY_BELONGS_FW(1),
	FULLY_BELONGS_PL(2),
	PARTIALLY_BELONGS_FW(3);
	
	private int level;

	private FWPlBelongLevel(int level) {
		this.level = level;
	}

	public int getLevel() {
		return level;
	}
	
	
}
