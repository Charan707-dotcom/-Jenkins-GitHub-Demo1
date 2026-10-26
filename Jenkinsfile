
pipeline {
    agent any

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build') {
            steps {
                bat 'mvn -B -DskipTests compile'
            }
        }

        stage('Test') {
            steps {
                bat 'mvn -B test'
            }
            post {
                always {
                    junit 'target/surefire-reports/*.xml'
                }
            }
        }

        stage('Package') {
            steps {
                bat 'mvn -B -DskipTests package'
                archiveArtifacts artifacts: 'target/*.jar',
                                 fingerprint: true
            }
        }

        stage('Docker Build') {
            steps {
                bat '"C:\\Users\\Reliance Digital\\AppData\\Local\\Programs\\DockerDesktop\\resources\\bin\\docker.exe" build -t banking-system:lab5 .'
            }
        }

        stage('Docker Deployment') {
            steps {
                bat '''
    "C:\\Users\\Reliance Digital\\AppData\\Local\\Programs\\DockerDesktop\\resources\\bin\\docker.exe" rm -f banking-system-lab5
    if errorlevel 1 exit /b 1
    "C:\\Users\\Reliance Digital\\AppData\\Local\\Programs\\DockerDesktop\\resources\\bin\\docker.exe" run -d --name banking-system-lab5 -p 8081:8080 -e "SPRING_DATASOURCE_URL=jdbc:mysql://host.docker.internal:3306/bankingsystem?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Kolkata" banking-system:lab5
'''
            }
        }
    }
}
