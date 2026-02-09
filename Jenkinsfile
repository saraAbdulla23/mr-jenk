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

        BACKEND_DEPLOY_DIR = "${WORKSPACE}/deploy/backend"
        FRONTEND_DEPLOY_DIR = "${WORKSPACE}/deploy/frontend"
        BACKUP_DIR = "${WORKSPACE}/deploy/backup"

        NPM_CACHE = "${WORKSPACE}/.npm"
        CI = "true"

        NOTIFY_EMAIL = "sarakhalaf2312@gmail.com"
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
            mail to: "${env.NOTIFY_EMAIL}",
                 subject: '✅ Jenkins Build & Deploy Successful',
                 body: """CI/CD pipeline completed successfully.

Backend services built and deployed:
- Discovery Service
- API Gateway
- User Service
- Product Service
- Media Service

Frontend built and deployed successfully.

Check Jenkins console for detailed logs: ${env.BUILD_URL}"""
        }
        failure {
            script {
                def failedStage = currentBuild.rawBuild.getLog(1000).find { it =~ /❌/ } ?: "Unknown stage"
                mail to: "${env.NOTIFY_EMAIL}",
                     subject: '❌ Jenkins Build/Deploy Failed',
                     body: """Pipeline failed.

Failed Stage / Service: ${failedStage}

Check Jenkins console for detailed errors and rollback status: ${env.BUILD_URL}"""
            }
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

        def jarFile = sh(script: "ls target/*.jar | head -n 1 || true", returnStdout: true).trim()
        if (!jarFile) {
            error "❌ No JAR found in ${dirPath}/target — check Maven build."
        }

        archiveArtifacts artifacts: 'target/*.jar', allowEmptyArchive: false
    }
}

// ================= DEPLOY BACKEND =================
def deployBackend(String dirPath) {
    dir(dirPath) {
        def services = ["discovery-service","api-gateway","user-service","product-service","media-service"]
        services.each { serviceName ->
            dir("${dirPath}/${serviceName}") {
                def jarFile = sh(script: "ls target/*.jar | head -n 1 || true", returnStdout: true).trim()
                if (!jarFile) {
                    error "❌ No JAR found in ${serviceName}/target — cannot deploy."
                }

                sh "mkdir -p ${env.BACKEND_DEPLOY_DIR}/${serviceName}"
                sh "mkdir -p ${env.BACKUP_DIR}/${serviceName}"

                // Backup current deployment
                sh """
                    if [ -f ${env.BACKEND_DEPLOY_DIR}/${serviceName}/${serviceName}.jar ]; then
                        cp ${env.BACKEND_DEPLOY_DIR}/${serviceName}/${serviceName}.jar ${env.BACKUP_DIR}/${serviceName}/
                    fi
                """

                // Deploy with rollback
                try {
                    sh "cp ${jarFile} ${env.BACKEND_DEPLOY_DIR}/${serviceName}/${serviceName}.jar"
                    sh "systemctl restart ${serviceName} || echo 'Service restart failed, check manually.'"
                } catch (err) {
                    echo "⚠ Deployment failed for ${serviceName}, rolling back..."
                    sh """
                        if ls ${env.BACKUP_DIR}/${serviceName}/*.jar 1> /dev/null 2>&1; then
                            cp \$(ls ${env.BACKUP_DIR}/${serviceName}/*.jar | tail -n 1) ${env.BACKEND_DEPLOY_DIR}/${serviceName}/${serviceName}.jar
                            systemctl restart ${serviceName} || echo 'Rollback service restart failed.'
                        fi
                    """
                    error "Deployment failed for ${serviceName}, rollback executed."
                }
            }
        }
    }
}

// ================= DEPLOY FRONTEND =================
def deployFrontend(String dirPath) {
    dir(dirPath) {
        sh "mkdir -p ${env.FRONTEND_DEPLOY_DIR}"
        sh "mkdir -p ${env.BACKUP_DIR}/frontend"

        // Backup current frontend
        sh """
            cp -r ${env.FRONTEND_DEPLOY_DIR}/* ${env.BACKUP_DIR}/frontend/ || true
            rm -rf ${env.FRONTEND_DEPLOY_DIR}/*
            cp -r dist/* ${env.FRONTEND_DEPLOY_DIR}/
        """

        // Restart web server
        sh "systemctl restart nginx || echo 'Nginx restart failed, check manually.'"
    }
}
