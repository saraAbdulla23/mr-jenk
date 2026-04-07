pipeline {
    agent any

    tools {
        maven 'maven-3'
        nodejs 'node-20'
    }

    environment {
        VERSION        = "v${env.BUILD_NUMBER}"
        IMAGE_TAG      = "${VERSION}"

        MVN_LOCAL_REPO = "${WORKSPACE}/.m2/repository"
        CI             = "true"

        NOTIFY_EMAIL   = "sarakhalaf2312@gmail.com"

        API_GATEWAY_PORT     = "8087"
        DISCOVERY_PORT       = "8761"
        USER_SERVICE_PORT    = "8082"
        TRAVEL_SERVICE_PORT  = "8085"

        POSTGRES_PORT = "5432"
        NEO4J_HTTP    = "7474"

        BRANCH_NAME = "master"
        REPO_URL    = "https://github.com/saraAbdulla23/mr-jenk.git"
    }

    options {
        timestamps()
        timeout(time: 60, unit: 'MINUTES')
        buildDiscarder(logRotator(numToKeepStr: '10'))
    }

    stages {

        stage('Precheck - Docker Access') {
            steps {
                sh 'docker --version'
                sh 'docker compose version'
            }
        }

        stage('Checkout') {
            steps {
                script {
                    echo "Cloning branch ${env.BRANCH_NAME} from ${env.REPO_URL}"
                    sh "git clone --branch ${env.BRANCH_NAME} ${env.REPO_URL} ."
                }
            }
        }

        stage('Backend - Build & Test') {
            steps {
                script {
                    parallel(
                        "Discovery": { buildBackend("backend/discovery-service") },
                        "Gateway":   { buildBackend("backend/api-gateway") },
                        "User":      { buildBackend("backend/user-service") },
                        "Travel":    { buildBackend("backend/travel-service") }
                    )
                }
            }
        }

        stage('SonarQube Analysis') {
            steps {
                script {
                    def sonarUrl = "http://sonarqube:9000"

                    echo "Waiting for SonarQube to be UP..."
                    timeout(time: 2, unit: 'MINUTES') {
                        waitUntil {
                            def response = sh(script: "curl -s ${sonarUrl}/api/system/status || true", returnStdout: true).trim()
                            return response.contains('"UP"')
                        }
                    }

                    withSonarQubeEnv('SonarQube') {
                        withCredentials([string(credentialsId: 'sonar-token', variable: 'SONAR_TOKEN')]) {
                            def modules = ["discovery-service", "api-gateway", "user-service", "travel-service"]
                            def sonarStages = [:]

                            modules.each { module ->
                                sonarStages[module] = {
                                    dir("backend/${module}") {
                                        echo "Running SonarQube analysis for ${module}"
                                        sh """
                                        mvn clean verify sonar:sonar \
                                            -Dsonar.projectKey=${module}-travel-app \
                                            -Dsonar.host.url=${sonarUrl} \
                                            -Dsonar.login=\$SONAR_TOKEN
                                        """
                                    }
                                }
                            }

                            parallel sonarStages
                        }
                    }
                }
            }
        }

        stage('Quality Gate') {
            steps {
                timeout(time: 5, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }

        stage('Build Docker Images') {
            steps {
                script {
                    echo "Building Docker images: ${VERSION}"
                    withEnv(["IMAGE_TAG=${VERSION}"]) {
                        sh 'docker compose build'
                    }
                }
            }
        }

        stage('Deploy & Verify') {
            steps {
                script {
                    echo "Deploying ${VERSION}"
                    withEnv(["IMAGE_TAG=${VERSION}"]) {
                        sh 'ansible-playbook -i ansible/inventory ansible/playbook.yml || echo "Fallback to docker compose"'
                        sh 'docker compose up -d || true'
                    }

                    echo "Waiting for services to start..."
                    sleep 25

                    checkService("PostgreSQL", "http://postgres:${POSTGRES_PORT}", false)
                    checkService("Neo4j Browser", "http://neo4j:${NEO4J_HTTP}", false)

                    checkService("API Gateway", "http://api-gateway:${API_GATEWAY_PORT}/actuator/health")
                    checkService("Discovery Service", "http://discovery-service:${DISCOVERY_PORT}/actuator/health")
                    checkService("User Service", "http://user-service:${USER_SERVICE_PORT}/actuator/health")
                    checkService("Travel Service", "http://travel-service:${TRAVEL_SERVICE_PORT}/actuator/health")

                    echo "Deployment verified."
                }
            }
        }
    }

    post {
        always {
            junit allowEmptyResults: true, testResults: '**/target/surefire-reports/*.xml'
            cleanWs()
        }

        success {
            mail to: "${env.NOTIFY_EMAIL}",
                 subject: "Build SUCCESS - ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                 body: """Build successful!\n\nBranch: ${env.BRANCH_NAME}\nVersion: ${env.VERSION}\n\n${env.BUILD_URL}"""
        }

        failure {
            mail to: "${env.NOTIFY_EMAIL}",
                 subject: "Build FAILED - ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                 body: """Build failed!\n\nBranch: ${env.BRANCH_NAME}\n\n${env.BUILD_URL}"""
        }
    }
}

// ---------------- Helper Functions ----------------
def buildBackend(String path) {
    dir(path) {
        sh 'mkdir -p ${MVN_LOCAL_REPO}'
        sh """
        mvn clean test package -B \
        -Dmaven.repo.local=${env.MVN_LOCAL_REPO}
        """
    }
}

def checkService(String name, String url, boolean strict = true) {
    sh """
    echo "Checking ${name}..."
    for i in {1..10}; do
        if curl -s ${strict ? '-f' : ''} ${url} > /dev/null; then
            echo "${name} is reachable"
            break
        else
            echo "Waiting for ${name}..."
            sleep 5
        fi
        if [ \$i -eq 10 ]; then
            echo "${name} failed!"
            if [ "${strict}" = "true" ]; then
                exit 1
            fi
        fi
    done
    """
}