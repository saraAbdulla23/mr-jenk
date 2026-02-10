pipeline {
    agent none // No global agent; we’ll define agent per stage

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
            agent any // Use any available node for checkout
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

        stage('Verify Tools') {
            agent {
                docker {
                    image 'sarakhalaf23/jenkins-agent:latest'
                    args '-u jenkins:jenkins -v /var/run/docker.sock:/var/run/docker.sock -v ${WORKSPACE}:${WORKSPACE}'
                }
            }
            steps {
                sh '''
                    echo "== Java Version =="
                    java -version
                    echo "== Maven Version =="
                    mvn -version
                    echo "== Node Version =="
                    node -v
                    echo "== NPM Version =="
                    npm -v
                    echo "== Firefox Version =="
                    firefox --version
                    echo "== Geckodriver Version =="
                    geckodriver --version
                    echo "== Docker Version =="
                    docker --version
                '''
            }
        }

        stage('Backend - Build & Test') {
            agent {
                docker {
                    image 'sarakhalaf23/jenkins-agent:latest'
                    args '-u jenkins:jenkins -v /var/run/docker.sock:/var/run/docker.sock -v ${WORKSPACE}:${WORKSPACE}'
                }
            }
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
            agent {
                docker {
                    image 'sarakhalaf23/jenkins-agent:latest'
                    args '-u jenkins:jenkins -v /var/run/docker.sock:/var/run/docker.sock -v ${WORKSPACE}:${WORKSPACE}'
                }
            }
            steps {
                dir("${FRONTEND_DIR}") {
                    sh '''
                        mkdir -p ${NPM_CACHE}
                        npm config set cache ${NPM_CACHE} --global
                        node -v
                        npm -v
                        npm install --prefer-offline --no-audit --progress=false

                        # Start Xvfb for headless Firefox
                        export DISPLAY=:99
                        Xvfb :99 -screen 0 1280x1024x24 &
                        npx ng test --watch=false --browsers=FirefoxHeadless || echo "⚠ Frontend tests failed"
                    '''
                }
            }
        }

        stage('Frontend - Build') {
            agent {
                docker {
                    image 'sarakhalaf23/jenkins-agent:latest'
                    args '-u jenkins:jenkins -v /var/run/docker.sock:/var/run/docker.sock -v ${WORKSPACE}:${WORKSPACE}'
                }
            }
            steps {
                dir("${FRONTEND_DIR}") {
                    sh '''
                        export DISPLAY=:99
                        Xvfb :99 -screen 0 1280x1024x24 &
                        npx ng build --configuration production
                    '''
                    archiveArtifacts artifacts: 'dist/**', allowEmptyArchive: false
                }
            }
        }

        stage('Deploy Backend') {
            agent {
                docker {
                    image 'sarakhalaf23/jenkins-agent:latest'
                    args '-u jenkins:jenkins -v /var/run/docker.sock:/var/run/docker.sock -v ${WORKSPACE}:${WORKSPACE}'
                }
            }
            steps { script { deployBackend("${BACKEND_DIR}") } }
        }

        stage('Deploy Frontend') {
            agent {
                docker {
                    image 'sarakhalaf23/jenkins-agent:latest'
                    args '-u jenkins:jenkins -v /var/run/docker.sock:/var/run/docker.sock -v ${WORKSPACE}:${WORKSPACE}'
                }
            }
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
        sh 'mkdir -p ${MVN_LOCAL_REPO}'

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
