package com.example.todoapp.api;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.example.todoapp.Todo;
import com.example.todoapp.TodoService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TodoApiController {

    private final TodoService todoService;

    public TodoApiController(TodoService todoService) {
        this.todoService = todoService;
    }

    @GetMapping("/api/todos")
    public List<TodoDto> listTodos(@RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String order,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return todoService.search(keyword, category, order, from, to).stream()
                .map(TodoDto::from)
                .toList();
    }

    @GetMapping(value = "/api/todos.csv", produces = "text/csv")
    public ResponseEntity<byte[]> exportTodosCsv(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String order) {
        List<Todo> todos = todoService.search(keyword, category, order);
        StringBuilder csv = new StringBuilder();
        appendCsvRow(csv, "やること", "印", "ジャンル", "優先度", "期限", "状態", "メモ");
        for (Todo todo : todos) {
            appendCsvRow(csv,
                    todo.getTitle(),
                    Boolean.TRUE.equals(todo.getPinned()) ? "あり" : "なし",
                    todo.getCategory(),
                    priorityLabel(todo.getPriority()),
                    todo.getDueDate() == null ? "" : todo.getDueDate().toString(),
                    Boolean.TRUE.equals(todo.getCompleted()) ? "完了" : "未完了",
                    todo.getDetail());
        }

        byte[] body = ("\uFEFF" + csv).getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"todos.csv\"")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(body);
    }

    @GetMapping("/api/todos/{id}")
    public ResponseEntity<?> getTodo(@PathVariable Long id) {
        Todo todo = todoService.findById(id);
        if (todo == null) {
            return notFound(id);
        }
        return ResponseEntity.ok(TodoDto.from(todo));
    }

    @PostMapping("/api/todos")
    public ResponseEntity<?> createTodo(@Valid @RequestBody TodoRequest request, BindingResult bindingResult,
            HttpServletRequest httpRequest) {
        if (bindingResult.hasErrors()) {
            return badRequest(bindingResult, httpRequest);
        }

        Todo todo = request.toTodo();
        int createdCount = todoService.create(todo);
        if (createdCount <= 0 || todo.getId() == null) {
            throw new IllegalStateException("Todo could not be created");
        }

        Todo createdTodo = todoService.findById(todo.getId());
        if (createdTodo == null) {
            throw new IllegalStateException("Created todo could not be found");
        }

        return ResponseEntity.created(URI.create("/api/todos/" + todo.getId()))
                .body(TodoDto.from(createdTodo));
    }

    @PutMapping("/api/todos/{id}")
    public ResponseEntity<?> replaceTodo(@PathVariable Long id, @Valid @RequestBody TodoRequest request,
            BindingResult bindingResult, HttpServletRequest httpRequest) {
        if (bindingResult.hasErrors()) {
            return badRequest(bindingResult, httpRequest);
        }

        if (todoService.findById(id) == null) {
            return notFound(id);
        }

        Todo todo = request.toTodo();
        todo.setId(id);
        if (todoService.update(todo) == 0) {
            return notFound(id);
        }

        Todo updatedTodo = todoService.findById(id);
        if (updatedTodo == null) {
            return notFound(id);
        }
        return ResponseEntity.ok(TodoDto.from(updatedTodo));
    }

    @DeleteMapping("/api/todos/{id}")
    public ResponseEntity<?> deleteTodo(@PathVariable Long id) {
        if (todoService.findById(id) == null) {
            return notFound(id);
        }
        if (todoService.delete(id) == 0) {
            return notFound(id);
        }
        return ResponseEntity.noContent().build();
    }

    private ResponseEntity<ProblemDetail> notFound(Long id) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, "Todo not found");
        problem.setInstance(URI.create("/api/todos/" + id));
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problem);
    }

    private void appendCsvRow(StringBuilder csv, String... values) {
        for (int i = 0; i < values.length; i++) {
            if (i > 0) {
                csv.append(',');
            }
            String value = spreadsheetSafe(values[i]).replace("\"", "\"\"");
            csv.append('"').append(value).append('"');
        }
        csv.append("\r\n");
    }

    private String spreadsheetSafe(String value) {
        if (value == null) {
            return "";
        }
        int firstText = 0;
        while (firstText < value.length()
                && (Character.isWhitespace(value.charAt(firstText))
                        || Character.isISOControl(value.charAt(firstText)))) {
            firstText++;
        }
        if (firstText < value.length() && "=+-@".indexOf(value.charAt(firstText)) >= 0) {
            return "'" + value;
        }
        return value;
    }

    private String priorityLabel(Integer priority) {
        if (priority == null) {
            return "";
        }
        return switch (priority) {
            case 1 -> "高";
            case 2 -> "中";
            default -> "低";
        };
    }

    private ResponseEntity<ProblemDetail> badRequest(BindingResult bindingResult, HttpServletRequest httpRequest) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "入力に誤りがあります");
        String instance = httpRequest.getRequestURI();
        if (httpRequest.getQueryString() != null) {
            instance += "?" + httpRequest.getQueryString();
        }
        problem.setInstance(URI.create(instance));

        Map<String, String> errors = new LinkedHashMap<>();
        for (FieldError fieldError : bindingResult.getFieldErrors()) {
            String field = fieldError.getField();
            String message = fieldError.getDefaultMessage();
            if (!errors.containsKey(field) || "やることを入力してください".equals(message)) {
                errors.put(field, message);
            }
        }
        problem.setProperty("errors", errors.entrySet().stream()
                .map(error -> Map.of("field", error.getKey(), "message", error.getValue()))
                .toList());
        return ResponseEntity.badRequest().body(problem);
    }
}
