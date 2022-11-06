pipeline {
    agent any



    environment {
        registry = "monambengouta/storewithspring"
        registryCredential = 'dockerHub'
        dockerImage = ''

        NEXUS_VERSION = "nexus3"
        NEXUS_PROTOCOL = "http"
        NEXUS_URL = "127.0.0.1:8081"
        NEXUS_REPOSITORY = "maven-nexus-repo"
        NEXUS_CREDENTIAL_ID = "nexus-user-credentials"
    }



    stages {
        stage('GIT') {
            steps {
                echo "Getting projrct from GIT"
                url: 'https://github.com/monambengouta/StoreWithSpring'
            }
        }
        stage('MVN CLEAN') {
            steps {
                sh "mvn clean"
            }
        }
        stage('MVN PACKAGE') {
		 steps {
			    sh "mvn package"
			  }
		  }
        stage('MVN COMPILE') {
            steps {
                sh "mvn compile"
            }
        }

        stage("Publish to Nexus Repository Manager") {
            steps {
                script {
                    pom = readMavenPom file: "pom.xml";
                    filesByGlob = findFiles(glob: "target/*.${pom.packaging}");
                    echo "${filesByGlob[0].name} ${filesByGlob[0].path} ${filesByGlob[0].directory} ${filesByGlob[0].length} ${filesByGlob[0].lastModified}"
                    artifactPath = filesByGlob[0].path;
                    artifactExists = fileExists artifactPath;
                    if(artifactExists) {
                        echo "*** File: ${artifactPath}, group: ${pom.groupId}, packaging: ${pom.packaging}, version ${pom.version}";
                        nexusArtifactUploader(
                            nexusVersion: NEXUS_VERSION,
                            protocol: NEXUS_PROTOCOL,
                            nexusUrl: NEXUS_URL,
                            groupId: pom.groupId,
                            version: pom.version,
                            repository: NEXUS_REPOSITORY,
                            credentialsId: NEXUS_CREDENTIAL_ID,
                            artifacts: [
                                [artifactId: pom.artifactId,
                                classifier: '',
                                file: artifactPath,
                                type: pom.packaging],
                                [artifactId: pom.artifactId,
                                classifier: '',
                                file: "pom.xml",
                                type: "pom"]
                            ]
                        );
                    } else {
                        error "*** File: ${artifactPath}, could not be found";
                    }
                }
            }
        }

        stage('JUNIT TESTS') {
            steps {
                sh 'mvn test'
            }
        }
        stage('MVN SONARQUBE') {
            steps {
                withSonarQubeEnv(installationName: 'SonarQube Server', credentialsId: 'SonarQube Token') {
                sh 'mvn sonar:sonar'
                }
                // sh "mvn sonar:sonar -Dsonar.login=admin -Dsonar.password=Achref@99"
            }
        }

        stage('Building our image') {
            steps {
                script {
                    dockerImage = docker.build registry + ":$BUILD_NUMBER"
                }
            }
        }

        stage('Deploy our image') {
            steps {
                script {
                    docker.withRegistry( '', registryCredential ) {
                        dockerImage.push()
                    }
                }
            }
        }

    }
}