pipeline {
    agent any

    stages {

        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build Backend') {
            steps {
                dir('backend') {
                    sh '''
                    mvn clean package -DskipTests
                    '''
                }
            }
        }

        stage('Backend Tests') {
            steps {
                dir('backend') {
                    sh '''
                    mvn test
                    '''
                }
            }
        }

        stage('Build Frontend') {
            steps {
                dir('frontend') {
                    sh '''
                    npm install
                    npm run build
                    '''
                }
            }
        }

        stage('Frontend Tests') {
            steps {
                dir('frontend') {
                    sh '''
                    npm run test -- --watch=false
                    '''
                }
            }
        }

        stage('Deploy') {
            steps {
                sh '''
                docker-compose down
                docker-compose up -d --build
                '''
            }
        }
    }

    post {
        success {
            echo '✅ CI/CD Pipeline Completed Successfully'
        }
        failure {
            echo '❌ Pipeline Failed – Rolling Back'
            sh '''
            docker-compose down
            docker-compose up -d
            '''
        }
    }
}
