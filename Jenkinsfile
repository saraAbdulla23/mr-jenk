pipeline {
    agent any

    tools {
        maven 'maven-3'
        nodejs 'node-20'
    }

    environment {
        BACKEND_DIR  = "backend"
        FRONTEND_DIR = "front"

        MVN_LOCAL_REPO = "${WORKSPACE}/.m2/repository"
        SPRING_PROFILES_ACTIVE = credentials('SPRING_PROFILES_ACTIVE') // Secure credential in Jenkins

        BACKEND_DEPLOY_DIR  = "${WORKSPACE}/deploy/backend"
        FRONTEND_DEPLOY_DIR = "${WORKSPACE}/deploy/frontend"
        BACKUP_DIR          = "${WORKSPACE}/deploy/backup"

        NOTIFY_EMAIL = "sarakhalaf2312@gmail.com"
        CI = "true"
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

        // ================= BACKEND BUILD & TEST =================
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

        // ================= FRONTEND TEST =================
        stage('Frontend - Test') {
            steps {
                dir("${FRONTEND_DIR}") {
                    script {
                        sh 'npm install'

                        def arch = sh(script: "uname -m", returnStdout: true).trim()
                        if (arch.contains('arm') || arch.contains('aarch64')) {
                            echo "ARM architecture detected. Skipping frontend tests on this agent."
                        } else {
                            sh 'npx ng test --watch=false --browsers=ChromeHeadless --code-coverage'
                            junit 'coverage/test-results/**/*.xml' // Archive frontend test results
                        }
                    }
                }
            }
        }

        // ================= FRONTEND BUILD =================
        stage('Frontend - Build') {
            steps {
                dir("${FRONTEND_DIR}") {
                    sh 'npx ng build --configuration production'
                    archiveArtifacts artifacts: 'dist/**', allowEmptyArchive: false
                }
            }
        }

        // ================= DEPLOY BACKEND =================
        stage('Deploy Backend') {
            steps {
                script {
                    deployBackend("${BACKEND_DIR}")
                }
            }
        }

        // ================= DEPLOY FRONTEND =================
        stage('Deploy Frontend') {
            steps {
                script {
                    deployFrontend("${FRONTEND_DIR}")
                }
            }
        }
    }

    post {
        success {
            mail(
                to: env.NOTIFY_EMAIL,
                subject: '✅ CI/CD Pipeline SUCCESS',
                body: """Build, tests, and deployment completed successfully.

Jenkins URL:
${env.BUILD_URL}
"""
            )
            // Optional Slack notification
            // slackSend(channel: '#devops', message: "✅ Build SUCCESS: ${env.JOB_NAME} #${env.BUILD_NUMBER}")
        }

        failure {
            mail(
                to: env.NOTIFY_EMAIL,
                subject: '❌ CI/CD Pipeline FAILED',
                body: """Pipeline failed at stage: ${env.STAGE_NAME}

Check Jenkins logs:
${env.BUILD_URL}
"""
            )
            // Optional Slack notification
            // slackSend(channel: '#devops', message: "❌ Build FAILED: ${env.JOB_NAME} #${env.BUILD_NUMBER}")
        }

        always {
            cleanWs()
        }
    }
}

// ================= BACKEND BUILD & TEST FUNCTION =================
def buildAndTestBackend(String dirPath) {
    dir(dirPath) {
        sh 'java -version'
        sh 'mvn -version'
        sh "mkdir -p ${env.MVN_LOCAL_REPO}"

        sh """
            mvn clean test package -B \
            -Dmaven.repo.local=${env.MVN_LOCAL_REPO} \
            -Dspring.profiles.active=${env.SPRING_PROFILES_ACTIVE}
        """

        junit '**/target/surefire-reports/*.xml' // Archive backend test results
        archiveArtifacts artifacts: 'target/*.jar', allowEmptyArchive: false
    }
}

// ================= DEPLOY BACKEND FUNCTION =================
def deployBackend(String dirPath) {
    dir(dirPath) {
        def services = findFiles(glob: '**/target/*.jar')
            .collect { it.path.replaceAll('/target/.*', '') }
            .unique()

        services.each { serviceDir ->
            def serviceName = serviceDir.tokenize('/').last()
            def jarFile = sh(
                script: "ls ${serviceDir}/target/*.jar | head -n 1",
                returnStdout: true
            ).trim()

            echo "Deploying backend service: ${serviceName}"

            sh "mkdir -p ${env.BACKEND_DEPLOY_DIR} ${env.BACKUP_DIR}/${serviceName}"

            sh """
                if [ -f ${env.BACKEND_DEPLOY_DIR}/${serviceName}.jar ]; then
                    cp ${env.BACKEND_DEPLOY_DIR}/${serviceName}.jar ${env.BACKUP_DIR}/${serviceName}/
                fi
            """

            try {
                sh "cp ${jarFile} ${env.BACKEND_DEPLOY_DIR}/${serviceName}.jar"
            } catch (e) {
                echo "Rollback for ${serviceName}"
                sh """
                    cp \$(ls ${env.BACKUP_DIR}/${serviceName}/*.jar | tail -n 1) \
                       ${env.BACKEND_DEPLOY_DIR}/${serviceName}.jar
                """
                error("Deployment failed for ${serviceName}. Rollback executed.")
            }
        }
    }
}

// ================= DEPLOY FRONTEND FUNCTION =================
def deployFrontend(String dirPath) {
    dir(dirPath) {
        def distDir = "dist/front"

        if (!fileExists(distDir)) {
            error("Frontend build artifacts not found. Deployment aborted.")
        }

        sh "mkdir -p ${env.FRONTEND_DEPLOY_DIR} ${env.BACKUP_DIR}/frontend"

        try {
            sh """
                cp -r ${env.FRONTEND_DEPLOY_DIR}/* ${env.BACKUP_DIR}/frontend/ || true
                rm -rf ${env.FRONTEND_DEPLOY_DIR}/*
                cp -r ${distDir}/* ${env.FRONTEND_DEPLOY_DIR}/
            """
        } catch (e) {
            echo "Frontend deployment failed. Rolling back..."
            sh """
                rm -rf ${env.FRONTEND_DEPLOY_DIR}/*
                cp -r ${env.BACKUP_DIR}/frontend/* ${env.FRONTEND_DEPLOY_DIR}/
            """
            error("Frontend deployment failed. Rollback executed.")
        }
    }
}
