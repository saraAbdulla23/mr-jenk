pipeline {
    agent any

    tools {
        maven 'maven-3'
        nodejs 'node-20'
    }

    environment {
        VERSION        = "v${env.BUILD_NUMBER}"
        STABLE_TAG     = "stable"
        IMAGE_TAG      = "${VERSION}"
        MVN_LOCAL_REPO = "${WORKSPACE}/.m2/repository"
        NPM_CACHE      = "${WORKSPACE}/.npm"
        CI             = "true"
        NOTIFY_EMAIL   = "sarakhalaf2312@gmail.com"

        // Ports from docker-compose
        FRONTEND_PORT       = "4200"
        API_GATEWAY_PORT    = "8087"
        DISCOVERY_PORT      = "8761"
        USER_SERVICE_PORT   = "8082"
        PRODUCT_SERVICE_PORT= "8085"
    }

    options {
        timestamps()
        timeout(time: 60, unit: 'MINUTES')
        buildDiscarder(logRotator(numToKeepStr: '10'))
    }

    stages {

        // PRECHECK: Docker Permissions
        stage('Precheck - Docker Access') {
            steps {
                script {
                    sh 'docker --version'
                    sh 'docker compose version'

                    def sock = '/var/run/docker.sock'
                    def sockInfo = sh(script: "ls -l ${sock}", returnStdout: true).trim()
                    echo "Docker socket info: ${sockInfo}"

                    def canAccess = sh(script: "docker info > /dev/null 2>&1 && echo OK || echo FAIL", returnStdout: true).trim()
                    if (canAccess != 'OK') {
                        error """
                        Jenkins cannot access Docker. 
                        Make sure the Jenkins user is in the 'docker' group:
                        sudo usermod -aG docker jenkins
                        """
                    }
                }
            }
        }

        // CHECKOUT
        stage('Checkout') {
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

        // BACKEND BUILD + TEST
        stage('Backend - Build & Test') {
            steps {
                script {
                    parallel(
                        "Discovery": { buildBackend("backend/discovery-service") },
                        "Gateway":   { buildBackend("backend/api-gateway") },
                        "User":      { buildBackend("backend/user-service") },
                        "Product":   { buildBackend("backend/product-service") }
                    )
                }
            }
        }

        // FRONTEND BUILD + TEST
        stage('Frontend - Build') {
            steps {
                dir('front') {
                    sh 'mkdir -p ${NPM_CACHE}'
                    sh 'npm config set cache ${NPM_CACHE} --global'
                    sh 'npm ci'
                    sh 'npm run build'
                }
            }
        }

        // BUILD DOCKER IMAGES
        stage('Build Docker Images') {
            steps {
                script {
                    echo "Building Docker images with tag: ${VERSION}"
                    withEnv(["IMAGE_TAG=${VERSION}"]) {
                        sh 'docker compose build'
                    }
                }
            }
        }

        // DEPLOY & VERIFY
        stage('Deploy & Verify') {
            steps {
                script {
                    try {
                        echo "Deploying version ${VERSION}"
                        withEnv(["IMAGE_TAG=${VERSION}"]) {
                            sh 'docker compose up -d'
                        }

                        echo "Waiting for containers to start..."
                        sleep 20

                        // Check for crashed containers
                        sh """
                            if docker compose ps | grep Exit; then
                                echo "Detected crashed containers!"
                                exit 1
                            fi
                        """

                        echo "Deployment verified successfully."

                        // Smoke tests for each service
                        checkService("Frontend", "http://localhost:${FRONTEND_PORT}")
                        checkService("API Gateway", "http://localhost:${API_GATEWAY_PORT}/actuator/health")
                        checkService("Discovery Service", "http://localhost:${DISCOVERY_PORT}/actuator/health")
                        checkService("User Service", "http://localhost:${USER_SERVICE_PORT}/actuator/health")
                        checkService("Product Service", "http://localhost:${PRODUCT_SERVICE_PORT}/actuator/health")

                        // Tag images as stable
                        sh """
                            docker tag discovery-service:${VERSION} discovery-service:${STABLE_TAG} || true
                            docker tag api-gateway:${VERSION} api-gateway:${STABLE_TAG} || true
                            docker tag user-service:${VERSION} user-service:${STABLE_TAG} || true
                            docker tag product-service:${VERSION} product-service:${STABLE_TAG} || true
                            docker tag front:${VERSION} front:${STABLE_TAG} || true
                        """

                        echo "Stable version updated."

                        // Print clickable URLs
                        echo """
✅ Access your deployed services:
Frontend:       http://localhost:${FRONTEND_PORT}
API Gateway:    http://localhost:${API_GATEWAY_PORT}
Discovery:      http://localhost:${DISCOVERY_PORT}
User Service:   http://localhost:${USER_SERVICE_PORT}/actuator/health
Product Service:http://localhost:${PRODUCT_SERVICE_PORT}/actuator/health
"""
                    } catch (Exception e) {
                        echo "Deployment failed! Rolling back..."

                        withEnv(["IMAGE_TAG=${STABLE_TAG}"]) {
                            sh 'docker compose up -d'
                        }

                        error "Rollback executed due to failure."
                    }
                }
            }
        }
    }

    // POST ACTIONS
    post {

        always {
            junit allowEmptyResults: true, testResults: '**/target/surefire-reports/*.xml'
            cleanWs()
        }

        success {
            mail to: "${env.NOTIFY_EMAIL}",
                 subject: "✅ Build & Deployment SUCCESS - ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                 body: """
Build successful!

Version: ${env.VERSION}
Branch: master
Job: ${env.JOB_NAME}
Build: ${env.BUILD_NUMBER}

Details: ${env.BUILD_URL}

Frontend:       http://localhost:${FRONTEND_PORT}
API Gateway:    http://localhost:${API_GATEWAY_PORT}
"""
        }

        failure {
            mail to: "${env.NOTIFY_EMAIL}",
                 subject: "❌ Build or Deployment FAILED - ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                 body: """
Build failed!

Version: ${env.VERSION}
Job: ${env.JOB_NAME}
Build: ${env.BUILD_NUMBER}

Rollback (if needed) attempted.

Check logs:
${env.BUILD_URL}
"""
        }
    }
}

// BACKEND BUILD FUNCTION
def buildBackend(String path) {
    dir(path) {
        sh 'mkdir -p ${MVN_LOCAL_REPO}'
        sh """
            mvn clean test package -B \
            -Dmaven.repo.local=${env.MVN_LOCAL_REPO}
        """
    }
}

// SMOKE TEST FUNCTION
def checkService(String name, String url) {
    sh """
        echo "Checking ${name} at ${url} ..."
        for i in {1..10}; do
            if curl -s -f ${url} > /dev/null; then
                echo "${name} is healthy."
                break
            else
                echo "Waiting for ${name}..."
                sleep 5
            fi
            if [ \$i -eq 10 ]; then
                echo "${name} failed health check!"
                exit 1
            fi
        done
    """
}
