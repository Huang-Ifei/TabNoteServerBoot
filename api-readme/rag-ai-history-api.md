# RAG AI 对话历史 接口文档

基础路径：`/rag`

---

## 建表语句

```sql
CREATE TABLE rag_ai_messages (
    rag_ms_id VARCHAR(255) PRIMARY KEY COMMENT '对话记录ID',
    book_id VARCHAR(255) NOT NULL COMMENT '关联书本ID（即subject）',
    mainly VARCHAR(255) COMMENT '主题摘要（取第一条用户消息前18字）',
    usr_id VARCHAR(255) NOT NULL COMMENT '用户ID',
    contents JSON NOT NULL COMMENT '对话内容JSON',
    date_time datetime COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

### contents 字段结构

格式与 `ai_messages` 表一致，为 `{"messages": [[{...}]]}`，外层 messages 是一个数组，内层每个元素是一轮完整的对话消息数组。AI 回复在对话完成后自动追加到消息数组中。

```json
{
  "messages": [
    [
      { "role": "user", "content": "浏览器显示 https 链接不安全的原理" },
      { "role": "assistant", "content": "当浏览器在访问 HTTPS 链接时，出现"不安全"提示，主要原因和原理涉及以下几个方面：\n\n1. 证书无效或不受信任\n..." }
    ]
  ]
}
```

---

## 一、RAG 对话（自动入库）

### 1.1 RAG 对话

`POST /rag/chat`

**响应方式**：SSE 流式输出（`application/json`），每行一个 JSON 对象。

> 对话完成后自动保存到 `rag_ai_messages` 表。流式输出中每行的 `ai_ms_id` 即为该条对话的 `rag_ms_id`，前端直接取用即可。

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | String | 是 | 用户ID |
| messages | JSONArray | 是 | 对话消息数组 |
| subject | String | 是 | 书本ID（即 RAG 检索的知识库标识） |
| ai_ms_id | String | 否 | 对话记录ID，不传或为空则自动生成 |

**流式输出格式**（每行一个 JSON）：

```
{"ai_ms_id":"xxx","cdn_ai_id":"yyy","model":"deepseek-...","message":{"content":"部分"},"response":"stream"}
{"ai_ms_id":"xxx","cdn_ai_id":"yyy","model":"deepseek-...","message":{"content":"内容"},"response":"stream"}
{"ai_ms_id":"xxx","cdn_ai_id":"yyy","model":null,"message":null,"response":"success"}
```

> 每行中的 `ai_ms_id` 即为该条对话的 `rag_ms_id`，前端通过刷新保存即可。`cdn_ai_id` 为 CDN 分发通道 ID。

---

## 二、历史记录查询

### 2.1 获取 RAG 对话历史列表

`POST /rag/historyList`

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | String | 是 | 用户ID |
| token | String | 是 | 身份令牌 |
| book_id | String | 否 | 书本ID，传了则只返回该书的历史，不传返回全部 |

**请求示例**（按书筛选）：

```json
{
  "id": "user_001",
  "token": "xxx",
  "book_id": "book_123"
}
```

**请求示例**（全部）：

```json
{
  "id": "user_001",
  "token": "xxx"
}
```

**响应示例**：

```json
{
  "response": "success",
  "list": [
    {
      "rag_ms_id": "123456789_123456789_1234567891714000000000",
      "book_id": "book_123",
      "mainly": "这本书的主要内容是什么",
      "date_time": "2026-04-29 14:30:00"
    },
    {
      "rag_ms_id": "987654321_123456789_1234567891714000000001",
      "book_id": "book_123",
      "mainly": "第三章讲了什么",
      "date_time": "2026-04-29 15:00:00"
    }
  ]
}
```

**失败响应**：

```json
{ "response": "token_check_failed" }
```

---

### 2.2 获取某条对话详情

`POST /rag/historyDetail`

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| rag_ms_id | String | 是 | 对话记录ID（从 historyList 获取） |
| token | String | 是 | 身份令牌 |

**请求示例**：

```json
{
  "rag_ms_id": "123456789_123456789_1234567891714000000000",
  "token": "xxx"
}
```

**响应示例**：

```json
{
  "response": "success",
  "rag_ms_id": "123456789_123456789_1234567891714000000000",
  "book_id": "book_123",
  "mainly": "这本书的主要内容是什么",
  "date_time": "2026-04-29 14:30:00",
  "contents": {
    "messages": [
      [
        { "role": "user", "content": "这本书的主要内容是什么" },
        { "role": "assistant", "content": "这本书主要讲述了..." }
      ]
    ]
  }
}
```

**失败响应**：

```json
{ "response": "token_check_failed" }
```

```json
{ "response": "not_found_or_no_permission" }
```

---

## 与 ai_messages 表的对应关系

| ai_messages | rag_ai_messages | 说明 |
|-------------|-----------------|------|
| ai_ms_id | rag_ms_id | 主键 |
| — | book_id | RAG 特有，关联书本 |
| mainly | mainly | 主题摘要 |
| usr_id | usr_id | 用户ID |
| contents | contents | 对话内容 |
| date_time | date_time | 时间 |

| ai_messages 接口 | rag_ai_messages 接口 | 说明 |
|---|---|---|
| `POST /ai/messages` | `POST /rag/chat` | 对话（rag 自动入库） |
| `POST /ai/getAiMessagesList` | `POST /rag/historyList` | 历史列表 |
| `POST /ai/getAiMessages` | `POST /rag/historyDetail` | 历史详情 |

> 两个表的 `contents` 字段格式完全一致，均为 `{"messages": [[{...}]]}`，前端可复用同一套解析逻辑。