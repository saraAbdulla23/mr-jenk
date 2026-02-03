pipeline {
    agent any

    tools {
        maven 'maven-3'        // Jenkins Maven tool name
        nodejs 'node-18'       // Jenkins NodeJS tool name
        jdk 'jdk-22'           // Jenkins JDK tool name
    }

    environment {
        // Set JAVA_HOME dynamically using Jenkins tool installation
        JAVA_HOME = "${tool 'jdk-22'}"
        PATH = "${JAVA_HOME}/bin:${tool 'maven-3'}/bin:${tool 'node-18'}/bin:${env.PATH}"
        MVN_OPTS = "-B -Dmaven.repo.local=$WORKSPACE/.m2/repository"
        BACKEND_DIR = "backend"
        FRONTEND_DIR = "front"
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
                checkout([$class: 'GitSCM',
                    branches: [[name: '*/master']],
                    userRemoteConfigs: [[url: 'https://github.com/saraAbdulla23/mr-jenk.git']]
                ])
            }
        }

        stage('Backend - Build & Test') {
            steps {
                script {
                    parallel(
                        "Discovery Service": { buildBackend("${BACKEND_DIR}/discovery-service") },
                        "API Gateway": { buildBackend("${BACKEND_DIR}/api-gateway") },
                        "User Service": { buildBackend("${BACKEND_DIR}/user-service") },
                        "Product Service": { buildBackend("${BACKEND_DIR}/product-service") },
                        "Media Service": { buildBackend("${BACKEND_DIR}/media-service") }
                    )
                }
            }
        }

        stage('Frontend - Install & Test') {
            steps {
                dir("${FRONTEND_DIR}") {
                    echo "Installing frontend dependencies..."
                    sh 'npm install'
                    echo "Running frontend tests..."
                    sh 'npx ng test --watch=false --browsers=ChromeHeadless'
                }
            }
        }

        stage('Frontend - Build') {
            steps {
                dir("${FRONTEND_DIR}") {
                    echo "Building Angular frontend..."
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
                 body: 'CI/CD pipeline completed successfully. All backend services and frontend built.'
        }

        failure {
            mail to: 'sarakhalaf2312@gmail.com',
                 subject: '❌ Jenkins Build Failed',
                 body: 'Pipeline failed. Check Jenkins console output for failed stages.'
        }
    }
}

// ---------------------------
// Shared backend build function
// ---------------------------
def buildBackend(String dirPath) {
    dir(dirPath) {
        withEnv(["JAVA_HOME=${env.JAVA_HOME}", "PATH=${env.PATH}"]) {
            echo "Building and testing ${dirPath}..."
            sh "${env.JAVA_HOME}/bin/java -version"
            sh "mvn clean test ${env.MVN_OPTS}"
            archiveArtifacts artifacts: '**/target/*.jar', allowEmptyArchive: true
        }
    }
}
