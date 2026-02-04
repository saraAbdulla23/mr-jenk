pipeline {
    agent any

    tools {
        maven 'maven-3'
        nodejs 'node-18'
    }

    environment {
        BACKEND_DIR    = "backend"
        FRONTEND_DIR   = "front"
        MVN_LOCAL_REPO = "${WORKSPACE}/.m2/repository"
        SPRING_PROFILES_ACTIVE = "test"

        // Deployment paths (adjust to your servers)
        BACKEND_DEPLOY_DIR = "/opt/ecommerce/backend"
        FRONTEND_DEPLOY_DIR = "/opt/ecommerce/frontend"
        BACKUP_DIR = "/opt/ecommerce/backup"
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
                checkout([
                    $class: 'GitSCM',
                    branches: [[name: '*/master']],
                    userRemoteConfigs: [[
                        url: 'https://github.com/saraAbdulla23/mr-jenk.git'
                    ]]
                ])
            }
        }

        stage('Backend - Build & Test') {
            steps {
                script {
                    parallel(
                        "Discovery Service": { buildBackend("${BACKEND_DIR}/discovery-service") },
                        "API Gateway":       { buildBackend("${BACKEND_DIR}/api-gateway") },
                        "User Service":      { buildBackend("${BACKEND_DIR}/user-service") },
                        "Product Service":   { buildBackend("${BACKEND_DIR}/product-service") },
                        "Media Service":     { buildBackend("${BACKEND_DIR}/media-service") }
                    )
                }
            }
        }

        stage('Frontend - Install & Test') {
            steps {
                dir("${FRONTEND_DIR}") {
                    sh 'node -v'
                    sh 'npm -v'
                    sh 'npm install'
                    sh 'npx ng test --watch=false --browsers=ChromeHeadless'
                }
            }
        }

        stage('Frontend - Build') {
            steps {
                dir("${FRONTEND_DIR}") {
                    sh 'npx ng build --configuration production'
                    archiveArtifacts artifacts: 'dist/**/*', allowEmptyArchive: true
                }
            }
        }

        stage('Deploy Backend') {
            steps {
                script {
                    deployBackend("${BACKEND_DIR}")
                }
            }
        }

        stage('Deploy Frontend') {
            steps {
                script {
                    deployFrontend("${FRONTEND_DIR}")
                }
            }
        }
    }

    post {
        always { cleanWs() }
        success {
            mail to: 'sarakhalaf2312@gmail.com',
                 subject: '✅ Jenkins Build & Deploy Successful',
                 body: 'CI/CD pipeline completed successfully. Backend + Frontend built and deployed.'
        }
        failure {
            mail to: 'sarakhalaf2312@gmail.com',
                 subject: '❌ Jenkins Build/Deploy Failed',
                 body: 'Pipeline failed. Check Jenkins console output for details.'
        }
    }
}

// =================================================
// Shared backend build function
// =================================================
def buildBackend(String dirPath) {
    dir(dirPath) {
        sh 'java -version'
        sh 'mvn -version'

        // Embedded Kafka & MongoDB are used automatically via test dependencies
        sh """
            mvn clean package \
            -B \
            -Dmaven.repo.local=${env.MVN_LOCAL_REPO} \
            -Dspring.profiles.active=${env.SPRING_PROFILES_ACTIVE}
        """

        archiveArtifacts artifacts: 'target/*.jar', allowEmptyArchive: false
    }
}

// =================================================
// Deploy functions with rollback
// =================================================
def deployBackend(String dirPath) {
    dir(dirPath) {
        def jarFile = sh(script: "ls target/*.jar | head -n 1", returnStdout: true).trim()
        def serviceName = dirPath.split('/')[-1]

        echo "Deploying backend service: ${serviceName}"

        // Backup old jar
        sh """
            mkdir -p ${env.BACKUP_DIR}/${serviceName}
            if [ -f ${env.BACKEND_DEPLOY_DIR}/${serviceName}.jar ]; then
                cp ${env.BACKEND_DEPLOY_DIR}/${serviceName}.jar ${env.BACKUP_DIR}/${serviceName}/
            fi
        """

        // Deploy new jar
        sh """
            cp ${jarFile} ${env.BACKEND_DEPLOY_DIR}/${serviceName}.jar
        """

        // Restart service (systemd example)
        sh """
            systemctl restart ${serviceName} || (
                echo 'Deployment failed, rolling back...'
                cp ${env.BACKUP_DIR}/${serviceName}/$(ls -t ${env.BACKUP_DIR}/${serviceName} | head -n1) ${env.BACKEND_DEPLOY_DIR}/${serviceName}.jar
                systemctl restart ${serviceName}
                exit 1
            )
        """
    }
}

def deployFrontend(String dirPath) {
    dir(dirPath) {
        echo "Deploying frontend"

        // Backup old frontend
        sh """
            mkdir -p ${env.BACKUP_DIR}/frontend
            if [ -d ${env.FRONTEND_DEPLOY_DIR} ]; then
                cp -r ${env.FRONTEND_DEPLOY_DIR}/* ${env.BACKUP_DIR}/frontend/
            fi
        """

        // Copy new build
        sh """
            rm -rf ${env.FRONTEND_DEPLOY_DIR}/*
            cp -r dist/* ${env.FRONTEND_DEPLOY_DIR}/
        """

        // Optional: restart frontend server (nginx example)
        sh """
            systemctl restart nginx || (
                echo 'Frontend deployment failed, rolling back...'
                rm -rf ${env.FRONTEND_DEPLOY_DIR}/*
                cp -r ${env.BACKUP_DIR}/frontend/* ${env.FRONTEND_DEPLOY_DIR}/
                systemctl restart nginx
                exit 1
            )
        """
    }
}
