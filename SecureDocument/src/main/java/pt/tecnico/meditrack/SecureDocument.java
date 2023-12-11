import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import static javax.xml.bind.DatatypeConverter.printHexBinary;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Instant;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;


public class SecureDocument {

    private static final String ASYM_ALGO = "RSA";
    private static final String ASYM_CIPHER = "RSA/ECB/PKCS1Padding";
    private static final String SYM_CIPHER = "AES/ECB/PKCS5Padding";

	/** Digital signature algorithm. */
	private static final String SIGNATURE_ALGO = "SHA256withRSA";


	public static KeyPair read(String publicKeyPath, String privateKeyPath) throws Exception {

		System.out.println("Reading public key from file " + publicKeyPath + " ...");
		byte[] pubEncoded = readFile(publicKeyPath);

		X509EncodedKeySpec pubSpec = new X509EncodedKeySpec(pubEncoded);
		KeyFactory keyFacPub = KeyFactory.getInstance(ASYM_ALGO);
		PublicKey pub = keyFacPub.generatePublic(pubSpec);
		System.out.println(pub);

		System.out.println("---");

		System.out.println("Reading private key from file " + privateKeyPath + " ...");
		byte[] privEncoded = readFile(privateKeyPath);

		PKCS8EncodedKeySpec privSpec = new PKCS8EncodedKeySpec(privEncoded);
		KeyFactory keyFacPriv = KeyFactory.getInstance(ASYM_ALGO);
		PrivateKey priv = keyFacPriv.generatePrivate(privSpec);

		System.out.println("---");

		KeyPair keys = new KeyPair(pub, priv);
		return keys;
	}

	private static byte[] readFile(String path) throws FileNotFoundException, IOException {
		FileInputStream fis = new FileInputStream(path);
		byte[] content = new byte[fis.available()];
		fis.read(content);
		fis.close();
		return content;
	}

    private static void writeFile(String path, byte[] content) throws FileNotFoundException, IOException {
		FileOutputStream fos = new FileOutputStream(path);
		fos.write(content);
		fos.close();
	}
	/**
	 * Calculates new digest from text and compares it to the to deciphered digest.
	 */
	private static boolean verifyDigitalSignature(byte[] receivedSignature, byte[] bytes, KeyPair keyPair)
			throws Exception {

		// verify the signature with the public key
		Signature sig = Signature.getInstance(SIGNATURE_ALGO);
		sig.initVerify(keyPair.getPublic());
		sig.update(bytes);
		try {
			return sig.verify(receivedSignature);
		} catch (SignatureException se) {
			System.err.println("Caught exception while verifying " + se);
			return false;
		}
	}

	private static JsonObject readJsonFile(String filename) throws FileNotFoundException, IOException{
		try (FileReader fileReader = new FileReader(filename)) {
            Gson gson = new Gson();
            JsonObject rootJson = gson.fromJson(fileReader, JsonObject.class);

			return rootJson;
        }
	}	

	private static byte [] decryptRSAWithPrivateKey(byte[] content, KeyPair keyPair) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {

		Cipher cipher = Cipher.getInstance(ASYM_ALGO);
		cipher.init(Cipher.DECRYPT_MODE, keyPair.getPrivate());
		return cipher.doFinal(content);

	}

		private static byte [] decryptRSAWithPublicKey(byte[] content, KeyPair keyPair) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {

		Cipher cipher = Cipher.getInstance(ASYM_ALGO);
		cipher.init(Cipher.DECRYPT_MODE, keyPair.getPublic());
		return cipher.doFinal(content);

	}


	private static byte[] decryptAES(byte[] content, Key secretKey) throws IllegalBlockSizeException, BadPaddingException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException {
		Cipher cipher = Cipher.getInstance(SYM_CIPHER);
		cipher.init(Cipher.DECRYPT_MODE, secretKey);
		return cipher.doFinal(content);
	}

	private static void deleteFile(String filePath) {
        try {
            Path path = Paths.get(filePath);
            Files.delete(path);
            System.out.println("File deleted successfully.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static long bytesToLong(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        return buffer.getLong();
    }

    public static boolean verifyFreshnessToken(String freshnessToken, int temporalLimitSeconds) {
		
        // Split the token into timestamp and random number
        String[] tokenParts = freshnessToken.split("_");

		// Get the timestamp
        byte [] timestamp = Base64.getDecoder().decode(tokenParts[0]);
		long fileTimestamp = bytesToLong(timestamp);
		System.out.println("File timestamp:" + fileTimestamp);

        // Get the current timestamp in milliseconds
        long currentTimestamp = Instant.now().getEpochSecond();
		System.out.println("Current timestamp " + currentTimestamp);

        // Calculate the time difference between the file timestamp and the current timestamp
        long temporalDifference = currentTimestamp - fileTimestamp;

        // Check if the file is fresh enough based on the temporal limit
        return temporalDifference <= temporalLimitSeconds;
    }

	public static Key readSecretKey(String secretKeyPath) throws Exception {
        byte[] encoded = readFile(secretKeyPath);
        SecretKeySpec keySpec = new SecretKeySpec(encoded, "AES");
        return keySpec;
    }

    public static PublicKey readPublicKey(String publicKeyPath) throws Exception {
        System.out.println("Reading public key from file " + publicKeyPath + " ...");
        byte[] pubEncoded = readFile(publicKeyPath);
        X509EncodedKeySpec pubSpec = new X509EncodedKeySpec(pubEncoded);
        KeyFactory keyFacPub = KeyFactory.getInstance("RSA");
        PublicKey pub = keyFacPub.generatePublic(pubSpec);
        return pub;
    }

    public static PrivateKey readPrivateKey(String privateKeyPath) throws Exception {
        byte[] privEncoded = readFile(privateKeyPath);
        PKCS8EncodedKeySpec privSpec = new PKCS8EncodedKeySpec(privEncoded);
        KeyFactory keyFacPriv = KeyFactory.getInstance("RSA");
        PrivateKey priv = keyFacPriv.generatePrivate(privSpec);
        return priv;
    }


    public static void protect(String input_filename, String output_filename) throws Exception {

		JsonObject file = readJsonFile(input_filename);

		JsonArray file_values = file.get("value").getAsJsonArray() ;
		String content = file_values.get(0).getAsString();
		byte[] contentBytes = Base64.getDecoder().decode(content);

		String secret_key = file_values.get(1).getAsString();
		byte[] secretKeyBytes = Base64.getDecoder().decode(secret_key);

        String signature = file.get("digital-signature").getAsString();
		byte [] cipherSignature = Base64.getDecoder().decode(signature);

		String freshnessToken = file.get("token").getAsString();
		int temporalLimitSeconds = 60;
        
		// get keys
        KeyPair keyPair = read("keys/bob.pubkey", "keys/alice.privkey");
        
		byte[] secretKeyDecodedBytes = decryptRSAWithPrivateKey(secretKeyBytes, keyPair);
		writeFile("secret.key", secretKeyDecodedBytes);
		Key secretKey = readSecretKey("secret.key");
		deleteFile("secret.key");

		byte[] contentDecoded = decryptAES(contentBytes, secretKey);

		System.out.println("Verifying...");
		boolean resultSignature = verifyDigitalSignature(cipherSignature, contentDecoded, keyPair);
		System.out.println("Signature is " + (resultSignature ? "right" : "wrong"));

		boolean resultFreshnessToken = verifyFreshnessToken(freshnessToken, temporalLimitSeconds);
		System.out.println("Freshness token is " + (resultFreshnessToken ? "right" : "wrong"));

		writeFile(output_filename, contentDecoded);
    }


    public static void unprotected(String input_filename, String output_filename) throws Exception {

		JsonObject file = readJsonFile(input_filename);

		JsonArray file_values = file.get("value").getAsJsonArray() ;
		String content = file_values.get(0).getAsString();
		byte[] contentBytes = Base64.getDecoder().decode(content);

		String secret_key = file_values.get(1).getAsString();
		byte[] secretKeyBytes = Base64.getDecoder().decode(secret_key);

        String signature = file.get("digital-signature").getAsString();
		byte [] cipherSignature = Base64.getDecoder().decode(signature);

		String freshnessToken = file.get("token").getAsString();
		int temporalLimitSeconds = 60;
        
		// get keys
        KeyPair keyPair = read("keys/bob.pubkey", "keys/alice.privkey");
        
		byte[] secretKeyDecodedBytes = decryptRSAWithPrivateKey(secretKeyBytes, keyPair);
		writeFile("secret.key", secretKeyDecodedBytes);
		Key secretKey = readSecretKey("secret.key");
		deleteFile("secret.key");

		byte[] contentDecoded = decryptAES(contentBytes, secretKey);

		System.out.println("Verifying...");
		boolean resultSignature = verifyDigitalSignature(cipherSignature, contentDecoded, keyPair);
		System.out.println("Signature is " + (resultSignature ? "right" : "wrong"));

		boolean resultFreshnessToken = verifyFreshnessToken(freshnessToken, temporalLimitSeconds);
		System.out.println("Freshness token is " + (resultFreshnessToken ? "right" : "wrong"));

		writeFile(output_filename, contentDecoded);
    }

    public void help(){

        System.out.println(String.format("help:    display information on the available commands"));
        System.out.println(String.format("protect (input-file) (output-file): adds security to the input file"));
        System.out.println(String.format("check (input-file): verify document security"));
        System.out.println(String.format("unprotect (input-file) (output-file): removes the security to the input file"));

    }
}

