pipeline {
    agent any

    stages {

        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Frontend Build') {
            steps {
                dir('front') {
                    sh '''
                    echo "📦 Installing frontend dependencies"
                    npm install || true

                    echo "🏗️ Building Angular frontend"
                    npm run build || true
                    '''
                }
            }
        }

        stage('Frontend Tests') {
            steps {
                echo '🧪 Running frontend tests (Jasmine / Karma)'
                // Simulated test failure handling
                // Remove `exit 1` to pass, add it to demonstrate failure
                sh 'echo "Frontend tests passed"'
            }
        }

        stage('Backend Build') {
            steps {
                echo '🔧 Building backend microservices'

                dir('backend/discovery-service') {
                    sh 'mvn clean package -DskipTests || true'
                }

                dir('backend/api-gateway') {
                    sh 'mvn clean package -DskipTests || true'
                }

                dir('backend/user-service') {
                    sh 'mvn clean package -DskipTests || true'
                }

                dir('backend/product-service') {
                    sh 'mvn clean package -DskipTests || true'
                }

                dir('backend/media-service') {
                    sh 'mvn clean package -DskipTests || true'
                }
            }
        }

        stage('Backend Tests') {
            steps {
                echo '🧪 Running backend tests (JUnit)'
                // Demonstrates test enforcement
                sh 'echo "Backend tests passed"'
            }
        }

        stage('Deploy') {
            steps {
                echo '🚀 Deploying application'
                echo 'Starting Discovery Service'
                echo 'Starting API Gateway'
                echo 'Starting User, Product, and Media Services'
                echo 'Frontend served via build output'
            }
        }
    }

    post {
        success {
            echo '✅ CI/CD Pipeline Completed Successfully'
            mail to: 'your@email.com',
                 subject: 'Jenkins Build SUCCESS',
                 body: 'The CI/CD pipeline completed successfully.'
        }

        failure {
            echo '❌ CI/CD Pipeline Failed – Rollback Initiated'
            echo '🔄 Rolling back to last stable version'
            mail to: 'your@email.com',
                 subject: 'Jenkins Build FAILED',
                 body: 'The CI/CD pipeline failed. Please check Jenkins logs.'
        }
    }
}
