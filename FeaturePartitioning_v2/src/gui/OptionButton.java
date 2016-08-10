package gui;


public class OptionButton {
	private String partition_name;
	private Boolean fprivate;
	private Boolean fpublic;
	private Boolean is_container_feature;
	
	
	public OptionButton(String partition_name, Boolean fprivate, Boolean fpublic, Boolean is_container_feature) {
		super();
		this.partition_name = partition_name;
		this.fprivate = fprivate;
		this.fpublic = fpublic;
		this.is_container_feature = is_container_feature;
	}
	
	public Boolean getIs_container_feature() {
		return is_container_feature;
	}

	public String getPartition_name() {
		return partition_name;
	}
	public Boolean getFprivate() {
		return fprivate;
	}
	public Boolean getFpublic() {
		return fpublic;
	}
	public void setFprivate(Boolean fprivate) {
		this.fprivate = fprivate;
	}
	public void setFpublic(Boolean fpublic) {
		this.fpublic = fpublic;
	}
	@Override
	public boolean equals(Object object) {
		if (object != null && object instanceof OptionButton)
        {
            if( this.partition_name.equals( ((OptionButton)object).partition_name)
            		&& this.fprivate.equals( ((OptionButton)object).fprivate)
            		&& this.fpublic.equals( ((OptionButton)object).fpublic)){
            	return true;
            }
        }

        return false;
	}

	@Override
	public String toString() {
		return "OptionButton [partition_name=" + partition_name + ", fprivate=" + fprivate + ", fpublic=" + fpublic
				+ ", is_container_feature=" + is_container_feature + "]\n";
	}
	
	
	
}
