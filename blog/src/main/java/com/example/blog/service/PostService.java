package com.example.blog.service;
import com.example.blog.dto.PostDto;
import com.example.blog.entity.Post;
import com.example.blog.entity.Tag;
import com.example.blog.repository.PostRepository;
import com.example.blog.repository.TagRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class PostService {

    private final PostRepository repository;
    private final TagRepository tagRepository;

    public PostService(PostRepository repository, TagRepository tagRepository) {
        this.repository = repository;
        this.tagRepository = tagRepository;
    }
    @Transactional
    public PostDto createPost(PostDto postDto) {
        Post post = new Post();
        post.setTitle(postDto.getTitle());
        post.setContent(postDto.getContent());

        // NOVO: Processa as tags se existirem
        if (postDto.getTags() != null && !postDto.getTags().isEmpty()) {
            Set<Tag> tags = processTagsFromDto(postDto.getTags());
            post.setTags(tags);
        }

        Post savedPost = repository.save(post);
        return new PostDto(savedPost.getId(), savedPost.getTitle(), savedPost.getContent(), savedPost.getCreatedAt());
    }

    public List<PostDto> findAll() {
        List<Post> posts = repository.findAll();
        return posts.stream()
                .map(post -> new PostDto(post.getId(), post.getTitle(), post.getContent(), post.getCreatedAt()))
                .toList();
    }

    public List<PostDto>findAllByOrderByDate() {
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        List<Post> posts = repository.findAll(sort);
        return posts.stream()
                .map(post -> new PostDto(post.getId(), post.getTitle(), post.getContent(), post.getCreatedAt()))
                .toList();
    }

    public PostDto findById(Long id) {
        Post post = repository.findById(id).orElseThrow(() -> new RuntimeException("Post not found"));
        return convertToDto(post);
    }
    @Transactional
    public PostDto updatePost(Long id, PostDto postDto) {
        Post post = repository.findById(id).orElseThrow(() -> new RuntimeException("Post not found"));
        post.setTitle(postDto.getTitle());
        post.setContent(postDto.getContent());
        if (postDto.getTags() != null) {
            post.getTags().clear();
            if (!postDto.getTags().isEmpty()) {
                Set<Tag> tags = processTagsFromDto(postDto.getTags());
                post.setTags(tags);
            }
        }

        Post updatedPost = repository.save(post);
        return convertToDto(updatedPost);
    }

    public void deletePost(Long id) {
        repository.deleteById(id);
    }

    //Método auxiliar para processar tags do DTO
    private Set<Tag> processTagsFromDto(Set<String> tagNames) {
        Set<Tag> tags = new HashSet<>();

        for (String tagName : tagNames) {
            String normalizedName = tagName.trim().toLowerCase();
            if (!normalizedName.isEmpty()) {
                Tag tag = tagRepository.findByName(normalizedName)
                        .orElseGet(() -> {
                            Tag newTag = new Tag(normalizedName);
                            return tagRepository.save(newTag);
                        });
                tags.add(tag);
            }
        }

        return tags;
    }

    //Método auxiliar para converter Post em PostDto (com tags)
    private PostDto convertToDto(Post post) {
        Set<String> tagNames = post.getTags().stream()
                .map(Tag::getName)
                .collect(Collectors.toSet());

        PostDto dto = new PostDto(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                post.getCreatedAt()
        );
        dto.setTags(tagNames);

        return dto;
    }
}


