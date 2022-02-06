properties([
    parameters([
        string(description: 'Provide Linux Machine', name: 'IPADDRESS', trim: true)
        ])
    ])
    
node("ansible"){
    stage("Pull repo"){
        git 'https://github.com/emirkoroglu/ansible-melody-app.git'
    }

     withCredentials([sshUserPrivateKey(credentialsId: '81abff63-8354-4563-addd-396d083783f8', keyFileVariable: 'SSHKEY', usernameVariable: 'SSHUSERNAME')]) {
        stage("Install Melodi"){
            sh """
                export ANSIBLE_HOST_KEY_CHECKING=False
                ansible-playbook --private-key $SSHKEY -i '${params.IPADDRESS},' main.yml
            """
        }
    } 
}