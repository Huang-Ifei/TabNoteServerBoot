package com.tabnote.server.tabnoteserverboot.services;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.tabnote.server.tabnoteserverboot.mappers.BookMapper;
import com.tabnote.server.tabnoteserverboot.models.Book;
import com.tabnote.server.tabnoteserverboot.services.inteface.BookServiceInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class BookServiceImpl implements BookServiceInterface {

    private static final Logger log = LoggerFactory.getLogger(BookServiceImpl.class);

    @Autowired
    private BookMapper bookMapper;

    @Autowired
    private RagService ragService;

    private String getCurrentTimestamp() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    @Override
    public JSONObject addBook(String usr_id, String book_name, String author, String description, String cover_image) {
        JSONObject result = new JSONObject();
        try {
            String book_id = Math.abs(usr_id.hashCode()) + "_" + System.currentTimeMillis();
            String currentTime = getCurrentTimestamp();

            bookMapper.insertBook(book_id, usr_id, book_name, author, description, cover_image, currentTime, currentTime, 0);

            result.put("success", true);
            result.put("book_id", book_id);
            result.put("message", "Book added successfully");
            log.info("Book added: book_id={}, usr_id={}, book_name={}", book_id, usr_id, book_name);
        } catch (Exception e) {
            log.error("Add book failed: {}", e.getMessage());
            result.put("success", false);
            result.put("message", "Failed to add book: " + e.getMessage());
        }
        return result;
    }

    @Override
    public JSONObject deleteBook(String book_id, String usr_id) {
        JSONObject result = new JSONObject();
        try {
            Book book = bookMapper.getBookById(book_id);
            if (book == null) {
                result.put("success", false);
                result.put("message", "Book not found");
                return result;
            }
            if (!book.getUsr_id().equals(usr_id)) {
                result.put("success", false);
                result.put("message", "Permission denied");
                return result;
            }

            bookMapper.deleteBook(book_id);

            result.put("success", true);
            result.put("message", "Book deleted successfully");
            log.info("Book deleted: book_id={}", book_id);
        } catch (Exception e) {
            log.error("Delete book failed: {}", e.getMessage());
            result.put("success", false);
            result.put("message", "Failed to delete book: " + e.getMessage());
        }
        return result;
    }

    @Override
    public JSONObject updateBook(String book_id, String usr_id, String book_name, String author, String description, String cover_image) {
        JSONObject result = new JSONObject();
        try {
            Book book = bookMapper.getBookById(book_id);
            if (book == null) {
                result.put("success", false);
                result.put("message", "Book not found");
                return result;
            }
            if (!book.getUsr_id().equals(usr_id)) {
                result.put("success", false);
                result.put("message", "Permission denied");
                return result;
            }

            String currentTime = getCurrentTimestamp();
            bookMapper.updateBook(book_id, book_name, author, description, cover_image, currentTime);

            result.put("success", true);
            result.put("message", "Book updated successfully");
            log.info("Book updated: book_id={}", book_id);
        } catch (Exception e) {
            log.error("Update book failed: {}", e.getMessage());
            result.put("success", false);
            result.put("message", "Failed to update book: " + e.getMessage());
        }
        return result;
    }

    @Override
    public JSONObject getBookDetail(String book_id, String usr_id) {
        JSONObject result = new JSONObject();
        try {
            Book book = bookMapper.getBookById(book_id);
            if (book == null) {
                result.put("success", false);
                result.put("message", "Book not found");
                return result;
            }
            if (!book.getUsr_id().equals(usr_id)) {
                result.put("success", false);
                result.put("message", "Permission denied");
                return result;
            }

            JSONObject bookJson = new JSONObject();
            bookJson.put("book_id", book.getBook_id());
            bookJson.put("usr_id", book.getUsr_id());
            bookJson.put("book_name", book.getBook_name());
            bookJson.put("author", book.getAuthor());
            bookJson.put("description", book.getDescription());
            bookJson.put("cover_image", book.getCover_image());
            bookJson.put("create_time", book.getCreate_time());
            bookJson.put("update_time", book.getUpdate_time());

            result.put("success", true);
            result.put("book", bookJson);
            log.info("Book detail retrieved: book_id={}", book_id);
        } catch (Exception e) {
            log.error("Get book detail failed: {}", e.getMessage());
            result.put("success", false);
            result.put("message", "Failed to get book detail: " + e.getMessage());
        }
        return result;
    }

    @Override
    public JSONObject getBookList(String usr_id, Integer page) {
        JSONObject result = new JSONObject();
        try {
            int offset = (page - 1) * 20;
            int limit = 20;

            List<Book> books = bookMapper.getBooksByUserId(usr_id, offset, limit);
            int total = bookMapper.countBooksByUserId(usr_id);
            int totalPages = (int) Math.ceil((double) total / limit);

            JSONArray booksArray = new JSONArray();
            for (Book book : books) {
                JSONObject bookJson = new JSONObject();
                bookJson.put("book_id", book.getBook_id());
                bookJson.put("book_name", book.getBook_name());
                bookJson.put("author", book.getAuthor());
                bookJson.put("description", book.getDescription());
                bookJson.put("cover_image", book.getCover_image());
                bookJson.put("create_time", book.getCreate_time());
                booksArray.add(bookJson);
            }

            result.put("success", true);
            result.put("books", booksArray);
            result.put("total", total);
            result.put("page", page);
            result.put("totalPages", totalPages);
            log.info("Book list retrieved: usr_id={}, page={}", usr_id, page);
        } catch (Exception e) {
            log.error("Get book list failed: {}", e.getMessage());
            result.put("success", false);
            result.put("message", "Failed to get book list: " + e.getMessage());
        }
        return result;
    }

    @Override
    public JSONObject insertBookContent(String book_id, String book_name, String text) {
        JSONObject result = new JSONObject();
        try {
            boolean success = ragService.insertContent(book_name, text);
            if (success) {
                result.put("success", true);
                result.put("message", "Content inserted to RAG successfully");
                log.info("Book content inserted to RAG: book_id={}, book_name={}", book_id, book_name);
            } else {
                result.put("success", false);
                result.put("message", "Failed to insert content to RAG");
            }
        } catch (Exception e) {
            log.error("Insert book content to RAG failed: {}", e.getMessage());
            result.put("success", false);
            result.put("message", "Failed to insert content: " + e.getMessage());
        }
        return result;
    }

    @Override
    public JSONObject insertBookContentWithParagraphs(String book_id, String book_name, String text) {
        JSONObject result = new JSONObject();
        try {
            boolean success = ragService.insertContentWithParagraphs(book_name, text);
            if (success) {
                result.put("success", true);
                result.put("message", "Content inserted to RAG successfully with paragraphs");
                log.info("Book content inserted to RAG with paragraphs: book_id={}, book_name={}", book_id, book_name);
            } else {
                result.put("success", false);
                result.put("message", "Failed to insert content to RAG");
            }
        } catch (Exception e) {
            log.error("Insert book content to RAG with paragraphs failed: {}", e.getMessage());
            result.put("success", false);
            result.put("message", "Failed to insert content: " + e.getMessage());
        }
        return result;
    }

    @Override
    public JSONObject searchBookContent(String book_name, String text, Integer limit, Integer minDistance) {
        JSONObject result = new JSONObject();
        try {
            JSONObject searchResult = ragService.searchContent(book_name, text, limit != null ? limit : 1000, minDistance != null ? minDistance : 0);
            result.put("success", true);
            result.put("data", searchResult);
            log.info("Book content searched from RAG: book_name={}", book_name);
        } catch (Exception e) {
            log.error("Search book content from RAG failed: {}", e.getMessage());
            result.put("success", false);
            result.put("message", "Failed to search content: " + e.getMessage());
        }
        return result;
    }
}