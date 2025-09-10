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






/* 
了解内容,知道即可:

There is also a “builder pattern” trick using Groovy’s Closure.DELEGATE_FIRST, which permits Jenkinsfile to look slightly more like a configuration file than a program, but this is more complex and error-prone and is not recommended.

上面这段话的意思是：还可以利用 Groovy 的 `Closure.DELEGATE_FIRST` 特性实现一种"构建器模式"技巧，这种方式能让 Jenkinsfile 看起来更像配置文件而非程序代码，但这种做法更复杂且容易出错，因此不推荐使用。


### 核心含义解析：
- **构建器模式（builder pattern）**：一种设计模式，通过链式调用或代码块配置的方式简化复杂对象的创建。  
- **`Closure.DELEGATE_FIRST`**：Groovy 闭包的一种解析策略，让闭包中的方法调用优先委托给指定的“代理对象”（而非闭包自身或外部上下文）。  
- **不推荐的原因**：虽然能让流水线脚本更像“配置文件”，但会增加代码复杂度、降低可读性，且容易因委托逻辑混乱导致难以调试的错误。


### 示例（展示这种模式的实现方式及潜在问题）：

#### 1. 共享库中定义“构建器”逻辑（`vars/myBuilder.groovy`）
```groovy
// vars/myBuilder.groovy
def call(Closure config) {
    // 创建一个代理对象，用于接收闭包中的配置
    def builder = new MyBuilder()
    
    // 配置闭包的解析策略：优先使用代理对象的方法/属性
    config.resolveStrategy = Closure.DELEGATE_FIRST
    config.delegate = builder  // 将闭包的委托设置为 builder 对象
    
    // 执行闭包，解析配置
    config()
    
    // 执行最终构建逻辑
    builder.execute()
}

// 定义构建器类，包含配置方法和执行逻辑
class MyBuilder {
    String name
    String branch = 'main'  // 默认值
    List<String> steps = []
    
    // 配置项目名称
    void name(String name) {
        this.name = name
    }
    
    // 配置分支
    void branch(String branch) {
        this.branch = branch
    }
    
    // 配置步骤
    void steps(Closure c) {
        c.resolveStrategy = Closure.DELEGATE_FIRST
        c.delegate = new StepBuilder(this)  // 嵌套委托
        c()
    }
    
    // 执行最终构建
    void execute() {
        if (!name) {
            error("项目名称未配置")
        }
        node {
            echo "构建项目: ${name} (分支: ${branch})"
            git url: "https://github.com/example/${name}.git", branch: branch
            steps.each { step ->
                sh step
            }
        }
    }
}

// 嵌套的步骤构建器
class StepBuilder {
    private final MyBuilder parent
    
    StepBuilder(MyBuilder parent) {
        this.parent = parent
    }
    
    // 添加步骤
    void sh(String command) {
        parent.steps << command
    }
}
```


#### 2. 流水线中使用（看起来像配置文件）
```groovy
@Library('my-lib') _

// 使用构建器模式，代码块看起来像配置
myBuilder {
    name 'my-project'
    branch 'dev'
    steps {
        sh 'npm install'
        sh 'npm run build'
        sh 'npm test'
    }
}
```


#### 3. 潜在问题（为什么不推荐）：
- **复杂性高**：需要理解闭包委托机制、嵌套构建器的逻辑，增加了维护成本。  
- **调试困难**：若配置中出现拼写错误（如把 `name` 写成 `nme`），Groovy 可能不会直接报错，而是因找不到方法导致隐晦的逻辑错误。  
- **可读性迷惑**：看似简单的配置背后隐藏了复杂的委托逻辑，新维护者可能难以理解“`sh` 方法到底在哪里定义”。  


### 更推荐的替代方案（简单直接）：
用普通的 `Map` 参数或方法调用替代，虽然看起来像“程序代码”，但更清晰易懂：
```groovy
// vars/buildProject.groovy
def call(Map config) {
    node {
        echo "构建项目: ${config.name} (分支: ${config.branch ?: 'main'})"
        git url: "https://github.com/example/${config.name}.git", branch: config.branch
        config.steps.each { sh it }
    }
}

// 流水线中使用
buildProject(
    name: 'my-project',
    branch: 'dev',
    steps: [
        'npm install',
        'npm run build',
        'npm test'
    ]
)
```

这种方式虽然不像“配置文件”，但逻辑明确、易于调试，更符合 Jenkins 共享库的最佳实践。

*/
