pipeline {
    agent any
    
    environment {
        DOCKER_REGISTRY = 'docker.io'
        IMAGE_NAME = 'ngpro/ng-pro-system'
        DOCKER_CREDS = credentials('docker-hub-credentials')
    }
    
    stages {
        stage('Checkout') {
            steps {
                echo 'Clonando repositório...'
                checkout scm
            }
        }
        
        stage('Build Backend') {
            steps {
                echo 'Compilando Backend Java...'
                dir('backend') {
                    sh 'mvn clean compile -DskipTests'
                }
            }
        }
        
        stage('Tests') {
            steps {
                echo 'Executando testes...'
                dir('backend') {
                    sh 'mvn test'
                }
            }
        }
        
        stage('Security Scan') {
            steps {
                echo 'Executando análise de segurança...'
                dir('backend') {
                    sh 'mvn org.owasp:dependency-check-maven:check || true'
                    sh 'mvn spotbugs:check || true'
                }
            }
        }
        
        stage('Build Docker Images') {
            steps {
                echo 'Buildando imagens Docker...'
                
                // Backend
                sh '''
                    cd backend
                    docker build -t ${IMAGE_NAME}-backend:latest .
                '''
                
                // Frontend
                sh '''
                    cd frontend
                    docker build -t ${IMAGE_NAME}-frontend:latest .
                '''
                
                // WhatsApp Bot
                sh '''
                    cd whatsapp-bot
                    docker build -t ${IMAGE_NAME}-whatsapp:latest .
                '''
                
                // NAS Simulator
                sh '''
                    cd nas-simulator
                    docker build -t ${IMAGE_NAME}-nas:latest .
                '''
            }
        }
        
        stage('Push to Registry') {
            steps {
                echo 'Enviando imagens para registry...'
                withCredentials([usernamePassword(credentialsId: 'docker-hub-credentials', usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
                    sh '''
                        echo $DOCKER_PASS | docker login -u $DOCKER_USER --password-stdin
                        docker push ${IMAGE_NAME}-backend:latest
                        docker push ${IMAGE_NAME}-frontend:latest
                        docker push ${IMAGE_NAME}-whatsapp:latest
                        docker push ${IMAGE_NAME}-nas:latest
                        docker logout
                    '''
                }
            }
        }
        
        stage('Deploy to Server') {
            when {
                branch 'main'
            }
            steps {
                echo 'Fazendo deploy no servidor...'
                withCredentials([ssh(usernameVariable: 'SSH_USER', passwordVariable: 'SSH_PASS')]) {
                    sh '''
                        ssh -o StrictHostKeyChecking=no $SSH_USER@$DEPLOY_HOST << 'EOF'
                            cd /opt/ng-pro-system
                            docker-compose pull
                            docker-compose up -d
                            docker system prune -f
                        EOF
                    '''
                }
            }
        }
    }
    
    post {
        success {
            echo 'Pipeline executado com sucesso!'
            emailext (
                subject: "SUCCESS: Jenkins Pipeline - ${env.JOB_NAME}",
                body: "O pipeline foi executado com sucesso!\n\nBuild: ${env.BUILD_NUMBER}\nBranch: ${env.GIT_BRANCH}",
                to: 'admin@ngpro.com.br'
            )
        }
        failure {
            echo 'Pipeline falhou!'
            emailext (
                subject: "FAILURE: Jenkins Pipeline - ${env.JOB_NAME}",
                body: "O pipeline falhou!\n\nBuild: ${env.BUILD_NUMBER}\nBranch: ${env.GIT_BRANCH}\n\nVerifique os logs em: ${env.BUILD_URL}",
                to: 'admin@ngpro.com.br'
            )
        }
        always {
            cleanWs()
        }
    }
}
