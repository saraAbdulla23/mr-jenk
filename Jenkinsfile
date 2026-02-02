pipeline {
    agent any

    tools {
        maven 'Maven3'
        nodejs 'Node18'
    }

    environment {
        BACKEND_DIR = "backend"
        FRONTEND_DIR = "front"
    }

    stages {

        stage('Checkout Code') {
            steps {
                git branch: 'main',
                    url: 'https://github.com/your-username/your-repo.git'
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
                script {
                    try {
                        sh '''
                        for service in discovery-service api-gateway user-service product-service media-service
                        do
                          cd backend/$service
                          mvn spring-boot:run &
                          cd -
                        done
                        '''
                    } catch (Exception e) {
                        error "Backend deployment failed"
                    }
                }
            }
        }

        stage('Deploy Frontend') {
            steps {
                sh '''
                cd front
                npm install -g serve
                serve -s dist/front -l 4200 &
                '''
            }
        }
    }

    post {
        success {
            mail to: 'team@example.com',
                 subject: '✅ Jenkins Build Successful',
                 body: 'The CI/CD pipeline completed successfully.'
        }

        failure {
            mail to: 'team@example.com',
                 subject: '❌ Jenkins Build Failed',
                 body: 'The pipeline failed. Please check Jenkins logs.'
        }
    }
}
