/**
 * 
 */
package com.rwd.database;

/**
 * Entity that represents the awareness levels
 * 
 * @author manuel.lopez
 *
 */
public class Level {

	//Table columns
	private long id;							//Level code
	private String description;				//Level description
	
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
