# Technologies

> Java 17  
> Spring Boot 3.X  
> JUnit 5  

# How to have gpsUtil, rewardCentral and tripPricer dependencies available ?

> Run : 
- mvn install:install-file -Dfile=/libs/gpsUtil.jar -DgroupId=gpsUtil -DartifactId=gpsUtil -Dversion=1.0.0 -Dpackaging=jar  
- mvn install:install-file -Dfile=/libs/RewardCentral.jar -DgroupId=rewardCentral -DartifactId=rewardCentral -Dversion=1.0.0 -Dpackaging=jar  
- mvn install:install-file -Dfile=/libs/TripPricer.jar -DgroupId=tripPricer -DartifactId=tripPricer -Dversion=1.0.0 -Dpackaging=jar


## Continuous Integration (CI) / Continuous Deployment (CD)

### GitHub Actions

- **Configuration File** : `.github/workflows/main.yml`
- **Pipeline steps** : Compilation, testing, building jar file with Maven.
- **Triggers** : Runs on each `push` and `merge` events on `dev` et `master`.

### GitLab CI

- **Configuration File** : `.gitlab-ci.yml`
- **Pipeline steps** : build, test, building jar file with Maven.
- **Triggers** : Triggered on `push` and `merge` events on `dev` et `master` or manually in the GitLab CI/CD interface.


### Jenkins

- **SetUp** : Download `jenkins.war` file from the Jenkins website. In the terminal, navigate to the directory containing jenkins.war and start Jenkins with :`java -jar jenkins.war --httpPort=8081`.
- **Pipeline Configuration** : `Jenkinsfile` located in the root of the repository defines the steps for building and testing the project with Maven.
- **Pipeline Steps** : Build the jar file using Maven and execute tests. 
- **Triggers** : The Jenkins pipeline can be trigerred manually or automatically based on specific events.

### Troubleshooting
- **Jenkins Connection Issue** : Ensure Jenkins is running with the command `java -jar jenkins.war --httpPort=8081` in a terminal at the directory where jenkins.war is located.
