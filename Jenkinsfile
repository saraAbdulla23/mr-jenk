pipeline {
    agent any

    tools {
        maven 'maven-3'
        nodejs 'node-18'
    }

    environment {
        BACKEND_DIR    = "backend"
        FRONTEND_DIR   = "front"
        MVN_LOCAL_REPO = "${WORKSPACE}/.m2/repository"
        SPRING_PROFILES_ACTIVE = "test"
        KAFKA_BOOTSTRAP_SERVERS = "" // Set dynamically
        MONGO_EMBEDDED_URI = ""      // Set dynamically
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
                    userRemoteConfigs: [[
                        url: 'https://github.com/saraAbdulla23/mr-jenk.git'
                    ]]
                ])
            }
        }

        stage('Start Embedded Kafka') {
            steps {
                script {
                    echo "Starting Kafka..."
                    sh '''
                        docker run -d --name ci-kafka -p 9092:9092 \
                        -e KAFKA_ADVERTISED_LISTENERS=PLAINTEXT://localhost:9092 \
                        -e KAFKA_ZOOKEEPER_CONNECT=localhost:2181 \
                        -e KAFKA_BROKER_ID=1 \
                        wurstmeister/kafka:2.13-2.8.0
                        sleep 15
                    '''
                    env.KAFKA_BOOTSTRAP_SERVERS = "localhost:9092"
                    echo "Kafka running at ${env.KAFKA_BOOTSTRAP_SERVERS}"
                }
            }
        }

        stage('Start Embedded MongoDB') {
            steps {
                script {
                    echo "Starting MongoDB..."
                    sh 'docker run -d --name ci-mongo -P mongo:7.0.4'
                    def port = sh(
                        script: "docker port ci-mongo 27017 | cut -d':' -f2",
                        returnStdout: true
                    ).trim()
                    env.MONGO_EMBEDDED_URI = "mongodb://localhost:${port}"
                    echo "MongoDB running at ${env.MONGO_EMBEDDED_URI}"
                }
            }
        }

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

        stage('Deploy Backend (Optional)') { steps { echo "Skipping backend deployment." } }
        stage('Deploy Frontend (Optional)') { steps { echo "Skipping frontend deployment." } }
    }

    post {
        always {
            sh 'docker rm -f ci-kafka || true'
            sh 'docker rm -f ci-mongo || true'
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

// ==========================================
// Shared backend build function
// ==========================================
def buildBackend(String dirPath) {
    dir(dirPath) {
        sh 'java -version'
        sh 'mvn -version'

        sh """
            mvn clean package \
            -B \
            -Dmaven.repo.local=${env.MVN_LOCAL_REPO} \
            -Dspring.kafka.bootstrap-servers=${env.KAFKA_BOOTSTRAP_SERVERS} \
            -Dspring.data.mongodb.uri=${env.MONGO_EMBEDDED_URI} \
            -Dspring.profiles.active=${env.SPRING_PROFILES_ACTIVE}
        """

        archiveArtifacts artifacts: 'target/*.jar', allowEmptyArchive: false
    }
}
