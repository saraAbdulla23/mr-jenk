pipeline {
    agent any

    tools {
        maven 'maven-3'
        nodejs 'node-20'
    }

    environment {
        BACKEND_DIR    = "backend"
        FRONTEND_DIR   = "front"
        MVN_LOCAL_REPO = "${WORKSPACE}/.m2/repository"
        SPRING_PROFILES_ACTIVE = "test"

        BACKEND_DEPLOY_DIR = "/opt/ecommerce/backend"
        FRONTEND_DEPLOY_DIR = "/opt/ecommerce/frontend"
        BACKUP_DIR = "/opt/ecommerce/backup"

        NPM_CACHE = "${WORKSPACE}/.npm"
        CI = "true"
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

        stage('Frontend - Install') {
            steps {
                dir("${FRONTEND_DIR}") {
                    sh 'mkdir -p ${NPM_CACHE}'
                    sh 'npm config set cache ${NPM_CACHE} --global'
                    sh 'node -v'
                    sh 'npm -v'
                    sh 'npm install --prefer-offline --no-audit --progress=false'
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
        always {
            cleanWs()
        }
        success {
            mail to: 'sarakhalaf2312@gmail.com',
                 subject: '✅ Jenkins Build & Deploy Successful',
                 body: "CI/CD pipeline completed successfully.\n\nBackend + Frontend built and deployed.\nBuild URL: ${env.BUILD_URL}"
        }
        failure {
            mail to: 'sarakhalaf2312@gmail.com',
                 subject: '❌ Jenkins Build/Deploy Failed',
                 body: "Pipeline failed. Check Jenkins console output: ${env.BUILD_URL}"
        }
    }
}

// ================= BACKEND BUILD =================
def buildBackend(String dirPath) {
    dir(dirPath) {
        sh 'java -version'
        sh 'mvn -version'
        sh 'mkdir -p ${MVN_LOCAL_REPO}'

        sh """
            mvn clean package -B \
            -Dmaven.repo.local=${env.MVN_LOCAL_REPO} \
            -Dspring.profiles.active=${env.SPRING_PROFILES_ACTIVE}
        """

        archiveArtifacts artifacts: 'target/*.jar', allowEmptyArchive: false
    }
}

// ================= DEPLOY BACKEND =================
def deployBackend(String dirPath) {
    dir(dirPath) {
        def jarFile = sh(script: "ls target/*.jar | head -n 1", returnStdout: true).trim()
        def serviceName = dirPath.split('/')[-1]

        sh "sudo mkdir -p ${env.BACKEND_DEPLOY_DIR}"
        sh "sudo mkdir -p ${env.BACKUP_DIR}/${serviceName}"

        // Backup current deployment
        sh """
            if [ -f ${env.BACKEND_DEPLOY_DIR}/${serviceName}.jar ]; then
                sudo cp ${env.BACKEND_DEPLOY_DIR}/${serviceName}.jar ${env.BACKUP_DIR}/${serviceName}/
            fi
        """

        // Deploy with rollback
        try {
            sh "sudo cp ${jarFile} ${env.BACKEND_DEPLOY_DIR}/${serviceName}.jar"
            sh "sudo systemctl restart ${serviceName}"
        } catch (err) {
            echo "⚠ Deployment failed for ${serviceName}, rolling back..."
            sh """
                if ls ${env.BACKUP_DIR}/${serviceName}/*.jar 1> /dev/null 2>&1; then
                    sudo cp \$(ls ${env.BACKUP_DIR}/${serviceName}/*.jar | tail -n 1) ${env.BACKEND_DEPLOY_DIR}/${serviceName}.jar
                    sudo systemctl restart ${serviceName}
                fi
            """
            error "Deployment failed for ${serviceName}, rollback executed."
        }
    }
}

// ================= DEPLOY FRONTEND =================
def deployFrontend(String dirPath) {
    dir(dirPath) {
        sh "sudo mkdir -p ${env.FRONTEND_DEPLOY_DIR}"
        sh "sudo mkdir -p ${env.BACKUP_DIR}/frontend"

        // Backup current frontend
        sh 'cp -r ${FRONTEND_DEPLOY_DIR}/* ${BACKUP_DIR}/frontend/ || true'

        // Deploy with rollback
        try {
            sh "rm -rf ${env.FRONTEND_DEPLOY_DIR}/*"
            sh "cp -r dist/* ${env.FRONTEND_DEPLOY_DIR}/"
            sh "sudo systemctl restart nginx"
        } catch (err) {
            echo "⚠ Frontend deployment failed, rolling back..."
            sh "rm -rf ${env.FRONTEND_DEPLOY_DIR}/*"
            sh 'cp -r ${BACKUP_DIR}/frontend/* ${FRONTEND_DEPLOY_DIR}/'
            sh "sudo systemctl restart nginx"
            error "Frontend deployment failed, rollback executed."
        }
    }
}
