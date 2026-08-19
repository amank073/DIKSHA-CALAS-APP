package com.diksha.controller;

import com.diksha.service.engine.ContentRecommender;
import com.diksha.service.engine.Contracts.ContentInput;
import com.diksha.service.engine.Contracts.ContentOutput;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/student/videos")
@CrossOrigin(origins = "*", maxAge = 3600)
public class VideoController {

    private final ContentRecommender contentRecommender;

    public VideoController(ContentRecommender contentRecommender) {
        this.contentRecommender = contentRecommender;
    }

    @GetMapping("/recommend")
    public ResponseEntity<List<ContentOutput>> recommendVideos(
            @RequestParam String topicName,
            @RequestParam(required = false) String subjectName,
            @RequestParam(required = false) String examType) {
        
        ContentInput input = new ContentInput(null, topicName, subjectName, examType);
        List<ContentOutput> recommendations = contentRecommender.recommendList(input);
        
        return ResponseEntity.ok(recommendations);
    }

    @GetMapping("/test")
    public ResponseEntity<List<ContentOutput>> testRecommend(
            @RequestParam String topicName,
            @RequestParam(required = false) String subjectName,
            @RequestParam(required = false) String examType) {
        ContentInput input = new ContentInput(null, topicName, subjectName, examType);
        return ResponseEntity.ok(contentRecommender.recommendList(input));
    }
}
