package org.globus.cog.abstraction.impl.scheduler.sge;

import java.util.ArrayList;

/**
 * Data structure for defining queue properties
 */
public class QueueInformation {

	private String name;
	private ArrayList<String> pe_list = new ArrayList<String>();
	private int slots;
	private String walltime;

	/**
	 * Add data to queue information
	 * @param data Two dimensional array in the format of {"setting", "value is here"}
	 */
	public void addData(String[] data) {
		if(data.length != 2) return;
		if(data[0].equals("h_rt")) setWalltime(data[1]);
		if(data[0].equals("qname")) setName(data[1]);
		if(data[0].equals("pe_list")) setPe_list(data[1]);
		if(data[0].equals("slots")) setSlots(data[1]);
	}
	
	public String getWalltime() {
		return walltime;
	}

	public void setWalltime(String walltime) {
		this.walltime = walltime;
	}

	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public ArrayList<String> getPe_list() {
		return pe_list;
	}
	
	public void setPe_list(String list) {
		this.pe_list.clear();
		for(String s : list.split(" ")) {
			this.pe_list.add(s);
		}
	}
	
	public int getSlots() {
		return slots;
	}
	
	public void setSlots(String slots) {
		String slot_list[] = slots.split(",");
		this.slots = Integer.valueOf(slot_list[0]);
	}
}
