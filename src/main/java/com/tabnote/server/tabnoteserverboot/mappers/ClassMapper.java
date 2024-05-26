package com.tabnote.server.tabnoteserverboot.mappers;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.HashMap;
import java.util.List;

public interface ClassMapper {
    @Insert("INSERT INTO classes (class_name) VALUE (#{class_name})")
    void insertClass(@Param("class_name") String classes);
    @Select("SELECT * FROM classes;")
    List<HashMap<String, String>> getClasses();
}
