## 需求: Sonar 配置项目多分支模式- ##
```
社区版 Sonar 不支持多分支,但是用插件可以实现: https://github.com/mc1arke/sonarqube-community-branch-plugin
按照文档安装, 然后重启Sonar.
```

<br/> <br/>

### ShareLibray --> sonarqube.groovy ###
```
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
```




