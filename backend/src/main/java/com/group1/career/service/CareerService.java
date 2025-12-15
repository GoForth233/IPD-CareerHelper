package com.group1.career.service;

import com.group1.career.model.entity.CareerNode;
import com.group1.career.model.entity.CareerPath;
import com.group1.career.model.entity.UserCareerProgress;

import java.util.List;

public interface CareerService {
    /**
     * Get all career paths
     */
    List<CareerPath> getAllPaths();

    /**
     * Get career path by ID
     */
    CareerPath getPathById(Integer pathId);

    /**
     * Get all nodes for a specific path
     */
    List<CareerNode> getPathNodes(Integer pathId);

    /**
     * Get user's career progress
     */
    List<UserCareerProgress> getUserProgress(Long userId);

    /**
     * Unlock a node for user
     */
    void unlockNode(Long userId, Long nodeId);

    /**
     * Complete a node for user
     */
    void completeNode(Long userId, Long nodeId);

    /**
     * Initialize default career paths (for testing)
     */
    void initializeDefaultPaths();
}

