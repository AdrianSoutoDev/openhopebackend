package es.udc.OpenHope.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;

public interface ResourceService {
    String save(MultipartFile image);
    Resource get(String imageName) throws MalformedURLException;
    void remove(String imageName);
    boolean areEquals(MultipartFile multipartFile, String filePath) throws IOException;
}
