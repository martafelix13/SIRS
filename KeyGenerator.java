import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;

public class KeyGenerator {

    private static final String KEY_OWNER = "doctor";
    
    public static void generateAndSaveKeyPair(String publicKeyPath, String privateKeyPath) throws Exception {
        // Generating a key pair
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048); // Key size
        KeyPair keyPair = keyPairGenerator.generateKeyPair();

        // Obtaining public and private keys
        PublicKey publicKey = keyPair.getPublic();
        PrivateKey privateKey = keyPair.getPrivate();

        // Saving the public key to a file
        saveKey(publicKey, publicKeyPath);

        // Saving the private key to a file
        saveKey(privateKey, privateKeyPath);
    }

    private static void saveKey(Key key, String filePath) throws Exception {
        // Converting the key to an appropriate format (e.g., Base64 encoding)
        byte[] keyBytes = key.getEncoded();

        // Saving the key to a file
        Files.write(Paths.get(filePath), keyBytes);
    }

    public static void main(String[] args) {
        try {
            generateAndSaveKeyPair(KEY_OWNER +".pubkey", KEY_OWNER + ".privkey");
            System.out.println("Keys generated and saved successfully!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

