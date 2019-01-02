package io.electrum.suv.server.model;

import java.util.Objects;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

import io.electrum.vas.Utils;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * A request to reset user data in the Test Server.
 */
@ApiModel(description = "A request to reset user data in the Test Server.")
public class ResetRequest {

   private Acknowledgments acknowledgement = Acknowledgments.FALSE;
   private Declarations declaration = Declarations.FALSE;

   public ResetRequest acknowledgement(Acknowledgments acknowledgement) {
      this.acknowledgement = acknowledgement;
      return this;
   }

   /**
    * An acknowledgement that all user data will be lost.
    *
    * @return acknowledgement
    **/
   @ApiModelProperty(required = true, value = "An acknowledgement that all user data will be lost.")
   @JsonProperty("userDataWillBeLost")
   @NotNull
   public Acknowledgments getAcknowledgement() {
      return acknowledgement;
   }

   public void setAcknowledgement(Acknowledgments acknowledgement) {
      this.acknowledgement = acknowledgement;
   }

   public ResetRequest declaration(Declarations declaration) {
      this.declaration = declaration;
      return this;
   }

   /**
    * A declaration to reset user data.
    *
    * @return declaration
    **/
   @ApiModelProperty(required = true, value = "A declaration to reset user data.")
   @JsonProperty("resetUserData")
   @NotNull
   public Declarations getDeclaration() {
      return declaration;
   }

   public void setDeclaration(Declarations declaration) {
      this.declaration = declaration;
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      }
      if (o == null || getClass() != o.getClass()) {
         return false;
      }
      ResetRequest resetRequest = (ResetRequest) o;
      return Objects.equals(acknowledgement, resetRequest.acknowledgement)
            && Objects.equals(declaration, resetRequest.declaration);
   }

   @Override
   public int hashCode() {
      return Objects.hash(acknowledgement, declaration);
   }

   @Override
   public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append("class ResetRequest {\n");

      sb.append("    userDataWillBeLost: ").append(Utils.toIndentedString(acknowledgement)).append("\n");
      sb.append("    resetUserData: ").append(Utils.toIndentedString(declaration)).append("\n");
      sb.append("}");
      return sb.toString();
   }

   /**
    * Recognised acknowledgments.
    */
   public enum Acknowledgments {
      TRUE("TRUE"), FALSE("FALSE");

      private String value;

      Acknowledgments(String value) {
         this.value = value;
      }

      @Override
      @JsonValue
      public String toString() {
         return String.valueOf(value);
      }
   }

   /**
    * Recognised declarations.
    */
   public enum Declarations {
      TRUE("TRUE"), FALSE("FALSE");

      private String value;

      Declarations(String value) {
         this.value = value;
      }

      @Override
      @JsonValue
      public String toString() {
         return String.valueOf(value);
      }
   }
}
