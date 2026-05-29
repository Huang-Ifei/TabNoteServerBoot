     作业统计功能实现计划

     Context

     在现有作业管理模块基础上增加两个统计接口，前端使用 ECharts 直接渲染图表，后端尽量把数据处理好。

     新增接口

     1. 单次作业统计（柱状图）

     POST /homeworkManage/homeworkStats

     响应格式（前端直接可用）：
     {
       "success": true,
       "homework": { "title": "Java基础练习", "total_score": 100 },
       "chart": {
         "categories": ["张三", "李四", "王五", "赵六"],
         "series": [
           { "value": 85, "color": "#409EFF" },
           { "value": 45, "color": "#E6A23C" },
           { "value": 15, "color": "#F56C6C" },
           { "value": 0,  "color": "#909399", "submitted": 0 }
         ]
       },
       "details": {
         "张三": { "student_no": "2024001", "score": 85, "late": 0, "submitted": 1 },
         "李四": { "student_no": "2024002", "score": 45, "late": 0, "submitted": 1 }
       }
     }
     - details key = 学生姓名，点击柱状图时前端用名字查找详情
     - 未提交: score=0, submitted=0, color=灰色

     2. 班级全部作业统计（3D柱状图）

     POST /homeworkManage/classHomeworkStats

     响应格式：
     {
       "success": true,
       "homework_names": ["作业1", "作业2", "作业3"],
       "student_names": ["张三", "李四", "王五"],
       "chart_data": [
         [0, 0, 85, "#409EFF"],
         [0, 1, 92, "#409EFF"],
         [0, 2, 0, "#909399"],
         [1, 0, 75, "#67C23A"],
         [1, 1, 55, "#E6A23C"],
         [1, 2, 70, "#F56C6C"]
       ],
       "detail_list": [
         {"hi": 0, "si": 0, "student_no": "2024001", "name": "张三", "homework_title": "作业1", "score": 85, "late": 0, "submitted": 1}
       ]
     }
     - chart_data: [homeworkIndex, studentIndex, score, color]，ECharts bar3D 直接用
     - detail_list: 点击时通过 hi（作业索引）+ si（学生索引）查找详情

     颜色规则

     ┌───────────────────┬──────┬─────────┐
     │       条件        │ 颜色 │   hex   │
     ├───────────────────┼──────┼─────────┤
     │ 迟交              │ 红色 │ #F56C6C │
     ├───────────────────┼──────┼─────────┤
     │ 未提交            │ 灰色 │ #909399 │
     ├───────────────────┼──────┼─────────┤
     │ 分数 < 60%满分    │ 黄色 │ #E6A23C │
     ├───────────────────┼──────┼─────────┤
     │ 60% <= 分数 < 90% │ 绿色 │ #67C23A │
     ├───────────────────┼──────┼─────────┤
     │ 分数 >= 90%       │ 蓝色 │ #409EFF │
     └───────────────────┴──────┴─────────┘

     优先级：迟交 > 未提交 > 低分 > 中分 > 高分

     修改文件清单

     1. HomeworkService.java（接口）

     路径：services/inteface/HomeworkService.java
     新增两个方法声明

     2. HomeworkServiceImpl.java（实现）

     路径：services/HomeworkServiceImpl.java
     新增两个方法实现，复用现有 mapper：
     - classMemberMapper.getStudentsByClassId() — 获取班级所有学生
     - homeworkMapper.getHomeworkById() — 获取作业信息
     - homeworkMapper.getHomeworkListByClassId() — 获取班级所有作业
     - homeworkSubmissionMapper.getSubmissionsByHomeworkId() — 获取提交记录

     3. HomeworkManageController.java（控制器）

     路径：controller/HomeworkManageController.java
     新增两个 POST 接口

     颜色计算逻辑

     private String getBarColor(Double score, Double totalScore, Integer late, Integer submitted) {
         if (submitted == 0) return "#909399";       // 灰色：未提交
         if (late == 1) return "#F56C6C";            // 红色：迟交
         if (totalScore > 0 && score < totalScore * 0.6) return "#E6A23C";  // 黄色：<60%
         if (totalScore > 0 && score >= totalScore * 0.9) return "#409EFF"; // 蓝色：>=90%
         return "#67C23A";                          // 绿色：60%-90%
     }
