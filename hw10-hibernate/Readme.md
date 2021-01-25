Использование Hibernate
Цель: На практике освоить основы Hibernate.
Понять как аннотации-hibernate влияют на формирование sql-запросов.
Работа должна использовать базу данных в docker-контейнере .

Возьмите за основу предыдущее ДЗ (Самодельный ORM),
используйте предложенный на вебинаре api (пакет ru.otus.core, вебинар про Hibernate).
и реализуйте функционал сохранения и чтения объекта User через Hibernate.
(Рефлексия больше не нужна)
Конфигурация Hibernate должна быть вынесена в файл.

Добавьте в User поля:
адрес (OneToOne)
class AddressDataSet {
private String street;
}
и телефон (OneToMany)
class PhoneDataSet {
private String number;
}

Разметьте классы таким образом, чтобы при сохранении/чтении объека User каскадно сохранялись/читались вложенные объекты.

ВАЖНО.
1) Hibernate должен создать только три таблицы: для телефонов, адресов и пользователей.
2) При сохранении нового объекта не должно быть update-ов.
Посмотрите в логи и проверьте, что эти два требования выполняются.