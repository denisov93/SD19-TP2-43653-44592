package utils;

import org.bson.codecs.pojo.annotations.BsonCreator;
import org.bson.codecs.pojo.annotations.BsonProperty;

public class TableR {

	private String field1;
	private String field2;
	
	@BsonCreator
	public TableR(@BsonProperty("field1")String field1,@BsonProperty("field2")String field2) {
		this.field1 = field1;
		this.field2 = field2;
	}
	
	public String getField1() {
		return field1;
	}
	
	public String getField2() {
		return field2;
	}
}
