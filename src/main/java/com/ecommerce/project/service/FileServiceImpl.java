package com.ecommerce.project.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileServiceImpl implements FileService{

    @Override
    public String uploadImage(String path, MultipartFile file) throws IOException {

        // 1. Get the original filename uploaded by the user.
        //    Example: "laptop.jpg"
        String originalFileName = file.getOriginalFilename();

        // 2. Generate a random UUID.
        //    Used to make the filename unique and avoid overwriting another image.
        String randomId = UUID.randomUUID().toString();

        // 3. Extract the file extension from the original filename.
        //    Example: "laptop.jpg" -> ".jpg"
        //
        //    Then combine UUID + extension.
        //    Example:
        //    "550e8400-e29b-41d4-a716-446655440000.jpg"
        String fileName = randomId.concat(
                originalFileName.substring(originalFileName.lastIndexOf('.'))
        );

        // 4. Create the complete path where the image will be stored.
        //    Example:
        //    images/550e8400-e29b-41d4-a716-446655440000.jpg
        //
        //    File.separator makes the path OS-independent.
        String filePath = path + File.separator + fileName;

        // 5. Create a File object representing the images/ folder.
        File folder = new File(path);

        // 6. Check whether the images/ folder already exists.
        //    If it doesn't exist, create it.
        if (!folder.exists()) {
            folder.mkdir();
        }

        // 7. Read the uploaded image and copy it to the destination path.
        //
        //    file.getInputStream() -> reads the uploaded image
        //    Paths.get(filePath)   -> tells Java where to save it
        //
        //    So this line actually saves the image into the images/ folder.
        Files.copy(
                file.getInputStream(),
                Paths.get(filePath)
        );

        // 8. Return only the generated filename.
        //    This filename will later be stored in the Product table.
        return fileName;
    }


}
