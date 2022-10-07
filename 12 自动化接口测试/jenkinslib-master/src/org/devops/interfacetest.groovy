package org.devops


// 运行测试
def Runtest(){
    antHome = tool "ANT"
    sh "${antHome}/bin/ant -f build.xml"
}

//展示测试报告
