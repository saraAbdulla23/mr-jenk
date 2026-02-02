pipeline {
    agent any

    tools {
        maven 'maven-3'
        nodejs 'node-18'
    }

    environment {
        BACKEND_DIR = "backend"
        FRONTEND_DIR = "front"
    }

    options {
        skipDefaultCheckout(false)
        timestamps()
    }

    stages {

        stage('Checkout Source Code') {
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

        stage('Backend - Build & Test') {
            steps {
                script {
                    def services = [
                        'discovery-service',
                        'api-gateway',
                        'user-service',
                        'product-service',
                        'media-service'
                    ]

                    for (service in services) {
                        dir("${BACKEND_DIR}/${service}") {
                            echo "Building and testing ${service}..."
                            sh 'mvn clean test'
                        }
                    }
                }
            }
        }

        stage('Frontend - Install & Test') {
            steps {
                dir("${FRONTEND_DIR}") {
                    echo "Installing dependencies and running tests..."
                    sh 'npm install'
                    sh 'ng test --watch=false --browsers=ChromeHeadless'
                }
            }
        }

        stage('Frontend - Build') {
            steps {
                dir("${FRONTEND_DIR}") {
                    echo "Building frontend for production..."
                    sh 'ng build --configuration production'
                }
            }
        }

        // Optional: backend deployment (local/dev)
        stage('Deploy Backend (Optional)') {
            steps {
                echo "Skipping backend deployment in CI/CD. Deploy manually or via Docker/K8s."
            }
        }

        // Optional: frontend deployment (local/dev)
        stage('Deploy Frontend (Optional)') {
            steps {
                echo "Skipping frontend serve in CI/CD. Use built files from dist/ for deployment."
            }
        }
    }

    post {
        success {
            mail to: 'sarakhalaf2312@gmail.com',
                 subject: '✅ Jenkins Build Successful',
                 body: 'CI/CD pipeline completed successfully.'
        }

        failure {
            mail to: 'sarakhalaf2312@gmail.com',
                 subject: '❌ Jenkins Build Failed',
                 body: 'Pipeline failed. Check Jenkins console output.'
        }
    }
}
