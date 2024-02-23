

import java.io.*;
import java.net.Socket;
import java.nio.file.*;
import java.security.*;
import java.security.spec.*;
import java.util.Scanner;
import java.util.zip.CRC32;
import javax.crypto.*;
import javax.swing.JFileChooser;


public class Sender {

    
    public static PublicKey receiverPublicKey;
    public static  SecretKey aesKey;
    public static  Cipher cipher;
    
    
    public static void main(String[] args) throws Exception{
        
        
        String host="127.0.0.1";
        int port=8080;
        
        
        
        // Step 1: get the receiver public key to encrypt the session key
        getReceiverPublicKey();

        // Step 2: generate the session key and encrypt it with receiver public key
        GenerateSessionKey();
        
        // step 3: Choose file and encrypt it with the session key
         byte[] EncyptedFile = EncryptFile();
         
         System.out.println("Do want to start the connection? ");
         Scanner scan = new Scanner(System.in);
         String wait=scan.nextLine();
         
         
        // Connect to the server and transfer shared secret
        Socket socket = new Socket(host, port);
        OutputStream out1= socket.getOutputStream();
        out1.write(EncyptedFile);
        out1.flush();
        out1.close();
        // close socket 
        socket.close();
        
        System.out.println("The file has been encrypted and transferred successfully :)");
         
    }

    public static PublicKey getPublicKey(byte[] keyBytes) throws Exception {
        
        // X509EncodedKeySpec object to store the key in bytes
        X509EncodedKeySpec RealKey = new X509EncodedKeySpec(keyBytes);
        // KeyFactory object to give it key spesifcation "I want it as RSA key"
        KeyFactory Creator = KeyFactory.getInstance("RSA");
        // generate RSA public key
        return Creator.generatePublic(RealKey);
        
    }
      public static void getReceiverPublicKey() throws Exception {
         String receiver_public_key=FILESelector("Choose the file that contain the receiver public key");
         // reading all byte from public key file in array of byte
         byte[] publicKeyBytes = Files.readAllBytes(Paths.get(receiver_public_key));
         // get the byte in Public key form using getPublicKey method that we create it to specific the key type and convert it 
        receiverPublicKey = getPublicKey(publicKeyBytes);
    }

    // This class will take bytes and convert it to hex
    public static String ToHex(byte[] bytes) {
        
        // StringBuilder object to minpulate string
        StringBuilder sb = new StringBuilder();
        // byte by byte 
        for (byte b : bytes) {
            // append each converted byte to sb
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
        
    }

    public static void GenerateSessionKey() throws Exception {
       // AES key
       
       // KeyGenerator object and select the AES scheme
        KeyGenerator KG = KeyGenerator.getInstance("AES");
        // select size
        KG.init(128);
        // Know generate the secret key and store it in SecretKey object
        aesKey = KG.generateKey();

        // Encrypt AES with reciver RSA public key
        cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        // select the mode, and the key
        cipher.init(Cipher.ENCRYPT_MODE, receiverPublicKey);
        // Get encoded method to encode the secret key 
        byte[] encryptedAesKey = cipher.doFinal(aesKey.getEncoded());

        // write encrypted AES
        Files.write(Paths.get("encrypted_aes_key.bin"), encryptedAesKey);
    }

    public static byte[] EncryptFile() throws Exception {
         String inputFile =FILESelector("Choose the file you want to encrypt");

          String outputFile = "output.enc";
        // Read the content of the input file and encrypt it
        FileInputStream inputStream = new FileInputStream(inputFile);
        
        
        CRC32 checksum=new CRC32();
        FileOutputStream outputStream = new FileOutputStream(outputFile);


        // array of byte to store the plain txt byte 
            byte[] Plaintext = new byte[1028];
            int Read;
           

            while ((Read = inputStream.read(Plaintext)) != -1) {
                // use our cipher object to encrypt each byte then write the result in ciphertext array "block by block"
                checksum.update(Plaintext, 0, Read);
                byte[] ciphertext = cipher.update(Plaintext, 0, Read);
                    outputStream.write(ciphertext);
                   
             
            }
          
           // do the final encryption process
            long CheckSum=checksum.getValue();
            byte[] finalEncryptedBytes = cipher.doFinal();
            outputStream.write(finalEncryptedBytes);
            
            
            
            System.out.println("Chucksum value: "+CheckSum);
            
            return finalEncryptedBytes;
    }

    public static String FILESelector(String promo) {
        System.out.println(promo);
         String Path="";
        JFileChooser file = new JFileChooser();
        if (file.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
        File f = file.getSelectedFile(); 
        Path=f.getPath();
          }
         return Path;
      }


}

