

pipeline {
    // run on jenkins nodes tha has java 8 label
    agent any
    // global env variables
    environment {
        EMAIL_RECIPIENTS = 'monaam.bengouta@gmail.com'
        registry = "monambengouta/storewithspring"
        registryCredential = ''
        dockerImage = ''
        NEXUS_VERSION = "nexus3"
        NEXUS_PROTOCOL = "http"
        NEXUS_URL = "127.0.0.1:8081"
        NEXUS_REPOSITORY = "maven-releases"
        NEXUS_CREDENTIAL_ID = "nexus-user-credentials"
    }
    stages {

        stage('Build with unit testing') {
            steps {
                // Run the maven build
                script {
                    // Get the Maven tool.
                    // ** NOTE: This 'M3' Maven tool must be configured
                    // **       in the global configuration.
                    echo 'Pulling... master'
                    def mvnHome = tool 'M2_HOME'
                    if (isUnix()) {
                        def targetVersion = getDevVersion()
                        print 'target build version...'
                        print targetVersion
                        sh "'${mvnHome}/bin/mvn' -Dintegration-tests.skip=true -Dbuild.number=${targetVersion} clean package"
                        def pom = readMavenPom file: 'pom.xml'
                        // get the current development version
                        developmentArtifactVersion = "${pom.version}-${targetVersion}"
                        print pom.version
                        // execute the unit testing and collect the reports
                        // junit '**//*target/surefire-reports/TEST-*.xml'
                        // archive 'target*//*.jar'
                    } else {
                        bat(/"${mvnHome}\bin\mvn" -Dintegration-tests.skip=true clean package/)
                        def pom = readMavenPom file: 'pom.xml'
                        print pom.version
                        // junit '**//*target/surefire-reports/TEST-*.xml'
                        // archive 'target*//*.jar'
                    }
                }

            }
        }
        // stage('Integration tests') {
            // Run integration test
            // steps {
                // script {
                    // def mvnHome = tool 'M2_HOME'
                    // if (isUnix()) {
                        // just to trigger the integration test without unit testing
                        // sh "'${mvnHome}/bin/mvn'  verify -Dunit-tests.skip=true"
                    // } else {
                       //bat(/"${mvnHome}\bin\mvn" verify -Dunit-tests.skip=true/)
                    // }
                // }
                // cucumber reports collection
                // cucumber buildStatus: null, fileIncludePattern: '**/cucumber.json', jsonReportDirectory: 'target', sortingMethod: 'ALPHABETICAL'
            // }
        // }
        stage("Publish to Nexus Repository Manager") {
                    steps {
                        script {
                            pom = readMavenPom file: "pom.xml";
                            filesByGlob = findFiles(glob: "target/.${pom.packaging}");
                            echo "${filesByGlob[0].name} ${filesByGlob[0].path} ${filesByGlob[0].directory} ${filesByGlob[0].length} ${filesByGlob[0].lastModified}"
                            artifactPath = filesByGlob[0].path;
                            artifactExists = fileExists artifactPath;
                            if(artifactExists) {
                                echo " File: ${artifactPath}, group: ${pom.groupId}, packaging: ${pom.packaging}, version ${pom.version}";
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
                                error " File: ${artifactPath}, could not be found";
                            }
                        }
                    }
        }
        stage('Sonar scan execution') {
                    // Run the sonar scan
                    steps {
                        script {
                            def mvnHome = tool 'M2_HOME'
                            withSonarQubeEnv {

                                sh "'${mvnHome}/bin/mvn'  verify sonar:sonar -Dintegration-tests.skip=true -Dmaven.test.failure.ignore=true  -Dsonar.login=admin -Dsonar.password=monaam1234"
                            }
                        }
                    }
        }
        // stage('DEV sanity check') {
            // steps {
                // give some time till the deployment is done, so we wait 45 seconds
                // sleep(45)
                // script {
                    // if (currentBuild.result == null || currentBuild.result == 'SUCCESS') {
                        // timeout(time: 1, unit: 'MINUTES') {
                            // script {
                                // def mvnHome = tool 'M2_HOME'
                                // //NOTE : if u change the sanity test class name , change it here as well
                                // sh "'${mvnHome}/bin/mvn' -Dtest=ApplicationSanityCheck_ITT surefire:test"
                            // }

                        // }
                    // }
                // }
            // }
        // }
        stage('Release and publish artifact') {
            when {
                // check if branch is master
                branch 'master'
            }
            steps {
                // create the release version then create a tage with it , then push to nexus releases the released jar
                script {
                    def mvnHome = tool 'M2_HOME' //
                    if (currentBuild.result == null || currentBuild.result == 'SUCCESS') {
                        def v = getReleaseVersion()
                        releasedVersion = v;
                        if (v) {
                            echo "Building version ${v} - so released version is ${releasedVersion}"
                        }
                        // jenkins user credentials ID which is transparent to the user and password change
                        sh "git tag -f v${v}"
                        sh "git push -f --tags"
                        sh "'${mvnHome}/bin/mvn' -Dmaven.test.skip=true  versions:set  -DgenerateBackupPoms=false -DnewVersion=${v}"
                        sh "'${mvnHome}/bin/mvn' -Dmaven.test.skip=true clean deploy"

                    } else {
                        error "Release is not possible. as build is not successful"
                    }
                }
            }
        }
        stage('Deploy to Acceptance') {
            when {
                // check if branch is master
                branch 'master'
            }
            steps {
                script {
                    if (currentBuild.result == null || currentBuild.result == 'SUCCESS') {
                        timeout(time: 3, unit: 'MINUTES') {
                            //input message:'Approve deployment?', submitter: 'it-ops'
                            input message: 'Approve deployment to UAT?'
                        }
                        timeout(time: 3, unit: 'MINUTES') {
                            //  deployment job which will take the relasesed version
                            if (releasedVersion != null && !releasedVersion.isEmpty()) {
                                // make the applciation name for the jar configurable
                                def jarName = "application-${releasedVersion}.jar"
                                echo "the application is deploying ${jarName}"
                                // NOTE : DO NOT FORGET to create your UAT deployment jar , check Job AlertManagerToUAT in Jenkins for reference
                                // the deployemnt should be based into Nexus repo
                                build job: 'AApplicationToACC', parameters: [[$class: 'StringParameterValue', name: 'jarName', value: jarName], [$class: 'StringParameterValue', name: 'appVersion', value: releasedVersion]]
                                echo 'the application is deployed !'
                            } else {
                                error 'the application is not  deployed as released version is null!'
                            }

                        }
                    }
                }
            }
        }
    }
    // post {
        // Always runs. And it runs before any of the other post conditions.
        // always {
            // Let's wipe out the workspace before we finish!
            // deleteDir()
        // }
        // success {
            // sendEmail("Successful");
        // }
        // unstable {
            // sendEmail("Unstable");
        // }
        // failure {
            // sendEmail("Failed");
        // }
    // }

// The options directive is for configuration that applies to the whole job.
    options {
        // For example, we'd like to make sure we only keep 10 builds at a time, so
        // we don't fill up our storage!
        buildDiscarder(logRotator(numToKeepStr: '5'))

        // And we'd really like to be sure that this build doesn't hang forever, so
        // let's time it out after an hour.
        timeout(time: 25, unit: 'MINUTES')
    }

}
def developmentArtifactVersion = ''
def releasedVersion = ''
// get change log to be send over the mail
@NonCPS
def getChangeString() {
    MAX_MSG_LEN = 100
    def changeString = ""

    echo "Gathering SCM changes"
    def changeLogSets = currentBuild.changeSets
    for (int i = 0; i < changeLogSets.size(); i++) {
        def entries = changeLogSets[i].items
        for (int j = 0; j < entries.length; j++) {
            def entry = entries[j]
            truncated_msg = entry.msg.take(MAX_MSG_LEN)
            changeString += " - ${truncated_msg} [${entry.author}]\n"
        }
    }

    if (!changeString) {
        changeString = " - No new changes"
    }
    return changeString
}

// def sendEmail(status) {
    // mail(
            // to: "$EMAIL_RECIPIENTS",
            // subject: "Build - " + status + " (${currentBuild.fullDisplayName})",
            // body: "Changes:\n " + getChangeString() + "\n\n Check console output at: /console" + "\n")
// }

def getDevVersion() {
    def gitCommit = sh(returnStdout: true, script: 'git rev-parse HEAD').trim()
    def versionNumber;
    if (gitCommit == null) {
        versionNumber = 1;
    } else {
        versionNumber = gitCommit.take(8);
    }
    print 'build  versions...'
    print versionNumber
    return versionNumber
}

def getReleaseVersion() {
    def pom = readMavenPom file: 'pom.xml'
    def gitCommit = sh(returnStdout: true, script: 'git rev-parse HEAD').trim()
    def versionNumber;
    if (gitCommit == null) {
        versionNumber = 1;
    } else {
        versionNumber = gitCommit.take(8);
    }
    return pom.version.replace("-SNAPSHOT", ".${versionNumber}")
}