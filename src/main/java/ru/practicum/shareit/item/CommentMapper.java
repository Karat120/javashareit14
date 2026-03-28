package ru.practicum.shareit.item;

import lombok.experimental.UtilityClass;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.model.Comment;

@UtilityClass
public class CommentMapper {
    public static CommentDto toCommentDto(Comment entity) {
        return CommentDto.builder()
                .id(entity.getId())
                .text(entity.getText())
                .authorName(entity.getAuthor().getName())
                .created(entity.getCreated())
                .build();
    }

    public static Comment toComment(CommentDto dto) {
        return Comment.builder()
                .id(dto.getId())
                .text(dto.getText())
                .build();
    }
}