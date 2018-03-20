package com.bing.data;

public class LocalDetailsState {
	private String query;
	private String id;
	private Boolean disableComputingMapViewForLoadingOnly;
	
	public String getQuery() {
		return query;
	}
	
	public void setQuery(String query) {
		this.query = query;
	}
	
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public Boolean getDisableComputingMapViewForLoadingOnly() {
		return disableComputingMapViewForLoadingOnly;
	}
	
	public void setDisableComputingMapViewForLoadingOnly(Boolean disableComputingMapViewForLoadingOnly) {
		this.disableComputingMapViewForLoadingOnly = disableComputingMapViewForLoadingOnly;
	}
}
