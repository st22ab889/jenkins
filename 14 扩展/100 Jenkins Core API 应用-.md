## Jenkins Core API 应用- ##
```
https://javadoc.jenkins-ci.org/index-core.html
```
![jave core api](./readee-ref-resource/100/Jenkins_1_java_online_core_api.jpg)


<br/><br/>

## 1. JaveCoreApi介绍 ##
![jave core api course](./readee-ref-resource/100/Jenkins_2_java_api_course.jpg)

<br/><br/>

## 2. JaveCoreApi实践 ##
```
把java代码拷贝到Jenkins的脚本命令行中就可以直接运行java代码
```
```

import javax.xml.transform.stream.StreamSource
import jenkins.model.Jenkins

//创建项目
void createOrUpdateJob(String name, String xml) {
    def j = Jenkins.instance
    String fullName = name
    if(name.contains('/')) {
        j = j.getItemByFullName(name.tokenize('/')[0..-2])
        name = name.tokenize('/')[-1]
    }
    Jenkins.checkGoodName(name)
    if(j.getItem(name) == null) {
        println "Created job \"${fullName}\"."
        j.createProjectFromXML(name, new ByteArrayInputStream(xml.getBytes()))
        j.save()
    }
    else if(j.getItem(name).configFile.asString().trim() != xml.trim()) {
        j.getItem(name).updateByXml(new StreamSource(new ByteArrayInputStream(xml.getBytes())))
        j.getItem(name).save()
        println "Job \"${fullName}\" already exists.  Updated using XML."
    }
    else {
        println "Nothing changed.  Job \"${fullName}\" already exists."
    }
}

try {
    //just by trying to access properties should throw an exception
} catch(MissingPropertyException e) {
    println 'ERROR Can\'t create job.'
    println 'ERROR Missing properties: itemName, xmlData'
    return
}

String xmlData = """<!-- 1. test-schdule-service -->
<flow-definition>
    <actions></actions>
    <description>this is my first job</description>
    <keepDependencies>false</keepDependencies>
    <properties>
        <hudson.model.ParametersDefinitionProperty>
            <parameterDefinitions>
                <hudson.model.ChoiceParameterDefinition>
                    <choices class='java.util.Arrays$ArrayList'>
                        <a class='string-array'>
                            <string>1</string>
                            <string>2</string>
                            <string>3</string>
                        </a>
                    </choices>
                    <name>test</name>
                    <description></description>
                </hudson.model.ChoiceParameterDefinition>
            </parameterDefinitions>
        </hudson.model.ParametersDefinitionProperty>
        <com.coravy.hudson.plugins.github.GithubProjectProperty>
            <projectUrl>https://github.com/https://gitlab.com/xxx/xxx.git/</projectUrl>
        </com.coravy.hudson.plugins.github.GithubProjectProperty>
    </properties>
    <triggers></triggers>
    <definition class='org.jenkinsci.plugins.workflow.cps.CpsScmFlowDefinition'>
        <scriptPath>Jenkinsfile</scriptPath>
        <lightweight>false</lightweight>
        <scm class='hudson.plugins.git.GitSCM'>
            <userRemoteConfigs>
                <hudson.plugins.git.UserRemoteConfig>
                    <url>https://github.com/https://gitlab.com/xxx/xxx.git.git</url>
                    <credentialsId>24982560-17fc-4589-819b-bc5bea89da77</credentialsId>
                </hudson.plugins.git.UserRemoteConfig>
            </userRemoteConfigs>
            <branches>
                <hudson.plugins.git.BranchSpec>
                    <name>*/master</name>
                </hudson.plugins.git.BranchSpec>
            </branches>
            <configVersion>2</configVersion>
            <doGenerateSubmoduleConfigurations>false</doGenerateSubmoduleConfigurations>
            <gitTool>Default</gitTool>
            <browser class='hudson.plugins.git.browser.GithubWeb'>
                <url>https://github.com/https://gitlab.com/xxx/xxx.git/</url>
            </browser>
        </scm>
    </definition>
    <disabled>false</disabled>
</flow-definition>
"""
String itemName = "my-first-pipeline2"

createOrUpdateJob(itemName, xmlData)
```
![jave core api course](./readee-ref-resource/100/Jenkins_3_java_api_example.jpg)