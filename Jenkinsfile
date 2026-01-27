pipeline {
    agent any

    triggers {
        pollSCM('H/5 * * * *') // Poll Git every 5 minutes
    }

    environment {
        SMTP_USER = credentials('smtp_user') // replace with your Jenkins SMTP user ID
        SMTP_PASS = credentials('smtp_pass') // replace with your Jenkins SMTP password ID
    }

    stages {

        stage('Checkout') {
            steps {
                echo '🔄 Checking out source code from Git'
                checkout scm
            }
        }

        stage('Frontend Build') {
            steps {
                dir('front') {
                    echo '📦 Installing frontend dependencies'
                    sh 'npm install'

                    echo '🏗️ Building Angular frontend'
                    sh 'npm run build'
                }
            }
        }

        stage('Frontend Tests') {
            steps {
                dir('front') {
                    echo '🧪 Running frontend tests (Jasmine/Karma)'
                    sh 'npm test'
                    junit 'test-results/**/*.xml'
                }
            }
        }

        stage('Backend Build') {
            steps {
                echo '🔧 Building backend microservices'
                dir('backend/discovery-service') { sh 'mvn clean package -DskipTests=false' }
                dir('backend/api-gateway') { sh 'mvn clean package -DskipTests=false' }
                dir('backend/user-service') { sh 'mvn clean package -DskipTests=false' }
                dir('backend/product-service') { sh 'mvn clean package -DskipTests=false' }
                dir('backend/media-service') { sh 'mvn clean package -DskipTests=false' }
            }
        }

        stage('Backend Tests') {
            steps {
                dir('backend') {
                    echo '🧪 Running backend tests (JUnit)'
                    sh 'mvn test'
                    junit '**/target/surefire-reports/*.xml'
                }
            }
        }

        stage('Deploy') {
            steps {
                echo '🚀 Deploying application...'
                sh '''
                echo "Starting Discovery Service..."
                echo "Starting API Gateway..."
                echo "Starting User, Product, and Media Services..."
                echo "Frontend served via build output..."
                '''
            }
        }
    }

    // Separate rollback stage to avoid post-context issues
    post {
        success {
            echo '✅ CI/CD Pipeline Completed Successfully'
            script {
                try {
                    mail to: 'sarakhalaf2312@gmail.com',
                         subject: '✅ Jenkins Build SUCCESS',
                         body: 'Your CI/CD pipeline completed successfully.'
                } catch (err) {
                    echo '⚠️ Email notification skipped (SMTP not configured)'
                }
            }
        }

        failure {
            echo '❌ CI/CD Pipeline Failed'
        }
    }

    // Optional: run rollback in a separate stage
    // This ensures a node context is allocated
    // and avoids MissingContextVariableException
    stages {
        stage('Rollback on Failure') {
            when {
                expression { currentBuild.result == 'FAILURE' }
            }
            steps {
                echo '🔄 Rolling back to last stable version...'
                sh '''
                echo "Stopping all services..."
                echo "Reverting to last stable deployment..."
                '''
            }
        }
    }
}
