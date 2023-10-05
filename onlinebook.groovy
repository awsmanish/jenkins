pipeline{   // declarative pipeline
    agent {
        label 'worker-node'
    }
    stages{
        stage('Git checkout from Github'){   // stage 1
            steps{
                git branch: 'master', credentialsId: 'ec2', url: 'git@github.com:usertan786/onlinebookstore.git' 
                sh 'ls'
            }
        }
        stage('Build project '){   //  stage 2
            steps{
                echo "Build with maven" 
                sh "ls -l"   
                sh "sudo apt install maven -y"
                sh "sudo mvn clean package"
                sh "ls -l target" 

                
            }
        }
        stage('push artifact to s3 bucket'){
            steps{
                withAWS(credentials: 'new-creds', region: 'us-east-1') {
                sh '''  
                sudo apt install awscli -y 
                aws s3 ls 
                sudo mv /home/ubuntu/workspace/onlinebook/target/onlinebookstore.war /home/ubuntu/workspace/onlinebook/target/bookstore-${BUILD_ID}.war
                aws s3 cp /home/ubuntu/workspace/onlinebook/target/bookstore-${BUILD_ID}.war s3://student-new-jenkins
                '''
                }
            }
        }
        
        // stage('deploy the artifact'){   
        //     steps{
        //         withCredentials([sshUserPrivateKey(credentialsId: 'amazon', keyFileVariable: 'ec2')]) {
        //            sh '''  
        //            ssh -o StrictHostKeyChecking=no -i $ec2  ec2-user@54.204.211.0<<EOF
        //            sudo yum update -y
        //            sudo yum install java-openjdk -y
        //            curl -O https://dlcdn.apache.org/tomcat/tomcat-9/v9.0.80/bin/apache-tomcat-9.0.80.tar.gz 
        //            sudo tar -xvf apache-tomcat-9.0.80.tar.gz -C /opt/
        //            sudo mv /opt/apache-tomcat-9.0.80 /opt/tomcat
        //            sudo aws s3 cp s3://student-new-jenkins/student-31.war .
        //            sudo mv student-${BUILD_ID}.war student.war
        //            ls
        //            sudo mv student.war /opt/tomcat/webapps/
        //            cd /opt/tomcat 
        //            pwd
        //            ls -l
        //            sudo ./bin/catalina.sh start

                   
        //            '''
        //         }
                
        //     }
        // }
        stage('deploy the artifact'){   
            steps{
               withCredentials([sshUserPrivateKey(credentialsId: 'amazon', keyFileVariable: 'ec2', usernameVariable: 'ec2-user'), usernamePassword(credentialsId: 'creds-new', passwordVariable: 'AWS_SECRET_KEY', usernameVariable: 'AWS_ACCESS_KEY')]) {
                   sh '''  
                   ssh -o StrictHostKeyChecking=no -i $ec2  ec2-user@54.166.174.172<<EOF
                   sudo yum update -y
                   sudo yum install java-openjdk -y
                   curl -O https://dlcdn.apache.org/tomcat/tomcat-9/v9.0.80/bin/apache-tomcat-9.0.80.tar.gz 
                   sudo tar -xvf apache-tomcat-9.0.80.tar.gz -C /opt/
                   sudo mv /opt/apache-tomcat-9.0.80 /opt/tomcat
                   aws configure set aws_access_key_id ${AWS_ACCESS_KEY} 
                   aws configure set aws_secret_access_key ${AWS_SECRET_KEY}
                   aws s3 ls 
                   aws s3 cp s3://student-new-jenkins/bookstore-${BUILD_ID}.war .
                   sudo mv bookstore-${BUILD_ID}.war bookstore.war
                   ls
                   sudo mv bookstore.war /opt/tomcat/webapps/
                   cd /opt/tomcat 
                   pwd
                   ls -l
                   sudo ./bin/catalina.sh start

                   
                   '''
                }
                
            }
        }
        
    }
}