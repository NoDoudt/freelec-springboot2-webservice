package com.jojoldu.book.springboot.web.dto;

import com.jojoldu.book.springboot.domain.posts.Posts;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PostsUpdateResponseDto {
    private Long id;

    @Builder
    public PostsUpdateResponseDto(Long id) {
        this.id = id;
    }

}
