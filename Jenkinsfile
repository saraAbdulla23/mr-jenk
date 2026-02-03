pipeline {
    agent any

    environment {
        BACKEND_DIR = "backend"
        FRONTEND_DIR = "front"
        MVN_OPTS = "-B -Dmaven.repo.local=$WORKSPACE/.m2/repository"
    }

    options {
        timestamps()
        timeout(time: 60, unit: 'MINUTES')
        buildDiscarder(logRotator(numToKeepStr: '10'))
        parallelsAlwaysFailFast()
    }

    stages {

        stage('Checkout SCM') {
            steps {
                checkout([$class: 'GitSCM',
                    branches: [[name: '*/master']],
                    userRemoteConfigs: [[url: 'https://github.com/saraAbdulla23/mr-jenk.git']]
                ])
            }
        }

        stage('Backend - Build & Test') {
            steps {
                script {
                    parallel(
                        "Discovery Service": { buildBackend("${BACKEND_DIR}/discovery-service") },
                        "API Gateway": { buildBackend("${BACKEND_DIR}/api-gateway") },
                        "User Service": { buildBackend("${BACKEND_DIR}/user-service") },
                        "Product Service": { buildBackend("${BACKEND_DIR}/product-service") },
                        "Media Service": { buildBackend("${BACKEND_DIR}/media-service") }
                    )
                }
            }
        }

        stage('Frontend - Install & Test') {
            steps {
                dir("${FRONTEND_DIR}") {
                    echo "Installing frontend dependencies..."
                    detectNode()
                    sh 'npm install'
                    echo "Running frontend tests..."
                    sh 'npx ng test --watch=false --browsers=ChromeHeadless'
                }
            }
        }

        stage('Frontend - Build') {
            steps {
                dir("${FRONTEND_DIR}") {
                    echo "Building Angular frontend..."
                    detectNode()
                    sh 'npx ng build --configuration production'
                    archiveArtifacts artifacts: 'dist/**/*', allowEmptyArchive: true
                }
            }
        }

        stage('Deploy Backend (Optional)') {
            steps {
                echo "Skipping backend deployment in CI/CD."
            }
        }

        stage('Deploy Frontend (Optional)') {
            steps {
                echo "Skipping frontend deployment in CI/CD."
            }
        }
    }

    post {
        always {
            cleanWs()   // <-- fixed, no node {} wrapper
        }

        success {
            mail to: 'sarakhalaf2312@gmail.com',
                 subject: '✅ Jenkins Build Successful',
                 body: 'CI/CD pipeline completed successfully. All backend services and frontend built.'
        }

        failure {
            mail to: 'sarakhalaf2312@gmail.com',
                 subject: '❌ Jenkins Build Failed',
                 body: 'Pipeline failed. Check Jenkins console output for failed stages.'
        }
    }
}

// ---------------------------
// Shared backend build function
// ---------------------------
def buildBackend(String dirPath) {
    dir(dirPath) {
        detectJava()
        echo "Building and testing ${dirPath}..."
        sh "mvn clean test ${MVN_OPTS}"
        archiveArtifacts artifacts: '**/target/*.jar', allowEmptyArchive: true
    }
}

// ---------------------------
// Utility: Detect Java
// ---------------------------
def detectJava() {
    sh '''
        if ! command -v java &> /dev/null; then
            echo "Java not found! Trying to auto-detect..."
            JAVA_HOME=$(dirname $(dirname $(readlink -f $(which javac))))
            PATH=$JAVA_HOME/bin:$PATH
            export JAVA_HOME PATH
        fi
        java -version
    '''
}

// ---------------------------
// Utility: Detect NodeJS
// ---------------------------
def detectNode() {
    sh '''
        if ! command -v node &> /dev/null; then
            echo "NodeJS not found! Trying to auto-detect..."
            NODE_HOME=$(dirname $(dirname $(readlink -f $(which node))))
            PATH=$NODE_HOME/bin:$PATH
            export NODE_HOME PATH
        fi
        node -v
        npm -v
    '''
}
