package org.devops

// 获取POM中的坐标
def GetGav() {
    // 上传制品
    def jarName = sh returnStdout: true, script: "cd target;ls *.jar"
    env.jarName = jarName - "\n"

    def pom = readMavenPom file: 'pom.xml'
    //pomVersion = "${pom.version}"
    //pomArtifact = "${pom.artifactId}"
    //pomPackaging = "${pom.packaging}"
    //pomGroupId = "${pom.groupId}"

    // 如果把这些变量定义为全局变量,其实都不用return
    env.pomVersion = "${pom.version}"
    env.pomArtifact = "${pom.artifactId}"
    env.pomPackaging = "${pom.packaging}"
    env.pomGroupId = "${pom.groupId}"

    println("${pomGroupId}-${pomArtifact}-${pomVersion}-${pomPackaging}")

    return ["${pomGroupId}","${pomArtifact}","${pomVersion}","${pomPackaging}"]
}

// maven deploy
def MavenUpload() {
    def mvnHome = tool "M2"
    sh  """ 
    cd target/
    ${mvnHome}/bin/mvn deploy:deploy-file -Dmaven.test.skip=true  \
                                -Dfile=${jarName} -DgroupId=${pomGroupId} \
                                -DartifactId=${pomArtifact} -Dversion=${pomVersion}  \
                                -Dpackaging=${pomPackaging} -DrepositoryId=maven-hostd \
                                -Durl=http://192.168.1.200:30083/repository/maven-hostd 
    """
}

// nexus plugin deploy
def NexusUpload() {
    //use nexus plugin
    nexusArtifactUploader artifacts: [[artifactId: "${pomArtifact}", 
                    classifier: '', 
                    file: "${filePath}", 
                    type: "${pomPackaging}"]], 
        credentialsId: 'nexus-admin-user', 
        groupId: "${pomGroupId}", 
        nexusUrl: '192.168.1.200:30083', 
        nexusVersion: 'nexus3', 
        protocol: 'http', 
        repository: "${repoName}", 
        version: "${pomVersion}"
}


// 制品晋级
def ArtifactUpdate(updateType, artifactUrl) {
    // 晋级策略
    if("${updateType}" == "snapshot -> release"){
        println("snapshot -> release")
        
        // 下载原始制品
        sh "rm -rf updates && mkdir updates && cd updates && wget ${artifactUrl} && ls -l"
        
        // 获取ArtifactID
        artifactUrl = artifactUrl - "http://192.168.1.200:30083/repository/maven-hostd/"
        artifactUrl = artifactUrl.split("/").toList()
        println(artifactUrl.size())

        env.jarName = artifactUrl[-1]
        env.pomVersion = artifactUrl[-2].replace("SNAPSHOT","RELEASE")
        env.pomArtifact = artifactUrl[-3]
        env.pomPackaging = artifactUrl[-1].split("\\.").toList()[-1][-1]
        env.pomGroupId = artifactUrl[0..-4].join(".")

        println("${pomGroupId}-${pomArtifact}-${pomVersion}-${pomPackaging}")
        
        env.newJarName = "${pomArtifact}-${pomVersion}.${pomPackaging}"
        // 更改名称
        sh "cd updates && mv ${jarName} ${newJarName}"

        // 上传制品
        env.repoName = "maven-release"
        env.filePath = "updates/${newJarName}"

        NexusUpload()
    }
}


def main(uploadType) {
    
    GetGav();
    
    if("${uploadType}" == "maven"){
        MavenUpload()
    }else if("${uploadType}" == "nexus"){
        env.repoName = "maven-hostd"
        env.filePath = "target/${jarName}"
        NexusUpload()
    }
}