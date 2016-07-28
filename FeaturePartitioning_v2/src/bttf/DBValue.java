package bttf;

import java.sql.Date;

public class DBValue {
	private ValueType type = null;
	private String str_val = null;
	private int int_val = -1;
	private Date date_val = null;
	private Boolean bool_val = false;
	private Boolean null_val = false;
	
	DBValue(String val){
		this.type = ValueType.TYPE_STRING;
		this.str_val = val;
	}
	
	DBValue(int val){
		this.type = ValueType.TYPE_INT;
		this.int_val = val;
	}
	
	DBValue(Date val){
		this.type = ValueType.TYPE_DATE;
		this.date_val = val;
	}
	
	DBValue(Boolean val){
		this.type = ValueType.TYPE_BOOLEAN;
		this.bool_val = val;
	}
	
	DBValue(){
		null_val = true;
	}

	public ValueType getType() {
		return type;
	}

	public String getStr_val() {
		return str_val;
	}

	public int getInt_val() {
		return int_val;
	}

	public Date getDate_val() {
		return date_val;
	}

	public Boolean getBool_val() {
		return bool_val;
	}

	public Boolean getNull_val() {
		return null_val;
	}

	public void setType(ValueType type) {
		this.type = type;
	}

	public void setStr_val(String str_val) {
		this.str_val = str_val;
	}

	public void setInt_val(int int_val) {
		this.int_val = int_val;
	}

	public void setDate_val(Date date_val) {
		this.date_val = date_val;
	}

	public void setBool_val(Boolean bool_val) {
		this.bool_val = bool_val;
	}

	public void setNull_val(Boolean null_val) {
		this.null_val = null_val;
	}
	
	
}
