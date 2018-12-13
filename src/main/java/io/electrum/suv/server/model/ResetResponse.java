package io.electrum.suv.server.model;

import java.util.Objects;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

import io.electrum.vas.Utils;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * The outcome of the reset request.
 */
@ApiModel(description = "The outcome of the reset request.")
public class ResetResponse {

   /**
    * Reset outcomes.
    */
   public enum Outcomes {
      EMPTY_REQUEST("No ResetRequest received in POST body."),
      SUCCESSFUL("Reset successful - all data has been reset for the specified user."),
      NO_ACK("The certainty of data loss was not acknowledged. Please acknowledge it to reset the test server."),
      NO_DEC("The intent to reset all test data was not declared. Please declare it to reset the test server."),
      SERVER_ERROR("There was an error while trying to reset user data. Please contact Electrum if this eror persists."),
      UNKNOWN_USER("The Test Server was unable to locate any records associated with this user to reset. Please confirm HTTP Basic Auth credentials used or continue testing with different credentials.");

      private String value;

      Outcomes(String value) {
         this.value = value;
      }

      @Override
      @JsonValue
      public String toString() {
         return String.valueOf(value);
      }
   }

   private Outcomes outcome = null;

   public ResetResponse outcome(Outcomes outcome) {
      this.outcome = outcome;
      return this;
   }

   /**
    * The outcome of the reset request.
    * 
    * @return acknowledgement
    **/
   @ApiModelProperty(required = true, value = "The outcome of the reset request.")
   @JsonProperty("outcome")
   @NotNull
   public Outcomes getOutcome() {
      return outcome;
   }

   public void setOutcome(Outcomes outcome) {
      this.outcome = outcome;
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      }
      if (o == null || getClass() != o.getClass()) {
         return false;
      }
      ResetResponse resetResponse = (ResetResponse) o;
      return Objects.equals(outcome, resetResponse.outcome);
   }

   @Override
   public int hashCode() {
      return Objects.hash(outcome);
   }

   @Override
   public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append("class ResetResponse {\n");

      sb.append("    outcome: ").append(Utils.toIndentedString(outcome)).append("\n");
      sb.append("}");
      return sb.toString();
   }
}
