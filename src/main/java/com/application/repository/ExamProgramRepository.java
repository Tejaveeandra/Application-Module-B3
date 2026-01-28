package com.application.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.application.entity.ExamProgram;

@Repository
public interface ExamProgramRepository extends JpaRepository<ExamProgram, Integer> {

    @Query("SELECT e FROM ExamProgram e WHERE e.stream_id = :streamId")
    List<ExamProgram> findByStream_id(int streamId);

}
