
def call(Closure body) {
    node('windows') {
        body()
    }
}


// 脚本式pipeline调用
/*
windows {
    bat "cmd /?"
}
*/



// 声明式pipeline中调用,必须放在 script 块中:
/*
script {
	windows {
		bat "cmd /?"
	}
}
*/

