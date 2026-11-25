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

    public List<Todo> searchPage(String keyword, String category, String order, int limit, int offset,
            boolean trash) {
        return todoMapper.searchPage(keyword, category, order, limit, offset, trash);
    }

    public int countSearch(String keyword, String category, boolean trash) {
        return todoMapper.countSearch(keyword, category, trash);
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

    public int restore(Long id) {
        int result = todoMapper.restoreById(id);
        log.info("Todo restore completed: id={}", id);
        return result;
    }

    public int togglePinned(Long id) {
        int result = todoMapper.togglePinned(id);
        log.info("Todo pin toggled: id={}", id);
        return result;
    }
}

