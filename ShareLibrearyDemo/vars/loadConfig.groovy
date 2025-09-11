
def call(){
    // 加载资源文件(相对路径对应 resources 目录下的结构)
    def configContent = libraryResource 'com/mycorp/pipeline/config.json'

    // 将 JSON 字符串解析为 Map,需要 Groovy 的 JsonSlurper
    import groovy.json.JsonSlurper
    def config = new JsonSlurper().parseText(configContent)
    
    // 使用配置内容
    echo "构建工具: ${config.buildTool}"
    echo "测试命令: ${config.testCommand}"
    
	// 返回解析后的配置,供流水线使用
    return config  
}



/*
Jenkins 共享库中**加载资源文件(如配置文件、模板、JSON 等)** 的方法，核心是通过 `libraryResource` 步骤从共享库的 `resources` 目录加载文件内容，具体含义和示例如下：

### 核心含义解析：
1. **资源文件的存放位置**：  
   共享库中可以创建 `resources` 目录，用于存放辅助文件(如 JSON 配置、XML 模板、脚本片段等)。这些文件会随共享库一起被 Jenkins 加载，供流水线或共享库的代码使用。

2. **`libraryResource` 步骤的作用**：  
   用于读取 `resources` 目录中的文件，返回文件内容的字符串形式。参数是文件的**相对路径**(类似 Java 中加载资源的路径格式)。

3. **命名规范建议**：  
   推荐使用**唯一的包结构**(如 `com/yourcompany/project/...`)存放资源文件，避免与其他共享库的资源文件重名冲突。

### 关键说明：
- **路径规则**：`libraryResource` 的参数是相对于 `resources` 目录的路径，例如 `com/mycorp/pipeline/config.json` 对应 `resources/com/mycorp/pipeline/config.json`。
- **返回值**：`libraryResource` 返回文件内容的字符串，可直接用于解析（如 JSON、XML）或通过 `writeFile` 写入工作区。
- **冲突避免**：使用类似 Java 包的结构（如 `com/公司名/项目名/`）命名资源目录，避免不同共享库的资源文件重名(例如 `com/othercorp/...` 就不会与示例中的 `com/mycorp/...` 冲突)。

总结：在 Jenkins 共享库中通过 `libraryResource` 步骤加载 `resources` 目录下的资源文件，实现配置文件、模板等辅助文件的复用，同时通过包结构避免命名冲突。
*/

