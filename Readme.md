# Telegram schedule bot

WORK IN PROGRESS

Бот, основная функциональность которого - напонимание о событиях. Данные о событиях хранятся в БД Postgresql.

Реализовано:

* Смена имени
* Создание простого напоминания вводом даты и описания
* Отправка напоминания пользователю в нужное время 

В планах:

* Создание напоминание через промежуток времени
* Создание повторяющегося напоминания

Проверить текущую реализацию можно на боте @SimpleWatchBot

## Особенности запуска

Уже всё настроено для запуска через heroku, см. `Procfile`

Сейчас для запуска необходимо иметь переменные окружения

* `SCHEDULE_BOT_NAME` - название бота
* `SCHEDULE_BOT_TOKEN` - токен бота
* `SCHEDULE_DATABASE_URL` - url подключения к БД вида `jdbc:postgresql://host:port/db`
* `SCHEDULE_DATABASE_USERNAME` - пользователь БД
* `SCHEDULE_DATABASE_PASSWORD"` - пароль пользователя БД

Т.к. в проекте пока не используются миграции и ORM, необходимо создать таблицы вручную

```roomsql
CREATE TABLE public.tbl_reminder
(
    id integer NOT NULL DEFAULT nextval('schedule_id_seq'::regclass),
    creation_time timestamp without time zone NOT NULL,
    reminder_time timestamp without time zone NOT NULL,
    description text COLLATE pg_catalog."default",
    user_id integer,
    completed boolean NOT NULL DEFAULT false,
    CONSTRAINT schedule_pkey PRIMARY KEY (id),
    CONSTRAINT fk_user_id FOREIGN KEY (user_id)
        REFERENCES public.tbl_user (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
        NOT VALID
);

CREATE TABLE public.tbl_user
(
    id integer NOT NULL DEFAULT nextval('user_id_seq'::regclass),
    tg_id integer NOT NULL,
    nickname text COLLATE pg_catalog."default" NOT NULL,
    tg_chat_id bigint,
    CONSTRAINT user_pkey PRIMARY KEY (id)
)

```