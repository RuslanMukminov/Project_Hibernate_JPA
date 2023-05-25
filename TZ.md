# Техническое задание для проекта "цифровой учет книг"

За основу взят [Проект №1](https://github.com/RuslanMukminov/Test_SpringMVC_JDBC-Template), который переписан 
с использованием Hibernate и Spring Data JPA.

## Новый функционал: 
1) [Пагинация для книг](TZ.md#1-пагинация)
2) [Сортировка книг по году](TZ.md#2-сортировка-книг-по-году)
3) [Страница поиска книг](TZ.md#3-страница-поиска-книг)
4) [Автоматическая проверка на то, что человек просрочил возврат книги](TZ.md#4-проверка-на-то-что-человек-просрочил-возврат-книги)

### 1. Пагинация

Метод `index()` в `BooksController` принимает в адресной строке два ключа: `page` и `books_per_page`. 
Первый ключ сообщает, какую страницу мы запрашиваем, а второй ключ сообщает, сколько книг должно быть 
на одной странице. Если в адресной строке не передаются эти ключи, то возвращаются как обычно все книги.

Реализация: с помощью `PageRequest.of()` где эти два ключа передаем в метод `findAll()` в JPA репозитории:
<details>
 <summary>пример вызова метода в сервисе:</summary>

 ```java
	booksRepository.findAll(PageRequest.of(page, itemsPerPage)).getContent();
 ```
</details>

### 2. Сортировка книг по году

Метод `index()` в `BooksController` принимает в адресной строке ключ `sort_by_year`. Если он имеет 
значение `true`, то выдача сортируется по году. Если в адресной строке не передается этот ключ, 
то книги возвращаются в обычном порядке.

Реализация: критерий сортировки задаем с помощью `Sort.by()` и передаем в метод `findAll()` в JPA репозитории:
`booksRepository.findAll(Sort.by("year"));`

### Пагинация и сортировка работают одновременно

<details>
 <summary>пример метода `index()` в `BooksController`:</summary>

 ```java
	@GetMapping()
    public String index(Model model,
                        @RequestParam(required = false, name = "page") Integer page,
                        @RequestParam(required = false, name = "books_per_page") Integer itemPerPage,
                        @RequestParam(required = false, name = "sort_by_year") boolean sortByYear) {

        if (page == null) {
            model.addAttribute("books", booksService.findAll(sortByYear));
        } else {
            model.addAttribute("books", booksService.pageableSortByYear(page, itemPerPage, sortByYear));
        }

        return "books/index";
    }
 ```
</details>

<details>
 <summary>пример метода `pageableSortByYear()` в `BooksService`:</summary>

 ```java
	public List<Book> pageableSortByYear(int page, int itemsPerPage, boolean sortByYear) {
        if (sortByYear) {
            return booksRepository.findAll(PageRequest.of(page, itemsPerPage, Sort.by("year"))).getContent();
        } else {
            return booksRepository.findAll(PageRequest.of(page, itemsPerPage)).getContent();
        }
    }
 ```
</details>

### 3. Страница поиска книг

Поиск книги происходит по начальным буквам названия книги, получаем полное название книги и имя автора. 
Также, если книга сейчас находится у кого-то, получаем имя этого человека.

Реализация функционала поиска: в основе такое свойство JPA репозитория, что запросы к сущности можно 
строить прямо из имени метода; в `BooksRepository` объявлен метод `findByTitleStartingWith()` без 
имплементации; 
<details>
 <summary>пример объявления метода в `BooksRepository`:</summary>

 ```java
	List<Book> findByTitleStartingWith (String startingWith);
 ```
</details>

<details>
 <summary>пример метода `search()` в `BooksController`:</summary>

 ```java
    @GetMapping("/search")
    public String search(@RequestParam(required = false, name = "start_with") String startWith,
        Model model) {
        if (startWith != null) {
            model.addAttribute("books", booksService.searchByTitleStartingWith(startWith));
        }
        return "books/search";
    }
 ```
</details>

### 4. Проверка на то, что человек просрочил возврат книги

Если человек взял книгу более 10 дней назад и до сих пор не вернул ее, эта книга на странице этого человека 
должна подсвечиваться красным цветом.

Реализация: 
* в сущности `Book`: в поле `private Date assignAt;` записываем время когда книгу взяли; в поле 
`private boolean overdue;` - флаг просрочки возврата;
* в методе `getBooksByPersonId()` в `PeopleService` в цикле по полученным книгам меняем флаг `overdue` 
на `true` в случае просрочки возврата;
* для расчета просрочки используется разница между текущей датой и временем взятия книги в милисекундах.
Далее эту разницу можно сразу сравнивать с 10 дней (в милисекундах) или полученную разницу первести в 
количество дней с помощью `TimeUnit.DAYS.convert()`.

<details>
 <summary>пример метода `getBooksByPersonId()` в `PeopleService`:</summary>

 ```java
    public List<Book> getBooksByPersonId(int id) {
        Person owner = findOne(id);
        List<Book> books = booksRepository.findByOwner(owner);
        Date currentDate = new Date();

        for (Book book : books) {
            long diffInMillies = Math.abs(currentDate.getTime() - book.getAssignAt().getTime());
            long diff = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);
            if (diff > 10) {
                book.setOverdue(true);
            }
        }
        return books;
    }
 ```
</details>

