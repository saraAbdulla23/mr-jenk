pipeline {
    agent any

    tools {
        maven 'maven-3'        // Jenkins Maven tool (3.9.12)
        nodejs 'node-18'       // Jenkins NodeJS tool
    }

    environment {
        BACKEND_DIR = "backend"
        FRONTEND_DIR = "front"
        MVN_LOCAL_REPO = "${WORKSPACE}/.m2/repository"
    }

    options {
        timestamps()
        timeout(time: 60, unit: 'MINUTES')
        buildDiscarder(logRotator(numToKeepStr: '10'))
        parallelsAlwaysFailFast()
    }

    stages {

        stage('Checkout SCM') {
            steps {
                checkout([
                    $class: 'GitSCM',
                    branches: [[name: '*/master']],
                    userRemoteConfigs: [[url: 'https://github.com/saraAbdulla23/mr-jenk.git']]
                ])
            }
        }

        stage('Backend - Build & Test') {
            steps {
                script {
                    parallel(
                        "Discovery Service": {
                            buildBackend("${BACKEND_DIR}/discovery-service")
                        },
                        "API Gateway": {
                            buildBackend("${BACKEND_DIR}/api-gateway")
                        },
                        "User Service": {
                            buildBackend("${BACKEND_DIR}/user-service")
                        },
                        "Product Service": {
                            buildBackend("${BACKEND_DIR}/product-service")
                        },
                        "Media Service": {
                            buildBackend("${BACKEND_DIR}/media-service")
                        }
                    )
                }
            }
        }

        stage('Frontend - Install & Test') {
            steps {
                dir("${FRONTEND_DIR}") {
                    sh 'node -v'
                    sh 'npm -v'

                    sh 'npm install'
                    sh 'npx ng test --watch=false --browsers=ChromeHeadless'
                }
            }
        }

        stage('Frontend - Build') {
            steps {
                dir("${FRONTEND_DIR}") {
                    sh 'npx ng build --configuration production'
                    archiveArtifacts artifacts: 'dist/**/*', allowEmptyArchive: true
                }
            }
        }

        stage('Deploy Backend (Optional)') {
            steps {
                echo "Skipping backend deployment in CI/CD."
            }
        }

        stage('Deploy Frontend (Optional)') {
            steps {
                echo "Skipping frontend deployment in CI/CD."
            }
        }
    }

    post {
        always {
            cleanWs()
        }

        success {
            mail to: 'sarakhalaf2312@gmail.com',
                 subject: '✅ Jenkins Build Successful',
                 body: 'CI/CD pipeline completed successfully. Backend + Frontend built.'
        }

        failure {
            mail to: 'sarakhalaf2312@gmail.com',
                 subject: '❌ Jenkins Build Failed',
                 body: 'Pipeline failed. Check Jenkins console output.'
        }
    }
}

// ---------------------------
// Shared backend build function
// ---------------------------
def buildBackend(String dirPath) {
    dir(dirPath) {
        sh 'java -version'
        sh 'mvn -version'

        sh """
            mvn clean test \
            -B \
            -Dmaven.repo.local=${env.MVN_LOCAL_REPO}
        """

        archiveArtifacts artifacts: 'target/*.jar', allowEmptyArchive: true
    }
}
