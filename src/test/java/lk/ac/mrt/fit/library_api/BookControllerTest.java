package lk.ac.mrt.fit.library_api;

import com.fasterxml.jackson.databind.ObjectMapper;
import lk.ac.mrt.fit.library_api.model.Book;
import lk.ac.mrt.fit.library_api.repository.BookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BookRepository bookRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        bookRepository.deleteAll();
    }

    @Test
    void testGetAllBooksEmpty() throws Exception {
        mockMvc.perform(get("/api/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void testCreateBookSuccess() throws Exception {
        Book book = new Book();
        book.setTitle("Clean Code");
        book.setAuthor("Robert C. Martin");
        book.setIsbn("9780132350884");
        book.setPrice(4500.0);

        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(book)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.title", is("Clean Code")))
                .andExpect(jsonPath("$.author", is("Robert C. Martin")))
                .andExpect(jsonPath("$.isbn", is("9780132350884")))
                .andExpect(jsonPath("$.price", is(4500.0)));
    }

    @Test
    void testCreateBookValidationFailure_EmptyTitle() throws Exception {
        Book book = new Book();
        book.setTitle("");
        book.setAuthor("Robert C. Martin");
        book.setIsbn("9780132350884");
        book.setPrice(4500.0);

        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(book)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetBookByIdFound() throws Exception {
        Book book = new Book();
        book.setTitle("The Pragmatic Programmer");
        book.setAuthor("Andy Hunt");
        book.setIsbn("9780201616224");
        book.setPrice(3500.0);
        Book savedBook = bookRepository.save(book);

        mockMvc.perform(get("/api/books/" + savedBook.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(savedBook.getId().intValue())))
                .andExpect(jsonPath("$.title", is("The Pragmatic Programmer")));
    }

    @Test
    void testGetBookByIdNotFound() throws Exception {
        mockMvc.perform(get("/api/books/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testUpdateBookFound() throws Exception {
        Book book = new Book();
        book.setTitle("Refactoring");
        book.setAuthor("Martin Fowler");
        book.setIsbn("9780201485677");
        book.setPrice(4000.0);
        Book savedBook = bookRepository.save(book);

        Book updatedDetails = new Book();
        updatedDetails.setTitle("Refactoring (2nd Edition)");
        updatedDetails.setAuthor("Martin Fowler");
        updatedDetails.setIsbn("9780134757599");
        updatedDetails.setPrice(5000.0);

        mockMvc.perform(put("/api/books/" + savedBook.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(savedBook.getId().intValue())))
                .andExpect(jsonPath("$.title", is("Refactoring (2nd Edition)")))
                .andExpect(jsonPath("$.price", is(5000.0)));
    }

    @Test
    void testUpdateBookNotFound() throws Exception {
        Book updatedDetails = new Book();
        updatedDetails.setTitle("Non-existent Book");
        updatedDetails.setAuthor("Unknown");
        updatedDetails.setIsbn("0000000000");
        updatedDetails.setPrice(100.0);

        mockMvc.perform(put("/api/books/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedDetails)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDeleteBookFound() throws Exception {
        Book book = new Book();
        book.setTitle("Design Patterns");
        book.setAuthor("Gang of Four");
        book.setIsbn("9780201633610");
        book.setPrice(6000.0);
        Book savedBook = bookRepository.save(book);

        mockMvc.perform(delete("/api/books/" + savedBook.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/books/" + savedBook.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDeleteBookNotFound() throws Exception {
        mockMvc.perform(delete("/api/books/999"))
                .andExpect(status().isNotFound());
    }
}
