package com.example.blog.controller;

import com.example.blog.dto.PostDto;
import com.example.blog.service.PostService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final PostService postService;

    public AdminController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping
    public String showAdminPage(Model model) {
        model.addAttribute("postDto", new PostDto());
        List<PostDto> posts = postService.findAllByOrderByDate();
        model.addAttribute("posts", posts);
        model.addAttribute("editMode", false);
        return "admin-page";
    }

    // Criar post via formulário
    @PostMapping("/create")
    public String createPost(@ModelAttribute PostDto postDto,
                             @RequestParam(value = "tagsInput", required = false) String tagsInput,
                             RedirectAttributes redirectAttributes) {

        //Processa as tags separadas por vírgula
        if (tagsInput != null && !tagsInput.trim().isEmpty()) {
            Set<String> tags = Arrays.stream(tagsInput.split(","))
                    .map(String::trim)
                    .filter(tag -> !tag.isEmpty())
                    .collect(Collectors.toSet());
            postDto.setTags(tags);
        }

        postService.createPost(postDto);
        redirectAttributes.addFlashAttribute("message", "Post criado com sucesso!");
        redirectAttributes.addFlashAttribute("messageType", "success");
        return "redirect:/admin";
    }

    // Exibir formulário de edição
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        PostDto post = postService.findById(id);
        List<PostDto> posts = postService.findAll();

        // Converte tags para string separada por vírgula para exibir no form
        String tagsString = post.getTags() != null && !post.getTags().isEmpty()
                ? String.join(", ", post.getTags())
                : "";

        model.addAttribute("postDto", post);
        model.addAttribute("posts", posts);
        model.addAttribute("editMode", true);
        model.addAttribute("editingId", id);
        model.addAttribute("tagsString", tagsString);

        return "admin-page";
    }

    // Atualizar post
    @PostMapping("/update/{id}")
    public String updatePost(@PathVariable Long id,
                             @ModelAttribute PostDto postDto,
                             @RequestParam(value = "tagsInput", required = false) String tagsInput,
                             RedirectAttributes redirectAttributes) {

        //Processa as tags separadas por vírgula
        if (tagsInput != null && !tagsInput.trim().isEmpty()) {
            Set<String> tags = Arrays.stream(tagsInput.split(","))
                    .map(String::trim)
                    .filter(tag -> !tag.isEmpty())
                    .collect(Collectors.toSet());
            postDto.setTags(tags);
        } else {
            postDto.setTags(new HashSet<>());
        }

        postService.updatePost(id, postDto);
        redirectAttributes.addFlashAttribute("message", "Post atualizado com sucesso!");
        redirectAttributes.addFlashAttribute("messageType", "success");
        return "redirect:/admin";
    }

    // Deletar post
    @PostMapping("/delete/{id}")
    public String deletePost(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            postService.deletePost(id);
            redirectAttributes.addFlashAttribute("message", "Post deletado com sucesso!");
            redirectAttributes.addFlashAttribute("messageType", "success");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", "Erro ao deletar post!");
            redirectAttributes.addFlashAttribute("messageType", "error");
        }
        return "redirect:/admin";
    }
}