package pt.tecnico.meditrack;
//import pt.tecnico.meditrack.utils.SecureDocument;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import static javax.xml.bind.DatatypeConverter.printHexBinary;

import java.io.ByteArrayOutputStream;
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
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Instant;
import java.util.Base64;
import java.util.Objects;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;


public class SecureDocument {

    private static final String ASYM_ALGO = "RSA";
    private static final String ASYM_CIPHER = "RSA/ECB/PKCS1Padding";
    private static final String SYM_CIPHER = "AES/ECB/PKCS5Padding";

	/** Digital signature algorithm. */
	private static final String SIGNATURE_ALGO = "SHA256withRSA";

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

	private static Key generateSecretKey(String keyPath) throws GeneralSecurityException, IOException {

		// get an AES private key
		System.out.println("Generating AES key ...");
		KeyGenerator keyGen = KeyGenerator.getInstance("AES");
		keyGen.init(256);
		Key key = keyGen.generateKey();
		System.out.println("Finish generating AES key");
		byte[] encoded = key.getEncoded();
		System.out.println("Key:");
		System.out.println(printHexBinary(encoded));

		System.out.println("Writing key to '" + keyPath + "' ...");

		FileOutputStream fos = new FileOutputStream(keyPath);
		fos.write(encoded);
		fos.close();
		return key;
}

	private static JsonObject readJsonFile(String filename) throws FileNotFoundException, IOException{
		try (FileReader fileReader = new FileReader(filename)) {
            Gson gson = new Gson();
            JsonObject rootJson = gson.fromJson(fileReader, JsonObject.class);

			return rootJson;
        }
	}	

	private static byte [] decryptRSAWithPrivateKey(byte[] content, PrivateKey privateKey) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {

		Cipher cipher = Cipher.getInstance(ASYM_ALGO);
		cipher.init(Cipher.DECRYPT_MODE, privateKey);
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

    private static boolean verifyFreshnessToken(String freshnessToken, int temporalLimitSeconds) {
		
        // Split the token into timestamp and random number
        String[] tokenParts = freshnessToken.split("_");

		// Get the timestamp
        byte [] timestamp = Base64.getDecoder().decode(tokenParts[0]);
		long fileTimestamp = bytesToLong(timestamp);
		//System.out.println("File timestamp:" + fileTimestamp);

        // Get the current timestamp in milliseconds
        long currentTimestamp = Instant.now().getEpochSecond();
		//System.out.println("Current timestamp " + currentTimestamp);

        // Calculate the time difference between the file timestamp and the current timestamp
        long temporalDifference = currentTimestamp - fileTimestamp;

        // Check if the file is fresh enough based on the temporal limit
        return temporalDifference <= temporalLimitSeconds;
    }

	private static Key readSecretKey(String secretKeyPath) throws Exception {
        byte[] encoded = readFile(secretKeyPath);
        SecretKeySpec keySpec = new SecretKeySpec(encoded, "AES");
        return keySpec;
    }

	private static Key secretKeyBytesToLong(byte[] encoded) throws Exception {
        SecretKeySpec keySpec = new SecretKeySpec(encoded, "AES");
        return keySpec;
    }

    // Read a public key from a file
    private static PublicKey readPublicKey(String publicKeyPath) throws Exception {
        byte[] pubEncoded = readFile(publicKeyPath);
        X509EncodedKeySpec pubSpec = new X509EncodedKeySpec(pubEncoded);
        KeyFactory keyFactory = KeyFactory.getInstance(ASYM_ALGO);
        return keyFactory.generatePublic(pubSpec);
    }

    // Read a private key from a file
    private static PrivateKey readPrivateKey(String privateKeyPath) throws Exception {
        byte[] privEncoded = readFile(privateKeyPath);
        PKCS8EncodedKeySpec privSpec = new PKCS8EncodedKeySpec(privEncoded);
        KeyFactory keyFactory = KeyFactory.getInstance(ASYM_ALGO);
        return keyFactory.generatePrivate(privSpec);
    }

	// Encrypt content using a assymmetric key
	private static byte[] encryptContentRSAWithPublicKey(byte[] content, PublicKey publicKey) throws Exception {
		Cipher cipher = Cipher.getInstance(ASYM_ALGO);
		cipher.init(Cipher.ENCRYPT_MODE, publicKey);
		return cipher.doFinal(content);
	}

    // Encrypt content using a symmetric key
    private static byte[] encryptContentAES(byte[] content, Key secretKey) throws Exception {
        Cipher cipher = Cipher.getInstance(SYM_CIPHER);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        return cipher.doFinal(content);
    }

    // Create a digital signature for a given byte array
    private static byte[] makeDigitalSignature(byte[] content, PrivateKey privateKey) throws Exception {
		Signature signature = Signature.getInstance(SIGNATURE_ALGO);
		signature.initSign(privateKey);
		signature.update(content);
		return signature.sign();
    }

	/**
	 * Calculates new digest from text and compares it to the to deciphered digest.
	 */
	private static boolean verifyDigitalSignature(byte[] receivedSignature, byte[] bytes, PublicKey publicKey)
			throws Exception {

		// verify the signature with the public key
		Signature sig = Signature.getInstance(SIGNATURE_ALGO);
		sig.initVerify(publicKey);
		sig.update(bytes);
		try {
			return sig.verify(receivedSignature);
		} catch (SignatureException se) {
			System.err.println("Caught exception while verifying " + se);
			return false;
		}
	}

    private static String createFreshnessToken() throws Exception {

        // Get the current timestamp in milliseconds
        long timestampMillis = Instant.now().getEpochSecond();

        // Convert the timestamp to a byte array
        byte[] timestampBytes = longToBytes(timestampMillis);

        SecureRandom random = SecureRandom.getInstance("SHA1PRNG");

        System.out.println("Generating random byte array ...");

        final byte array[] = new byte[32];
        random.nextBytes(array);

        // Create the freshness token by combining the timestamp and the random number
        String freshnessToken = Base64.getEncoder().encodeToString(timestampBytes) + "_" + Base64.getEncoder().encodeToString(array);

        return freshnessToken;
    }

    // Convert a long to a byte array
    private static byte[] longToBytes(long value) {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.putLong(value);
        return buffer.array();
    }

    public static void protect(String input_filename, String output_filename, String public_key, String private_key, String secret_key) throws Exception {

		// Prepare the JSON content
        JsonObject document = new JsonObject();
        JsonArray valueArray = new JsonArray();

        //JsonObject content = fillJsonContent();
		String jsonContent = new String(Files.readAllBytes(Paths.get(input_filename)));
        JsonObject content = new Gson().fromJson(jsonContent, JsonObject.class);
        byte[] jsonBytes = content.toString().getBytes();

		
        // Read keys
		PublicKey publicKey = readPublicKey(public_key);
        PrivateKey privateKey = readPrivateKey(private_key);
		Key secretKey = generateSecretKey(secret_key);
        //KeyPair keyPair = readKeyPair(public_key, private_key);

        // Encrypt content
        byte[] contentBytes = encryptContentAES(jsonBytes, secretKey);
        String contentBytes_b64 = Base64.getEncoder().encodeToString(contentBytes);
        valueArray.add(contentBytes_b64);

        // Encrypt the secret key with the public target key
        byte[] secretKeyFile = readFile(secret_key);
        byte[] secretKeyCipher = encryptContentRSAWithPublicKey(secretKeyFile, publicKey);
        String secretKeyCipher_b64 = Base64.getEncoder().encodeToString(secretKeyCipher);
        valueArray.add(secretKeyCipher_b64);

        document.add("value", valueArray);

        // Make digital signature
        byte[] digitalSignatureBytes = makeDigitalSignature(jsonBytes, privateKey);
		System.out.println("Original Signature size: " + digitalSignatureBytes.length + " bytes");
        String digitalSignatureCipher_b64 = Base64.getEncoder().encodeToString(digitalSignatureBytes);

        document.addProperty("digital-signature", digitalSignatureCipher_b64);
        document.addProperty("token", createFreshnessToken());

        // Write JSON object to file
        try (FileWriter fileWriter = new FileWriter(output_filename)) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(document, fileWriter);
        }
    }


	public static JsonObject protectJson(JsonObject input, JsonObject output, PublicKey publicKeyReceiver, PrivateKey privateKeySender, String secret_key) throws Exception {
		
		// Prepare the JSON content
		JsonObject document = new JsonObject();
		JsonArray valueArray = new JsonArray();

		//JsonObject content = fillJsonContent();
		String jsonContent = input.toString();
		//JsonObject content = new Gson().fromJson(jsonContent, JsonObject.class);
		byte[] jsonBytes = jsonContent.getBytes();

		
		// Read keys
		PublicKey publicKey = publicKeyReceiver;
		PrivateKey privateKey = privateKeySender;
		Key secretKey = generateSecretKey(secret_key);
		//KeyPair keyPair = readKeyPair(public_key, private_key);

		// Encrypt content
		byte[] contentBytes = encryptContentAES(jsonBytes, secretKey);
		String contentBytes_b64 = Base64.getEncoder().encodeToString(contentBytes);
		valueArray.add(contentBytes_b64);

		// Encrypt the secret key with the public target key
		byte[] secretKeyFile = readFile(secret_key);
		byte[] secretKeyCipher = encryptContentRSAWithPublicKey(secretKeyFile, publicKey);
		String secretKeyCipher_b64 = Base64.getEncoder().encodeToString(secretKeyCipher);
		valueArray.add(secretKeyCipher_b64);

		document.add("value", valueArray);

		// Make digital signature
		byte[] digitalSignatureBytes = makeDigitalSignature(jsonBytes, privateKey);
		System.out.println("Original Signature size: " + digitalSignatureBytes.length + " bytes");
		String digitalSignatureCipher_b64 = Base64.getEncoder().encodeToString(digitalSignatureBytes);

		document.addProperty("digital-signature", digitalSignatureCipher_b64);
		document.addProperty("token", createFreshnessToken());

		return document;
	}

    public static void unprotect(String input_filename, String output_filename, String private_key) throws Exception {

		JsonObject file = readJsonFile(input_filename);
		JsonObject document = new JsonObject();

		JsonArray file_values = file.get("value").getAsJsonArray() ;
		String contentCipher = file_values.get(0).getAsString();
		byte[] contentBytesCipher = Base64.getDecoder().decode(contentCipher);

		String secret_key = file_values.get(1).getAsString();
		byte[] secretKeyBytesCipher = Base64.getDecoder().decode(secret_key);

        String signature = file.get("digital-signature").getAsString();

		String freshnessToken = file.get("token").getAsString();
        
		// get keys
        PrivateKey privateKey = readPrivateKey(private_key);
        
		byte[] secretKeyDecodedBytes = decryptRSAWithPrivateKey(secretKeyBytesCipher, privateKey);
		Key secretKey = secretKeyBytesToLong(secretKeyDecodedBytes);

		byte[] contentDecoded = decryptAES(contentBytesCipher, secretKey);
		writeFile("keys/secret.key", secretKeyDecodedBytes);
		//deleteFile("secret.key");

		document.addProperty("content", new String(contentDecoded));
		document.addProperty("digital-signature", signature);
        document.addProperty("token", freshnessToken);
		
		// Write JSON object to file
        try (FileWriter fileWriter = new FileWriter(output_filename)) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(document, fileWriter);
        }

    }

	public static boolean check(String input_filename, String public_key) throws Exception {

		JsonObject file = readJsonFile(input_filename);

		String contentString = file.get("content").getAsString();
		byte[] contentBytes = contentString.getBytes();
	
		String signature = file.get("digital-signature").getAsString();
		byte[] signatureBytesCipher = Base64.getDecoder().decode(signature);
	
		String freshnessToken = file.get("token").getAsString();
		int temporalLimitSeconds = 60;
	
		// Get private key
		PublicKey publicKey = readPublicKey(public_key);
	
		System.out.println("Verifying Signature...");
		// Verify digital signature
		boolean resultSignature = verifyDigitalSignature(signatureBytesCipher, contentBytes, publicKey);
		System.out.println("Signature is " + (resultSignature ? "right" : "wrong"));
	
		System.out.println("Verifying Freshness token...");
		// Verify freshness token
		boolean resultFreshnessToken = verifyFreshnessToken(freshnessToken, temporalLimitSeconds);
		System.out.println("Freshness token is " + (resultFreshnessToken ? "right" : "wrong"));
	
		System.out.println("Result: " + (resultSignature && resultFreshnessToken ? "VALID" : "INVALID"));

		return resultSignature && resultFreshnessToken;
	}


    public static void help(){

        System.out.println(String.format("help:    display information on the available commands"));
        System.out.println(String.format("protect (input-file) (output-file): adds security to the input file"));
        System.out.println(String.format("check (input-file): verify document security"));
        System.out.println(String.format("unprotect (input-file) (output-file): removes the security to the input file"));

    }

	public static void main(String args[]) throws Exception {
		// command <input_filename> <output_filename>
		if (args.length < 1) {
			System.out.println("Please provide a command.");
			return;
		}
	
		String command = args[0];
	
		switch (command) {
			case "help":
				help();
				break;
	
			case "protect":
				if (args.length != 6) {
					System.out.println("Invalid number of arguments for 'protect' command.");
					return;
				}
				protect(args[1], args[2], args[3], args[4], args[5]);
				break;
	
			case "unprotect":
				if (args.length != 4) {
					System.out.println("Invalid number of arguments for 'unprotect' command.");
					return;
				}
				unprotect(args[1], args[2], args[3]);
				break;

			case "check":
				if (args.length != 3) {
					System.out.println("Invalid number of arguments for 'check' command.");
					return;
				}
				check(args[1], args[2]);
				break;
	
			default:
				System.out.println("Unknown command: " + command);
				break;
		}
	}
}


