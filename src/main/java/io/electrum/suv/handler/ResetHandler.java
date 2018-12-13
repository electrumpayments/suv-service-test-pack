package io.electrum.suv.handler;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.electrum.suv.resource.impl.SUVTestServer;
import io.electrum.suv.server.SUVTestServerRunner;
import io.electrum.suv.server.model.Backend;
import io.electrum.suv.server.model.ResetRequest;
import io.electrum.suv.server.model.ResetRequest.Acknowledgments;
import io.electrum.suv.server.model.ResetRequest.Declarations;
import io.electrum.suv.server.model.ResetResponse;
import io.electrum.suv.server.model.ResetResponse.Outcomes;

public class ResetHandler extends BaseHandler {
   private static final Logger log = LoggerFactory.getLogger(SUVTestServer.class);

   public ResetHandler(HttpHeaders httpHeaders) {
      super(httpHeaders);
   }

   public Response handle(ResetRequest request) {
      if (request == null) {
         return Response.status(400).entity(new ResetResponse().outcome(Outcomes.EMPTY_REQUEST)).build();
      }
      try {
         if (request.getAcknowledgement() != Acknowledgments.TRUE) {
            return Response.status(400).entity(new ResetResponse().outcome(Outcomes.NO_ACK)).build();
         }
         if (request.getDeclaration() != Declarations.TRUE) {
            return Response.status(400).entity(new ResetResponse().outcome(Outcomes.NO_DEC)).build();
         }

         Backend backend = SUVTestServerRunner.getTestServer().getBackend();
         if (backend.doRecordsForUserExist(username, password)) {
            backend.reset(username, password);
         } else {
            return Response.status(400).entity(new ResetResponse().outcome(Outcomes.UNKNOWN_USER)).build();
         }
         return Response.status(200).entity(new ResetResponse().outcome(Outcomes.SUCCESSFUL)).build();

      } catch (Exception e) {
         log.debug("error processing ResetRequest", e);
         for (StackTraceElement ste : e.getStackTrace()) {
            log.debug(ste.toString());
         }
         Response rsp = Response.serverError().entity(new ResetResponse().outcome(Outcomes.SERVER_ERROR)).build();
         return rsp;
      }
   }

   @Override
   protected String getRequestName() {
      return "Reset";
   }
}
