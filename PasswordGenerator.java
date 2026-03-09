import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordGenerator {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        System.out.println("=== BCrypt 密码哈希生成器 ===");
        System.out.println();
        System.out.println("管理员密码 (admin123):");
        System.out.println(encoder.encode("admin123"));
        System.out.println();
        System.out.println("测试用户密码 (user123):");
        System.out.println(encoder.encode("user123"));
        System.out.println();
        System.out.println("注意：每次运行生成的哈希值都不同，这是正常的（BCrypt 自动加盐）");
    }
}
