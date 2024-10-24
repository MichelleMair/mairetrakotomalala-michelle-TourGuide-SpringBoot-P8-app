package com.openclassrooms.tourguide.dto;

import gpsUtil.location.Location;

public class NearByAttractionDto {

	public String attractionName;
	public Location attractionLocation;
	public Location userLocation;
	public double distance;
	public int rewardPoints;
	
	
	public String getAttractionName() {
		return attractionName;
	}
	public void setAttractionName(String attractionName) {
		this.attractionName = attractionName;
	}
	public Location getAttractionLocation() {
		return attractionLocation;
	}
	public void setAttractionLocation(Location attractionLocation) {
		this.attractionLocation = attractionLocation;
	}
	public Location getUserLocation() {
		return userLocation;
	}
	public void setUserLocation(Location userLocation) {
		this.userLocation = userLocation;
	}
	public double getDistance() {
		return distance;
	}
	public void setDistance(double distance) {
		this.distance = distance;
	}
	public int getRewardPoints() {
		return rewardPoints;
	}
	public void setRewardPoints(int rewardPoints) {
		this.rewardPoints = rewardPoints;
	}
	
	//All args constructor
	public NearByAttractionDto(String attractionName, Location attractionLocation, Location userLocation,
			double distance, int rewardPoints) {
		super();
		this.attractionName = attractionName;
		this.attractionLocation = attractionLocation;
		this.userLocation = userLocation;
		this.distance = distance;
		this.rewardPoints = rewardPoints;
	}
	


}
