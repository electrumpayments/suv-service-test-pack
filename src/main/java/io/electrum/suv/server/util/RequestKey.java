package io.electrum.suv.server.util;

import java.util.Objects;

public class RequestKey {
   private String username;
   private String password;
   private ResourceType resourceType;
   private String uuid;

   public RequestKey(String username, String password, ResourceType resourceType, String uuid) {
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

   public ResourceType getResourceType() {
      return resourceType;
   }

   public void setResourceType(ResourceType resourceType) {
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
      return username + "|" + password + "|" + resourceType + "|" + uuid;
   }

   public enum ResourceType {
      VOUCHERS_RESOURCE("vouchers"),
      REVERSALS_RESOURCE("reversals"),
      CONFIRMATIONS_RESOURCE("confirmations"),
      REDEMPTIONS_RESOURCE("redemptions"),
      REFUNDS_RESOURCE("refunds");

      private String value;

      ResourceType(String value) {
         this.value = value;
      }

   }

}
