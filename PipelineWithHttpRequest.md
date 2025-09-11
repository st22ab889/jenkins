## 在 Jenkins Pipeline 中发起 HTTP 请求可以通过多种方式实现，常用的有以下几种方法，适用于不同场景：

### 方法 1：使用 `sh` 或 `bat` 调用 `curl`（推荐，简单直接）
通过 shell 命令 `curl` 发起 HTTP 请求，兼容适用于大多数环境（需要代理节点安装 `curl`）。

#### 示例：发起 GET 请求
```groovy
pipeline {
    agent any
    stages {
        stage('HTTP Request') {
            steps {
                script {
                    // 发起 GET 请求并获取响应
                    def response = sh(
                        script: 'curl -s "https://api.example.com/data"',  // -s 静默模式
                        returnStdout: true  // 捕获输出
                    ).trim()  // 去除首尾空格和换行
                    
                    echo "响应内容: ${response}"
                    
                    // 如需解析 JSON 响应（可结合 readJSON 步骤）
                    def jsonResponse = readJSON text: response
                    echo "提取的字段值: ${jsonResponse.name}"
                }
            }
        }
    }
}
```

#### 示例：发起 POST 请求（发送 JSON 数据）
```groovy
pipeline {
    agent any
    stages {
        stage('POST Request') {
            steps {
                script {
                    def postData = '{"name": "test", "value": "123"}'
                    
                    // 发送 JSON 格式的 POST 请求
                    def response = sh(
                        script: """
                            curl -s -X POST \
                              -H "Content-Type: application/json" \
                              -d '${postData}' \
                              "https://api.example.com/submit"
                        """,
                        returnStdout: true
                    ).trim()
                    
                    echo "POST 响应: ${response}"
                }
            }
        }
    }
}
```


### 方法 2：使用 `httpRequest` 插件（功能更丰富）
安装 [HTTP Request Plugin](https://plugins.jenkins.io/http_request/) 后，可以使用 `httpRequest` 步骤，支持更多参数（如认证、超时设置、 headers 等）。

#### 安装插件：
在 Jenkins 管理界面 → 插件管理 → 搜索 `HTTP Request Plugin` 并安装。

#### 示例：GET 请求（带认证）
```groovy
pipeline {
    agent any
    stages {
        stage('HTTP Request with Plugin') {
            steps {
                script {
                    // 发起 GET 请求，带基本认证
                    def response = httpRequest(
                        url: 'https://api.example.com/secure-data',
                        authentication: 'my-credentials-id',  // Jenkins 中配置的凭证 ID
                        httpMode: 'GET',
                        timeout: 30,  // 超时时间（秒）
                        validResponseCodes: '200'  // 允许的响应码（多个用逗号分隔）
                    )
                    
                    echo "响应状态码: ${response.status}"
                    echo "响应内容: ${response.content}"
                    
                    // 解析 JSON 响应
                    def jsonResponse = readJSON text: response.content
                    echo "解析结果: ${jsonResponse}"
                }
            }
        }
    }
}
```

#### 示例：POST 请求（带自定义 headers）
```groovy
pipeline {
    agent any
    stages {
        stage('POST with Headers') {
            steps {
                script {
                    def response = httpRequest(
                        url: 'https://api.example.com/upload',
                        httpMode: 'POST',
                        requestBody: '{"action": "start"}',
                        customHeaders: [
                            [name: 'Content-Type', value: 'application/json'],
                            [name: 'X-API-Key', value: 'your-api-key']
                        ],
                        validResponseCodes: '200,201'  // 允许 200 或 201 响应码
                    )
                    
                    echo "响应: ${response.content}"
                }
            }
        }
    }
}
```


### 方法 3：使用 Groovy 代码（纯脚本式，无需插件）
通过 Groovy 内置的 `HttpURLConnection` 或 `HttpClient` 类发起请求，适用于无法使用 `curl` 或插件的场景。

#### 示例：Groovy 发起 GET 请求
```groovy
pipeline {
    agent any
    stages {
        stage('Groovy HTTP Request') {
            steps {
                script {
                    def url = new URL('https://api.example.com/data')
                    def connection = url.openConnection()
                    connection.requestMethod = 'GET'
                    connection.setRequestProperty('Accept', 'application/json')
                    
                    // 读取响应
                    def responseCode = connection.responseCode
                    def response = new BufferedReader(new InputStreamReader(connection.inputStream)).text
                    
                    echo "状态码: ${responseCode}"
                    echo "响应内容: ${response}"
                    
                    connection.disconnect()
                }
            }
        }
    }
}
```


### 各方法对比与选择：
| 方法 | 优点 | 缺点 | 适用场景 |
|------|------|------|----------|
| `curl` + `sh` | 简单直接，无需安装插件 | 依赖 `curl` 工具，复杂请求语法繁琐 | 简单的 GET/POST 请求，环境有 `curl` |
| `httpRequest` 插件 | 功能丰富（认证、超时、headers 等），语法清晰 | 需要安装插件 | 复杂请求（带认证、自定义 headers 等） |
| Groovy 原生代码 | 不依赖外部工具或插件 | 代码冗长，需手动处理异常 | 无插件/工具可用的受限环境 |


根据实际需求选择即可，大多数情况下推荐使用 `curl`（简单场景）或 `httpRequest` 插件（复杂场景）。


---


## (了解)在 Jenkins Pipeline 中使用 Groovy 代码发起原生 HTTP POST 请求，可以通过 `HttpURLConnection` 或 `HttpClient` 实现。以下是使用 `HttpURLConnection` 的示例，无需依赖外部插件，纯 Groovy 原生代码：

```groovy 
pipeline {
    agent any
    stages {
        stage('Groovy POST Request') {
            steps {
                script {
                    // 目标 API 地址
                    def url = new URL("https://api.example.com/submit")
                    
                    // 创建连接
                    def connection = url.openConnection()
                    connection.requestMethod = "POST"
                    
                    // 设置请求头（根据 API 要求调整）
                    connection.setRequestProperty("Content-Type", "application/json")
                    connection.setRequestProperty("Accept", "application/json")
                    connection.setRequestProperty("X-API-Key", "your-api-key-here")
                    
                    // 允许输出和输入
                    connection.doOutput = true
                    connection.doInput = true
                    
                    // 要发送的 JSON 数据
                    def postData = """
                        {
                            "name": "Jenkins Pipeline",
                            "action": "deploy",
                            "environment": "test"
                        }
                    """.trim()  // 去除多余空格
                    
                    // 发送数据
                    try {
                        // 获取输出流并写入数据
                        def outputStream = new DataOutputStream(connection.outputStream)
                        outputStream.writeBytes(postData)
                        outputStream.flush()
                        outputStream.close()
                        
                        // 获取响应状态码
                        def responseCode = connection.responseCode
                        echo "POST 请求响应码: ${responseCode}"
                        
                        // 读取响应内容
                        def inputStream = connection.inputStream
                        def response = new BufferedReader(new InputStreamReader(inputStream)).text
                        echo "POST 请求响应内容:\n${response}"
                        
                        // 解析 JSON 响应（如果需要）
                        if (response) {
                            def jsonSlurper = new groovy.json.JsonSlurper()
                            def jsonResponse = jsonSlurper.parseText(response)
                            echo "解析后的响应数据: ${jsonResponse}"
                            // 可以根据需要提取字段，例如：jsonResponse.status
                        }
                        
                    } catch (Exception e) {
                        // 处理异常（如网络错误、超时等）
                        echo "POST 请求失败: ${e.getMessage()}"
                        // 输出错误流内容（如果服务器返回错误信息）
                        def errorStream = connection.errorStream
                        if (errorStream) {
                            def errorResponse = new BufferedReader(new InputStreamReader(errorStream)).text
                            echo "错误响应内容:\n${errorResponse}"
                        }
                        throw e  // 抛出异常终止流水线（可选）
                    } finally {
                        // 关闭连接
                        connection.disconnect()
                    }
                }
            }
        }
    }
}
```


### 代码说明：
1. **创建连接**：通过 `url.openConnection()` 获取 `HttpURLConnection` 对象，设置请求方法为 `POST`。

2. **设置请求头**：
   - `Content-Type`：指定发送数据的格式（示例中为 `application/json`）
   - 其他自定义头（如 `Accept`、`X-API-Key` 等，根据 API 要求添加）

3. **发送数据**：
   - 必须设置 `connection.doOutput = true` 才能写入请求体
   - 通过 `outputStream` 写入 JSON 字符串（或其他格式数据）

4. **处理响应**：
   - 获取响应状态码（`responseCode`）判断请求是否成功
   - 读取响应内容（`inputStream`）并解析（示例中使用 `JsonSlurper` 解析 JSON）

5. **异常处理**：
   - 捕获网络异常并输出错误信息
   - 通过 `errorStream` 获取服务器返回的错误详情（如 400/500 错误的具体原因）


### 关键注意事项：
- 如果发送的是表单数据（`application/x-www-form-urlencoded`），需将 `postData` 改为 `key1=value1&key2=value2` 格式，并调整 `Content-Type`
- 如需设置超时，可添加：`connection.connectTimeout = 5000`（连接超时 5 秒）和 `connection.readTimeout = 10000`（读取超时 10 秒）
- 对于复杂场景（如 HTTPS 证书验证、代理设置等），可能需要额外配置 SSL 上下文

这种方式完全基于 Groovy 原生类，不依赖任何 Jenkins 插件或外部工具，适合在受限环境中使用。


---

## 遇到 `org.jenkinsci.plugins.scriptsecurity.sandbox.RejectedAccessException: Scripts not permitted to use method java.net.URL openConnection` 错误，是因为 Jenkins 的**脚本安全沙箱**限制了 Groovy 代码调用 `URL.openConnection()` 方法（出于安全考虑，未授权的危险操作会被拦截）。


### 解决方案：
根据 Jenkins 的安全机制，有两种处理方式：


#### 方法 1：临时批准该方法（快速测试用）
1. **找到批准入口**：  
   登录 Jenkins 管理界面 → 进入 **Manage Jenkins** → **In-process Script Approval**（进程内脚本批准）。

2. **批准方法调用**：  
   在“Pending Signatures”（待批准签名）列表中，找到包含 `java.net.URL openConnection` 的条目，点击其右侧的 **Approve** 按钮批准该方法调用。

3. **重新运行流水线**：  
   批准后，沙箱会允许该方法执行，流水线可正常运行。

   ⚠️ 注意：这种方式是临时的，若后续代码变更触发新的未授权方法，仍会报错，适合快速测试场景。


#### 方法 2：将共享库标记为“可信库”（推荐，长期解决方案）
如果你的代码放在**共享库**中（而非直接写在 Jenkinsfile 中），可将共享库配置为“可信库”（Trusted Library），绕过沙箱限制：

1. **配置共享库为可信**：  
   - 进入 Jenkins 管理界面 → **Configure System** → 找到 **Global Pipeline Libraries** 配置区。  
   - 找到你的共享库，勾选 **Trust this library**（信任此库）选项。  
   - 保存配置。

2. **将代码迁移到共享库**：  
   将涉及 `URL.openConnection()` 的 HTTP 请求代码，移到共享库的 `src/` 目录（如 `src/com/example/HttpUtils.groovy`），通过类方法封装：
   ```groovy
   // src/com/example/HttpUtils.groovy
   package com.example
   
   class HttpUtils {
       static void sendPostRequest(String url, String data) {
           def connection = new URL(url).openConnection()
           // 后续的 POST 请求逻辑...
       }
   }
   ```

3. **在流水线中调用**：  
   ```groovy
   @Library('my-trusted-lib') import com.example.HttpUtils
   
   pipeline {
       agent any
       stages {
           stage('Call HTTP') {
               steps {
                   script {
                       HttpUtils.sendPostRequest("https://api.example.com", "data")
                   }
               }
           }
       }
   }
   ```

   ⚠️ 注意：只有管理员可以配置“可信库”，且需确保共享库的代码来源可靠（避免安全风险）。


#### 方法 3：改用无沙箱限制的方式（如 `curl` 或 `httpRequest` 插件）
如果上述两种方式不可行，可放弃 Groovy 原生代码，改用沙箱允许的方式发起 HTTP 请求：
- 使用 `sh 'curl ...'` 调用系统的 `curl` 命令（推荐，简单且无沙箱限制）。
- 使用 `httpRequest` 插件（需安装，其方法已被沙箱信任）。

示例（`curl` 方式）：
```groovy
pipeline {
    agent any
    stages {
        stage('POST with curl') {
            steps {
                script {
                    def response = sh(
                        script: '''
                            curl -s -X POST \
                              -H "Content-Type: application/json" \
                              -d '{"name":"test"}' \
                              "https://api.example.com"
                        ''',
                        returnStdout: true
                    )
                    echo "响应: ${response}"
                }
            }
        }
    }
}
```


### 总结：
- 临时测试：用 **In-process Script Approval** 批准方法。  
- 长期使用：将代码放入**可信共享库**（推荐）。  
- 替代方案：用 `curl` 或 `httpRequest` 插件避开沙箱限制。  

选择最适合你环境的方式即可解决该错误。




