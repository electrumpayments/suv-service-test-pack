package io.electrum.suv.server.model;

import javax.ws.rs.core.Response;

public class ValidationResponse {
   private Response response;

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
