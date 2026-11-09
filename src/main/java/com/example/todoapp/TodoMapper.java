package com.example.todoapp;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TodoMapper {
    List<Todo> findAll();

    Todo findById(Long id);

    int insert(Todo todo);

    int update(Todo todo);

    int deleteById(Long id);
}
