pipeline{   // declarative pipeline
     agent {
        label 'worker-node'
    }
    stages{
        stage('Git checkout from Github'){   // stage 1
            steps{
                git credentialsId: '4db30dcd-39cf-4f58-a1a6-44daf74ad2b5', url: 'git@github.com:awsmanish/student-ui.git' 
                sh 'ls'
            }
        }
        stage('Build project '){   //  stage 2
            steps{
                echo "Build with maven" 
                sh "ls -l"
                sh "sudo apt update -y"   
                sh "sudo apt install maven -y"
                sh "sudo mvn clean package"
                sh "ls -l target" 

                    
            }
        }
        stage('push artifact to s3 bucket'){
            steps{
                withAWS(credentials: 'news3', region: 'ap-southeast-2') {
                sh '''  
                sudo apt install awscli -y 
                aws s3 ls 
                sudo mv /home/ubuntu/workspace/f2-new/target/studentapp-2.2-SNAPSHOT.war /home/ubuntu/workspace/f2-new/target/student-${BUILD_ID}.war
                aws s3 cp /home/ubuntu/workspace/f2-new/target/student-${BUILD_ID}.war s3://student-war-file/
                ls -a
                '''
                }
            }
        }
        
        stage('deploy the artifact'){   
            steps{
                withCredentials([sshUserPrivateKey(credentialsId: 'worker-node', keyFileVariable: 'ubuntu_user', usernameVariable: 'ubuntu'), usernamePassword(credentialsId: 'acess_or_ser', passwordVariable: 'AWS_SECRET_KEY', usernameVariable: 'AWS_ACCESS_KEY')]) { // ec2-user@54.152.138.33-----web servicw wali ip dalna
                   sh '''  
                   ssh -o StrictHostKeyChecking=no -i $ubuntu_user  ubuntu@3.27.59.201<<EOF
                   sudo apt update -y
                   sudo apt install java-openjdk -y
                   curl -O https://dlcdn.apache.org/tomcat/tomcat-9/v9.0.80/bin/apache-tomcat-9.0.80.tar.gz 
                   sudo tar -xvf apache-tomcat-9.0.80.tar.gz -C /opt/
                   sudo mv /opt/apache-tomcat-9.0.80 /opt/tomcat
                   aws configure set aws_access_key_id ${AWS_ACCESS_KEY} 
                   aws configure set aws_secret_access_key ${AWS_SECRET_KEY}
                   aws s3 ls 
                   aws s3 cp s3://student-war-file/student-${BUILD_ID}.war .
                   sudo mv student-${BUILD_ID}.war student.war
                   ls
                   sudo mv student.war /opt/tomcat/webapps/
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
