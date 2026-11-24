package com.example.todoapp;

import java.time.LocalDate;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class TodoService {

    private final TodoMapper todoMapper;

    public TodoService(TodoMapper todoMapper) {
        this.todoMapper = todoMapper;
    }

    public List<Todo> search(String keyword, String category, String order) {
        return search(keyword, category, order, null, null);
    }

    public List<Todo> search(String keyword, String category, String order, LocalDate from, LocalDate to) {
        return todoMapper.search(keyword, category, order, from, to);
    }

    public Todo findById(Long id) {
        return todoMapper.findById(id);
    }

    public int create(Todo todo) {
        int result = todoMapper.insert(todo);
        log.info("Todo create completed: id={}", todo.getId());
        return result;
    }

    public int update(Todo todo) {
        int result = todoMapper.update(todo);
        log.info("Todo update completed: id={}", todo.getId());
        return result;
    }

    public int delete(Long id) {
        int result = todoMapper.deleteById(id);
        log.info("Todo delete completed: id={}", id);
        return result;
    }
}

