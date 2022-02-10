properties([
   parameters([
      choice(choices: ['dev', 'qa', 'prod'], description: 'Choose Environment', name: 'Environment'),
      choice(choices: ['plan', 'apply', 'destroy'], description: 'Choose Terraform Action', name: 'terraformaction'),
   ])
])

if( params.environment == "dev"){
   aws_region_var = "us-east-1"
}
else if ( params.environment == "qa" ) {
   aws_region_var = "us-east-2"
}
else if ( params.environment == prod) {
   aws_region_var = "us-west-2"
}
else {
   error 'Parameter was not set'
}

def tfvars = """
s3_bucket = \"jenkins-terraform-evolvecybertraining\"


environment = \"{params.environment}\"
"""



node('terraform'){
    stage("Pull Code"){
        timestamps ([
            checkout([$class: 'GitSCM', branches: [[name: '*/master']], extensions: [], userRemoteConfigs: [[url: 'https://github.com/ikambarov/terraform-vpc.git']]
         ])
      }

    withCredentials([usernamePassword(credentialsId: 'aws_key', passwordVariable: 'AWS_SECRET_ACCESS_KEY', usernameVariable: 'AWS_ACCESS_KEY_ID')]) {
         withEnv(["AWS_REGION=${aws_region_var}"]) {
            stage("Terraform Init"){
              sh """
                 #!/bin/bash
                 source ./setenv.sh ${params.environment}.tfvars

                 terraform init 
               """
            }
         
         if ( params.terraformaction == "plan"){
            stage("Terraform Plan"){
              sh "terraform plan -var-file ${params.environment}.tf.vars"
         }

         if ( params.terraformaction == "apply"){
            stage("Terraform Apply"){
              sh "terraform apply -var-file ${params.environment}.tf.vars -auto-approve"
         }

         if ( params.terraformaction == "destroy"){
            stage("Terraform Destroy"){
              sh "terraform destroy -var-file ${params.environment}.tf.vars -auto-approve"
            }
         }
      }
   }
}