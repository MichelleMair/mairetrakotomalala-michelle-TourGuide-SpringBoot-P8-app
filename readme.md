# Technologies

> Java 17  
> Spring Boot 3.X  
> JUnit 5  

# How to have gpsUtil, rewardCentral and tripPricer dependencies available ?

> Run : 
- mvn install:install-file -Dfile=/libs/gpsUtil.jar -DgroupId=gpsUtil -DartifactId=gpsUtil -Dversion=1.0.0 -Dpackaging=jar  
- mvn install:install-file -Dfile=/libs/RewardCentral.jar -DgroupId=rewardCentral -DartifactId=rewardCentral -Dversion=1.0.0 -Dpackaging=jar  
- mvn install:install-file -Dfile=/libs/TripPricer.jar -DgroupId=tripPricer -DartifactId=tripPricer -Dversion=1.0.0 -Dpackaging=jar


## Intégration Continue (CI) / Déploiement Continu (CD)

### GitHub Actions

- **Fichier de configuration** : `.github/workflows/main.yml`
- **Étapes** : Compilation, exécution des tests, analyse de code.
- **Déclencheurs** : S'exécute lors de chaque `push` et `pull request` sur les branches `dev` et `master`.

### Jenkins

- **Installation** : Télécharger `jenkins.war` et lancer avec `java -jar jenkins.war --httpPort=8081`.
- **Configuration** : Le fichier `Jenkinsfile` se trouve à la racine du dépôt et contient les étapes de construction, de test, et de déploiement.
- **Déclencheurs** : Le pipeline Jenkins se déclenche manuellement ou automatiquement sur certains événements.

### Déploiement
Le déploiement se fait automatiquement après des builds réussis sur la branche `master`.

### Dépannage
- **Problème de connexion Jenkins** : Assurez-vous que Jenkins est bien démarré avec `java -jar jenkins.war --httpPort=8081`.
