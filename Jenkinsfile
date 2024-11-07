pipeline {
    agent any

    environment {
        // Install the Maven version
        JAVA_HOME = tool 'JDK 17'
        MAVEN_HOME = tool 'Maven 3.8.4'
    }

    stages {
        stage('Checkout') {
            steps {
                // Récupère le code source depuis mon repo Github
                git url: 'https://github.com/MichelleMair/mairetrakotomalala-michelle-TourGuide-SpringBoot-P8-app.git', branch: 'master'
            }
        }
        
        stage ('Install local JARs') {
            steps {
                dir('TourGuide') {
                    //Installation des jar locaux
                    bat 'mvn install:install-file -Dfile=libs/gpsUtil.jar -DgroupId=gpsUtil -DartifactId=gpsUtil -Dversion=1.0.0 -Dpackaging=jar'
                    bat 'mvn install:install-file -Dfile=libs/RewardCentral.jar -DgroupId=rewardCentral -DartifactId=rewardCentral -Dversion=1.0.0 -Dpackaging=jar'
                    bat 'mvn install:install-file -Dfile=libs/TripPricer.jar -DgroupId=tripPricer -DartifactId=tripPricer -Dversion=1.0.0 -Dpackaging=jar'
                }
            }
        }
        
        stage('Build') {
            steps {
                dir('TourGuide') {
                    //Compilation du projet
                    bat "${MAVEN_HOME}/bin/mvn clean package"
                }
            }
        }
        
        stage('Test') {
            steps {
                dir('TourGuide') {
                    bat "${MAVEN_HOME}/bin/mvn test"
                }
            }
        }
        
        stage ('Archive') {
            steps {
                //Archiver le fichier jar généré
                archiveArtifacts artifacts: 'TourGuide/target/*.jar', allowEmptyArchive: true
            }
        }
    }
    
    post {
        always {
            //Nettoyage après le build, succès ou échec
            cleanWs()
        }
        
        success {
            echo 'Build terminé avec succés !'
        }
        
        failure {
            echo 'Le build a échoué.'
        }
    }
}
