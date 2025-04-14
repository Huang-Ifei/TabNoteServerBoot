package com.tabnote.server.tabnoteserverboot.mappers;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.HashMap;
@Mapper
public interface ImageMapper {
    String selectImageByTabNoteId(@Param("tabNoteId") String tabNoteId);
    void insertImage(@Param("tabNoteId") String tabNoteId, @Param("image") String image);
}
