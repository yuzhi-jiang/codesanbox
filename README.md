# CodeSandbox

CodeSandbox 是一个多语言代码沙盒平台，旨在提供安全、可控的环境来运行代码。您可以使用它来运行各种编程语言的代码，并且能够对内存和运行时间进行限制，以确保安全性和稳定性。

## 功能特性

- **多语言支持**：目前支持Java,C++和Python支持正在开发中。
- **资源限制**：限制代码的内存使用和运行时间，防止资源耗尽。
- **API接口**：提供简洁的API接口，方便外部系统调用。
- **安全性**：API接口使用Access Key（AK）和Secret Key（SK）进行加密，确保数据安全。

## 完成进度

- [x] **Java**
- [ ] **C++** （开发中）
- [ ] **Python**（开发中）

## 快速开始

### 环境配置

1. 克隆项目到本地：

    ```bash
    git clone https://github.com/your-repo/codesandbox.git
    cd codesandbox
    ```

2. 配置 `application.yml` 文件，设置服务器端口和安全密钥：

    ```yaml
    server:
      port: 8080
    security:
      ak: your-access-key
      sk: your-secret-key
    ```





# CodeSandbox API 使用文档

## 运行代码接口 `/run`

该接口用于运行指定的代码，并限制其运行时间和内存使用。

### 请求方法

`POST /run`

### 请求头参数

- `x-access-key`: 您的 Access Key，用于认证。
- `x-sign`: 请求签名，用于确保请求的完整性和安全性。

### 请求体参数

请求体应为JSON格式，参数如下：

- `code` (字符串，必填): 要运行的代码。
- `language` (字符串，可选): 代码的编程语言。如果未指定，系统将尝试自动检测。
- `inputList` (字符串列表，可选): 运行代码时的输入列表。
- `timeLimit` (长整型，必填): 代码运行的时间限制，单位为毫秒。
- `memoryLimit` (长整型，必填): 代码运行的内存限制，单位为字节。

### 示例请求

以下是一个完整的请求示例，展示了如何使用该接口：

```http
POST /run HTTP/1.1
Host: api.codesandbox.com
Content-Type: application/json
x-access-key: your-access-key
x-sign: your-sign

{
  "code": "public class Main { public static void main(String[] args) { System.out.println(\"Hello, World!\"); } }",
  "language": "java",
  "inputList": ["input1", "input2"],
  "timeLimit": 2000,
  "memoryLimit": 1048576
}
```


