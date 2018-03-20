package com.bing.data;

public class MapDetail {
	private String centerLatitude;
	private String centerLongitude;
	private LocalDetailsState localDetailsState;
	
	public String getCenterLatitude() {
		return centerLatitude;
	}
	
	public void setCenterLatitude(String centerLatitude) {
		this.centerLatitude = centerLatitude;
	}
	
	public String getCenterLongitude() {
		return centerLongitude;
	}
	
	public void setCenterLongitude(String centerLongitude) {
		this.centerLongitude = centerLongitude;
	}
	
	public LocalDetailsState getLocalDetailsState() {
		return localDetailsState;
	}
	
	public void setLocalDetailsState(LocalDetailsState localDetailsState) {
		this.localDetailsState = localDetailsState;
	}
	
}
