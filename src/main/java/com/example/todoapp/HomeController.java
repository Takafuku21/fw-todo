package com.example.todoapp;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.validation.Valid;

import java.util.List;

@Controller
public class HomeController {

    private static final int PAGE_SIZE = 10;

    private final TodoService todoService;

    public HomeController(TodoService todoService) {
        this.todoService = todoService;
    }

    @GetMapping("/")
    public String todo(Model model) {
        model.addAttribute("title", "ToDo");
        return "index";
    }

    @GetMapping("/todos")
    public String listTodos(@RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false, defaultValue = "asc") String order,
            @RequestParam(required = false, defaultValue = "1") int page,
            @RequestParam(required = false, defaultValue = "0") String trash, Model model) {
        boolean trashView = "1".equals(trash);
        if (category == null || category.isEmpty() || "すべて".equals(category)) {
            category = null;
        }
        if (!"desc".equals(order)) {
            order = "asc";
        }
        int totalCount = todoService.countSearch(keyword, category, trashView);
        int totalPages = (totalCount + PAGE_SIZE - 1) / PAGE_SIZE;
        int currentPage = totalPages == 0 ? 1 : Math.max(1, Math.min(page, totalPages));
        int offset = (currentPage - 1) * PAGE_SIZE;
        List<Todo> todos = todoService.searchPage(keyword, category, order, PAGE_SIZE, offset, trashView);
        model.addAttribute("todos", todos);
        model.addAttribute("keyword", keyword);
        model.addAttribute("category", category == null ? "すべて" : category);
        model.addAttribute("order", order);
        model.addAttribute("currentPage", currentPage);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("trash", trashView);
        return "todos";
    }

    @GetMapping("/todos/new")
    public String newTodo(Model model) {
        model.addAttribute("todo", new Todo());
        return "create";
    }

    @PostMapping("/todos/confirm")
    public String confirmTodo(@Valid @ModelAttribute Todo todo, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            return "create";
        }
        model.addAttribute("todo", todo);
        return "create-confirm";
    }

    @PostMapping("/todos")
    public String createTodo(@ModelAttribute Todo todo, RedirectAttributes redirectAttributes) {
        todoService.create(todo);
        redirectAttributes.addFlashAttribute("message", "登録しました");
        return "redirect:/todos";
    }

    @PostMapping("/todos/new")
    public String rewriteTodo(@ModelAttribute Todo todo, Model model) {
        model.addAttribute("todo", todo);
        return "create";
    }

    @GetMapping("/todos/{id}/edit")
    public String editTodo(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Todo todo = todoService.findById(id);
        if (todo == null) {
            redirectAttributes.addFlashAttribute("message", "見つかりませんでした");
            return "redirect:/todos";
        }
        model.addAttribute("todo", todo);
        model.addAttribute("id", id);
        return "edit";
    }

    @PostMapping("/todos/{id}/confirm")
    public String confirmEditTodo(@PathVariable Long id, @Valid @ModelAttribute Todo todo,
            BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("id", id);
            return "edit";
        }
        model.addAttribute("todo", todo);
        model.addAttribute("id", id);
        return "edit-confirm";
    }

    @PostMapping("/todos/{id}/edit")
    public String rewriteEditTodo(@PathVariable Long id, @ModelAttribute Todo todo, Model model) {
        model.addAttribute("todo", todo);
        model.addAttribute("id", id);
        return "edit";
    }

    @PostMapping("/todos/{id}")
    public String updateTodo(@PathVariable Long id, @ModelAttribute Todo todo,
            RedirectAttributes redirectAttributes) {
        todo.setId(id);
        todoService.update(todo);
        redirectAttributes.addFlashAttribute("message", "保存しました");
        return "redirect:/todos";
    }

    @GetMapping("/todos/{id}/delete")
    public String confirmDeleteTodo(@PathVariable Long id, Model model,
            RedirectAttributes redirectAttributes) {
        Todo todo = todoService.findById(id);
        if (todo == null) {
            redirectAttributes.addFlashAttribute("message", "見つかりませんでした");
            return "redirect:/todos";
        }
        model.addAttribute("todo", todo);
        model.addAttribute("id", id);
        return "delete";
    }

    @PostMapping("/todos/{id}/delete")
    public String deleteTodo(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        todoService.delete(id);
        redirectAttributes.addFlashAttribute("message", "削除しました");
        return "redirect:/todos";
    }

    @PostMapping("/todos/{id}/restore")
    public String restoreTodo(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        todoService.restore(id);
        redirectAttributes.addFlashAttribute("message", "ごみ箱から戻しました");
        return "redirect:/todos?trash=1";
    }

    @PostMapping("/todos/{id}/pin")
    public String togglePinned(@PathVariable Long id,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false, defaultValue = "asc") String order,
            @RequestParam(required = false, defaultValue = "1") int page,
            RedirectAttributes redirectAttributes) {
        todoService.togglePinned(id);
        if (keyword != null && !keyword.isEmpty()) {
            redirectAttributes.addAttribute("keyword", keyword);
        }
        if (category != null && !category.isEmpty() && !"すべて".equals(category)) {
            redirectAttributes.addAttribute("category", category);
        }
        redirectAttributes.addAttribute("order", order);
        redirectAttributes.addAttribute("page", page);
        redirectAttributes.addFlashAttribute("message", "印を切り替えました");
        return "redirect:/todos";
    }
}
