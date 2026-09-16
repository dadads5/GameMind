package com.zuel.springtest.services;

import com.zuel.springtest.common.BusinessException;
import com.zuel.springtest.config.UploadProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * 本地文件存储
 *
 * <p>文件按子目录归类存放，对外通过 {@code app.upload.url-prefix} 暴露访问路径。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileStorageService {

    /** 允许上传的图片扩展名 */
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif", "webp");

    /** 单文件大小上限：2MB */
    private static final long MAX_FILE_SIZE = 2 * 1024 * 1024L;

    private final UploadProperties uploadProperties;

    /**
     * 保存上传文件
     *
     * @param file         上传的文件
     * @param subDirectory 子目录，如 avatars
     * @return 可访问的相对 URL
     */
    public String store(MultipartFile file, String subDirectory) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("请选择要上传的文件");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException("文件大小不能超过 2MB");
        }

        String originalFilename = StringUtils.cleanPath(
                Objects.requireNonNull(file.getOriginalFilename(), "文件名不能为空"));
        String extension = extractExtension(originalFilename).toLowerCase(Locale.ROOT);

        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new BusinessException("仅支持 jpg、jpeg、png、gif、webp 格式的图片");
        }

        try {
            Path directory = Paths.get(uploadProperties.getDir())
                    .toAbsolutePath()
                    .normalize()
                    .resolve(subDirectory == null ? "" : subDirectory);
            Files.createDirectories(directory);

            String filename = UUID.randomUUID() + "." + extension;
            Path target = directory.resolve(filename);
            file.transferTo(target);

            String url = normalizePrefix(uploadProperties.getUrlPrefix())
                    + (subDirectory == null || subDirectory.isBlank() ? "" : subDirectory + "/")
                    + filename;
            log.info("文件已保存：{} -> {}", originalFilename, target);
            return url;
        } catch (IOException e) {
            log.error("保存上传文件失败", e);
            throw new BusinessException("文件上传失败，请稍后重试");
        }
    }

    private String extractExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == filename.length() - 1) {
            throw new BusinessException("文件缺少扩展名");
        }
        return filename.substring(dotIndex + 1);
    }

    private String normalizePrefix(String prefix) {
        if (prefix == null || prefix.isBlank()) {
            return "/uploads/";
        }
        String normalized = prefix.trim();
        if (!normalized.startsWith("/")) {
            normalized = "/" + normalized;
        }
        if (!normalized.endsWith("/")) {
            normalized = normalized + "/";
        }
        return normalized;
    }
}
