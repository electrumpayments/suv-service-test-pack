package io.electrum.suv.server.util;

//TODO Should this be repeated in each project
public class RandomData {
   public static String random09(int length) {
      StringBuilder sb = new StringBuilder();
      for (int i = 0; i < length; i++) {
         int randomNum = (int) (Math.random() * 10);
         sb.append(randomNum);
      }

      return sb.toString();
   }

   public static String randomaz(int length) {
      StringBuilder sb = new StringBuilder();
      for (int i = 0; i < length; i++) {
         sb.append((int) (Math.random() * 27 + 'a'));
      }

      return sb.toString();
   }

   public static String randomAZ(int length) {
      StringBuilder sb = new StringBuilder();
      for (int i = 0; i < length; i++) {
         sb.append((char) (Math.random() * 27 + 'A'));
      }

      return sb.toString();
   }

   public static String random09azAZ(int length) {
      StringBuilder sb = new StringBuilder();
      for (int i = 0; i < length; i++) {
         switch ((int) Math.random() * 3) {
         case 0:
            sb.append(random09(1));
            break;
         case 1:
            sb.append(randomaz(1));
            break;
         case 2:
            sb.append(randomAZ(1));
            break;
         }
      }

      return sb.toString();
   }

   public static String random09AZ(int length) {
      StringBuilder sb = new StringBuilder();
      for (int i = 0; i < length; i++) {
         switch ((int) Math.random() * 2) {
         case 0:
            sb.append(random09(1));
            break;
         case 1:
            sb.append(randomAZ(1));
            break;
         }
      }

      return sb.toString();
   }
}
