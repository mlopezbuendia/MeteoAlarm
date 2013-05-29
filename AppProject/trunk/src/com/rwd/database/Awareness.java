/**
 * 
 */
package com.rwd.database;

/**
 * Entity that represent the awareness table
 * 
 * @author manuel.lopez
 *
 */
public class Awareness {

	//Table columns
	private long id;							//Awareness code
	private String description;				//Awareness description
	
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	
	@Override
	public String toString(){
		return description;
	}
	
}
