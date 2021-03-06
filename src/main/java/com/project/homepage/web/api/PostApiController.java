package com.project.homepage.web.api;

import com.project.homepage.domain.post.Post;
import com.project.homepage.domain.post.PostType;
import com.project.homepage.domain.user.Role;
import com.project.homepage.domain.user.User;
import com.project.homepage.service.PostService;
import com.project.homepage.web.dto.comment.CommentResponseDto;
import com.project.homepage.web.dto.post.PostCommentDto;
import com.project.homepage.web.dto.post.PostEditRequestDto;
import com.project.homepage.web.dto.post.PostResponseDto;
import com.project.homepage.web.dto.post.PostSaveRequestDto;
import com.project.homepage.web.login.Login;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/post")
@RequiredArgsConstructor
public class PostApiController {

    private final PostService postService;

    @GetMapping
    public ResponseEntity<List<PostResponseDto>> getAllPostList() {
        List<PostResponseDto> postDtoList = postService.findAllPost().stream()
                .map(PostResponseDto::new)
                .collect(Collectors.toList());

        HttpHeaders headers = new HttpHeaders();

        return new ResponseEntity<>(postDtoList, headers, HttpStatus.OK);
    }

    @GetMapping("/announcement")
    public ResponseEntity<List<PostResponseDto>> getALLAnnouncementPostList() {
        List<PostResponseDto> postDtoList = postService.findAllAnnouncementPost().stream()
                .map(PostResponseDto::new)
                .collect(Collectors.toList());

        HttpHeaders headers = new HttpHeaders();

        return new ResponseEntity<>(postDtoList, headers, HttpStatus.OK);
    }

    @GetMapping("/question")
    public ResponseEntity<List<PostResponseDto>> getAllQuestionPostList() {
        List<PostResponseDto> postDtoList = postService.findAllQuestionPost().stream()
                .map(PostResponseDto::new)
                .collect(Collectors.toList());

        HttpHeaders headers = new HttpHeaders();

        return new ResponseEntity<>(postDtoList, headers, HttpStatus.OK);
    }

    @PostMapping("/announcement")
    public ResponseEntity<Post> saveAnnouncement(@Valid @RequestBody PostSaveRequestDto dto, @Login User user) {
        if (UserNotAuthentication(user)) {
            return new ResponseEntity<>(null, HttpStatus.UNAUTHORIZED);
        }

        Post post = dto.toEntity();
        post.setUser(user);
        post.setType(PostType.ANNOUNCEMENT);
        postService.savePost(post);

        return new ResponseEntity<>(post, HttpStatus.CREATED);
    }

    @PostMapping("/question")
    public ResponseEntity<Post> saveQuestion(@Valid @RequestBody PostSaveRequestDto dto, @Login User user) {
        if (UserNotAuthentication(user)) {
            return new ResponseEntity<>(null, HttpStatus.UNAUTHORIZED);
        }

        Post post = dto.toEntity();
        post.setUser(user);
        post.setType(PostType.QUESTION);
        postService.savePost(post);

        return new ResponseEntity<>(post, HttpStatus.CREATED);
    }

    @GetMapping("/{postId}")
    public ResponseEntity<PostCommentDto> getPost(@PathVariable Long postId) {
        Post post;
        try {
            post = postService.findById(postId);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
        PostResponseDto postDto = new PostResponseDto(post);
        List<CommentResponseDto> commentDtoList = post.getCommentList().stream()
                .map(CommentResponseDto::new)
                .collect(Collectors.toList());

        PostCommentDto data = new PostCommentDto(postDto, commentDtoList);

        return new ResponseEntity<>(data, HttpStatus.OK);
    }

    @PutMapping("/{postId}")
    public ResponseEntity<String> editPost(@PathVariable Long postId, @RequestBody PostEditRequestDto dto, @Login User user) {
        if (UserNotAuthentication(user) || UserNotAuthorization(postId, user))
            return new ResponseEntity<>("You are not authorized", HttpStatus.UNAUTHORIZED);
        postService.edit(postId, dto.getTitle(), dto.getContent());

        return new ResponseEntity<>("Edit Post Success", HttpStatus.OK);
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<String> deletePost(@PathVariable Long postId, @Login User user) {
        if (UserNotAuthentication(user) || UserNotAuthorization(postId, user))
            return new ResponseEntity<>("You are not authorized", HttpStatus.UNAUTHORIZED);

        Post post = postService.findById(postId);
        postService.delete(post);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    private boolean UserNotAuthentication(User user) {
        return user == null;
    }

    private boolean UserNotAuthorization(@PathVariable Long postId, @Login User user) {
        Post post = postService.findById(postId);
        if (user.getRole().equals(Role.ADMIN)) {
            return false;
        }

        return !post.getUser().getId().equals(user.getId());
    }
}
