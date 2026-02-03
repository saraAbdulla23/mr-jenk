pipeline {
    agent any

    tools {
        jdk 'jdk-22'      // Must match your JDK installation name in Jenkins
        maven 'maven-3'   // Must match your Maven installation name in Jenkins
        nodejs 'node-18'  // Must match Node.js installation name in Jenkins
    }

    environment {
        BACKEND_DIR = "backend"
        FRONTEND_DIR = "front"
        MVN_OPTS = "-B -Dmaven.repo.local=$WORKSPACE/.m2/repository"
        MVN_HOME = tool name: 'maven-3', type: 'maven'
        PATH = "${env.MVN_HOME}/bin:${env.PATH}"
    }

    options {
        skipDefaultCheckout(false)
        timestamps()
        timeout(time: 60, unit: 'MINUTES')
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
            parallel {
                stage('Discovery Service') {
                    steps {
                        dir("${BACKEND_DIR}/discovery-service") {
                            echo "Building and testing discovery-service..."
                            sh "mvn clean test -Dspring.profiles.active=test $MVN_OPTS"
                        }
                    }
                }

                stage('API Gateway') {
                    steps {
                        dir("${BACKEND_DIR}/api-gateway") {
                            echo "Building and testing api-gateway..."
                            sh "mvn clean test -Dspring.profiles.active=test $MVN_OPTS"
                        }
                    }
                }

                stage('User Service') {
                    steps {
                        dir("${BACKEND_DIR}/user-service") {
                            echo "Building and testing user-service..."
                            sh "mvn clean test -Dspring.profiles.active=test $MVN_OPTS"
                        }
                    }
                }

                stage('Product Service') {
                    steps {
                        dir("${BACKEND_DIR}/product-service") {
                            echo "Building and testing product-service..."
                            sh "mvn clean test -Dspring.profiles.active=test $MVN_OPTS"
                        }
                    }
                }

                stage('Media Service') {
                    steps {
                        dir("${BACKEND_DIR}/media-service") {
                            echo "Building and testing media-service..."
                            sh "mvn clean test -Dspring.profiles.active=test $MVN_OPTS"
                        }
                    }
                }
            }
        }

        stage('Frontend - Install & Test') {
            steps {
                dir("${FRONTEND_DIR}") {
                    echo "Installing frontend dependencies..."
                    sh 'npm install'
                    echo "Running frontend tests..."
                    sh 'ng test --watch=false --browsers=ChromeHeadless'
                }
            }
        }

        stage('Frontend - Build') {
            steps {
                dir("${FRONTEND_DIR}") {
                    echo "Building Angular frontend for production..."
                    sh 'ng build --configuration production'
                }
            }
        }

        stage('Deploy Backend (Optional)') {
            steps {
                echo "Skipping backend deployment in CI/CD. Use Docker/K8s for production deployment."
            }
        }

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
