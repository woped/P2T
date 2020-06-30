pipeline {
    environment {
        MVN_SET = credentials('nexus-credentials')
        //VERSION = getVersion()
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
        stage('build docker') {
            steps {
                script {
                    node {
                        docker.build("p2t:3.7.1")
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