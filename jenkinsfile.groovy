pipeline{   // declarative pipeline
    agent any  // master
    stages{
        stage('Git checkout from Github'){   // stage 1
            steps{
                //git branch: 'main', url: 'https://github.com/usertan786/git.git' -----public
                //git branch: 'main', credentialsId: 'github', url: 'https://github.com/usertan786/repogit.git'  //----private
                git branch: 'main', credentialsId: 'ec2', url: 'git@github.com:usertan786/repogit.git' //---ssh 
                sh 'ls'
            }
        }
        stage('Build project '){   //  stage 2
            steps{
                echo "Build with maven"     //task 
                sh "ls -la ${pwd()}"
            }
        }
        stage('Test the code'){
            steps{
                echo "Code testing"
            }
        }
        stage('Deploy the software'){
            steps{
                echo "Software Deployed"
            }
        }
    }
}
