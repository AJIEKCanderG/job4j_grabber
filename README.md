[![Build Status](https://www.travis-ci.com/AJIEKCanderG/job4j_grabber.svg?branch=master)](https://www.travis-ci.com/AJIEKCanderG/job4j_grabber)
[![codecov](https://codecov.io/gh/AJIEKCanderG/job4j_grabber/branch/master/graph/badge.svg)](https://codecov.io/gh/AJIEKCanderG/job4j_grabber)

## job4j_grabber (Агрегатор вакансий).

Используем Maven, Travis, Jacoco, Checkstyle, Quartz, Jsoup ... 

Приложение собирается в jar.

Описание.
Система запускается по расписанию. Период запуска указывается в настройках - app.properties. 
Первый сайт будет sql.ru. В нем есть раздел job. Программа должна считывать все вакансии относящиеся к Java и записывать их в базу.
Доступ к интерфейсу будет через REST API.
 
Расширение.
1. В проект можно добавить новые сайты без изменения кода.
2. В проекте можно сделать параллельный парсинг сайтов.
