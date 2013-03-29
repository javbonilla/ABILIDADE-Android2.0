package org.abilidade.model;

public class Point {
	String address;
	String description;
	String submitterMail;
	double lat;
	double lon;
	String[] photo = new String[3];
	
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getSubmitterMail() {
		return submitterMail;
	}
	public void setSubmitterMail(String submitterMail) {
		this.submitterMail = submitterMail;
	}
	public double getLat() {
		return lat;
	}
	public void setLat(double lat) {
		this.lat = lat;
	}
	public double getLon() {
		return lon;
	}
	public void setLon(double lon) {
		this.lon = lon;
	}
	public String[] getPhoto() {
		return photo;
	}
	public void addPhoto(String photo) {
		this.photo[0] = photo;
	}
}
