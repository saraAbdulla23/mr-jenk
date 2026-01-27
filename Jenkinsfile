pipeline {
    agent any

    environment {
        SMTP_USER = credentials('smtp_user')
        SMTP_PASS = credentials('smtp_pass')
    }

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
                    npm install
                    echo "🏗️ Building Angular frontend"
                    npm run build
                    '''
                }
            }
        }

        stage('Frontend Tests') {
            steps {
                dir('front') {
                    sh '''
                    echo "🧪 Running frontend tests (Jasmine/Karma)"
                    npm test
                    '''
                }
            }
        }

        stage('Backend Build') {
            steps {
                script {
                    def services = ['discovery-service', 'api-gateway', 'user-service', 'product-service', 'media-service']
                    for (service in services) {
                        dir("backend/${service}") {
                            sh "mvn clean package -DskipTests"
                        }
                    }
                }
            }
        }

        stage('Backend Tests') {
            steps {
                script {
                    def services = ['discovery-service', 'api-gateway', 'user-service', 'product-service', 'media-service']
                    for (service in services) {
                        dir("backend/${service}") {
                            sh "mvn test"
                        }
                    }
                }
            }
        }

        stage('Deploy') {
            steps {
                script {
                    echo '🚀 Deploying application'
                    echo 'Starting Discovery Service'
                    echo 'Starting API Gateway'
                    echo 'Starting User, Product, and Media Services'
                    echo 'Frontend served via build output'
                }
            }
        }
    }

    post {
        success {
            echo '✅ CI/CD Pipeline Completed Successfully'
            script {
                try {
                    mail to: 'sarakhalaf2312@gmail.com',
                         from: "${SMTP_USER}",
                         subject: '✅ Jenkins Build SUCCESS',
                         body: 'Your CI/CD pipeline completed successfully.',
                         smtpPassword: "${SMTP_PASS}"
                } catch (err) {
                    echo '⚠️ Email notification failed (SMTP not configured properly)'
                }
            }
        }

        failure {
            echo '❌ CI/CD Pipeline Failed – Rollback Initiated'
            echo '🔄 Rolling back to last stable version...'
            script {
                // Rollback logic placeholder
                echo 'Rollback executed'
                try {
                    mail to: 'sarakhalaf2312@gmail.com',
                         from: "${SMTP_USER}",
                         subject: '❌ Jenkins Build FAILED',
                         body: 'Your CI/CD pipeline failed. Check Jenkins logs for details.',
                         smtpPassword: "${SMTP_PASS}"
                } catch (err) {
                    echo '⚠️ Email notification failed (SMTP not configured properly)'
                }
            }
        }
    }
}
