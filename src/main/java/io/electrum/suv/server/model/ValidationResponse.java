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
    * Setting the response field to null indicates that no error response was created. This method abstracts that null
    * check away.
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

   public ValidationResponse(Response rsp) {
      response = rsp;
   }
}
