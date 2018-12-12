package io.electrum.suv.server.model;

import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.electrum.suv.api.models.Voucher;
import io.electrum.vas.Utils;
import io.electrum.vas.model.Institution;
import io.electrum.vas.model.Originator;
import io.electrum.vas.model.SlipData;
import io.electrum.vas.model.ThirdPartyIdentifier;
import io.swagger.annotations.ApiModelProperty;

//import io.electrum.airtime.api.model.Product;
//import io.electrum.airtime.api.model.Voucher;

public class DetailMessage {

   private String pathId = null;
   private String voucherId = null;
   private String reversalId = null;
   private String confirmationId = null;
   private String voidId = null;
   private String requestTime = null;
   private String reversalTime = null;
   private String confirmDate = null;
   private String voidDate = null;
   private Institution client = null;
   private Originator originator = null;
   private Institution settlementEntity = null;
   private Institution receiver = null;
   private Voucher voucher = null;
   private SlipData slipData = null;
   private String freeString = null;
   private List<ThirdPartyIdentifier> thirdPartyIdentifiers = null;
   private List<FormatError> formatErrors = null;

   /**
    * The randomly generated String identifying this voucher request, as defined for a variant 4 String in [RFC
    * 4122](https://tools.ietf.org/html/rfc4122).
    **/
   public DetailMessage pathId(String pathId) {
      this.pathId = pathId;
      return this;
   }

   @ApiModelProperty(value = "The randomly generated String identifying this voucher request, as defined for a variant 4 String in [RFC 4122](https://tools.ietf.org/html/rfc4122).")
   @JsonProperty("pathId")
   public String getPathId() {
      return pathId;
   }

   public void setPathId(String pathId) {
      this.pathId = pathId;
   }

   /**
    * The randomly generated String identifying this voucher request, as defined for a variant 4 String in [RFC
    * 4122](https://tools.ietf.org/html/rfc4122).
    **/
   public DetailMessage voucherId(String voucherId) {
      this.voucherId = voucherId;
      return this;
   }

   @ApiModelProperty(value = "The randomly generated String identifying this voucher request, as defined for a variant 4 String in [RFC 4122](https://tools.ietf.org/html/rfc4122).")
   @JsonProperty("voucherId")
   public String getVoucherId() {
      return voucherId;
   }

   public void setVoucherId(String voucherId) {
      this.voucherId = voucherId;
   }

   /**
    * The randomly generated String identifying this voucher reversal, as defined for a variant 4 String in [RFC
    * 4122](https://tools.ietf.org/html/rfc4122).
    **/
   public DetailMessage reversalId(String reversalId) {
      this.reversalId = reversalId;
      return this;
   }

   @ApiModelProperty(value = "The randomly generated String identifying this voucher reversal, as defined for a variant 4 String in [RFC 4122](https://tools.ietf.org/html/rfc4122).")
   @JsonProperty("reversalId")
   public String getReversalId() {
      return reversalId;
   }

   public void setReversalId(String reversalId) {
      this.reversalId = reversalId;
   }

   /**
    * The randomly generated String identifying this voucher confirmation, as defined for a variant 4 String in [RFC
    * 4122](https://tools.ietf.org/html/rfc4122).
    **/
   public DetailMessage confirmationId(String confirmationId) {
      this.confirmationId = confirmationId;
      return this;
   }

   @ApiModelProperty(value = "The randomly generated String identifying this voucher confirmation, as defined for a variant 4 String in [RFC 4122](https://tools.ietf.org/html/rfc4122).")
   @JsonProperty("confirmationId")
   public String getConfirmationId() {
      return confirmationId;
   }

   public void setConfirmationId(String confirmationId) {
      this.confirmationId = confirmationId;
   }

   /**
    * The randomly generated String identifying this voucher void, as defined for a variant 4 String in [RFC
    * 4122](https://tools.ietf.org/html/rfc4122).
    **/
   public DetailMessage voidId(String voidId) {
      this.voidId = voidId;
      return this;
   }

   @ApiModelProperty(value = "The randomly generated String identifying this voucher void, as defined for a variant 4 String in [RFC 4122](https://tools.ietf.org/html/rfc4122).")
   @JsonProperty("voidId")
   public String getVoidId() {
      return voidId;
   }

   public void setVoidId(String voidId) {
      this.voidId = voidId;
   }

   /**
    * The date and time of the request as recorded by the sender. The format shall be as defined for date-time in [RFC
    * 3339 section 5.6](https://tools.ietf.org/html/rfc3339#section-5.6). It is recommended that the optional
    * time-secfrac be included up to millisecond precision.
    */
   public DetailMessage requestTime(String requestTime) {
      this.requestTime = requestTime;
      return this;
   }

   @ApiModelProperty(value = "The date and time of the request as recorded by the sender. The format shall be as defined for date-time in [RFC 3339 section 5.6](https://tools.ietf.org/html/rfc3339#section-5.6). It is recommended that the optional time-secfrac be included up to millisecond precision.")
   @JsonProperty("requestTime")
   public String getRequestTime() {
      return requestTime;
   }

   public void setRequestTime(String requestTime) {
      this.requestTime = requestTime;
   }

   /**
    * The date and time of the reversal as recorded by the sender. The format shall be as defined for date-time in [RFC
    * 3339 section 5.6](https://tools.ietf.org/html/rfc3339#section-5.6). It is recommended that the optional
    * time-secfrac be included up to millisecond precision.
    **/
   public DetailMessage reversalTime(String reversalTime) {
      this.reversalTime = reversalTime;
      return this;
   }

   @ApiModelProperty(value = "The date and time of the reversal as recorded by the sender. The format shall be as defined for date-time in [RFC 3339 section 5.6](https://tools.ietf.org/html/rfc3339#section-5.6). It is recommended that the optional time-secfrac be included up to millisecond precision.")
   @JsonProperty("reversalTime")
   public String getReversalTime() {
      return reversalTime;
   }

   public void setReversalTime(String reversalTime) {
      this.reversalTime = reversalTime;
   }

   /**
    * The date and time of the confirmation as recorded by the sender. The format shall be as defined for date-time in
    * [RFC 3339 section 5.6](https://tools.ietf.org/html/rfc3339#section-5.6). It is recommended that the optional
    * time-secfrac be included up to millisecond precision.
    **/
   public DetailMessage confirmDate(String confirmDate) {
      this.confirmDate = confirmDate;
      return this;
   }

   @ApiModelProperty(value = "The date and time of the confirmation as recorded by the sender. The format shall be as defined for date-time in [RFC 3339 section 5.6](https://tools.ietf.org/html/rfc3339#section-5.6). It is recommended that the optional time-secfrac be included up to millisecond precision.")
   @JsonProperty("confirmDate")
   public String getConfirmDate() {
      return confirmDate;
   }

   public void setConfirmDate(String confirmDate) {
      this.confirmDate = confirmDate;
   }

   /**
    * The date and time of the void as recorded by the sender. The format shall be as defined for date-time in [RFC 3339
    * section 5.6](https://tools.ietf.org/html/rfc3339#section-5.6). It is recommended that the optional time-secfrac be
    * included up to millisecond precision.
    **/
   public DetailMessage voidDate(String voidDate) {
      this.voidDate = voidDate;
      return this;
   }

   @ApiModelProperty(value = "The date and time of the void as recorded by the sender. The format shall be as defined for date-time in [RFC 3339 section 5.6](https://tools.ietf.org/html/rfc3339#section-5.6). It is recommended that the optional time-secfrac be included up to millisecond precision.")
   @JsonProperty("voidDate")
   public String getVoidDate() {
      return voidDate;
   }

   public void setVoidDate(String voidDate) {
      this.voidDate = voidDate;
   }

   /**
    * Information about the merchant who originated this request.
    **/
   public DetailMessage originator(Originator originator) {
      this.originator = originator;
      return this;
   }

   @ApiModelProperty(value = "Information about the merchant who originated this request.")
   @JsonProperty("originator")
   public Originator getOriginator() {
      return originator;
   }

   public void setOriginator(Originator originator) {
      this.originator = originator;
   }

   /**
    * Information about the sender of this request.
    **/
   public DetailMessage client(Institution client) {
      this.client = client;
      return this;
   }

   @ApiModelProperty(value = "Information about the sender of this request.")
   @JsonProperty("client")
   public Institution getClient() {
      return client;
   }

   public void setClient(Institution client) {
      this.client = client;
   }

   /**
    * Information about the processor who should process this request.
    **/
   public DetailMessage settlementEntity(Institution settlementEntity) {
      this.settlementEntity = settlementEntity;
      return this;
   }

   @ApiModelProperty(value = "Information about the processor who should process this request.")
   @JsonProperty("settlementEntity")
   public Institution getSettlementEntity() {
      return settlementEntity;
   }

   public void setSettlementEntity(Institution SettlementEntity) {
      this.settlementEntity = SettlementEntity;
   }

   /**
    * Information about the vendor who should process this request.
    **/
   public DetailMessage receiver(Institution receiver) {
      this.receiver = receiver;
      return this;
   }

   @ApiModelProperty(value = "Information about the vendor who should process this request.")
   @JsonProperty("receiver")
   public Institution getReceiver() {
      return receiver;
   }

   public void setReceiver(Institution vendor) {
      this.receiver = vendor;
   }

   /**
    * The voucher provisioned if the vendor processed the request successfully.
    **/
   public DetailMessage voucher(Voucher voucher) {
      this.voucher = voucher;
      return this;
   }

   @ApiModelProperty(value = "The voucher provisioned if the vendor processed the request successfully.")
   @JsonProperty("voucher")
   public Voucher getVoucher() {
      return voucher;
   }

   public void setVoucher(Voucher voucher) {
      this.voucher = voucher;
   }

   /**
    * Data to be printed on the slip in addition to the voucher instructions.
    **/
   public DetailMessage slipData(SlipData slipData) {
      this.slipData = slipData;
      return this;
   }

   @ApiModelProperty(value = "Data to be printed on the slip in addition to the voucher instructions.")
   @JsonProperty("slipData")
   public SlipData getSlipData() {
      return slipData;
   }

   public void setSlipData(SlipData slipData) {
      this.slipData = slipData;
   }

   /**
    * Data to be printed on the slip in addition to the voucher instructions.
    **/
   public DetailMessage freeString(String freeString) {
      this.freeString = freeString;
      return this;
   }

   @ApiModelProperty(value = "Free string which may provide further information.")
   @JsonProperty("freeString")
   public String getFreeString() {
      return freeString;
   }

   public void setFreeString(String freeString) {
      this.freeString = freeString;
   }

   /**
    * The unaltered thirdPartyIdentifiers array as supplied in the related BasicResponse message. Required if
    * thirdPartyIdentifiers field was present in the BasicResponse. If no thirdPartyIdentifiers was received in the
    * BasicResponse or no BasicResponse was received then this should be set to the thirdPartyIdentifiers sent in the
    * original request.
    **/
   public DetailMessage transactionIdentifiers(List<ThirdPartyIdentifier> transactionIdentifiers) {
      this.thirdPartyIdentifiers = transactionIdentifiers;
      return this;
   }

   @ApiModelProperty(value = "The unaltered thirdPartyIdentifiers array as supplied in the related BasicResponse message. Required if thirdPartyIdentifiers field was present in the BasicResponse. If no thirdPartyIdentifiers was received in the BasicResponse or no BasicResponse was received then this should be set to the thirdPartyIdentifiers sent in the original request.")
   @JsonProperty("thirdPartyIdentifiers")
   public List<ThirdPartyIdentifier> getThirdPartyIdentifiers() {
      return thirdPartyIdentifiers;
   }

   public void setThirdPartyIdentifiers(List<ThirdPartyIdentifier> transactionIdentifiers) {
      this.thirdPartyIdentifiers = transactionIdentifiers;
   }

   /**
    * Data to be printed on the slip in addition to the voucher instructions.
    **/
   public DetailMessage formatErrors(List<FormatError> formatErrors) {
      this.formatErrors = formatErrors;
      return this;
   }

   @ApiModelProperty(value = "List of incorrectly formatted fields and a description of the formatting error.")
   @JsonProperty("formatErrors")
   public List<FormatError> getFormatErrors() {
      return formatErrors;
   }

   public void setFormatErrors(List<FormatError> formatErrors) {
      this.formatErrors = formatErrors;
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      }
      if (o == null || getClass() != o.getClass()) {
         return false;
      }
      DetailMessage detailMessage = (DetailMessage) o;
      return Objects.equals(pathId, detailMessage.pathId) && Objects.equals(voucherId, detailMessage.voucherId)
            && Objects.equals(reversalId, detailMessage.reversalId)
            && Objects.equals(confirmationId, detailMessage.confirmationId)
            // && Objects.equals(voidId, detailMessage.voidId) && Objects.equals(product, detailMessage.product)
            && Objects.equals(requestTime, detailMessage.requestTime)
            && Objects.equals(reversalTime, detailMessage.reversalTime)
            && Objects.equals(confirmDate, detailMessage.confirmDate)
            && Objects.equals(voidDate, detailMessage.voidDate) && Objects.equals(originator, detailMessage.originator)
            && Objects.equals(client, detailMessage.client)
            && Objects.equals(settlementEntity, detailMessage.settlementEntity)
            // && Objects.equals(receiver, detailMessage.receiver) && Objects.equals(voucher, detailMessage.voucher)
            && Objects.equals(slipData, detailMessage.slipData) && Objects.equals(freeString, detailMessage.freeString)
            && Objects.equals(formatErrors, detailMessage.formatErrors);
   }

   @Override
   public int hashCode() {
      return Objects.hash(
            pathId,
            voucherId,
            reversalId,
            confirmationId,
            voidId,
            // product,
            requestTime,
            reversalTime,
            confirmDate,
            voidDate,
            originator,
            client,
            settlementEntity,
            receiver,
            // voucher,
            slipData,
            freeString,
            formatErrors);
   }

   @Override
   public String toString() {
      return "class DetailMessage {\n" +
              "    pathId: " + Utils.toIndentedString(pathId) + "\n" +
              "    voucherId: " + Utils.toIndentedString(voucherId) + "\n" +
              "    reversalId: " + Utils.toIndentedString(reversalId) + "\n" +
              "    confirmationId: " + Utils.toIndentedString(confirmationId) + "\n" +
              "    voidId: " + Utils.toIndentedString(voidId) + "\n" +
              "    requestTime: " + Utils.toIndentedString(requestTime) + "\n" +
              "    reversalTime: " + Utils.toIndentedString(reversalTime) + "\n" +
              "    confirmDate: " + Utils.toIndentedString(confirmDate) + "\n" +
              "    voidDate: " + Utils.toIndentedString(voidDate) + "\n" +
              "    originator: " + Utils.toIndentedString(originator) + "\n" +
              "    sender: " + Utils.toIndentedString(client) + "\n" +
              "    processor: " + Utils.toIndentedString(settlementEntity) + "\n" +
              "    vendor: " + Utils.toIndentedString(receiver) + "\n" +
              " voucher: " + Utils.toIndentedString(voucher) + "\n" +
              "    slipData: " + Utils.toIndentedString(slipData) + "\n" +
              "    thirdPartyIdentifiers: " + Utils.toIndentedString(thirdPartyIdentifiers) + "\n" +
              "    freeString: " + Utils.toIndentedString(freeString) + "\n" +
              "    formatErrors: " + Utils.toIndentedString(formatErrors) + "\n" +
              "}";
   }
}
