package com.example.rentalcar.utils;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * ImageHelper – Utility dùng chung cho toàn bộ chức năng upload ảnh.
 *
 * Cách dùng:
 *   String savedPath = ImageHelper.chooseAndSave(window, ImageHelper.Category.VEHICLE);
 *   ImageHelper.loadInto(imageView, savedPath);
 *
 * Ảnh được lưu vào:  <user.home>/VehicleRent/uploads/<category>/<timestamp>_<filename>
 * Đường dẫn trả về:  uploads/<category>/<timestamp>_<filename>  (relative, lưu vào DB)
 */
public class ImageHelper {

    // =========================================================
    //  CATEGORY – phân loại thư mục lưu ảnh
    // =========================================================
    public enum Category {
        VEHICLE("vehicles"),
        CCCD("cccd"),
        AVATAR("avatars");

        private final String folder;
        Category(String folder) { this.folder = folder; }
        public String getFolder() { return folder; }
    }

    // Thư mục gốc lưu ảnh: <user.home>/VehicleRent/uploads/
    private static final String BASE_DIR =
            System.getProperty("user.home") + File.separator + "VehicleRent"
                    + File.separator + "uploads" + File.separator;

    private static final DateTimeFormatter TS_FMT =
            DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    // =========================================================
    //  1. MỞ FileChooser → LƯU ẢNH → TRẢ VỀ ĐƯỜNG DẪN RELATIVE
    // =========================================================
    /**
     * Mở hộp thoại chọn ảnh, copy vào thư mục uploads, trả về đường dẫn relative.
     * Trả về null nếu người dùng huỷ.
     */
    public static String chooseAndSave(Window owner, Category category) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Chọn ảnh");
        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Ảnh", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.webp"),
                new FileChooser.ExtensionFilter("Tất cả file", "*.*")
        );

        File chosen = chooser.showOpenDialog(owner);
        if (chosen == null) return null;

        return saveFile(chosen, category);
    }

    // =========================================================
    //  2. LƯU FILE VÀO THƯ MỤC ĐÍCH
    // =========================================================
    private static String saveFile(File source, Category category) {
        try {
            // Tạo thư mục đích nếu chưa có
            Path destDir = Paths.get(BASE_DIR + category.getFolder());
            Files.createDirectories(destDir);

            // Tên file: <timestamp>_<originalName>
            String timestamp = LocalDateTime.now().format(TS_FMT);
            String fileName  = timestamp + "_" + sanitize(source.getName());
            Path destFile    = destDir.resolve(fileName);

            Files.copy(source.toPath(), destFile, StandardCopyOption.REPLACE_EXISTING);

            // Trả về đường dẫn relative (dùng lưu DB)
            return "uploads" + File.separator + category.getFolder()
                    + File.separator + fileName;

        } catch (IOException e) {
            System.err.println("Lỗi lưu ảnh: " + e.getMessage());
            return null;
        }
    }

    // =========================================================
    //  3. LOAD ẢNH VÀO ImageView
    // =========================================================
    /**
     * Load ảnh từ đường dẫn (absolute hoặc relative) vào ImageView.
     * Nếu không tìm thấy, thử load từ resources (classpath).
     */
    public static void loadInto(ImageView imageView, String path) {
        if (imageView == null || path == null || path.isBlank()) return;

        // Thử load từ file system (đường dẫn absolute hoặc relative trong BASE_DIR)
        File f = new File(path);
        if (!f.isAbsolute()) {
            f = new File(System.getProperty("user.home")
                    + File.separator + "VehicleRent"
                    + File.separator + path);
        }

        if (f.exists()) {
            imageView.setImage(new Image(f.toURI().toString(), true)); // true = background load
            return;
        }

        // Thử load từ classpath (ảnh mặc định trong resources)
        try {
            var stream = ImageHelper.class.getResourceAsStream("/" + path.replace("\\", "/"));
            if (stream != null) {
                imageView.setImage(new Image(stream));
                return;
            }
            // Thử thẳng đường dẫn
            imageView.setImage(new Image(path, true));
        } catch (Exception e) {
            System.err.println("Không load được ảnh: " + path);
        }
    }

    /**
     * Load ảnh từ classpath (resources), dùng cho ảnh mặc định.
     * Ví dụ: loadDefault(iv, "/image/dashboardform/card-moto.png")
     */
    public static void loadDefault(ImageView imageView, String classpathPath) {
        if (imageView == null) return;
        try {
            var url = ImageHelper.class.getResource(classpathPath);
            if (url != null) imageView.setImage(new Image(url.toExternalForm()));
        } catch (Exception e) {
            System.err.println("Không load được ảnh mặc định: " + classpathPath);
        }
    }

    // =========================================================
    //  4. LẤY ĐƯỜNG DẪN ĐẦY ĐỦ TỪ RELATIVE PATH
    // =========================================================
    public static String toAbsolute(String relativePath) {
        if (relativePath == null) return null;
        File f = new File(relativePath);
        if (f.isAbsolute()) return relativePath;
        return System.getProperty("user.home")
                + File.separator + "VehicleRent"
                + File.separator + relativePath;
    }

    // =========================================================
    //  HELPER
    // =========================================================
    private static String sanitize(String name) {
        // Giữ lại chữ, số, dấu chấm, gạch dưới; thay thế ký tự lạ bằng _
        return name.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}