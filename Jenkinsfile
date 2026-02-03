pipeline {
    agent any

    tools {
        maven 'maven-3'         // Name of Maven installation in Jenkins Global Tool Config
        nodejs 'node-18'        // Name of NodeJS installation in Jenkins Global Tool Config
    }

    environment {
        // Auto-detect JAVA_HOME or fallback to your Mac path
        JAVA_HOME = "${tool 'jdk-22'}"
        PATH = "${JAVA_HOME}/bin:${tool 'maven-3'}/bin:${tool 'node-18'}/bin:${env.PATH}"
        BACKEND_DIR = "backend"
        FRONTEND_DIR = "front"
        MVN_OPTS = "-B -Dmaven.repo.local=$WORKSPACE/.m2/repository"
    }

    options {
        skipDefaultCheckout(false)
        timestamps()
        timeout(time: 60, unit: 'MINUTES')
        buildDiscarder(logRotator(numToKeepStr: '10'))
        parallelsAlwaysFailFast()
    }

    stages {

        stage('Checkout Source Code') {
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
                        "Discovery Service": {
                            dir("${BACKEND_DIR}/discovery-service") {
                                withEnv(["JAVA_HOME=${env.JAVA_HOME}", "PATH=${env.PATH}"]) {
                                    echo "Building and testing discovery-service..."
                                    sh "${JAVA_HOME}/bin/java -version"
                                    sh "mvn clean test $MVN_OPTS"
                                    archiveArtifacts artifacts: '**/target/*.jar', allowEmptyArchive: true
                                }
                            }
                        },
                        "API Gateway": {
                            dir("${BACKEND_DIR}/api-gateway") {
                                withEnv(["JAVA_HOME=${env.JAVA_HOME}", "PATH=${env.PATH}"]) {
                                    echo "Building and testing api-gateway..."
                                    sh "${JAVA_HOME}/bin/java -version"
                                    sh "mvn clean test $MVN_OPTS"
                                    archiveArtifacts artifacts: '**/target/*.jar', allowEmptyArchive: true
                                }
                            }
                        },
                        "User Service": {
                            dir("${BACKEND_DIR}/user-service") {
                                withEnv(["JAVA_HOME=${env.JAVA_HOME}", "PATH=${env.PATH}"]) {
                                    echo "Building and testing user-service..."
                                    sh "${JAVA_HOME}/bin/java -version"
                                    sh "mvn clean test $MVN_OPTS"
                                    archiveArtifacts artifacts: '**/target/*.jar', allowEmptyArchive: true
                                }
                            }
                        },
                        "Product Service": {
                            dir("${BACKEND_DIR}/product-service") {
                                withEnv(["JAVA_HOME=${env.JAVA_HOME}", "PATH=${env.PATH}"]) {
                                    echo "Building and testing product-service..."
                                    sh "${JAVA_HOME}/bin/java -version"
                                    sh "mvn clean test $MVN_OPTS"
                                    archiveArtifacts artifacts: '**/target/*.jar', allowEmptyArchive: true
                                }
                            }
                        },
                        "Media Service": {
                            dir("${BACKEND_DIR}/media-service") {
                                withEnv(["JAVA_HOME=${env.JAVA_HOME}", "PATH=${env.PATH}"]) {
                                    echo "Building and testing media-service..."
                                    sh "${JAVA_HOME}/bin/java -version"
                                    sh "mvn clean test $MVN_OPTS"
                                    archiveArtifacts artifacts: '**/target/*.jar', allowEmptyArchive: true
                                }
                            }
                        }
                    )
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
            echo "Cleaning up workspace..."
            cleanWs()
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
