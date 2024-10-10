package com.openclassrooms.tourguide.service;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Service;

import com.openclassrooms.tourguide.model.User;
import com.openclassrooms.tourguide.model.UserReward;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import rewardCentral.RewardCentral;

@Service
public class RewardsService {
    private static final double STATUTE_MILES_PER_NAUTICAL_MILE = 1.15077945;

	// proximity in miles
    private int defaultProximityBuffer = 10;
	private int proximityBuffer = defaultProximityBuffer;
	private int attractionProximityRange = 200;
	private final GpsUtil gpsUtil;
	private final RewardCentral rewardsCentral;
	
	//Create a pool with 200 threads 
	private final ExecutorService executorService = Executors.newFixedThreadPool(200);
	
	public RewardsService(GpsUtil gpsUtil, RewardCentral rewardCentral) {
		this.gpsUtil = gpsUtil;
		this.rewardsCentral = rewardCentral;
	}
	
	public void setProximityBuffer(int proximityBuffer) {
		this.proximityBuffer = proximityBuffer;
	}
	
	public void setDefaultProximityBuffer() {
		proximityBuffer = defaultProximityBuffer;
	}
	

	public void calculateRewards(User user) {
		CopyOnWriteArrayList<VisitedLocation> userLocations =  new CopyOnWriteArrayList<>(user.getVisitedLocations());
		CopyOnWriteArrayList<Attraction> attractions = new CopyOnWriteArrayList<>(gpsUtil.getAttractions());
		
		userLocations.parallelStream().forEach(visitedLocation -> {
			attractions.parallelStream().forEach(attraction -> {
				if (user.getUserRewards().stream().noneMatch(r -> r.attraction.attractionName.equals(attraction.attractionName))) {
					if (nearAttraction(visitedLocation, attraction)) {
						user.addUserReward(new UserReward(visitedLocation, attraction, getRewardPoints(attraction, user)));
					}
				}
			});
		});
	}
	
	

	/**
	 * Calculate rewards for all users in parallel
	 * 
	 * @param users the list of users for whom rewards are calculated
	 */
	public void calculateAllRewards(List<User> users) {
		//Divided the users list into batches of 1000 users
		int batchSize = 1000;
		
		//CountDownLatch to monitor the end of each task
		CountDownLatch cdLatch = new CountDownLatch(users.size());
		
		for (int i = 0; i < users.size(); i += batchSize) {
			List<User> batch = users.subList(i, Math.min(i + batchSize, users.size()));
			
			executorService.submit(() -> {
				batch.forEach(user -> {
					calculateRewards(user);
					//Reduces latch after each reward calculation
					cdLatch.countDown();
				});
			});
		}
		
		//Waiting the end of all calculations before continuing 
		try {
			//Time limit to wait for all tasks
			cdLatch.await(20, TimeUnit.MINUTES);
		} catch (InterruptedException ie) {
			ie.printStackTrace();
		} finally {
			//Stop the executor after all tasks are completed
			executorService.shutdown();
		}
	}
	
	public boolean isWithinAttractionProximity(Attraction attraction, Location location) {
		return getDistance(attraction, location) > attractionProximityRange ? false : true;
	}
	
	private boolean nearAttraction(VisitedLocation visitedLocation, Attraction attraction) {
		return getDistance(attraction, visitedLocation.location) > proximityBuffer ? false : true;
	}
	
	private int getRewardPoints(Attraction attraction, User user) {
		return rewardsCentral.getAttractionRewardPoints(attraction.attractionId, user.getUserId());
	}
	
	public double getDistance(Location loc1, Location loc2) {
        double lat1 = Math.toRadians(loc1.latitude);
        double lon1 = Math.toRadians(loc1.longitude);
        double lat2 = Math.toRadians(loc2.latitude);
        double lon2 = Math.toRadians(loc2.longitude);

        double angle = Math.acos(Math.sin(lat1) * Math.sin(lat2)
                               + Math.cos(lat1) * Math.cos(lat2) * Math.cos(lon1 - lon2));

        double nauticalMiles = 60 * Math.toDegrees(angle);
        double statuteMiles = STATUTE_MILES_PER_NAUTICAL_MILE * nauticalMiles;
        return statuteMiles;
	}

}
