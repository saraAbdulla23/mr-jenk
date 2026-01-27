pipeline {
    agent any

    // Trigger pipeline automatically on Git commits
    triggers {
        pollSCM('H/5 * * * *') // Poll every 5 minutes
    }

    environment {
        // Use Jenkins Credentials for sensitive info (replace IDs with your Jenkins credentials)
        SMTP_USER = credentials('smtp_user')
        SMTP_PASS = credentials('smtp_pass')
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
                    // Archive frontend test reports (update path if needed)
                    junit 'test-results/**/*.xml'
                }
            }
        }

        stage('Backend Build') {
            steps {
                echo '🔧 Building backend microservices'

                dir('backend/discovery-service') {
                    sh 'mvn clean package -DskipTests=false'
                }

                dir('backend/api-gateway') {
                    sh 'mvn clean package -DskipTests=false'
                }

                dir('backend/user-service') {
                    sh 'mvn clean package -DskipTests=false'
                }

                dir('backend/product-service') {
                    sh 'mvn clean package -DskipTests=false'
                }

                dir('backend/media-service') {
                    sh 'mvn clean package -DskipTests=false'
                }
            }
        }

        stage('Backend Tests') {
            steps {
                echo '🧪 Running backend tests (JUnit)'
                dir('backend') {
                    sh 'mvn test'
                    // Archive JUnit test reports
                    junit '**/target/surefire-reports/*.xml'
                }
            }
        }

        stage('Deploy') {
            steps {
                echo '🚀 Deploying application...'

                // Replace these with real deployment commands
                sh '''
                echo "Starting Discovery Service..."
                echo "Starting API Gateway..."
                echo "Starting User, Product, and Media Services..."
                echo "Frontend served via build output..."
                '''
            }
        }
    }

    post {
        success {
            echo '✅ CI/CD Pipeline Completed Successfully'
            script {
                try {
                    mail to: 'sarakhalaf2312@gmail.com',
                         subject: '✅ Jenkins Build SUCCESS',
                         body: 'Your CI/CD pipeline completed successfully.'
                } catch (err) {
                    echo '⚠️ Email notification failed (SMTP not configured)'
                }
            }
        }

        failure {
            echo '❌ CI/CD Pipeline Failed – Rollback Initiated'
            echo '🔄 Rolling back to last stable version...'
            script {
                // Replace with real rollback logic
                sh '''
                echo "Stopping all services..."
                echo "Reverting to last stable deployment..."
                '''
                try {
                    mail to: 'sarakhalaf2312@gmail.com',
                         subject: '❌ Jenkins Build FAILED',
                         body: 'Your CI/CD pipeline failed. Please check Jenkins logs.'
                } catch (err) {
                    echo '⚠️ Email notification failed (SMTP not configured)'
                }
            }
        }
    }
}
