pipeline {
    agent any

    environment {
        // Add environment variables here if needed
        NODE_ENV = 'production'
    }

    stages {

        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Frontend Build') {
            agent {
                docker { image 'node:20' } // Node.js for frontend
            }
            steps {
                dir('front') {
                    sh '''
                    echo "📦 Installing frontend dependencies"
                    npm install
                    echo "🚀 Building frontend"
                    npm run build
                    '''
                }
            }
        }

        stage('Frontend Tests') {
            agent {
                docker { image 'node:20' }
            }
            steps {
                dir('front') {
                    sh '''
                    echo "🧪 Running frontend tests"
                    npm test
                    '''
                }
            }
        }

        stage('Backend Build') {
            steps {
                dir('back') {
                    sh '''
                    echo "🛠 Building backend"
                    # Add your backend build commands here
                    '''
                }
            }
        }

        stage('Backend Tests') {
            steps {
                dir('back') {
                    sh '''
                    echo "🧪 Running backend tests"
                    # Add your backend test commands here
                    '''
                }
            }
        }

        stage('Deploy') {
            steps {
                sh '''
                echo "🚀 Deploying application"
                # Add your deployment commands here
                '''
            }
        }
    }

    post {
        success {
            echo "✅ CI/CD Pipeline Succeeded"
            mail to: 'sarakhalaf2312@gmail.com',
                 subject: '✅ Jenkins Build SUCCESS',
                 body: 'Your pipeline completed successfully.'
        }

        failure {
            echo "❌ CI/CD Pipeline Failed – Rollback Initiated"
            echo "🔄 Rolling back to last stable version..."
            sh '''
            echo "Rollback executed"
            # Add rollback commands if needed
            '''
            mail to: 'sarakhalaf2312@gmail.com',
                 subject: '❌ Jenkins Build FAILED',
                 body: 'Your pipeline failed. Check Jenkins logs.'
        }
    }
}
