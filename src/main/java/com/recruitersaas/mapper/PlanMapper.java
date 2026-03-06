package com.recruitersaas.mapper;

import com.recruitersaas.dto.request.PlanRequest;
import com.recruitersaas.dto.response.PlanResponse;
import com.recruitersaas.model.Plan;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface PlanMapper {

    PlanResponse toResponse(Plan plan);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Plan toEntity(PlanRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    void updateEntity(PlanRequest request, @MappingTarget Plan plan);
}
