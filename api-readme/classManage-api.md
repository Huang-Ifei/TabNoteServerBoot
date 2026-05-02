# 班级管理接口文档

基础路径：`/classManage`

所有接口（除模板下载外）均需在请求体中携带 `usr_id` 和 `token` 进行身份验证。

---

## 一、班级管理

### 1.1 创建班级

`POST /classManage/createClass`

**权限要求**：VIP rank >= 10，且当前用户必须是教师

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| usr_id | String | 是 | 操作者用户ID |
| token | String | 是 | 身份令牌 |
| class_name | String | 是 | 班级名称 |
| description | String | 否 | 班级描述 |

**响应示例**：

```json
{
  "success": true,
  "class_id": "123456_1714000000000",
  "message": "Class created successfully"
}
```

---

### 1.2 删除班级

`POST /classManage/deleteClass`

**权限要求**：VIP rank >= 10

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| usr_id | String | 是 | 操作者用户ID |
| token | String | 是 | 身份令牌 |
| class_id | String | 是 | 班级ID |

**响应示例**：

```json
{
  "success": true,
  "message": "Class deleted successfully"
}
```

---

### 1.3 更新班级

`POST /classManage/updateClass`

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| usr_id | String | 是 | 操作者用户ID |
| token | String | 是 | 身份令牌 |
| class_id | String | 是 | 班级ID |
| class_name | String | 是 | 新班级名称 |
| description | String | 否 | 新班级描述 |

**响应示例**：

```json
{
  "success": true,
  "message": "Class updated successfully"
}
```

---

### 1.4 获取班级详情

`POST /classManage/classDetail`

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| usr_id | String | 是 | 操作者用户ID |
| token | String | 是 | 身份令牌 |
| class_id | String | 是 | 班级ID |

**响应示例**：

```json
{
  "success": true,
  "class": {
    "class_id": "123456_1714000000000",
    "class_name": "计算机科学1班",
    "description": "2024级",
    "create_time": "2024-01-01 00:00:00",
    "update_time": "2024-01-01 00:00:00",
    "display": 0
  }
}
```

---

### 1.5 获取班级列表

`POST /classManage/classList`

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| usr_id | String | 是 | 操作者用户ID |
| token | String | 是 | 身份令牌 |
| index | Integer | 否 | 分页偏移量，默认 0 |

**响应示例**：

```json
{
  "success": true,
  "classes": [
    {
      "class_id": "123456_1714000000000",
      "class_name": "计算机科学1班",
      "description": "2024级",
      "create_time": "2024-01-01 00:00:00"
    }
  ]
}
```

---

## 二、教师管理

### 2.1 创建教师

`POST /classManage/createTeacher`

**权限要求**：VIP rank >= 12，创建后自动赠送 12 年 VIP

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| usr_id | String | 是 | 操作者用户ID |
| token | String | 是 | 身份令牌 |
| teacher_usr_id | String | 是 | 被创建教师的用户ID |
| title | String | 否 | 教师职称 |

**响应示例**：

```json
{
  "success": true,
  "teacher_id": "789012_1714000000000",
  "message": "Teacher created successfully with 12-year VIP"
}
```

---

### 2.2 删除教师

`POST /classManage/deleteTeacher`

**权限要求**：VIP rank >= 12

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| usr_id | String | 是 | 操作者用户ID |
| token | String | 是 | 身份令牌 |
| teacher_id | String | 是 | 教师ID |

**响应示例**：

```json
{
  "success": true,
  "message": "Teacher deleted successfully"
}
```

---

### 2.3 更新教师

`POST /classManage/updateTeacher`

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| usr_id | String | 是 | 操作者用户ID |
| token | String | 是 | 身份令牌 |
| teacher_id | String | 是 | 教师ID |
| title | String | 是 | 新职称 |

**响应示例**：

```json
{
  "success": true,
  "message": "Teacher updated successfully"
}
```

---

### 2.4 获取教师详情

`POST /classManage/teacherDetail`

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| usr_id | String | 是 | 操作者用户ID |
| token | String | 是 | 身份令牌 |
| teacher_id | String | 是 | 教师ID |

**响应示例**：

```json
{
  "success": true,
  "teacher": {
    "teacher_id": "789012_1714000000000",
    "usr_id": "teacher_001",
    "title": "教授",
    "create_time": "2024-01-01 00:00:00"
  }
}
```

---

### 2.5 获取教师列表

`POST /classManage/teacherList`

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| usr_id | String | 是 | 操作者用户ID |
| token | String | 是 | 身份令牌 |
| index | Integer | 否 | 分页偏移量，默认 0 |

**响应示例**：

```json
{
  "success": true,
  "teachers": [
    {
      "teacher_id": "789012_1714000000000",
      "usr_id": "teacher_001",
      "title": "教授",
      "create_time": "2024-01-01 00:00:00"
    }
  ]
}
```

---

## 三、学生管理

### 3.1 创建学生

`POST /classManage/createStudent`

**权限要求**：VIP rank >= 10，创建后自动赠送 4 年 VIP

> 通过 **学号（student_no）** 判断学生是否已存在，学号相同时视为重复。

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| usr_id | String | 是 | 操作者用户ID |
| token | String | 是 | 身份令牌 |
| student_usr_id | String | 是 | 学生关联的用户ID（账号） |
| student_no | String | 是 | 学号（唯一标识） |

**响应示例**：

```json
{
  "success": true,
  "student_id": "345678_1714000000000",
  "message": "Student created successfully with 4-year VIP"
}
```

**失败响应**（学号已存在）：

```json
{
  "success": false,
  "message": "Student already exists for student_no: 2024001"
}
```

---

### 3.2 删除学生

`POST /classManage/deleteStudent`

**权限要求**：VIP rank >= 10

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| usr_id | String | 是 | 操作者用户ID |
| token | String | 是 | 身份令牌 |
| student_id | String | 是 | 学生ID |

**响应示例**：

```json
{
  "success": true,
  "message": "Student deleted successfully"
}
```

---

### 3.3 更新学生

`POST /classManage/updateStudent`

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| usr_id | String | 是 | 操作者用户ID |
| token | String | 是 | 身份令牌 |
| student_id | String | 是 | 学生ID |
| student_no | String | 是 | 新学号 |

**响应示例**：

```json
{
  "success": true,
  "message": "Student updated successfully"
}
```

---

### 3.4 获取学生详情

`POST /classManage/studentDetail`

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| usr_id | String | 是 | 操作者用户ID |
| token | String | 是 | 身份令牌 |
| student_id | String | 是 | 学生ID |

**响应示例**：

```json
{
  "success": true,
  "student": {
    "student_id": "345678_1714000000000",
    "usr_id": "student_001",
    "student_no": "2024001",
    "create_time": "2024-01-01 00:00:00"
  }
}
```

---

### 3.5 获取学生列表

`POST /classManage/studentList`

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| usr_id | String | 是 | 操作者用户ID |
| token | String | 是 | 身份令牌 |
| index | Integer | 否 | 分页偏移量，默认 0 |

**响应示例**：

```json
{
  "success": true,
  "students": [
    {
      "student_id": "345678_1714000000000",
      "usr_id": "student_001",
      "student_no": "2024001",
      "create_time": "2024-01-01 00:00:00"
    }
  ]
}
```

---

## 四、班级成员管理

### 4.1 添加学生到班级

`POST /classManage/addStudentToClass`

**权限要求**：仅该班级的教师可操作

> 通过 **学号（student_no）** 查找学生，学号不存在时返回失败。

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| usr_id | String | 是 | 操作者用户ID |
| token | String | 是 | 身份令牌 |
| class_id | String | 是 | 班级ID |
| student_no | String | 是 | 学号 |
| role | String | 否 | 角色，默认 `member` |

**响应示例**：

```json
{
  "success": true,
  "message": "Student added to class successfully"
}
```

---

### 4.2 从班级移除学生

`POST /classManage/removeStudentFromClass`

**权限要求**：仅该班级的教师可操作

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| usr_id | String | 是 | 操作者用户ID |
| token | String | 是 | 身份令牌 |
| class_id | String | 是 | 班级ID |
| student_usr_id | String | 是 | 学生关联的用户ID |

**响应示例**：

```json
{
  "success": true,
  "message": "Student removed from class successfully"
}
```

---

### 4.3 修改学生在班级中的角色

`POST /classManage/updateStudentRole`

**权限要求**：仅该班级的教师可操作

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| usr_id | String | 是 | 操作者用户ID |
| token | String | 是 | 身份令牌 |
| class_id | String | 是 | 班级ID |
| student_usr_id | String | 是 | 学生关联的用户ID |
| role | String | 是 | 新角色（如 `member`、`monitor`） |

**响应示例**：

```json
{
  "success": true,
  "message": "Student role updated successfully"
}
```

---

### 4.4 获取班级学生列表

`POST /classManage/classStudents`

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| usr_id | String | 是 | 操作者用户ID |
| token | String | 是 | 身份令牌 |
| class_id | String | 是 | 班级ID |

**响应示例**：

```json
{
  "success": true,
  "students": [
    {
      "student_id": "345678_1714000000000",
      "usr_id": "student_001",
      "student_no": "2024001",
      "user_name": "张三",
      "create_time": "2024-01-01 00:00:00",
      "role": "member",
      "join_time": "2024-01-01 00:00:00"
    }
  ]
}
```

---

### 4.5 获取班级教师列表

`POST /classManage/classTeachers`

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| usr_id | String | 是 | 操作者用户ID |
| token | String | 是 | 身份令牌 |
| class_id | String | 是 | 班级ID |

**响应示例**：

```json
{
  "success": true,
  "teachers": [
    {
      "teacher_id": "789012_1714000000000",
      "usr_id": "teacher_001",
      "user_name": "王老师",
      "title": "教授",
      "create_time": "2024-01-01 00:00:00"
    }
  ]
}
```

---

### 4.6 获取学生所在班级列表

`POST /classManage/studentClasses`

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| usr_id | String | 是 | 操作者用户ID |
| token | String | 是 | 身份令牌 |
| student_usr_id | String | 是 | 学生的用户ID |

**响应示例**：

```json
{
  "success": true,
  "classes": [
    {
      "class_id": "123456_1714000000000",
      "class_name": "计算机科学1班",
      "description": "2024级",
      "role": "member",
      "join_time": "2024-01-01 00:00:00"
    }
  ]
}
```

---

### 4.7 获取教师所在班级列表

`POST /classManage/teacherClasses`

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| usr_id | String | 是 | 操作者用户ID |
| token | String | 是 | 身份令牌 |
| teacher_usr_id | String | 是 | 教师的用户ID |

**响应示例**：

```json
{
  "success": true,
  "classes": [
    {
      "class_id": "123456_1714000000000",
      "class_name": "计算机科学1班",
      "description": "2024级"
    }
  ]
}
```

---

## 五、Excel 批量操作

### 5.1 批量创建学生

`POST /classManage/batchCreateStudents`

**Content-Type**：`multipart/form-data`

**权限要求**：VIP rank >= 10，创建后自动赠送 4 年 VIP

> 通过 **学号（student_no）** 判断学生是否已存在，学号相同时跳过该行。

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| file | File | 是 | Excel 文件（.xlsx） |
| usr_id | String | 是 | 操作者用户ID |
| token | String | 是 | 身份令牌 |

**Excel 文件格式**（第一行为表头，从第二行开始为数据）：

| 列A | 列B |
|-----|-----|
| student_no（学号） | usr_id（账号） |
| 2024001 | student_001 |
| 2024002 | student_002 |

> 可通过 `GET /classManage/downloadStudentTemplate` 下载模板。

**响应示例**：

```json
{
  "success": true,
  "total": 50,
  "success_count": 48,
  "fail_count": 2,
  "failed_rows": [
    { "row": 3, "student_no": "2024003", "reason": "Student already exists for this student_no" },
    { "row": 10, "student_no": "2024010", "reason": "usr_id is empty" }
  ],
  "message": "Batch create completed: 48 success, 2 failed"
}
```

---

### 5.2 批量添加学生到班级

`POST /classManage/batchAddStudentsToClass`

**Content-Type**：`multipart/form-data`

**权限要求**：仅该班级的教师可操作

> 通过 **学号（student_no）** 查找学生，学号不存在时跳过该行。

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| file | File | 是 | Excel 文件（.xlsx） |
| usr_id | String | 是 | 操作者用户ID |
| token | String | 是 | 身份令牌 |
| class_id | String | 是 | 目标班级ID |

**Excel 文件格式**（第一行为表头，从第二行开始为数据）：

| 列A | 列B |
|-----|-----|
| student_no（学号） | role（角色，默认member） |
| 2024001 | member |
| 2024002 | monitor |

> 可通过 `GET /classManage/downloadStudentClassTemplate` 下载模板。

**响应示例**：

```json
{
  "success": true,
  "total": 30,
  "success_count": 29,
  "fail_count": 1,
  "failed_rows": [
    { "row": 5, "student_no": "2024005", "reason": "Student not found, please create student first" }
  ],
  "message": "Batch add completed: 29 success, 1 failed"
}
```

---

### 5.3 下载批量创建学生模板

`GET /classManage/downloadStudentTemplate`

无需参数，直接下载 `student_template.xlsx` 文件。

**响应**：`application/vnd.openxmlformats-officedocument.spreadsheetml.sheet`

---

### 5.4 下载批量加入班级模板

`GET /classManage/downloadStudentClassTemplate`

无需参数，直接下载 `student_class_template.xlsx` 文件。

**响应**：`application/vnd.openxmlformats-officedocument.spreadsheetml.sheet`

---

### 5.5 批量注册学生（注册账号 + 创建学生信息）

`POST /classManage/batchRegisterStudents`

**Content-Type**：`multipart/form-data`

**权限要求**：VIP rank >= 10

> 自动完成两步操作：先用学号生成账号密码完成注册，再创建学生信息并赠送 4 年 VIP。

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| file | File | 是 | Excel 文件（.xlsx） |
| usr_id | String | 是 | 操作者用户ID |
| token | String | 是 | 身份令牌 |

**注册规则**：

| 字段 | 生成规则 | 说明 |
|------|----------|------|
| 账号ID | `stu_` + 学号 | 例：学号 `2024001` → 账号 `stu_2024001` |
| 密码 | 学号 | 登录时使用学号作为密码 |
| 姓名 | Excel 中的 name 列 | |

> 注意：账号ID以 `stu_` 开头，普通用户注册接口已屏蔽此前缀，仅能通过此批量接口创建。

**Excel 文件格式**（第一行为表头，从第二行开始为数据）：

| 列A | 列B |
|-----|-----|
| student_no（学号） | name（姓名） |
| 2024001 | 张三 |
| 2024002 | 李四 |

> 可通过 `GET /classManage/downloadRegisterTemplate` 下载模板。

**响应示例**：

```json
{
  "success": true,
  "total": 50,
  "success_count": 48,
  "fail_count": 2,
  "failed_rows": [
    { "row": 3, "student_no": "2024003", "name": "王五", "reason": "账号已存在" },
    { "row": 7, "student_no": "2024007", "name": "赵六", "reason": "名字已存在" }
  ],
  "message": "Batch register completed: 48 success, 2 failed"
}
```

---

### 5.6 下载批量注册学生模板

`GET /classManage/downloadRegisterTemplate`

无需参数，直接下载 `register_template.xlsx` 文件。

**响应**：`application/vnd.openxmlformats-officedocument.spreadsheetml.sheet`

---

## 通用响应说明

所有 JSON 接口的返回格式：

```json
{
  "success": true/false,
  "message": "操作结果描述",
  "...": "其他业务字段"
}
```

**错误情况**：
- `success: false` + `message: "token_check_failed"` — token 验证失败
- `success: false` + `message: "VIP rank must be >= 10 ..."` — 权限不足
- HTTP 400 响应体 `err` — 请求参数异常