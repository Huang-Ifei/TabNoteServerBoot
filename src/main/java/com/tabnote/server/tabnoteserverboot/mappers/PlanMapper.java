package com.tabnote.server.tabnoteserverboot.mappers;

import com.tabnote.server.tabnoteserverboot.models.Plan;
import org.apache.ibatis.annotations.*;
import java.util.List;


//plan_id由：（content的hashcode+时间戳）（客户端本地id同）组成
@Mapper
public interface PlanMapper {
    @Select("SELECT * FROM plans WHERE usr_id = #{id} ORDER BY date;")
    List<Plan> getPlans(@Param("id") String id);
    @Select("SELECT * FROM his_plans WHERE usr_id = #{id} ORDER BY date;")
    List<Plan> getHisPlans(@Param("id") String id);
    @Insert("INSERT INTO plans (plan_id , usr_id, content, link, date) VALUES (#{plan_id},#{id}, #{content}, #{link}, #{date});")
    void addPlan( @Param("plan_id")String plan_id,@Param("id") String id, @Param("content") String content, @Param("link") String link, @Param("date") String date);
    @Update("UPDATE plans SET content= #{new_content}, link= #{new_link}, date=#{new_date}  WHERE plan_id=#{plan_id};")
    void resetPlan(@Param("plan_id") String plan_id, @Param("new_content") String new_content, @Param("new_link") String new_link,  @Param("new_date") String new_date);

    @Delete("DELETE FROM plans WHERE plan_id=#{plan_id}")
    void deletePlan(@Param("plan_id") String plan_id);

    @Insert("INSERT INTO his_plans (plan_id , usr_id, content, link, date) VALUES (#{plan_id},#{id}, #{content}, #{link}, #{date});")
    void addHisPlan( @Param("plan_id")String plan_id,@Param("id") String id, @Param("content") String content, @Param("link") String link, @Param("date") String date);

}
