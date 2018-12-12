package io.electrum.suv.server.model;

import javax.ws.rs.core.Response;

/**
 * Wrapper for {@link Response} objects removing the need for explicit use of null checks in handlers to determine a
 * lack of error.
 */
public class ValidationResponse {
   private Response response;

   public ValidationResponse(Response rsp) {
      response = rsp;
   }

   /**
    * Abstracts away the use of null to indicate whether an error is present
    *
    * @returns whether an error response is stored or not
    */
   public boolean hasErrorResponse() {
      return response != null;
   }

   public Response getResponse() {
      return response;
   }

   public void setResponse(Response response) {
      this.response = response;
   }
}
