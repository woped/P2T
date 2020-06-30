pipeline {
    environment {
        MVN_SET = credentials('nexus-credentials')
        VERSION = getVersion()
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
            agent {
                docker {
                    image 'maven:3.6.3-jdk-11'
                    args '-u root'
                }
            }
            steps {
                sh "mvn -s ${MVN_SET} deploy -Dmaven.test.skip=true"
            }
        }
        stage('build docker') {
            steps {
                script {
                    node {
                        docker.withRegistry('http://localhost:5000/repository/WoPeD', 'nexus-docker-registry') {
                            def dockerImage = docker.build("p2t:$version")
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