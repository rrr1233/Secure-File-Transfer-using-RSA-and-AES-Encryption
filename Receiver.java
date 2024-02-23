

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.*;
import java.nio.file.*;
import java.security.*;
import java.util.zip.CRC32;
import javax.crypto.*;
import javax.swing.*;




public class Receiver {
    public static PublicKey RS;
    public static PrivateKey receiverPrivateKey;
    public static Cipher cipher;
    public static byte[] SessionKey;
    public static void main(String[] args) throws Exception{

        // Step 1: Generate the reviver RSA key pair
        GenerateRSAKeys();
        
        //Step 2: Write receiver RSA public key to file;
        Files.write(Paths.get("receiver_public_key.pem"), RS.getEncoded());
        
        //Step 3:  Decrypt the session key
         ESA_Decryption();
         
            // Start listining 
        int port=8080;
        ServerSocket server=new ServerSocket(port);
        System.out.println("Server start listining ....... ");
        Socket client = server.accept();
        System.out.println("Client connected :) ");
        
          FileOutputStream outputStream = new FileOutputStream("output.enc");
         InputStream input = client.getInputStream();
         byte[] buffer=new byte[4096];
         int read;
         while((read = input.read(buffer)) != -1) {
             outputStream.write(buffer, 0, read);
         }
         
         outputStream.close();
         input.close();
         server.close();
         client.close();

       //Final step: Decrypt the file by session key
         FileDecrypton("output.enc");
         
    }
    

        public static void GenerateRSAKeys() throws Exception {
             // Generate RSA key pair for receiver
        KeyPairGenerator KG = KeyPairGenerator.getInstance("RSA");
        KG.initialize(2048);
        KeyPair RKP = KG.generateKeyPair();
        receiverPrivateKey = RKP.getPrivate();
         RS = RKP.getPublic();
        }
        
        public static void ESA_Decryption() throws Exception {
            // Decrypt AES key using receiver RSA private key
        cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.DECRYPT_MODE, receiverPrivateKey);
        
        String encrypted_aes=FILESelector("Choose the encrypted session key");
        byte[] encryptedAesKey = Files.readAllBytes(Paths.get(encrypted_aes));
        SessionKey = cipher.doFinal(encryptedAesKey);
        
        }

    public static void FileDecrypton(String encrypted_file) throws Exception {
        

         FileInputStream inputStream = new FileInputStream(encrypted_file);
   
         
            byte[] Filecontent = new byte[1028];
            int Read;
            CRC32 checksum=new CRC32();
         
            StringBuilder decryptedBuilder = new StringBuilder();
            while ((Read =inputStream.read(Filecontent)) != -1) {
                byte[] decryptedBytes = cipher.update(Filecontent, 0, Read);
                
                decryptedBuilder.append(new String(decryptedBytes, StandardCharsets.UTF_8));
            }
            byte[] finalDecryptedBytes = cipher.doFinal();
            decryptedBuilder.append(new String(finalDecryptedBytes, StandardCharsets.UTF_8));
            checksum.update(finalDecryptedBytes);
            long CheckSum=checksum.getValue();
            System.out.println("Message: "+ decryptedBuilder+"\nChecksum: "+CheckSum);
                   
            
    }

        private static String FILESelector(String promo) {
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
