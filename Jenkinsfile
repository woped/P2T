pipeline {
        
    environment {
        VERSION = getVersion()
        DOCKER_VERSION = getDockerVersion()
        registryCredential = 'docker-hub'
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
                sh 'mvn clean install -Dmaven.test.skip=true'
            }
        }
        stage('deploy jar') {
            steps {
                configFileProvider([configFile(fileId: 'nexus-credentials', variable: 'MAVEN_SETTINGS')]) {
                    sh 'mvn -s $MAVEN_SETTINGS deploy -Dmaven.test.skip=true'
                }
            }
        }
        stage('build docker') {
            steps {
                script {
                        docker.withRegistry('https://registry.hub.docker.com/v1/repositories/woped', registryCredential) {
                            def dockerImage = docker.build("woped/process2text:$DOCKER_VERSION")
                            def dockerImageLatest = docker.build("woped/process2text:latest")
                            dockerImage.push();
                            dockerImageLatest.push();
                    }
                }
            }
        }

        stage('deploy when main') {

            when { branch 'update-ci-pipeline' }

            steps {
                script {
                    def remote = [:]
                    remote.name = "woped"
                    remote.host = "woped.dh-karlsruhe.de"
                    remote.allowAnyHosts = true
                            
                    withCredentials([usernamePassword(credentialsId: 'sshUserAcct', passwordVariable: 'password', usernameVariable: 'userName')]) {
                        remote.user = userName
                        remote.password = password

                        stage("SSH Commands") {
                            sshCommand remote: remote, command: "cd /usr/local/bin/woped-webservice"
                            sshCommand remote: remote, command: "docker-compose pull p2t"
                            sshCommand remote: remote, command: "docker-compose up -d"
                            sshCommand remote: remote, command: "docker image prune -f"
                        } 
                    }
                }
            }
        }
    }

    post {
        always {
            cleanWs()
            
            sh 'docker image prune -af'
        }
        success {
            setBuildStatus("Build succeeded", "SUCCESS");
        }
        failure {
            setBuildStatus("Build not Successfull", "FAILURE");
            
            emailext body: "Something is wrong with ${env.BUILD_URL}",
                subject: "Failed Pipeline: ${currentBuild.fullDisplayName}",
                to: '${DEFAULT_RECIPIENTS}'
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

    if (version.toString().contains('SNAPSHOT')) {
        return version + '-' + "${currentBuild.startTimeInMillis}"
    } else {
        return version
    }
}

void setBuildStatus(String message, String state) {
  step([
      $class: "GitHubCommitStatusSetter",
      reposSource: [$class: "ManuallyEnteredRepositorySource", url: "https://github.com/tfreytag/P2T"],
      contextSource: [$class: "ManuallyEnteredCommitContextSource", context: "ci/jenkins/build-status"],
      errorHandlers: [[$class: "ChangingBuildStatusErrorHandler", result: "UNSTABLE"]],
      statusResultSource: [ $class: "ConditionalStatusResultSource", results: [[$class: "AnyBuildResult", message: message, state: state]] ]
  ]);
}
