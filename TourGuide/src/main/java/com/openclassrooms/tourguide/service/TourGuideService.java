package com.openclassrooms.tourguide.service;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.openclassrooms.tourguide.dto.NearByAttractionDto;
import com.openclassrooms.tourguide.model.User;
import com.openclassrooms.tourguide.model.UserReward;
import com.openclassrooms.tourguide.tracker.Tracker;

import gpsUtil.GpsUtil;
import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import tripPricer.Provider;
import tripPricer.TripPricer;

@Service
public class TourGuideService {
	private Logger logger = LoggerFactory.getLogger(TourGuideService.class);
	private final GpsUtil gpsUtil;
	private final RewardsService rewardsService;
	private final TripPricer tripPricer = new TripPricer();
	private final UserService userService;
	//Create a pool with 200 threads 
	private final ExecutorService executorService = Executors.newFixedThreadPool(200);
	public final Tracker tracker;
	boolean testMode = true;

	public TourGuideService(GpsUtil gpsUtil, RewardsService rewardsService, UserService userService) {
		this.gpsUtil = gpsUtil;
		this.rewardsService = rewardsService;
		this.userService = userService;
		
		Locale.setDefault(Locale.US);

		if (testMode) {
			logger.info("TestMode enabled");
			logger.debug("Initializing users");
			userService.initializeInternalUsers();
			logger.debug("Finished initializing users");
		}
		tracker = new Tracker(this, userService); 
		addShutDownHook();
	}

	public List<UserReward> getUserRewards(User user) {
		return user.getUserRewards();
	}

	public VisitedLocation getUserLocation(User user) {
		VisitedLocation visitedLocation = (user.getVisitedLocations().size() > 0) ? user.getLastVisitedLocation()
				: trackUserLocation(user);
		return visitedLocation;
	}

	public List<Provider> getTripDeals(User user) {
		int cumulatativeRewardPoints = user.getUserRewards().stream()
				.mapToInt(i -> i.getRewardPoints()).sum();
		
		List<Provider> providers = tripPricer.getPrice(
				userService.getTripPricerApiKey(), user.getUserId(),
				user.getUserPreferences().getNumberOfAdults(), user.getUserPreferences().getNumberOfChildren(),
				user.getUserPreferences().getTripDuration(), cumulatativeRewardPoints);
		user.setTripDeals(providers);
		return providers;
	}

	public VisitedLocation trackUserLocation(User user) {
		VisitedLocation visitedLocation = gpsUtil.getUserLocation(user.getUserId());
		user.addToVisitedLocations(visitedLocation);
		rewardsService.calculateRewards(user);
		return visitedLocation;
	}
	

	/**
	 * Tracking multiple users in the same time to handle a large group of users
	 * and returns a map of user IDs to their latest visited locations. 
	 * This method initiates asynchronous location tracking for each user in the provided list, allowing efficient processing of large user groups. 
	 * Each user's location is retrieved concurrently using a CompletableFuture for performance optimization
	 * 
	 * @param users the list of users whose locations are to be tracked
	 * @return a map where the keys are user IDs (UUIDs) and the values are the latest visited locations (VisitedLocation) for each user
	 */
	public Map<UUID, VisitedLocation> trackUsersListLocation(List<User> users) {
		logger.info("Démarrage du suivi de localisation pour les users");
		
		Map<UUID, CompletableFuture<VisitedLocation>> visitedLocationFutures = users.parallelStream()
				.collect(Collectors.toMap(
						User::getUserId, 
						user -> CompletableFuture.supplyAsync(() -> trackUserLocation(user), executorService)
						));
		//Collecte des résultats de chaque future
		Map<UUID, VisitedLocation> visitedLocationMap = visitedLocationFutures.entrySet().stream()
				.collect(Collectors.toMap(
						Map.Entry::getKey, 
						entry -> entry.getValue().join()
						));
		return visitedLocationMap;
	}

	public List<NearByAttractionDto> getNearByAttractions(VisitedLocation visitedLocation) {
		return gpsUtil.getAttractions().stream().map(attraction -> {
			double distance = rewardsService.getDistance(attraction, visitedLocation.location);
			int rewardPoints = rewardsService.getRewardPoints(attraction, visitedLocation.userId);
			return new NearByAttractionDto(
					attraction.attractionName,
					new Location(attraction.latitude, attraction.longitude),
					new Location(visitedLocation.location.latitude, visitedLocation.location.longitude),
					distance,
					rewardPoints);					
		})
		.sorted(Comparator.comparingDouble(dto -> dto.distance))
		.limit(5)
		.collect(Collectors.toList());
	}
	
	public void clearUserData(List<User> users) {
		for (User user : users){
			user.clearVisitedLocations();
			user.getUserRewards().clear();
		}
		logger.info("Données utilisateur nettoyées avant le test.");
	}

	private void addShutDownHook() {
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			tracker.stopTracking();
		}));
	}

}
