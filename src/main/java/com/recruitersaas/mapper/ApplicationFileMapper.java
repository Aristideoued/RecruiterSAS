package com.recruitersaas.mapper;

import com.recruitersaas.dto.response.ApplicationFileResponse;
import com.recruitersaas.model.ApplicationFile;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ApplicationFileMapper {

    @Mapping(target = "downloadUrl", ignore = true)
    ApplicationFileResponse toResponse(ApplicationFile file);

    @AfterMapping
    default void setDownloadUrl(@MappingTarget ApplicationFileResponse response, ApplicationFile file) {
        response.setDownloadUrl("/api/files/" + file.getId());
    }
}
