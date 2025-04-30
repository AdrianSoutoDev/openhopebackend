package es.udc.OpenHope.controller;

import es.udc.OpenHope.service.ResourceService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/resources")
public class ResourceController {

    private final ResourceService resourceService;

    @Value("${server.port}")
    private String serverPort;

    @GetMapping("/{imageName}")
    public ResponseEntity<Resource> getImage(@PathVariable String imageName) {
        try {
            Resource imageResource = resourceService.getImage(imageName);

            if (!imageResource.exists() || !imageResource.isReadable()) {
                return ResponseEntity.notFound().build();
            }

            String contentType = Files.probeContentType(imageResource.getFile().toPath());
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(imageResource);

        } catch (MalformedURLException e) {
            return ResponseEntity.badRequest().build();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> postImage(@RequestParam(value = "file", required = false) MultipartFile file) {
        String imagePath = file != null ? resourceService.saveImage(file) : null;

        URI location = ServletUriComponentsBuilder
            .fromCurrentContextPath()
            .port(serverPort)
            .path("/api/resources/{imagePath}")
            .buildAndExpand(imagePath).toUri();
        Map<String, String> response = new HashMap<>();

        response.put("location", location.toString());

        return ResponseEntity.created(location).body(response);
    }
}