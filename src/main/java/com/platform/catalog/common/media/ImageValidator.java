package com.platform.catalog.common.media;

import com.platform.catalog.common.exception.CatalogException;
import com.platform.catalog.domain.dto.ImageUpload;
import com.platform.catalog.domain.enums.CatalogError;
import com.platform.catalog.domain.model.ValidatedImage;
import jakarta.enterprise.context.ApplicationScoped;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

@ApplicationScoped
public class ImageValidator {

    private static final int MAXIMUM_BYTES = 5 * 1024 * 1024;
    private static final int MAXIMUM_DIMENSION = 4096;

    public ValidatedImage validate(ImageUpload upload) {
        if (upload == null || upload.bytes() == null || upload.bytes().length == 0) {
            throw invalid("Image file is required.");
        }
        byte[] bytes = upload.bytes();
        if (bytes.length > MAXIMUM_BYTES) {
            throw invalid("Image file cannot exceed 5 MB.");
        }
        ImageType imageType = detectType(bytes);
        Dimensions dimensions = imageType == ImageType.WEBP
            ? readWebpDimensions(bytes)
            : readImageIoDimensions(bytes);
        if (dimensions.width() > MAXIMUM_DIMENSION
            || dimensions.height() > MAXIMUM_DIMENSION) {
            throw invalid("Image dimensions cannot exceed 4096 by 4096 pixels.");
        }
        return new ValidatedImage(
            bytes,
            imageType.contentType(),
            imageType.extension(),
            dimensions.width(),
            dimensions.height()
        );
    }

    // TODO this method is potentially too complex and "intelligent", simplify it
    private ImageType detectType(byte[] bytes) {
        if (bytes.length >= 3
            && (bytes[0] & 0xff) == 0xff
            && (bytes[1] & 0xff) == 0xd8
            && (bytes[2] & 0xff) == 0xff) {
            return ImageType.JPEG;
        }
        if (bytes.length >= 8
            && Arrays.equals(
                Arrays.copyOfRange(bytes, 0, 8),
                new byte[] {(byte) 0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a}
            )) {
            return ImageType.PNG;
        }
        if (bytes.length >= 12
            && ascii(bytes, 0, 4).equals("RIFF")
            && ascii(bytes, 8, 4).equals("WEBP")) {
            return ImageType.WEBP;
        }
        throw invalid("Image must be JPEG, PNG, or WebP.");
    }

    private Dimensions readImageIoDimensions(byte[] bytes) {
        try {
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(bytes));
            if (image == null) {
                throw invalid("Image content is invalid.");
            }
            return new Dimensions(image.getWidth(), image.getHeight());
        } catch (CatalogException exception) {
            throw exception;
        } catch (Exception exception) {
            throw invalid("Image content is invalid.");
        }
    }

    private Dimensions readWebpDimensions(byte[] bytes) {
        int offset = 12;
        while (offset + 8 <= bytes.length) {
            String chunkType = ascii(bytes, offset, 4);
            int chunkSize = littleEndianInt(bytes, offset + 4);
            int dataOffset = offset + 8;
            if (dataOffset + chunkSize > bytes.length) {
                throw invalid("Image content is invalid.");
            }
            if ("VP8X".equals(chunkType) && chunkSize >= 10) {
                int width = 1 + littleEndian24(bytes, dataOffset + 4);
                int height = 1 + littleEndian24(bytes, dataOffset + 7);
                return new Dimensions(width, height);
            }
            if ("VP8L".equals(chunkType) && chunkSize >= 5) {
                int bits = littleEndianInt(bytes, dataOffset + 1);
                int width = 1 + (bits & 0x3fff);
                int height = 1 + ((bits >> 14) & 0x3fff);
                return new Dimensions(width, height);
            }
            if ("VP8 ".equals(chunkType) && chunkSize >= 10) {
                if ((bytes[dataOffset + 3] & 0xff) != 0x9d
                    || (bytes[dataOffset + 4] & 0xff) != 0x01
                    || (bytes[dataOffset + 5] & 0xff) != 0x2a) {
                    throw invalid("Image content is invalid.");
                }
                int width = littleEndianShort(bytes, dataOffset + 6) & 0x3fff;
                int height = littleEndianShort(bytes, dataOffset + 8) & 0x3fff;
                return new Dimensions(width, height);
            }
            offset = dataOffset + chunkSize + (chunkSize % 2);
        }
        throw invalid("Image content is invalid.");
    }

    private String ascii(byte[] bytes, int offset, int length) {
        return new String(bytes, offset, length, StandardCharsets.US_ASCII);
    }

    private int littleEndianShort(byte[] bytes, int offset) {
        return (bytes[offset] & 0xff) | ((bytes[offset + 1] & 0xff) << 8);
    }

    private int littleEndian24(byte[] bytes, int offset) {
        return (bytes[offset] & 0xff)
            | ((bytes[offset + 1] & 0xff) << 8)
            | ((bytes[offset + 2] & 0xff) << 16);
    }

    private int littleEndianInt(byte[] bytes, int offset) {
        return (bytes[offset] & 0xff)
            | ((bytes[offset + 1] & 0xff) << 8)
            | ((bytes[offset + 2] & 0xff) << 16)
            | ((bytes[offset + 3] & 0xff) << 24);
    }

    private CatalogException invalid(String message) {
        return new CatalogException(CatalogError.INVALID_IMAGE, message);
    }

    private enum ImageType {
        JPEG("image/jpeg", "jpg"),
        PNG("image/png", "png"),
        WEBP("image/webp", "webp");

        private final String contentType;
        private final String extension;

        ImageType(String contentType, String extension) {
            this.contentType = contentType;
            this.extension = extension;
        }

        private String contentType() {
            return contentType;
        }

        private String extension() {
            return extension;
        }
    }

    private record Dimensions(int width, int height) {
    }
}
