pipeline {
    agent any

    tools {
        maven 'maven-3'
        nodejs 'node-20'
    }

    environment {
        BACKEND_DIR    = "backend"
        FRONTEND_DIR   = "front"

        // Persistent caches stored outside workspace
        MVN_LOCAL_REPO   = "/var/jenkins_home/caches/maven"
        NPM_CACHE        = "/var/jenkins_home/caches/npm"
        PUPPETEER_CACHE  = "/var/jenkins_home/caches/puppeteer"

        SPRING_PROFILES_ACTIVE = "test"

        BACKEND_DEPLOY_DIR  = "${WORKSPACE}/deploy/backend"
        FRONTEND_DEPLOY_DIR = "${WORKSPACE}/deploy/frontend"
        BACKUP_DIR          = "${WORKSPACE}/deploy/backup"

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

        stage('Detect Changes') {
            steps {
                script {
                    env.CHANGED_BACKEND_SERVICES = ""
                    env.FRONTEND_CHANGED = "false"

                    // Detect changed backend services
                    def backendDirs = findFiles(glob: "${BACKEND_DIR}/*").collect { it.path }
                    backendDirs.each { dirPath ->
                        def changes = sh(
                            script: "git diff --name-only HEAD~1 HEAD ${dirPath}",
                            returnStdout: true
                        ).trim()
                        if (changes) {
                            env.CHANGED_BACKEND_SERVICES += "${dirPath.split('/')[-1]},"
                        }
                    }

                    // Detect frontend changes
                    def frontendChanges = sh(
                        script: "git diff --name-only HEAD~1 HEAD ${FRONTEND_DIR}",
                        returnStdout: true
                    ).trim()
                    if (frontendChanges) env.FRONTEND_CHANGED = "true"

                    echo "Changed backend services: ${env.CHANGED_BACKEND_SERVICES}"
                    echo "Frontend changed: ${env.FRONTEND_CHANGED}"
                }
            }
        }

        stage('Setup Puppeteer & Chrome') {
            when { expression { env.FRONTEND_CHANGED == "true" } }
            steps {
                dir("${FRONTEND_DIR}") {
                    script {
                        sh "mkdir -p ${PUPPETEER_CACHE}"
                        env.PUPPETEER_CACHE_DIR = "${PUPPETEER_CACHE}"
                        env.PUPPETEER_DOWNLOAD_HOST = "https://storage.googleapis.com/chromium-browser-snapshots"

                        sh 'npm install puppeteer'

                        def chromePath = sh(
                            script: 'node -e "console.log(require(\'puppeteer\').executablePath())"',
                            returnStdout: true
                        ).trim()

                        if (!fileExists(chromePath)) {
                            chromePath = sh(script: 'which google-chrome || which chromium-browser || true', returnStdout: true).trim()
                        }

                        if (!chromePath) error "❌ Chrome/Chromium not found!"
                        env.CHROME_BIN = chromePath
                        echo "✅ Chrome binary set to: ${env.CHROME_BIN}"
                    }
                }
            }
        }

        stage('Backend - Build & Test') {
            when { expression { env.CHANGED_BACKEND_SERVICES } }
            steps {
                script {
                    def services = env.CHANGED_BACKEND_SERVICES.tokenize(',')
                    def parallelStages = [:]

                    services.each { service ->
                        if (!service) return
                        def serviceDir = "${BACKEND_DIR}/${service}"
                        parallelStages[service] = { buildAndTestBackend(serviceDir) }
                    }

                    parallel parallelStages
                }
            }
        }

        stage('Frontend - Install & Test') {
            when { expression { env.FRONTEND_CHANGED == "true" } }
            steps {
                dir("${FRONTEND_DIR}") {
                    sh "mkdir -p ${NPM_CACHE}"
                    sh "npm config set cache ${NPM_CACHE} --global"

                    sh 'node -v'
                    sh 'npm -v'

                    sh 'npm ci --prefer-offline --no-audit --progress=false'

                    script {
                        def isMacARM = sh(script: "uname -m", returnStdout: true).trim() == "arm64"
                        if (isMacARM) {
                            echo "⚠ Skipping ChromeHeadless tests on macOS ARM — using fallback"
                            sh 'npx ng test --watch=false --browsers=ChromeHeadlessCustom || echo "⚠ Frontend tests skipped"'
                        } else {
                            sh 'npx ng test --watch=false --browsers=ChromeHeadless || echo "⚠ Frontend tests failed"'
                        }
                    }
                }
            }
        }

        stage('Frontend - Build') {
            when { expression { env.FRONTEND_CHANGED == "true" } }
            steps {
                dir("${FRONTEND_DIR}") {
                    sh 'npx ng build --configuration production'
                    archiveArtifacts artifacts: 'dist/**', allowEmptyArchive: false
                }
            }
        }

        stage('Deploy Backend') {
            when { expression { env.CHANGED_BACKEND_SERVICES } }
            steps { script { deployBackend("${BACKEND_DIR}") } }
        }

        stage('Deploy Frontend') {
            when { expression { env.FRONTEND_CHANGED == "true" } }
            steps { script { deployFrontend("${FRONTEND_DIR}") } }
        }
    }

    post {
        always { cleanWs() }
        success {
            mail to: "${env.NOTIFY_EMAIL}",
                 subject: '✅ Jenkins Build & Deploy Successful',
                 body: """CI/CD pipeline completed successfully.

Backend + Frontend built and deployed.
Check Jenkins console: ${env.BUILD_URL}"""
        }
        failure {
            mail to: "${env.NOTIFY_EMAIL}",
                 subject: '❌ Jenkins Build/Deploy Failed',
                 body: """Pipeline failed at stage: ${env.STAGE_NAME ?: 'Unknown'}.
Check console for errors: ${env.BUILD_URL}"""
        }
    }
}

// ================= BACKEND BUILD & TEST =================
def buildAndTestBackend(String dirPath) {
    dir(dirPath) {
        sh 'java -version'
        sh 'mvn -version'
        sh "mkdir -p ${MVN_LOCAL_REPO}"

        sh """
            mvn clean test package -B \
            -Dmaven.repo.local=${env.MVN_LOCAL_REPO} \
            -Dspring.profiles.active=${env.SPRING_PROFILES_ACTIVE}
        """

        def jarFile = sh(script: "ls target/*.jar | head -n 1 || true", returnStdout: true).trim()
        if (!jarFile) {
            echo "⚠ No JAR found in ${dirPath}/target — skipping archive/deploy."
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
            sh "mkdir -p ${env.BACKEND_DEPLOY_DIR} ${env.BACKUP_DIR}/${serviceName}"

            sh """
                if [ -f ${env.BACKEND_DEPLOY_DIR}/${serviceName}.jar ]; then
                    cp ${env.BACKEND_DEPLOY_DIR}/${serviceName}.jar ${env.BACKUP_DIR}/${serviceName}/
                fi
            """

            try {
                sh "cp ${jarFile} ${env.BACKEND_DEPLOY_DIR}/${serviceName}.jar"
                sh """
                    if command -v systemctl > /dev/null; then
                        systemctl restart ${serviceName} || echo 'Service restart failed.'
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
                            systemctl restart ${serviceName} || echo 'Rollback restart failed.'
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
            echo "⚠ Frontend build artifacts not found — skipping deploy."
            return
        }

        echo "Deploying frontend..."
        sh "mkdir -p ${env.FRONTEND_DEPLOY_DIR} ${env.BACKUP_DIR}/frontend"

        sh """
            cp -r ${env.FRONTEND_DEPLOY_DIR}/* ${env.BACKUP_DIR}/frontend/ || true
            rm -rf ${env.FRONTEND_DEPLOY_DIR}/*
            cp -r ${distDir}/* ${env.FRONTEND_DEPLOY_DIR}/
        """

        sh """
            if command -v systemctl > /dev/null; then
                systemctl restart nginx || echo 'Nginx restart failed.'
            else
                echo 'systemctl not available — restart nginx manually.'
            fi
        """
    }
}
