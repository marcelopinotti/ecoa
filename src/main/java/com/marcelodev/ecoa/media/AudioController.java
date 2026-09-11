package com.marcelodev.ecoa.media;

import java.nio.file.Path;

import org.springframework.core.io.FileSystemResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/audio")
public class AudioController {

    private final AudioConversationService audioConversationService;

    public AudioController(AudioConversationService audioConversationService) {
        this.audioConversationService = audioConversationService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = "audio/wav")
    public ResponseEntity<FileSystemResource> answer(@RequestParam("file") MultipartFile file) {
        Path output = audioConversationService.answer(file);
        var resource = new FileSystemResource(output);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("audio/wav"))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(output.getFileName().toString()).build().toString())
                .body(resource);
    }
}
