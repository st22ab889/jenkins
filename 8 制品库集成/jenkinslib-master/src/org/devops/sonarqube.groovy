package org.devops



//scan
def SonarSacn(projectName,projectDesc,projectPath){
    def sonarServer = "http://192.168.1.200:9000"
    def sonarDate = sh returnStdout: true, script: 'date + %Y%m%d%H%M%S'
    sonarDate = sonarDate - "\n"

    def scannerHome = "/usr/local/sonar-scanner-3.2.0.1227-linux"

    sh  """
        ${scannerHome}/bin/sonar-scanner  -Dsonar.host.url=${sonarServer}  \
        -Dsonar.projectKey=${projectName}  \
        -Dsonar.projectName=${projectName}  \
        -Dsonar.projectVersion=${sonarDate} \
        -Dsonar.login=admin \
        -Dsonar.password=admin \
        -Dsonar.ws.timeout=30 \
        -Dsonar.projectDescription=${projectDesc}  \
        -Dsonar.links.homepage=http://www.baidu.com \
        -Dsonar.sources=${projectPath} \
        -Dsonar.sourceEncoding=UTF-8 \
        -Dsonar.java.binaries=target/classes \
        -Dsonar.java.test.binaries=target/test-classes \
        -Dsonar.java.surefire.report=target/surefire-reports
        """
}


//scan
def SonarSacnForJenkinsSonarPlugin(sonarServer,projectName,projectDesc,projectPath,runOpts='',projectId='',commitSha='',branchName=''){
    // "sonarqube-test"和"sonarqube-prod" 是Jenkins 安装 SonarQube Scanner 插件后, 在"Jenkins --> 系统配置 --> SonarQube installations --> Name"的值
    def servers=["test":"sonarqube-test", "prod":"sonarqube-prod"]
    
    withSonarQubeEnv("${servers[sonarServer]}"){
        def sonarDate = sh returnStdout: true, script: 'date + %Y%m%d%H%M%S'
        sonarDate = sonarDate - "\n"
        
        def scannerHome = "/usr/local/sonar-scanner-3.2.0.1227-linux"

        if(runOpts == "GitlabPush"){
            sh  """
                ${scannerHome}/bin/sonar-scanner -Dsonar.projectKey=${projectName}  \
                -Dsonar.projectName=${projectName}  \
                -Dsonar.projectVersion=${sonarDate} \
                -Dsonar.ws.timeout=30 \
                -Dsonar.projectDescription=${projectDesc}  \
                -Dsonar.links.homepage=http://www.baidu.com \
                -Dsonar.sources=${projectPath} \
                -Dsonar.sourceEncoding=UTF-8 \
                -Dsonar.java.binaries=target/classes \
                -Dsonar.java.test.binaries=target/test-classes \
                -Dsonar.java.surefire.report=target/surefire-reports \
                -Dsonar.analysis.mode=preview -Dsonar.gitlab.project_id=${projectId} -Dsonar.gitlab.commit_sha=${commitSha} \
                -Dsonar.gitlab.ref_name=${branchName}
                """
        }else{
            sh  """
                ${scannerHome}/bin/sonar-scanner -Dsonar.projectKey=${projectName}  \
                -Dsonar.projectName=${projectName}  \
                -Dsonar.projectVersion=${sonarDate} \
                -Dsonar.ws.timeout=30 \
                -Dsonar.projectDescription=${projectDesc}  \
                -Dsonar.links.homepage=http://www.baidu.com \
                -Dsonar.sources=${projectPath} \
                -Dsonar.sourceEncoding=UTF-8 \
                -Dsonar.java.binaries=target/classes \
                -Dsonar.java.test.binaries=target/test-classes \
                -Dsonar.java.surefire.report=target/surefire-reports
                """
        }
    }


   // 下面这种方式获取 sonar 状态有点问题
   // def  qg = waitForQualityGate()
   // if (qg.status != 'OK'){
   //     error "Pipeline aborted due to quality gate failure: ${qg.status}"
   // }
}



//使用插件让SonarSacn支持多分支
def SonarSacnForJenkinsSonarPluginAndMultipleBranches(sonarServer,projectName,projectDesc,projectPath,runOpts='',projectId='',commitSha='',branchName=''){
    // "sonarqube-test"和"sonarqube-prod" 是Jenkins 安装 SonarQube Scanner 插件后, 在"Jenkins --> 系统配置 --> SonarQube installations --> Name"的值
    def servers=["test":"sonarqube-test", "prod":"sonarqube-prod"]
    
    withSonarQubeEnv("${servers[sonarServer]}"){
        def sonarDate = sh returnStdout: true, script: 'date + %Y%m%d%H%M%S'
        sonarDate = sonarDate - "\n"
        
        def scannerHome = "/usr/local/sonar-scanner-3.2.0.1227-linux"
        sh  """
            ${scannerHome}/bin/sonar-scanner -Dsonar.projectKey=${projectName}  \
            -Dsonar.projectName=${projectName}  \
            -Dsonar.projectVersion=${sonarDate} \
            -Dsonar.ws.timeout=30 \
            -Dsonar.projectDescription=${projectDesc}  \
            -Dsonar.links.homepage=http://www.baidu.com \
            -Dsonar.sources=${projectPath} \
            -Dsonar.sourceEncoding=UTF-8 \
            -Dsonar.java.binaries=target/classes \
            -Dsonar.java.test.binaries=target/test-classes \
            -Dsonar.java.surefire.report=target/surefire-reports \
            -Dsonar.java.test.binaries=target/test-classes -Dsonar.java.surefire.report=target/surefire-reports -Dsonar.branch.name=${branchName} -X
            """
    }
}