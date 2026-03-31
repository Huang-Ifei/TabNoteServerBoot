package com.tabnote.server.tabnoteserverboot.mappers;

import com.tabnote.server.tabnoteserverboot.models.Book;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface BookMapper {

    @Insert("insert into books (book_id, usr_id, book_name, author, description, cover_image, create_time, update_time, display) values (#{0}, #{1}, #{2}, #{3}, #{4}, #{5}, #{6}, #{7}, #{8})")
    void insertBook(@Param("0") String book_id, @Param("1") String usr_id, @Param("2") String book_name, @Param("3") String author, @Param("4") String description, @Param("5") String cover_image, @Param("6") String create_time, @Param("7") String update_time, @Param("8") Integer display);

    @Delete("delete from books where book_id = #{0}")
    void deleteBook(@Param("0") String book_id);

    @Update("update books set book_name = #{1}, author = #{2}, description = #{3}, cover_image = #{4}, update_time = #{5} where book_id = #{0}")
    void updateBook(@Param("0") String book_id, @Param("1") String book_name, @Param("2") String author, @Param("3") String description, @Param("4") String cover_image, @Param("5") String update_time);

    @Select("select * from books where book_id = #{0}")
    Book getBookById(@Param("0") String book_id);

    @Select("select * from books where usr_id = #{usr_id} order by create_time desc limit #{limit} offset #{offset}")
    List<Book> getBooksByUserId(@Param("usr_id") String usr_id, @Param("offset") int offset, @Param("limit") int limit);

    @Select("select count(*) from books where usr_id = #{usr_id}")
    Integer countBooksByUserId(@Param("usr_id") String usr_id);

    @Select("select count(*) from books")
    Integer getTotalBookCount();
}
