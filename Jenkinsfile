pipeline {
    environment {
        MVN_SET = credentials('nexus-credentials')
    }
    agent {
        docker {
            image 'maven:3.6.3-jdk-11'
            args '-u root'
        }
    }

    stages {
        stage('build') {
            steps {
                sh 'mvn install -Dmaven.test.skip=true'
            }
        }
        stage('build docker') {
            steps {
                def pom = readMavenPom file: 'pom.xml'
                def version = pom.version
                sh "docker build -t p2t:$version ."
            }
        }
    }
}