package es.udc.OpenHope.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.net.MalformedURLException;

public interface ResourceService {
    String saveImage(MultipartFile image);
    Resource getImage(String imageName) throws MalformedURLException;
    void removeImage(String imageName);
}
