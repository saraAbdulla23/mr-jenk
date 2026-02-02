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
                            sh 'mvn clean test'
                        }
                    }
                }
            }
        }

        stage('Frontend - Install & Test') {
            steps {
                dir("${FRONTEND_DIR}") {
                    sh 'npm install'
                    sh 'ng test --watch=false --browsers=ChromeHeadless'
                }
            }
        }

        stage('Frontend - Build') {
            steps {
                dir("${FRONTEND_DIR}") {
                    sh 'ng build --configuration production'
                }
            }
        }

        stage('Deploy Backend') {
            steps {
                sh '''
                for service in discovery-service api-gateway user-service product-service media-service
                do
                  cd backend/$service
                  mvn spring-boot:run &
                  cd -
                done
                '''
            }
        }

        stage('Deploy Frontend') {
            steps {
                sh '''
                cd front
                npm i
                ng serve
                '''
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
