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

    triggers {
        pollSCM('H/2 * * * *')
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

        stage('Set Chrome/Chromium Binary') {
            steps {
                script {
                    // Detect Chrome or Chromium
                    def chromePath = sh(script: 'which google-chrome || which chromium-browser || true', returnStdout: true).trim()

                    if (!chromePath) {
                        echo "⚠ Chrome/Chromium not found. Installing local Chromium..."

                        // Prepare directory
                        sh "mkdir -p ${WORKSPACE}/chromium"

                        // Download latest stable Chromium (headless)
                        sh '''
                            CHROMIUM_URL=$(curl -s https://omahaproxy.appspot.com/linux | head -n1 | grep -v 404)
                            if [ -z "$CHROMIUM_URL" ]; then
                                CHROMIUM_URL="https://commondatastorage.googleapis.com/chromium-browser-snapshots/Linux_x64/1204567/chrome-linux.zip"
                            fi
                            echo "Downloading Chromium from $CHROMIUM_URL..."
                            curl -L -o ${WORKSPACE}/chromium/chrome.zip $CHROMIUM_URL
                            unzip -q ${WORKSPACE}/chromium/chrome.zip -d ${WORKSPACE}/chromium
                            mv ${WORKSPACE}/chromium/chrome-linux/chrome ${WORKSPACE}/chromium/chrome
                            chmod +x ${WORKSPACE}/chromium/chrome
                        '''

                        chromePath = "${WORKSPACE}/chromium/chrome"
                    }

                    if (!chromePath || !fileExists(chromePath)) {
                        error "❌ Chrome or Chromium installation failed. Cannot run frontend tests."
                    }

                    env.CHROME_BIN = chromePath
                    echo "✅ Chrome binary set to: ${env.CHROME_BIN}"
                }
            }
        }

        stage('Backend - Build & Test') {
            steps {
                script {
                    parallel(
                        "Discovery Service": { buildAndTestBackend("${BACKEND_DIR}/discovery-service") },
                        "API Gateway":       { buildAndTestBackend("${BACKEND_DIR}/api-gateway") },
                        "User Service":      { buildAndTestBackend("${BACKEND_DIR}/user-service") },
                        "Product Service":   { buildAndTestBackend("${BACKEND_DIR}/product-service") },
                        "Media Service":     { buildAndTestBackend("${BACKEND_DIR}/media-service") }
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
                    sh 'npm run test -- --watch=false --browsers=ChromeHeadless'
                }
            }
        }

        stage('Frontend - Build') {
            steps {
                dir("${FRONTEND_DIR}") {
                    sh 'npx ng build --configuration production'
                    archiveArtifacts artifacts: 'dist/**', allowEmptyArchive: false
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

Backend + Frontend built and deployed.
Check Jenkins console for details: ${env.BUILD_URL}"""
        }
        failure {
            mail to: "${env.NOTIFY_EMAIL}",
                 subject: '❌ Jenkins Build/Deploy Failed',
                 body: """Pipeline failed at stage: ${env.STAGE_NAME ?: 'Unknown'}.

Check Jenkins console for errors and rollback status: ${env.BUILD_URL}"""
        }
    }
}

// ================= BACKEND BUILD & TEST =================
def buildAndTestBackend(String dirPath) {
    dir(dirPath) {
        sh 'java -version'
        sh 'mvn -version'
        sh 'mkdir -p ${MVN_LOCAL_REPO}'

        sh """
            mvn clean test package -B \
            -Dmaven.repo.local=${env.MVN_LOCAL_REPO} \
            -Dspring.profiles.active=${env.SPRING_PROFILES_ACTIVE}
        """

        def jarFile = sh(script: "ls target/*.jar | head -n 1 || true", returnStdout: true).trim()
        if (!jarFile) {
            echo "⚠ No JAR found in ${dirPath}/target — skipping archive and deploy for this service."
            return
        }

        archiveArtifacts artifacts: 'target/*.jar', allowEmptyArchive: false
    }
}

// ================= DEPLOY BACKEND =================
def deployBackend(String dirPath) {
    dir(dirPath) {
        def services = findFiles(glob: '**/target/*.jar').collect { it.path.replaceAll('/target/.*', '') }.unique()

        services.each { serviceDir ->
            def jarFile = sh(script: "ls ${serviceDir}/target/*.jar | head -n 1 || true", returnStdout: true).trim()
            def serviceName = serviceDir.split('/')[-1]

            if (!jarFile) {
                echo "⚠ Skipping deployment for ${serviceName}: no JAR found."
                return
            }

            echo "Deploying backend service: ${serviceName}"

            sh "mkdir -p ${env.BACKEND_DEPLOY_DIR}"
            sh "mkdir -p ${env.BACKUP_DIR}/${serviceName}"

            // Backup current deployment
            sh """
                if [ -f ${env.BACKEND_DEPLOY_DIR}/${serviceName}.jar ]; then
                    cp ${env.BACKEND_DEPLOY_DIR}/${serviceName}.jar ${env.BACKUP_DIR}/${serviceName}/
                fi
            """

            // Deploy with rollback
            try {
                sh "cp ${jarFile} ${env.BACKEND_DEPLOY_DIR}/${serviceName}.jar"
                sh """
                    if command -v systemctl > /dev/null; then
                        systemctl restart ${serviceName} || echo 'Service restart failed, check manually.'
                    else
                        echo 'systemctl not available — restart manually.'
                    fi
                """
            } catch (err) {
                echo "⚠ Deployment failed for ${serviceName}, rolling back..."
                sh """
                    if ls ${env.BACKUP_DIR}/${serviceName}/*.jar 1> /dev/null 2>&1; then
                        cp \$(ls ${env.BACKUP_DIR}/${serviceName}/*.jar | tail -n 1) ${env.BACKEND_DEPLOY_DIR}/${serviceName}.jar
                        if command -v systemctl > /dev/null; then
                            systemctl restart ${serviceName} || echo 'Rollback service restart failed.'
                        fi
                    fi
                """
                echo "❌ Deployment failed for ${serviceName}, rollback executed."
            }
        }
    }
}

// ================= DEPLOY FRONTEND =================
def deployFrontend(String dirPath) {
    dir(dirPath) {
        def distDir = "${dirPath}/dist/front"
        if (!fileExists(distDir)) {
            echo "⚠ Frontend build artifacts not found — skipping frontend deploy."
            return
        }

        echo "Deploying frontend..."

        sh "mkdir -p ${env.FRONTEND_DEPLOY_DIR}"
        sh "mkdir -p ${env.BACKUP_DIR}/frontend"

        // Backup current frontend
        sh """
            cp -r ${env.FRONTEND_DEPLOY_DIR}/* ${env.BACKUP_DIR}/frontend/ || true
            rm -rf ${env.FRONTEND_DEPLOY_DIR}/*
            cp -r ${distDir}/* ${env.FRONTEND_DEPLOY_DIR}/
        """

        // Restart web server if systemctl exists
        sh """
            if command -v systemctl > /dev/null; then
                systemctl restart nginx || echo 'Nginx restart failed, check manually.'
            else
                echo 'systemctl not available — restart nginx manually.'
            fi
        """
    }
}
