package com.tabnote.server.tabnoteserverboot.services.inteface;

import com.alibaba.fastjson2.JSONObject;
import org.springframework.transaction.annotation.Transactional;

public interface BookServiceInterface {

    JSONObject addBook(String usr_id, String book_name, String author, String description, String cover_image);

    JSONObject deleteBook(String book_id, String usr_id);

    JSONObject updateBook(String book_id, String usr_id, String book_name, String author, String description, String cover_image);

    JSONObject getBookDetail(String book_id, String usr_id);

    JSONObject getBookList(String usr_id, Integer page);

    JSONObject insertBookContent(String book_id, String book_name, String text);

    JSONObject insertBookContentWithParagraphs(String book_id, String book_name, String text);

    JSONObject searchBookContent(String book_name, String text, Integer limit, Integer minDistance);
}