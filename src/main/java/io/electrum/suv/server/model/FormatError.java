package io.electrum.suv.server.model;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.electrum.vas.Utils;
import io.swagger.annotations.ApiModelProperty;

//TODO find out if this is useful now
public class FormatError {

   private String field = null;
   private String msg = null;
   private String value = null;

   public FormatError field(String field) {
      this.field = field;
      return this;
   }

   @ApiModelProperty(value = "The name of the incorrectly formatted field.")
   @JsonProperty("field")
   public String getField() {
      return field;
   }

   public void setField(String field) {
      this.field = field;
   }

   public FormatError msg(String msg) {
      this.msg = msg;
      return this;
   }

   @ApiModelProperty(value = "A description of the formatting error.")
   @JsonProperty("msg")
   public String getMsg() {
      return msg;
   }

   public void setMsg(String msg) {
      this.msg = msg;
   }

   public FormatError value(String value) {
      this.value = value;
      return this;
   }

   @ApiModelProperty(value = "The value of the incorrectly formatted field.")
   @JsonProperty("value")
   public String getValue() {
      return value;
   }

   public void setValue(String value) {
      this.value = value;
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      }
      if (o == null || getClass() != o.getClass()) {
         return false;
      }
      FormatError detailMessage = (FormatError) o;
      return Objects.equals(field, detailMessage.field) && Objects.equals(msg, detailMessage.msg)
            && Objects.equals(value, detailMessage.value);
   }

   @Override
   public int hashCode() {
      return Objects.hash(field, msg, value);
   }

   @Override
   public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append("class DetailMessage {\n");

      sb.append("    field: ").append(Utils.toIndentedString(field)).append("\n");
      sb.append("    msg: ").append(Utils.toIndentedString(msg)).append("\n");
      sb.append("    value: ").append(Utils.toIndentedString(value)).append("\n");
      sb.append("}");
      return sb.toString();
   }
}
