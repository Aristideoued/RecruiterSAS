package com.recruitersaas.repository;

import com.recruitersaas.model.ApplicationFile;
import com.recruitersaas.model.enums.FileType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApplicationFileRepository extends JpaRepository<ApplicationFile, String> {

    List<ApplicationFile> findAllByJobApplicationId(String jobApplicationId);

    List<ApplicationFile> findAllByJobApplicationIdAndFileType(String jobApplicationId, FileType fileType);
}
