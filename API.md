# API
for Avalon-Executive, API version `v0`

**发生错误时的返回**
```json
{
  "error":true,
  "message": "发生错误：XXX"
}
```

## **获取可用语言**：GetAllLang

- 调用URL：`/avalon/executive/v0/get_all_lang`
- 调用方法：POST
- 调用参数：<无>
- 调用返回：包含所有可用语言的JSON
- 示例返回：
```json
{
  "lang": [
    {
      "id": "cpp11",
      "name": "c++11"
    },
    {
      "id": "cpp98",
      "name": "c++98"
    },
    {
      "id": "py",
      "name": "python3"
    }
  ]
}
```

## 编译程序：Compile

- **调用URL：`/avalon/executive/v0/compile`**

- **调用方法：POST**

- **调用参数：JSON**

- **调用返回：包含编译结果的JSON**

- **示例调用：**

- ```json
  {
    "id": 1000,
    "lang": "py", // 此处lang为GetAllLang返回的id而非name
    "code": "print%281%2B1%29" // URLEncode后的代码
  }
  ```

- **示例返回：**

- ```json
  {
    "id": 1000,
    "error": false,
    "out": "URLEncoded后的编译错误信息，若无错误信息则为空"
  }
  ```

## 运行程序：Run

- **调用URL：`/avalon/executive/v0/run`**

- **调用方法：POST**

- **调用参数：JSON**

- **调用返回：包含运行返回的JSON**

- **示例调用：**

- ```json
  {
    "id": 1000 // 此处的id即为调用了Compile的id
  }
  ```

- **示例返回：**

- ```json
  {
    "id": 1000,
    "error": false,
    "return": "2" // URLEncoded后的错误信息（error为true）或程序返回（error为false）
  }
  ```