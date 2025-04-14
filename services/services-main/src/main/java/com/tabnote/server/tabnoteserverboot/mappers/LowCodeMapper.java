package com.tabnote.server.tabnoteserverboot.mappers;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface LowCodeMapper {
    @Insert("insert into low_code VALUES(#{lc_id},#{usr_id},#{type},#{fp},CURRENT_TIMESTAMP)")
    void insertLowCode(@Param("lc_id")String lc_id,@Param("usr_id")String usr_id,@Param("type")String type,@Param("fp")String fp);

    @Select("select file_path from low_code where lc_id=#{lc_id}")
    String getLowCode(@Param("lc_id")String lc_id);

    @Select("select lc_id from low_code where usr_id=#{usr_id} and type=#{type} and file_path=#{fp}")
    String isDone(@Param("usr_id")String usr_id,@Param("type")String type,@Param("fp")String fp);
}
