package ru.itmo.sd.deadliner2bot.model;

public enum ChatStateEnum {
    BASE_STATE,
    SELECT_TODO,
    TODO_SELECTED,
    ADD_TODO_NAME,
    EDIT_TODO,
    ADD_DESCRIPTION,
    ADD_START_DATE,
    ADD_END_DATE,
    SELECT_DAYS,
    SELECT_TIME
}
