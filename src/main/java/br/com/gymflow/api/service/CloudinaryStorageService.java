package br.com.gymflow.api.service;

import br.com.gymflow.api.domain.enums.ExerciseMediaType;
import br.com.gymflow.api.exception.InvalidFileException;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CloudinaryStorageService {

    private static final String EXERCISE_IMAGE_FOLDER = "gymflow/exercises/images";
    private static final String EXERCISE_VIDEO_FOLDER = "gymflow/exercises/videos";

    private final Cloudinary cloudinary;

    @Value("${gymflow.upload.image-max-size}")
    private long imageMaxSize;

    @Value("${gymflow.upload.video-max-size}")
    private long videoMaxSize;

    public String uploadExerciseMedia(MultipartFile file, ExerciseMediaType mediaType) {
        validateFile(file, mediaType);

        try {
            Map<?, ?> uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", getFolder(mediaType),
                            "resource_type", getResourceType(mediaType)
                    )
            );

            Object secureUrl = uploadResult.get("secure_url");

            if (secureUrl == null) {
                throw new InvalidFileException("Could not upload file");
            }

            return secureUrl.toString();
        } catch (IOException exception) {
            throw new InvalidFileException("Could not read uploaded file");
        } catch (RuntimeException exception) {
            throw new InvalidFileException("Could not upload file");
        }
    }

    private void validateFile(MultipartFile file, ExerciseMediaType mediaType) {
        if (file == null || file.isEmpty()) {
            throw new InvalidFileException("File is required");
        }

        String contentType = file.getContentType();

        if (contentType == null) {
            throw new InvalidFileException("File type is required");
        }

        if (mediaType == ExerciseMediaType.IMAGE) {
            validateImage(file, contentType);
            return;
        }

        validateVideo(file, contentType);
    }

    private void validateImage(MultipartFile file, String contentType) {
        if (!contentType.startsWith("image/")) {
            throw new InvalidFileException("File must be an image");
        }

        if (file.getSize() > imageMaxSize) {
            throw new InvalidFileException("Image file is too large");
        }
    }

    private void validateVideo(MultipartFile file, String contentType) {
        if (!contentType.startsWith("video/")) {
            throw new InvalidFileException("File must be a video");
        }

        if (file.getSize() > videoMaxSize) {
            throw new InvalidFileException("Video file is too large");
        }
    }

    private String getFolder(ExerciseMediaType mediaType) {
        if (mediaType == ExerciseMediaType.IMAGE) {
            return EXERCISE_IMAGE_FOLDER;
        }

        return EXERCISE_VIDEO_FOLDER;
    }

    private String getResourceType(ExerciseMediaType mediaType) {
        if (mediaType == ExerciseMediaType.IMAGE) {
            return "image";
        }

        return "video";
    }
}