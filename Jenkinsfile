pipeline {
    environment {
        MVN_SET = credentials('nexus-credentials')
        VERSION = getVersion()
        DOCKER_VERSION = getDockerVersion()
    }
    agent any

    stages {
        stage('build') {
            agent {
                docker {
                    image 'maven:3.6.3-jdk-11'
                    args '-u root'
                }
            }
            steps {
                sh 'mvn install -Dmaven.test.skip=true'
            }
        }
        stage('deploy jar') {
            environment {
                MVN_SET = credentials('nexus-credentials')
            }
            agent {
                docker {
                    image 'maven:3.6.3-jdk-11'
                    args '-u root'
                }
            }
            steps {
                sh 'mvn -s $MVN_SET deploy -Dmaven.test.skip=true'
            }
        }
        stage('build docker') {
            steps {
                script {
                    node {
                        docker.withRegistry('http://localhost:5000/repository/WoPeD', 'nexus-docker-registry') {
                            def dockerImage = docker.build("p2t:$DOCKER_VERSION")
                            dockerImage.push();
                        }
                    }
                }
            }
        }
    }
}

def getVersion() {
    pom = readMavenPom file: 'pom.xml'
    return pom.version
}

def getDockerVersion() {
    pom = readMavenPom file: 'pom.xml'
    version = pom.version

    if(version.toString().contains('SNAPSHOT')) {
        return version + '-' + "${currentBuild.startTimeInMillis}"
    } else {
        return version
    }
}