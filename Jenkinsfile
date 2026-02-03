pipeline {
    agent any

    tools {
        maven 'maven-3'        // Jenkins Maven installation name
        nodejs 'node-18'       // Jenkins Node.js installation name
    }

    environment {
        JAVA_HOME = "/Library/Java/JavaVirtualMachines/jdk-22.jdk/Contents/Home"
        PATH = "${JAVA_HOME}/bin:${tool 'maven-3'}/bin:${env.PATH}"
        BACKEND_DIR = "backend"
        FRONTEND_DIR = "front"
        MVN_OPTS = "-B -Dmaven.repo.local=$WORKSPACE/.m2/repository"
        ARTIFACTS_DIR = "$WORKSPACE/artifacts"
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
            parallel failFast: true, stages: [

                stage('Discovery Service') {
                    steps {
                        dir("${BACKEND_DIR}/discovery-service") {
                            withEnv(["JAVA_HOME=${env.JAVA_HOME}", "PATH=${env.PATH}"]) {
                                echo "Building and testing discovery-service..."
                                sh "mvn clean test $MVN_OPTS"
                                archiveArtifacts artifacts: '**/target/*.jar', allowEmptyArchive: true
                            }
                        }
                    }
                },

                stage('API Gateway') {
                    steps {
                        dir("${BACKEND_DIR}/api-gateway") {
                            withEnv(["JAVA_HOME=${env.JAVA_HOME}", "PATH=${env.PATH}"]) {
                                echo "Building and testing api-gateway..."
                                sh "mvn clean test $MVN_OPTS"
                                archiveArtifacts artifacts: '**/target/*.jar', allowEmptyArchive: true
                            }
                        }
                    }
                },

                stage('User Service') {
                    steps {
                        dir("${BACKEND_DIR}/user-service") {
                            withEnv(["JAVA_HOME=${env.JAVA_HOME}", "PATH=${env.PATH}"]) {
                                echo "Building and testing user-service..."
                                sh "mvn clean test $MVN_OPTS"
                                archiveArtifacts artifacts: '**/target/*.jar', allowEmptyArchive: true
                            }
                        }
                    }
                },

                stage('Product Service') {
                    steps {
                        dir("${BACKEND_DIR}/product-service") {
                            withEnv(["JAVA_HOME=${env.JAVA_HOME}", "PATH=${env.PATH}"]) {
                                echo "Building and testing product-service..."
                                sh "mvn clean test $MVN_OPTS"
                                archiveArtifacts artifacts: '**/target/*.jar', allowEmptyArchive: true
                            }
                        }
                    }
                },

                stage('Media Service') {
                    steps {
                        dir("${BACKEND_DIR}/media-service") {
                            withEnv(["JAVA_HOME=${env.JAVA_HOME}", "PATH=${env.PATH}"]) {
                                echo "Building and testing media-service..."
                                sh "mvn clean test $MVN_OPTS"
                                archiveArtifacts artifacts: '**/target/*.jar', allowEmptyArchive: true
                            }
                        }
                    }
                }

            ]
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
            script {
                def failedStages = currentBuild.rawBuild.getActions(hudson.model.ResultAction.class)
                    .collect { it.buildResult.toString() }
                mail to: 'sarakhalaf2312@gmail.com',
                     subject: '❌ Jenkins Build Failed',
                     body: "Pipeline failed. Check Jenkins console output for failed stages.\nFailed stages: ${failedStages}"
            }
        }
    }
}
