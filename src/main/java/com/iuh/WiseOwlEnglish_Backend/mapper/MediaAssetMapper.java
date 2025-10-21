package com.iuh.WiseOwlEnglish_Backend.mapper;

import com.iuh.WiseOwlEnglish_Backend.dto.respone.MediaAssetDTORes;
import com.iuh.WiseOwlEnglish_Backend.model.MediaAsset;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import java.util.List;
@Mapper(componentModel = "spring")
public interface MediaAssetMapper {

    // Nếu project có @MapperConfig(ignoreByDefault = true) đang áp vào, hãy dùng BeanMapping dưới đây
    @BeanMapping(ignoreByDefault = false) // đảm bảo không bỏ qua field
    @Mappings({
            @Mapping(target = "id",              source = "id"),
            @Mapping(target = "url",             source = "url"),
            @Mapping(target = "mediaType",       source = "mediaType"),
            @Mapping(target = "altText",         source = "altText"),
            @Mapping(target = "durationSec",     source = "durationSec"),
            @Mapping(target = "storageProvider", source = "storageProvider"),
            @Mapping(target = "publicId",        source = "publicId"),
            @Mapping(target = "tag",             source = "tag"),
    })
    MediaAssetDTORes toDto(MediaAsset entity);

    List<MediaAssetDTORes> toDtoList(List<MediaAsset> entities);
}
