package com.smartwardrobe.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartwardrobe.constant.ErrorCode;
import com.smartwardrobe.dto.TagDTO;
import com.smartwardrobe.entity.Tag;
import com.smartwardrobe.exception.BusinessException;
import com.smartwardrobe.mapper.TagMapper;
import com.smartwardrobe.vo.TagVO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TagService {

    private final TagMapper tagMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String TAG_CACHE_PREFIX = "tag:type:";
    private static final long TAG_CACHE_TTL = 24;

    public List<TagVO> getTagsByType(String type) {
        String cacheKey = TAG_CACHE_PREFIX + type;
        Object cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return (List<TagVO>) cached;
        }

        List<Tag> tags = tagMapper.selectList(
                new LambdaQueryWrapper<Tag>()
                        .eq(Tag::getType, type)
                        .eq(Tag::getDeleted, 0)
                        .orderByAsc(Tag::getSortOrder)
        );

        List<TagVO> result = tags.stream().map(this::toVO).collect(Collectors.toList());
        redisTemplate.opsForValue().set(cacheKey, result, TAG_CACHE_TTL, TimeUnit.HOURS);
        return result;
    }

    public TagVO createTag(TagDTO dto) {
        // 检查同类型下 code 是否重复
        Long count = tagMapper.selectCount(
                new LambdaQueryWrapper<Tag>()
                        .eq(Tag::getType, dto.getType())
                        .eq(Tag::getCode, dto.getCode())
                        .eq(Tag::getDeleted, 0)
        );
        if (count > 0) {
            throw new BusinessException(ErrorCode.TAG_CODE_DUPLICATE.getCode(),
                    ErrorCode.TAG_CODE_DUPLICATE.getMessage());
        }

        Tag tag = new Tag();
        tag.setType(dto.getType());
        tag.setCode(dto.getCode());
        tag.setName(dto.getName());
        tag.setColorHex(dto.getColorHex());
        tag.setSortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0);
        tagMapper.insert(tag);

        evictCache(dto.getType());
        return toVO(tag);
    }

    public TagVO updateTag(Long id, TagDTO dto) {
        Tag tag = getTagOrThrow(id);

        // 如果 code 发生变化，检查新 code 是否重复
        if (!tag.getCode().equals(dto.getCode())) {
            Long count = tagMapper.selectCount(
                    new LambdaQueryWrapper<Tag>()
                            .eq(Tag::getType, tag.getType())
                            .eq(Tag::getCode, dto.getCode())
                            .eq(Tag::getDeleted, 0)
                            .ne(Tag::getId, id)
            );
            if (count > 0) {
                throw new BusinessException(ErrorCode.TAG_CODE_DUPLICATE.getCode(),
                        ErrorCode.TAG_CODE_DUPLICATE.getMessage());
            }
        }

        tag.setCode(dto.getCode());
        tag.setName(dto.getName());
        tag.setColorHex(dto.getColorHex());
        if (dto.getSortOrder() != null) {
            tag.setSortOrder(dto.getSortOrder());
        }
        tagMapper.updateById(tag);

        evictCache(tag.getType());
        return toVO(tag);
    }

    public void deleteTag(Long id) {
        Tag tag = getTagOrThrow(id);
        tagMapper.deleteById(id);
        evictCache(tag.getType());
    }

    private Tag getTagOrThrow(Long id) {
        Tag tag = tagMapper.selectById(id);
        if (tag == null) {
            throw new BusinessException(ErrorCode.TAG_NOT_FOUND.getCode(),
                    ErrorCode.TAG_NOT_FOUND.getMessage());
        }
        return tag;
    }

    private void evictCache(String type) {
        redisTemplate.delete(TAG_CACHE_PREFIX + type);
    }

    private TagVO toVO(Tag tag) {
        TagVO vo = new TagVO();
        vo.setId(tag.getId());
        vo.setType(tag.getType());
        vo.setCode(tag.getCode());
        vo.setName(tag.getName());
        vo.setColorHex(tag.getColorHex());
        vo.setSortOrder(tag.getSortOrder());
        return vo;
    }
}
