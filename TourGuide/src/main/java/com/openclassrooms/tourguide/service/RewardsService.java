package com.openclassrooms.tourguide.service;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
	
	private static final Logger logger = LoggerFactory.getLogger(RewardsService.class);
    private static final double STATUTE_MILES_PER_NAUTICAL_MILE = 1.15077945;
    private final ExecutorService executorService = Executors.newFixedThreadPool(200);

	// proximity in miles
    private int defaultProximityBuffer = 10;
	private int proximityBuffer = defaultProximityBuffer;
	private int attractionProximityRange = 200;
	private final GpsUtil gpsUtil;
	private final RewardCentral rewardsCentral;

	@Autowired
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
	    List<Attraction> attractions = gpsUtil.getAttractions();
	        for (VisitedLocation visitedLocation : userLocations) {
	            for (Attraction attraction : attractions) {
	            	if (user.getUserRewards().stream().noneMatch(r -> r.attraction.attractionName.equals(attraction.attractionName))) {
	            		if (nearAttraction(visitedLocation, attraction)) {
	            			UserReward userReward = new UserReward(visitedLocation, attraction, getRewardPoints(attraction, user.getUserId()));
	            			user.addUserReward(userReward);
	            		}
	            	}
	            }
	        }
	}
	
	
	/**
	 * Calculate rewards for all users in parallel
	 * 
	 * @param users the list of users for whom rewards are calculated
	 */
	public void calculateAllRewards(List<User> users) {
		CountDownLatch cdLatch = new CountDownLatch(users.size());
		
		for (User user : users) {
			executorService.submit(() -> {
				try {
					calculateRewards(user);
				} finally {
					cdLatch.countDown();
				}
			});
		}
		try {
			if(!cdLatch.await(20, TimeUnit.MINUTES)) {
				logger.info("Le temps d'attente a expiré pour les calculs de récompenses. ");
			}
		} catch (InterruptedException ie) {
			Thread.currentThread().interrupt();
			ie.printStackTrace();
		}
			logger.info("Calcul des récompenses terminé pour tous les users.");
	}
	
	
	public boolean isWithinAttractionProximity(Attraction attraction, Location location) {
		return getDistance(attraction, location) > attractionProximityRange ? false : true;
	}
	
	private boolean nearAttraction(VisitedLocation visitedLocation, Attraction attraction) {
		return getDistance(attraction, visitedLocation.location) <= proximityBuffer;
	}
	
	public int getRewardPoints(Attraction attraction, UUID userId) {
		return rewardsCentral.getAttractionRewardPoints(attraction.attractionId, userId);
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
