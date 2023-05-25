package ru.mukminov.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.mukminov.models.Book;
import ru.mukminov.models.Person;
import ru.mukminov.repositories.BooksRepository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class BooksService {

    private final BooksRepository booksRepository;

    @Autowired
    public BooksService(BooksRepository booksRepository) {
        this.booksRepository = booksRepository;
    }

    public List<Book> findAll(boolean sortByYear) {
        if (sortByYear) {
            return booksRepository.findAll(Sort.by("year"));
        } else {
            return booksRepository.findAll();
        }
    }

    public Book findOne(int id) {
        Optional<Book> foundBook = booksRepository.findById(id);
        return foundBook.orElse(null);
    }

    @Transactional
    public void save(Book book) {
        booksRepository.save(book);
    }

    @Transactional
    public void update(int id, Book updatedBook) {
        updatedBook.setId(id);
        booksRepository.save(updatedBook);
    }

    @Transactional
    public void delete(int id) {
        booksRepository.deleteById(id);
    }

    @Transactional
    public void assign(int id, Person selectedPerson) {
        Book book = findOne(id);
        book.setOwner(selectedPerson);
        book.setAssignAt(new Date());
        save(book);
    }

    @Transactional
    public void release(int id) {
        Book book = findOne(id);
        book.setOwner(null);
        book.setAssignAt(null);
        save(book);
    }

    public Person getBookOwner(int id) {
        return findOne(id).getOwner();
    }

    // пагинация + сортировка:
    public List<Book> pageableSortByYear(int page, int itemsPerPage, boolean sortByYear) {
        if (sortByYear) {
            return booksRepository.findAll(PageRequest.of(page, itemsPerPage, Sort.by("year"))).getContent();
        } else {
            return booksRepository.findAll(PageRequest.of(page, itemsPerPage)).getContent();
        }
    }

    // функция поиска книги по названию:
    public List<Book> searchByTitleStartingWith(String startingWith) {
        return booksRepository.findByTitleStartingWith(startingWith);
    }
}
