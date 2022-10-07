# Groovy安装以及参考文档
```
1.在官网下载包: https://groovy.apache.org/download.html
    apache-groovy-binary-4.0.0.zip      //这个包只包含运行时
    apache-groovy-docs-4.0.0.zip        //文档
    apache-groovy-src-4.0.0.zip         //源码
    apache-groovy-sdk-4.0.0.zip         //包含运行时、文档、源码
    groovy-4.0.0.msi                    //windows 一键安装器,可以选择安装那些组件,以及自动配置好环境变量

2.安装方法: http://www.groovy-lang.org/install.html
    解压 apache-groovy-sdk-4.0.0.zip, 解压后的文件夹为 groovy-4.0.0

3.新建环境变量
    GROOVY_HOME  
    D:\JavaDevTools\groovy-4.0.0
    
    path环境变量中加入  %GROOVY_HOME%\bin

4.在CMD中运行 groovy --version
    C:\Users\WuJun>groovy --version
    Groovy Version: 4.0.0 JVM: 1.8.0_172 Vendor: Oracle Corporation OS: Windows 10    

5.开发Groovy的IDE插件： https://docs.groovy-lang.org/latest/html/documentation/tools-ide.html


6.intelij idea 配置Groovy: https://www.jianshu.com/p/d02efe10685b


7.https://www.jetbrains.com/idea/
GO(GoLand) WS(WebStorm) IJ(IntelliJ IDEA) PC (PyCharm)

8.groovy document：http://www.groovy-lang.org/documentation.html
```

# Groovy常用命令
```
groovyconsole
groovysh
exit
groovy  xxxx.groovy
```

# 单引号和双引号的区别: 
1. 当字符串中有变量的时候,使用双引号或三个双引号
2. 当字符串中是固定的值,那就可以使用单引号
```
"hello world"
"""hello world"""
'''hello world'''

name = "zhangsan"
"my name is ${name}"
'my name is ${name}'
```

# Groovy数据类型-String
```
"devopstestops".contains("ops")
"devopstestops".endsWith("ops")
"devopstestops".size()
"devopstestops".length()
"dev" + "ops"
"dev" - "ops"
"devops" - "ops" 
"devops".toUpperCase()
"DEVOPS".toLowerCase()
"host01,host02,host03".split(',')
hosts="host01,host02,host03".split(',')
for(i in hosts){
    println(i)
}
```

# Groovy数据类型-list
```
[]
[1,2,3,4] + 66
[1,2,3,4] << 66 
result = [1,2,3,4].add(66) // 返回true 或 false
[2,2,3,6,8,9,9].unique()
[2,2,3,6,8,9,9].join("-")
[2,2,3,6,8,9,9].each{
    print it 
}
```

# Groovy数据类型-map
```
[:]
[1:2][1]   // 通过key获取value
[1:2,3:4,5:6].keySet()
[1:2,3:4,5:6].values()
[1:2,3:4,5:6] + [7:8]
[1:2,3:4,5:6] - [7:8]
```

# Groovy条件语句-if
```
 buildType = "maven"
 if(buildType == "maven"){
     println("this is a maven project")
 } else if(buildType == "gradle"){
     println("this is a gradle project")
 } else {
      println("Project type error")
 }
```

# Groovy条件语句-switch
```
switch("${buildType}"){
    case 'maven':
        println("this is a maven project")
        break;
        ;;
    case 'gradle':
        println("this is a gradle project")
        break;
        ;;
    default:
        println("Project type error")
        break;
        ;;
}
```

# Groovy循环语句-for/while
```
langs = ['java', "python", 'groovy']
for(lang in langs){
    if(lang == "java"){
        println("lang error in java")
    }else{
        println("lang is ${lang}") 
    }
}
```

```
while (1 == 1) {
    println("true") 
}
```



# Groovy函数
```
//  def 关键字用来定义函数
def PrintMsg(info) {
    println(info) 
}
// 调用函数
PrintMsg("DevOps")
```

```
def PrintMsg(info) {
    println(info) 
    return info
}
// 调用函数
response = PrintMsg("DevOps")
println(response)
```

# Groovy函数插件
1. 有时候用插件有好处,有时候不用插件会更方便一些。
2. 要把 jenkins 做成无状态,或者是说Jenkins做成模板引擎,那么插件越少越好。

# centos相关
```
centos查看磁盘空间大小
Centos下查看cpu、磁盘、内存使用情况以及如何清理内存
```