pipeline {
    agent any

    tools {
        maven 'maven-3'        // Jenkins Maven tool
        nodejs 'node-18'       // Jenkins NodeJS tool
    }

    environment {
        BACKEND_DIR    = "backend"
        FRONTEND_DIR   = "front"
        MVN_LOCAL_REPO = "${WORKSPACE}/.m2/repository"
        SPRING_PROFILES_ACTIVE = "test"  // Use test profile
        KAFKA_BOOTSTRAP_SERVERS = ""     // Will be set dynamically
    }

    options {
        timestamps()
        timeout(time: 60, unit: 'MINUTES')
        buildDiscarder(logRotator(numToKeepStr: '10'))
        parallelsAlwaysFailFast()
    }

    stages {

        // =============================
        // Checkout SCM
        // =============================
        stage('Checkout SCM') {
            steps {
                checkout([
                    $class: 'GitSCM',
                    branches: [[name: '*/master']],
                    userRemoteConfigs: [[
                        url: 'https://github.com/saraAbdulla23/mr-jenk.git'
                    ]]
                ])
            }
        }

        // =============================
        // Start Embedded Kafka
        // =============================
        stage('Start Embedded Kafka') {
            steps {
                script {
                    // Start Kafka container using Testcontainers
                    KAFKA_CONTAINER_SCRIPT = '''
                        docker run -d --name ci-kafka -p 9092:9092 \
                        -e KAFKA_ADVERTISED_LISTENERS=PLAINTEXT://localhost:9092 \
                        -e KAFKA_ZOOKEEPER_CONNECT=localhost:2181 \
                        -e KAFKA_BROKER_ID=1 \
                        wurstmeister/kafka:2.13-2.8.0
                    '''
                    sh KAFKA_CONTAINER_SCRIPT

                    // Wait a few seconds for Kafka to start
                    sh 'sleep 15'

                    // Set environment variable for Maven builds
                    env.KAFKA_BOOTSTRAP_SERVERS = "localhost:9092"
                    echo "Kafka running at ${env.KAFKA_BOOTSTRAP_SERVERS}"
                }
            }
        }

        // =============================
        // Backend - Build & Test
        // =============================
        stage('Backend - Build & Test') {
            steps {
                script {
                    parallel(
                        "Discovery Service": { buildBackend("${BACKEND_DIR}/discovery-service") },
                        "API Gateway":       { buildBackend("${BACKEND_DIR}/api-gateway") },
                        "User Service":      { buildBackend("${BACKEND_DIR}/user-service") },
                        "Product Service":   { buildBackend("${BACKEND_DIR}/product-service") },
                        "Media Service":     { buildBackend("${BACKEND_DIR}/media-service") }
                    )
                }
            }
        }

        // =============================
        // Frontend - Install & Test
        // =============================
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

        // =============================
        // Frontend - Build
        // =============================
        stage('Frontend - Build') {
            steps {
                dir("${FRONTEND_DIR}") {
                    sh 'npx ng build --configuration production'
                    archiveArtifacts artifacts: 'dist/**/*', allowEmptyArchive: true
                }
            }
        }

        // =============================
        // Deploy (Optional)
        // =============================
        stage('Deploy Backend (Optional)') { steps { echo "Skipping backend deployment." } }
        stage('Deploy Frontend (Optional)') { steps { echo "Skipping frontend deployment." } }
    }

    // =============================
    // Post actions
    // =============================
    post {
        always {
            // Stop and remove Kafka container
            sh 'docker rm -f ci-kafka || true'
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

// =================================================
// Shared backend build function
// =================================================
def buildBackend(String dirPath) {
    dir(dirPath) {
        sh 'java -version'
        sh 'mvn -version'

        sh """
            mvn clean package \
            -B \
            -Dmaven.repo.local=${env.MVN_LOCAL_REPO} \
            -Dspring.kafka.bootstrap-servers=${env.KAFKA_BOOTSTRAP_SERVERS} \
            -Dspring.profiles.active=${env.SPRING_PROFILES_ACTIVE}
        """

        archiveArtifacts artifacts: 'target/*.jar', allowEmptyArchive: false
    }
}
