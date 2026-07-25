package br.com.gymflow.api.service;

import br.com.gymflow.api.domain.enums.ExerciseMediaType;
import br.com.gymflow.api.exception.InvalidFileException;
import com.cloudinary.Cloudinary;
import com.cloudinary.Uploader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CloudinaryStorageServiceTest {

    @Mock
    private Cloudinary cloudinary;

    @Mock
    private Uploader uploader;

    private CloudinaryStorageService cloudinaryStorageService;

    @BeforeEach
    void setUp() {
        cloudinaryStorageService = new CloudinaryStorageService(cloudinary);

        ReflectionTestUtils.setField(cloudinaryStorageService, "imageMaxSize", 5L);
        ReflectionTestUtils.setField(cloudinaryStorageService, "videoMaxSize", 10L);
    }

    @Test
    void shouldUploadExerciseImageSuccessfully() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "supino.png",
                "image/png",
                "image".getBytes()
        );

        String expectedUrl = "https://res.cloudinary.com/gymflow/image/upload/supino.png";

        when(cloudinary.uploader()).thenReturn(uploader);
        when(uploader.upload(any(byte[].class), anyMap()))
                .thenReturn(Map.of("secure_url", expectedUrl));

        String response = cloudinaryStorageService.uploadExerciseMedia(
                file,
                ExerciseMediaType.IMAGE
        );

        assertEquals(expectedUrl, response);

        ArgumentCaptor<Map> optionsCaptor = ArgumentCaptor.forClass(Map.class);

        verify(cloudinary).uploader();
        verify(uploader).upload(any(byte[].class), optionsCaptor.capture());

        Map options = optionsCaptor.getValue();

        assertEquals("gymflow/exercises/images", options.get("folder"));
        assertEquals("image", options.get("resource_type"));
    }

    @Test
    void shouldUploadExerciseVideoSuccessfully() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "supino.mp4",
                "video/mp4",
                "video".getBytes()
        );

        String expectedUrl = "https://res.cloudinary.com/gymflow/video/upload/supino.mp4";

        when(cloudinary.uploader()).thenReturn(uploader);
        when(uploader.upload(any(byte[].class), anyMap()))
                .thenReturn(Map.of("secure_url", expectedUrl));

        String response = cloudinaryStorageService.uploadExerciseMedia(
                file,
                ExerciseMediaType.VIDEO
        );

        assertEquals(expectedUrl, response);

        ArgumentCaptor<Map> optionsCaptor = ArgumentCaptor.forClass(Map.class);

        verify(cloudinary).uploader();
        verify(uploader).upload(any(byte[].class), optionsCaptor.capture());

        Map options = optionsCaptor.getValue();

        assertEquals("gymflow/exercises/videos", options.get("folder"));
        assertEquals("video", options.get("resource_type"));
    }

    @Test
    void shouldThrowInvalidFileExceptionWhenFileIsEmpty() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "empty.png",
                "image/png",
                new byte[0]
        );

        InvalidFileException exception = assertThrows(InvalidFileException.class, () ->
                cloudinaryStorageService.uploadExerciseMedia(file, ExerciseMediaType.IMAGE)
        );

        assertEquals("File is required", exception.getMessage());

        verifyNoInteractions(cloudinary);
        verifyNoInteractions(uploader);
    }

    @Test
    void shouldThrowInvalidFileExceptionWhenContentTypeIsNull() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "supino.png",
                null,
                "image".getBytes()
        );

        InvalidFileException exception = assertThrows(InvalidFileException.class, () ->
                cloudinaryStorageService.uploadExerciseMedia(file, ExerciseMediaType.IMAGE)
        );

        assertEquals("File type is required", exception.getMessage());

        verifyNoInteractions(cloudinary);
        verifyNoInteractions(uploader);
    }

    @Test
    void shouldThrowInvalidFileExceptionWhenImageUploadReceivesNonImageFile() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "documento.pdf",
                "application/pdf",
                "pdf".getBytes()
        );

        InvalidFileException exception = assertThrows(InvalidFileException.class, () ->
                cloudinaryStorageService.uploadExerciseMedia(file, ExerciseMediaType.IMAGE)
        );

        assertEquals("File must be an image", exception.getMessage());

        verifyNoInteractions(cloudinary);
        verifyNoInteractions(uploader);
    }

    @Test
    void shouldThrowInvalidFileExceptionWhenVideoUploadReceivesNonVideoFile() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "supino.png",
                "image/png",
                "image".getBytes()
        );

        InvalidFileException exception = assertThrows(InvalidFileException.class, () ->
                cloudinaryStorageService.uploadExerciseMedia(file, ExerciseMediaType.VIDEO)
        );

        assertEquals("File must be a video", exception.getMessage());

        verifyNoInteractions(cloudinary);
        verifyNoInteractions(uploader);
    }

    @Test
    void shouldThrowInvalidFileExceptionWhenImageFileIsTooLarge() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "supino.png",
                "image/png",
                "large-image".getBytes()
        );

        InvalidFileException exception = assertThrows(InvalidFileException.class, () ->
                cloudinaryStorageService.uploadExerciseMedia(file, ExerciseMediaType.IMAGE)
        );

        assertEquals("Image file is too large", exception.getMessage());

        verifyNoInteractions(cloudinary);
        verifyNoInteractions(uploader);
    }

    @Test
    void shouldThrowInvalidFileExceptionWhenVideoFileIsTooLarge() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "supino.mp4",
                "video/mp4",
                "large-video-file".getBytes()
        );

        InvalidFileException exception = assertThrows(InvalidFileException.class, () ->
                cloudinaryStorageService.uploadExerciseMedia(file, ExerciseMediaType.VIDEO)
        );

        assertEquals("Video file is too large", exception.getMessage());

        verifyNoInteractions(cloudinary);
        verifyNoInteractions(uploader);
    }

    @Test
    void shouldThrowInvalidFileExceptionWhenCloudinaryDoesNotReturnSecureUrl() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "supino.png",
                "image/png",
                "image".getBytes()
        );

        when(cloudinary.uploader()).thenReturn(uploader);
        when(uploader.upload(any(byte[].class), anyMap()))
                .thenReturn(Map.of());

        InvalidFileException exception = assertThrows(InvalidFileException.class, () ->
                cloudinaryStorageService.uploadExerciseMedia(file, ExerciseMediaType.IMAGE)
        );

        assertEquals("Could not upload file", exception.getMessage());

        verify(cloudinary).uploader();
        verify(uploader).upload(any(byte[].class), anyMap());
    }

    @Test
    void shouldThrowInvalidFileExceptionWhenCloudinaryUploadFails() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "supino.png",
                "image/png",
                "image".getBytes()
        );

        when(cloudinary.uploader()).thenReturn(uploader);
        when(uploader.upload(any(byte[].class), anyMap()))
                .thenThrow(new RuntimeException("Cloudinary error"));

        InvalidFileException exception = assertThrows(InvalidFileException.class, () ->
                cloudinaryStorageService.uploadExerciseMedia(file, ExerciseMediaType.IMAGE)
        );

        assertEquals("Could not upload file", exception.getMessage());

        verify(cloudinary).uploader();
        verify(uploader).upload(any(byte[].class), anyMap());
    }
}