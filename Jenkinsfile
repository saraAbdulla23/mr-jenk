pipeline {
    agent any
    environment {
        BACKEND_DIR = "backend"
        FRONTEND_DIR = "front"
        BACKEND_DEPLOY_DIR = "/opt/ecommerce/backend"
        FRONTEND_DEPLOY_DIR = "/opt/ecommerce/frontend"
        BACKUP_DIR = "/opt/ecommerce/backup"
        NODEJS_HOME = tool name: 'NodeJS', type: 'NodeJS'
        JAVA_HOME = tool name: 'JDK-20', type: 'jdk'
    }
    options {
        timestamps()
        timeout(time: 60, unit: 'MINUTES')
    }
    stages {
        stage('Build Backend') {
            parallel {
                stage('Build Discovery Service') {
                    steps {
                        dir("${BACKEND_DIR}/discovery-service") {
                            sh 'mvn clean package -DskipTests'
                            archiveArtifacts 'target/*.jar'
                        }
                    }
                }
                stage('Build API Gateway') {
                    steps {
                        dir("${BACKEND_DIR}/api-gateway") {
                            sh 'mvn clean package -DskipTests'
                            archiveArtifacts 'target/*.jar'
                        }
                    }
                }
                stage('Build User Service') {
                    steps {
                        dir("${BACKEND_DIR}/user-service") {
                            sh 'mvn clean package -DskipTests'
                            archiveArtifacts 'target/*.jar'
                        }
                    }
                }
                stage('Build Product Service') {
                    steps {
                        dir("${BACKEND_DIR}/product-service") {
                            sh 'mvn clean package -DskipTests'
                            archiveArtifacts 'target/*.jar'
                        }
                    }
                }
                stage('Build Media Service') {
                    steps {
                        dir("${BACKEND_DIR}/media-service") {
                            sh 'mvn clean package -DskipTests'
                            archiveArtifacts 'target/*.jar'
                        }
                    }
                }
            }
        }

        stage('Frontend - Install') {
            steps {
                dir("${FRONTEND_DIR}") {
                    withEnv(["PATH+NODE=${NODEJS_HOME}/bin"]) {
                        sh 'mkdir -p .npm'
                        sh 'npm config set cache .npm --global'
                        sh 'npm install --prefer-offline --no-audit --progress=false'
                    }
                }
            }
        }

        stage('Frontend - Build') {
            steps {
                dir("${FRONTEND_DIR}") {
                    withEnv(["PATH+NODE=${NODEJS_HOME}/bin"]) {
                        sh 'npx ng build --configuration production'
                        archiveArtifacts 'dist/front/**'
                    }
                }
            }
        }

        stage('Deploy Backend') {
            steps {
                script {
                    def services = ["discovery-service","api-gateway","user-service","product-service","media-service"]
                    services.each { service ->
                        dir("${BACKEND_DIR}/${service}") {
                            def jarFile = sh(script: "ls target/*.jar | head -n 1 || true", returnStdout: true).trim()
                            if (!jarFile) {
                                error "❌ No JAR found in ${service}/target — cannot deploy."
                            }

                            sh "mkdir -p ${BACKEND_DEPLOY_DIR}/${service}"
                            sh "mkdir -p ${BACKUP_DIR}/${service}"

                            // Backup current deployment
                            sh """
                                if [ -f ${BACKEND_DEPLOY_DIR}/${service}/${service}.jar ]; then
                                    cp ${BACKEND_DEPLOY_DIR}/${service}/${service}.jar ${BACKUP_DIR}/${service}/
                                fi
                            """

                            // Deploy with rollback
                            try {
                                sh "cp ${jarFile} ${BACKEND_DEPLOY_DIR}/${service}/${service}.jar"
                                sh "systemctl restart ${service} || echo 'Service restart failed, check manually.'"
                            } catch (err) {
                                echo "⚠ Deployment failed for ${service}, rolling back..."
                                sh """
                                    if ls ${BACKUP_DIR}/${service}/*.jar 1> /dev/null 2>&1; then
                                        cp \$(ls ${BACKUP_DIR}/${service}/*.jar | tail -n 1) ${BACKEND_DEPLOY_DIR}/${service}/${service}.jar
                                        systemctl restart ${service} || echo 'Rollback service restart failed.'
                                    fi
                                """
                                error "Deployment failed for ${service}, rollback executed."
                            }
                        }
                    }
                }
            }
        }

        stage('Deploy Frontend') {
            steps {
                dir("${FRONTEND_DIR}") {
                    sh """
                        mkdir -p ${FRONTEND_DEPLOY_DIR}/front
                        cp -r dist/front/* ${FRONTEND_DEPLOY_DIR}/front/
                    """
                }
            }
        }
    }

    post {
        always {
            cleanWs()
        }
        failure {
            mail to: 'dev-team@example.com',
                 subject: "Build Failed: ${currentBuild.fullDisplayName}",
                 body: "The Jenkins build failed. Check the console output: ${env.BUILD_URL}"
        }
        success {
            mail to: 'dev-team@example.com',
                 subject: "Build Success: ${currentBuild.fullDisplayName}",
                 body: "The Jenkins build succeeded! Check the deployment."
        }
    }
}
