package com.tabnote.server.tabnoteserverboot.mappers;

import org.apache.ibatis.annotations.*;

import java.util.HashMap;
import java.util.List;

@Mapper
public interface AccountMapper {
    @Select("SELECT * FROM user WHERE id = #{id};")
    HashMap<String, String> searchById(@Param("id") String id);
    @Select("SELECT * FROM user WHERE name = #{name};")
    HashMap<String, String> searchByName(@Param("name") String name);
    @Insert("INSERT INTO user (`id`, `password`, `name`, `date`) VALUES (#{id}, #{password}, #{name}, #{date});")
    void signUp(@Param("id") String id, @Param("password") String password, @Param("name") String name, @Param("date") String date);
    @Select("SELECT id FROM tokens WHERE token = #{token};")
    String tokenCheckIn(@Param("token") String token);
    @Insert("INSERT INTO tokens (token,id,date) VALUES (#{token},#{id},#{date});")
    void addToken(@Param("token") String token, @Param("id") String id, @Param("date") String date);
    @Delete("DELETE FROM tokens WHERE token = #{token};")
    void deleteToken(@Param("token") String token);
    @Update("UPDATE user SET name = #{name}  WHERE id =#{id};")
    void resetName(@Param("id") String id, @Param("name") String name);
    @Update("UPDATE user SET id = #{id}  WHERE id =#{old_id};")
    void resetID(@Param("old_id") String old_id, @Param("id") String id);
    @Update("UPDATE user SET password = #{password}  WHERE id = #{id};")
    void resetPassword(@Param("id") String id, @Param("password") String password);
    @Update("UPDATE tokens SET id = #{id} WHERE token = #{token};")
    void resetToken(@Param("id") String id, @Param("token") String token);
    @Select("SELECT date,token FROM tokens WHERE id = #{id}")
    List<HashMap<String,String>> getTokensById(@Param("id")String id);
    @Select("SELECT name FROM user WHERE id = #{id}")
    String getNameById(@Param("id")String id);
}
