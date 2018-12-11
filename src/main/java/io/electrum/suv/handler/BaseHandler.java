package io.electrum.suv.handler;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import io.electrum.suv.server.model.ValidationResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.electrum.suv.resource.impl.SUVTestServer;
import io.electrum.suv.server.util.VoucherModelUtils;
import io.electrum.vas.Utils;
import io.electrum.vas.model.Transaction;

public abstract class BaseHandler {
   // TODO Convert to SUVTestServer
   private static final Logger log = LoggerFactory.getLogger(SUVTestServer.class.getPackage().getName());

   protected final String username;
   protected final String password;

   protected BaseHandler(HttpHeaders httpHeaders) {
      String authString = Utils.getBasicAuthString(httpHeaders.getHeaderString(HttpHeaders.AUTHORIZATION));
      this.username = Utils.getUsernameFromBasicAuth(authString);
      this.password = Utils.getPasswordFromBasicAuth(authString);
   }

   protected Response logAndBuildException(Exception e) {
      log.debug("error processing " + getRequestName(), e);
      for (StackTraceElement ste : e.getStackTrace()) {
         log.debug(ste.toString());
      }
      return Response.serverError().entity(e.getMessage()).build();
   }

   protected ValidationResponse validateClientIdUsernameMatch(Transaction transaction, String uuid) {
      if (!transaction.getClient().getId().equals(username)) {
         return new ValidationResponse(VoucherModelUtils.buildIncorrectUsernameErrorResponse(uuid, transaction.getClient(), username));
      }
      return new ValidationResponse(null);
   }

   protected abstract String getRequestName();
}
