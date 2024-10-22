package com.tabnote.server.tabnoteserverboot.mappers;

import com.tabnote.server.tabnoteserverboot.models.Vip;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface VipMapper {

    @Select("select `rank` from vip where start_time<=NOW() and end_time>NOW() and usr_id=#{id}")
    Integer selectRankByUserId(@Param("id")String id);

    @Select("select vip_id,start_time,end_time,quota,`rank` from vip where usr_id=#{id} and end_time>NOW() order by `end_time`")
    List<Vip> selectVipListById(@Param("id")String id);

    //找到最后一个VIP月
    @Select("select end_time from vip where usr_id=#{id} order by end_time desc limit 1")
    String selectEndTimeById(@Param("id")String id);
    //如果最后一个VIP月为空
    @Insert("insert vip VALUES (UUID(),#{id},NOW(),DATE_ADD(NOW(),INTERVAL 1 MONTH),#{quota},#{rank})")
    void addNewVip(@Param("id")String id,@Param("quota")int quota,@Param("rank")int rank);
    //增加一个月份
    @Insert("insert vip VALUES (UUID(),#{id},GREATEST(#{startTime}, NOW()),DATE_ADD(GREATEST(#{startTime}, NOW()),INTERVAL 1 MONTH),#{quota},#{rank})")
    void addVip(@Param("id")String id,@Param("startTime")String startTime,@Param("quota")int quota,@Param("rank")int rank);

    //升级某月的VIP等级
    @Update("update vip set `rank`=#{rank},`quota`=#{quota} where vip_id = #{vip_id}")
    void updateVipRank(@Param("vip_id")String vip_id,@Param("rank")int rank,@Param("quota")int quota);

    @Update("update vip set `quota`=`quota`-#{quota} where usr_id=#{id} and start_time<=NOW() and end_time>NOW()")
    void useQuota(@Param("quota")int quota,@Param("id")String id);

}