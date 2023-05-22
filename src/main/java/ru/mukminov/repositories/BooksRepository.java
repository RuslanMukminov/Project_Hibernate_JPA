package ru.mukminov.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.mukminov.models.Book;
import ru.mukminov.models.Person;

import java.util.List;
import java.util.Optional;

@Repository
public interface BooksRepository extends JpaRepository<Book, Integer> {
    List<Book> findByOwner(Person owner);

    List<Book> findByTitle(String title);
}
