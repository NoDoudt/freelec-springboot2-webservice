package com.jojoldu.book.springboot.web.dto;

import com.jojoldu.book.springboot.domain.posts.Posts;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PostsSaveResponseDto {
    private Long id;

    @Builder
    public PostsSaveResponseDto(Long id) {
        this.id = id;
    }
}
