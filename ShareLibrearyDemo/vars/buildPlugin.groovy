
/*
* 利用 Jenkins 共享库的全局变量机制来定义更结构化的 DSL (领域特定语言),以便在多个相似的流水线中复用逻辑,减少重复代码.
* 当多个流水线有大量重复逻辑(如相同的构建步骤、测试流程、通知方式)时,可以将共性逻辑抽象成一个"自定义步骤"(如 buildPlugin),实现一次编写、多处复用.
*/
def call(Map config) {
	node {
        // git url: "https://github.com/jenkinsci/${config.name}-plugin.git"
        // sh 'mvn install'
        // mail to: '...', subject: "${config.name} plugin build", body: '...'
		
        echo "https://github.com/jenkinsci/${config.name}-plugin.git"
        echo "mvn ${config.action}"
        echo "..., subject: ${config.name} plugin build, body: ..."
	}
}