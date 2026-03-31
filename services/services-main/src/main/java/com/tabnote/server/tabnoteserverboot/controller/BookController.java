package com.tabnote.server.tabnoteserverboot.controller;

import com.alibaba.fastjson2.JSONObject;
import com.tabnote.server.tabnoteserverboot.component.TabNoteInfiniteEncryption;
import com.tabnote.server.tabnoteserverboot.services.inteface.BookServiceInterface;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@Controller
public class BookController {

    private static final Logger log = LoggerFactory.getLogger(BookController.class);

    @Autowired
    private BookServiceInterface bookService;

    @Autowired
    private TabNoteInfiniteEncryption tabNoteInfiniteEncryption;

    @PostMapping("bookAdd")
    public ResponseEntity<String> addBook(@RequestBody String requestBody, HttpServletRequest request) {
        log.info(tabNoteInfiniteEncryption.proxyGetIp(request) + "book_add");
        try {
            JSONObject jsonObject = JSONObject.parseObject(requestBody);
            String usr_id = jsonObject.getString("usr_id");
            String book_name = jsonObject.getString("book_name");
            String author = jsonObject.getString("author");
            String description = jsonObject.getString("description");
            String cover_image = jsonObject.getString("cover_image");

            return sendMes(bookService.addBook(usr_id, book_name, author, description, cover_image));
        } catch (Exception e) {
            log.error("Add book error: {}", e.getMessage());
            return sendErr();
        }
    }

    @PostMapping("bookDelete")
    public ResponseEntity<String> deleteBook(@RequestBody String requestBody, HttpServletRequest request) {
        log.info(tabNoteInfiniteEncryption.proxyGetIp(request) + "book_delete");
        try {
            JSONObject jsonObject = JSONObject.parseObject(requestBody);
            String book_id = jsonObject.getString("book_id");
            String usr_id = jsonObject.getString("usr_id");

            return sendMes(bookService.deleteBook(book_id, usr_id));
        } catch (Exception e) {
            log.error("Delete book error: {}", e.getMessage());
            return sendErr();
        }
    }

    @PostMapping("bookUpdate")
    public ResponseEntity<String> updateBook(@RequestBody String requestBody, HttpServletRequest request) {
        log.info(tabNoteInfiniteEncryption.proxyGetIp(request) + "book_update");
        try {
            JSONObject jsonObject = JSONObject.parseObject(requestBody);
            String book_id = jsonObject.getString("book_id");
            String usr_id = jsonObject.getString("usr_id");
            String book_name = jsonObject.getString("book_name");
            String author = jsonObject.getString("author");
            String description = jsonObject.getString("description");
            String cover_image = jsonObject.getString("cover_image");

            return sendMes(bookService.updateBook(book_id, usr_id, book_name, author, description, cover_image));
        } catch (Exception e) {
            log.error("Update book error: {}", e.getMessage());
            return sendErr();
        }
    }

    @PostMapping("bookDetail")
    public ResponseEntity<String> getBookDetail(@RequestBody String requestBody, HttpServletRequest request) {
        log.info(tabNoteInfiniteEncryption.proxyGetIp(request) + "book_detail");
        try {
            JSONObject jsonObject = JSONObject.parseObject(requestBody);
            String book_id = jsonObject.getString("book_id");
            String usr_id = jsonObject.getString("usr_id");

            return sendMes(bookService.getBookDetail(book_id, usr_id));
        } catch (Exception e) {
            log.error("Get book detail error: {}", e.getMessage());
            return sendErr();
        }
    }

    @PostMapping("bookList")
    public ResponseEntity<String> getBookList(@RequestBody String requestBody, HttpServletRequest request) {
        log.info(tabNoteInfiniteEncryption.proxyGetIp(request) + "book_list");
        try {
            JSONObject jsonObject = JSONObject.parseObject(requestBody);
            String usr_id = jsonObject.getString("usr_id");
            Integer page = jsonObject.getInteger("page");
            if (page == null || page <= 0) {
                page = 1;
            }

            return sendMes(bookService.getBookList(usr_id, page));
        } catch (Exception e) {
            log.error("Get book list error: {}", e.getMessage());
            return sendErr();
        }
    }

    @PostMapping("bookContentInsert")
    public ResponseEntity<String> insertBookContent(@RequestBody String requestBody, HttpServletRequest request) {
        log.info(tabNoteInfiniteEncryption.proxyGetIp(request) + "book_content_insert");
        try {
            JSONObject jsonObject = JSONObject.parseObject(requestBody);
            String book_id = jsonObject.getString("book_id");
            String book_name = jsonObject.getString("book_name");
            String text = jsonObject.getString("text");

            return sendMes(bookService.insertBookContent(book_id, book_name, text));
        } catch (Exception e) {
            log.error("Insert book content error: {}", e.getMessage());
            return sendErr();
        }
    }

    @PostMapping("bookContentInsertWithParagraphs")
    public ResponseEntity<String> insertBookContentWithParagraphs(@RequestBody String requestBody, HttpServletRequest request) {
        log.info(tabNoteInfiniteEncryption.proxyGetIp(request) + "book_content_insert_with_paragraphs");
        try {
            JSONObject jsonObject = JSONObject.parseObject(requestBody);
            String book_id = jsonObject.getString("book_id");
            String book_name = jsonObject.getString("book_name");
            String text = jsonObject.getString("text");

            return sendMes(bookService.insertBookContentWithParagraphs(book_id, book_name, text));
        } catch (Exception e) {
            log.error("Insert book content with paragraphs error: {}", e.getMessage());
            return sendErr();
        }
    }

    @PostMapping("bookContentSearch")
    public ResponseEntity<String> searchBookContent(@RequestBody String requestBody, HttpServletRequest request) {
        log.info(tabNoteInfiniteEncryption.proxyGetIp(request) + "book_content_search");
        try {
            JSONObject jsonObject = JSONObject.parseObject(requestBody);
            String book_name = jsonObject.getString("book_name");
            String text = jsonObject.getString("text");
            Integer limit = jsonObject.getInteger("limit");
            Integer minDistance = jsonObject.getInteger("minDistance");

            return sendMes(bookService.searchBookContent(book_name, text, limit, minDistance));
        } catch (Exception e) {
            log.error("Search book content error: {}", e.getMessage());
            return sendErr();
        }
    }

    private ResponseEntity<String> sendErr() {
        return ResponseEntity.badRequest().body("err");
    }

    private ResponseEntity<String> sendMes(JSONObject sendJSON) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(sendJSON.toString());
    }
}