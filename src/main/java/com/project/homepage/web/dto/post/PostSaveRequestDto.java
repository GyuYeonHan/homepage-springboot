package com.project.homepage.web.dto.post;

import com.project.homepage.domain.post.Post;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class PostSaveRequestDto {

    @NotBlank
    private String title;

    @NotBlank
    private String content;

    public Post toEntity() {
        return Post.builder()
                .title(title)
                .content(content)
                .build();
    }
}
