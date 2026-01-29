package com.application.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import com.application.entity.SctOrientation;

@Repository
public interface SctOrientationRepository extends JpaRepository<SctOrientation, Integer> {

    @Query("SELECT s FROM SctOrientation s WHERE s.cmpsOrientation.cmps_orientation_id = :orientationId AND s.is_active = 1")
    List<SctOrientation> findByCmpsOrientationId(@Param("orientationId") int orientationId);
}
