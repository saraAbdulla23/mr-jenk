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

        EMAIL_RECIPIENT = "sarakhalaf2312@gmail.com"
    }

    options {
        timestamps()
        timeout(time: 90, unit: 'MINUTES')
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
                    sh 'mkdir -p ${NPM_CACHE}'
                    sh 'npm config set cache ${NPM_CACHE} --global'
                    sh 'node -v'
                    sh 'npm -v'
                    sh 'npm install --prefer-offline --no-audit --progress=false'
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
        always {
            cleanWs()
        }
        success {
            mail to: "${EMAIL_RECIPIENT}",
                 subject: '✅ Jenkins Build & Deploy Successful',
                 body: """CI/CD pipeline completed successfully!
Backend + Frontend built and deployed.
Check build logs in Jenkins for details."""
        }
        failure {
            mail to: "${EMAIL_RECIPIENT}",
                 subject: '❌ Jenkins Build/Deploy Failed',
                 body: """Pipeline failed!
Check Jenkins console output and build logs for details.
If deployment failed, rollback may have been executed."""
        }
    }
}

// ================= BACKEND BUILD & TEST =================
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

        sh 'mvn test' // run backend tests
        archiveArtifacts artifacts: 'target/*.jar', allowEmptyArchive: false
    }
}

// ================= DEPLOY BACKEND WITH ROLLBACK =================
def deployBackend(String dirPath) {
    dir(dirPath) {
        def jarFile = sh(script: "ls target/*.jar | head -n 1", returnStdout: true).trim()
        def serviceName = dirPath.split('/')[-1]

        // Ensure directories exist with proper permissions
        sh "sudo mkdir -p ${env.BACKEND_DEPLOY_DIR}"
        sh "sudo mkdir -p ${env.BACKUP_DIR}/${serviceName}"

        // Backup current deployment
        sh """
            if [ -f ${env.BACKEND_DEPLOY_DIR}/${serviceName}.jar ]; then
                sudo cp ${env.BACKEND_DEPLOY_DIR}/${serviceName}.jar ${env.BACKUP_DIR}/${serviceName}/
            fi
        """

        // Deploy new jar
        try {
            sh "sudo cp ${jarFile} ${env.BACKEND_DEPLOY_DIR}/${serviceName}.jar"
            sh "sudo systemctl restart ${serviceName}"
        } catch (err) {
            // Rollback on failure
            echo "⚠ Deployment failed for ${serviceName}, rolling back..."
            sh """
                if [ -f ${env.BACKUP_DIR}/${serviceName}/$(basename ${jarFile}) ]; then
                    sudo cp ${env.BACKUP_DIR}/${serviceName}/$(basename ${jarFile}) ${env.BACKEND_DEPLOY_DIR}/${serviceName}.jar
                    sudo systemctl restart ${serviceName}
                fi
            """
            error "Deployment failed for ${serviceName}, rollback executed."
        }
    }
}

// ================= DEPLOY FRONTEND WITH ROLLBACK =================
def deployFrontend(String dirPath) {
    dir(dirPath) {
        // Ensure directories exist with proper permissions
        sh "sudo mkdir -p ${env.FRONTEND_DEPLOY_DIR}"
        sh "sudo mkdir -p ${env.BACKUP_DIR}/frontend"

        // Backup current deployment
        sh "sudo cp -r ${env.FRONTEND_DEPLOY_DIR}/* ${env.BACKUP_DIR}/frontend/ || true"

        // Deploy new build
        try {
            sh "sudo rm -rf ${env.FRONTEND_DEPLOY_DIR}/*"
            sh "sudo cp -r dist/* ${env.FRONTEND_DEPLOY_DIR}/"
            sh "sudo systemctl restart nginx"
        } catch (err) {
            echo "⚠ Frontend deployment failed, rolling back..."
            sh """
                sudo rm -rf ${env.FRONTEND_DEPLOY_DIR}/*
                sudo cp -r ${env.BACKUP_DIR}/frontend/* ${env.FRONTEND_DEPLOY_DIR}/
                sudo systemctl restart nginx
            """
            error "Frontend deployment failed, rollback executed."
        }
    }
}
