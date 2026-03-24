package com.ShreeNagariCRM.Repository;

import com.ShreeNagariCRM.Entity.DynamicData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;


import java.util.List;
import java.util.Optional;

public interface DynamicDataRepostory extends JpaRepository<DynamicData,Long> {


    List<DynamicData> findByFileName(String fileName);

    @Query("SELECT DISTINCT d.fileName FROM DynamicData d")
    List<String> findAllFileNames();

    Optional<DynamicData> findByIdAndFileName(Long id, String fileName);
}
