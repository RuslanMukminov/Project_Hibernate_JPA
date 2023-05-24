package ru.mukminov.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.mukminov.models.Book;
import ru.mukminov.models.Person;
import ru.mukminov.repositories.BooksRepository;
import ru.mukminov.repositories.PeopleRepository;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
@Transactional(readOnly = true)
public class PeopleService {
    private final PeopleRepository peopleRepository;
    private final BooksRepository booksRepository;

    @Autowired
    public PeopleService(PeopleRepository peopleRepository, BooksRepository booksRepository) {
        this.peopleRepository = peopleRepository;
        this.booksRepository = booksRepository;
    }

    public List<Person> findAll() {
        return peopleRepository.findAll();
    }

    public Person findOne(int id) {
        Optional<Person> foundPerson = peopleRepository.findById(id);
        return foundPerson.orElse(null);
    }

    @Transactional
    public void save(Person person) {
        peopleRepository.save(person);
    }

    @Transactional
    public void update(int id, Person updatedPerson) {
        updatedPerson.setId(id);
        peopleRepository.save(updatedPerson);
    }

    @Transactional
    public void delete(int id) {
        peopleRepository.deleteById(id);
    }

    public Optional<Person> findByFullName(String fullName) {
        return peopleRepository.findByFullName(fullName)
                .stream().findAny();
    }

    public List<Book> getBooksByPersonId(int id) {
        Person owner = findOne(id);
        List<Book> books = booksRepository.findByOwner(owner);
        Date currentDate = new Date();

        for (Book book : books) {
            long diffInMillies = Math.abs(currentDate.getTime() - book.getAssignAt().getTime());
            long diff = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);
//            int days = (int) (diff / (1000 * 60 * 60 * 24));
            if (diff > 10) {
                book.setOverdue(true);
            }
        }
        return books;
    }
}
