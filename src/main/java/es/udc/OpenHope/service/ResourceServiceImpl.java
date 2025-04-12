package es.udc.OpenHope.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class ResourceServiceImpl implements ResourceService {

    @Value("${upload.dir}")
    private String uploadDir;

    @Override
    public String saveImage(MultipartFile image) {
        try {
            return fileStorage(image);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Resource getImage(String imageName) throws MalformedURLException {
        Path imagePath = Path.of(uploadDir, imageName);
        return new UrlResource(imagePath.toUri());
    }

    @Override
    public void removeImage(String imageName) {
        try {
            Path imagePath = Path.of(uploadDir, imageName);
            File image = new File(imagePath.toString());
            image.delete();
        } catch (Exception ignored){}
    }

    private String fileStorage(MultipartFile file) throws IOException {
        String originalFileName = file.getOriginalFilename();
        String extension = "";
        if (originalFileName != null) {
            extension = originalFileName.substring(originalFileName.lastIndexOf("."));
        }

        File directory = new File(uploadDir);
        String newFileName = UUID.randomUUID().toString().concat(extension);

        if (!directory.exists()) {
            directory.mkdirs();
        }

        Path directoryPath = Paths.get(uploadDir);
        Path filePath = directoryPath.resolve(newFileName);
        Files.copy(file.getInputStream(), filePath);
        return newFileName;
    }

    public boolean filesAreEquals(MultipartFile multipartFile, String filePath) throws IOException {
        Path path = Path.of(uploadDir, filePath);

        try (
            InputStream multipartStream = multipartFile.getInputStream();
            InputStream fileStream = new FileInputStream(path.toFile());
            BufferedInputStream bufferedMultipartStream = new BufferedInputStream(multipartStream);
            BufferedInputStream bufferedFileStream = new BufferedInputStream(fileStream);
        ) {
            int byteMultipart;
            int byteFile;

            while ((byteMultipart = bufferedMultipartStream.read()) != -1 &&
                (byteFile = bufferedFileStream.read()) != -1) {
                if (byteMultipart != byteFile) {
                    return false;
                }
            }

            return bufferedMultipartStream.read() == -1 && bufferedFileStream.read() == -1;
        }
    }
}
