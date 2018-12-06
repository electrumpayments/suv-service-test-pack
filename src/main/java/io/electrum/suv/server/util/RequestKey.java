package io.electrum.suv.server.util;

import java.util.Objects;

public class RequestKey {
   // TODO Enum?
   public static final String VOUCHERS_RESOURCE = "vouchers";
   public static final String REVERSALS_RESOURCE = "reversals";
   public static final String CONFIRMATIONS_RESOURCE = "confirmations";
   public static final String REDEMPTIONS_RESOURCE = "redemptions";
   public static final String REFUNDS_RESOURCE = "refunds";

   private String username;
   private String password;
   private String resourceType;
   private String uuid;

   public RequestKey(String username, String password, String resourceType, String uuid) {
      this.username = username;
      this.password = password;
      this.resourceType = resourceType;
      this.uuid = uuid;
   }

   public String getUsername() {
      return username;
   }

   public void setUsername(String username) {
      this.username = username;
   }

   public String getPassword() {
      return password;
   }

   public void setPassword(String password) {
      this.password = password;
   }

   public String getResourceType() {
      return resourceType;
   }

   public void setResourceType(String resourceType) {
      this.resourceType = resourceType;
   }

   public String getUuid() {
      return uuid;
   }

   public void setUuid(String uuid) {
      this.uuid = uuid;
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      }
      if (o == null || getClass() != o.getClass()) {
         return false;
      }

      RequestKey otherKey = (RequestKey) o;

      return username != null && username.equals(otherKey.username) && password != null
            && password.equals(otherKey.password) && resourceType != null && resourceType.equals(otherKey.resourceType)
            && uuid != null && uuid.equals(otherKey.uuid);
   }

   @Override
   public int hashCode() {
      return Objects.hash(username, password, resourceType, uuid);
   }

   @Override
   public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append(username);
      sb.append("|");
      sb.append(password);
      sb.append("|");
      sb.append(resourceType);
      sb.append("|");
      sb.append(uuid);
      return sb.toString();
   }

}
