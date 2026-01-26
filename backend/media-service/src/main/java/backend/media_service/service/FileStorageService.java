package backend.media_service.service;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.MemoryCacheImageOutputStream;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

@Service
public class FileStorageService {

    private final Cloudinary cloudinary;

    public FileStorageService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    public String uploadCompressedImage(MultipartFile file) throws IOException {
        // Validate file size
        if (file.getSize() > 2 * 1024 * 1024) throw new IOException("File too large (>2MB)");

        // Validate MIME type
        String mime = file.getContentType();
        if (mime == null || !(mime.equals("image/jpeg") || mime.equals("image/png"))) {
            throw new IOException("Invalid file type");
        }

        // Read the original image
        BufferedImage originalImage = ImageIO.read(file.getInputStream());
        if (originalImage == null) throw new IOException("Invalid image format");

        // Compress the image
        byte[] compressedBytes = compressImage(originalImage, 0.7f);

        // Generate a safe unique filename
        String uniqueFileName = "media/" + UUID.randomUUID();

        // Upload to Cloudinary
        Map uploadResult = cloudinary.uploader().upload(
                compressedBytes,
                ObjectUtils.asMap(
                        "public_id", uniqueFileName,
                        "overwrite", true,
                        "resource_type", "image"
                )
        );

        return (String) uploadResult.get("secure_url");
    }

    public boolean deleteFileByUrl(String fileUrl) {
        try {
            String[] parts = fileUrl.split("/media/");
            if (parts.length < 2) return false;
            String publicIdWithExt = parts[1];
            String publicId = publicIdWithExt.substring(0, publicIdWithExt.lastIndexOf('.'));
            cloudinary.uploader().destroy("media/" + publicId, ObjectUtils.emptyMap());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private byte[] compressImage(BufferedImage image, float quality) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageWriter jpgWriter = ImageIO.getImageWritersByFormatName("jpg").next();
        ImageWriteParam jpgWriteParam = jpgWriter.getDefaultWriteParam();
        jpgWriteParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        jpgWriteParam.setCompressionQuality(quality);

        jpgWriter.setOutput(new MemoryCacheImageOutputStream(baos));
        jpgWriter.write(null, new IIOImage(image, null, null), jpgWriteParam);
        jpgWriter.dispose();

        return baos.toByteArray();
    }
}
