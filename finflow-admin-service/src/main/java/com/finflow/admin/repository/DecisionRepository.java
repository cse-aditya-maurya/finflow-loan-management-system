package com.finflow.admin.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.finflow.admin.entity.Decision;

public interface DecisionRepository
extends JpaRepository<Decision, Long> {

List<Decision> findByApplicationId(Long id);
}