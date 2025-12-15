package com.group1.career.controller;

import com.group1.career.common.Result;
import com.group1.career.model.entity.CareerNode;
import com.group1.career.model.entity.CareerPath;
import com.group1.career.model.entity.UserCareerProgress;
import com.group1.career.service.CareerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Career API")
@RestController
@RequestMapping("/api/careers")
@RequiredArgsConstructor
public class CareerController {

    private final CareerService careerService;

    @Operation(summary = "Get all career paths")
    @GetMapping("/paths")
    public Result<List<CareerPath>> getAllPaths() {
        List<CareerPath> paths = careerService.getAllPaths();
        return Result.success(paths);
    }

    @Operation(summary = "Get career path by ID")
    @GetMapping("/paths/{pathId}")
    public Result<CareerPath> getPath(@PathVariable Integer pathId) {
        CareerPath path = careerService.getPathById(pathId);
        return Result.success(path);
    }

    @Operation(summary = "Get nodes for a career path")
    @GetMapping("/paths/{pathId}/nodes")
    public Result<List<CareerNode>> getPathNodes(@PathVariable Integer pathId) {
        List<CareerNode> nodes = careerService.getPathNodes(pathId);
        return Result.success(nodes);
    }

    @Operation(summary = "Get user's career progress")
    @GetMapping("/progress/{userId}")
    public Result<List<UserCareerProgress>> getUserProgress(@PathVariable Long userId) {
        List<UserCareerProgress> progress = careerService.getUserProgress(userId);
        return Result.success(progress);
    }

    @Operation(summary = "Unlock a node for user")
    @PostMapping("/progress/unlock")
    public Result<String> unlockNode(@RequestBody UnlockNodeRequest request) {
        careerService.unlockNode(request.getUserId(), request.getNodeId());
        return Result.success("Node unlocked successfully");
    }

    @Operation(summary = "Complete a node for user")
    @PostMapping("/progress/complete")
    public Result<String> completeNode(@RequestBody CompleteNodeRequest request) {
        careerService.completeNode(request.getUserId(), request.getNodeId());
        return Result.success("Node completed successfully");
    }

    @Operation(summary = "Initialize default career paths (for testing)")
    @PostMapping("/initialize")
    public Result<String> initializePaths() {
        careerService.initializeDefaultPaths();
        return Result.success("Career paths initialized successfully");
    }

    // ================= DTO Classes =================

    @Data
    public static class UnlockNodeRequest {
        private Long userId;
        private Long nodeId;
    }

    @Data
    public static class CompleteNodeRequest {
        private Long userId;
        private Long nodeId;
    }
}

