package io.electrum.suv.resource.impl;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.electrum.suv.api.utils.Paths;
import io.electrum.suv.handler.SUVMessageHandlerFactory;
import io.electrum.suv.server.model.ResetRequest;
import io.electrum.suv.server.model.ResetResponse;
import io.swagger.annotations.*;

/**
 * Provides the ability to reset the data for a specific user
 */
@Path(Paths.BASE_PATH + "/testServerAdmin")
@Consumes({ "application/json" })
@Produces({ "application/json" })
@Api(description = "the SUV API")
public class TestServerAdminResourceImpl {
   private static final Logger log = LoggerFactory.getLogger(SUVTestServer.class);
   static TestServerAdminResourceImpl instance = null;

   protected TestServerAdminResourceImpl getResourceImplementation() {
      if (instance == null) {
         instance = new TestServerAdminResourceImpl();
      }
      return instance;
   }

   @POST
   @Path("/reset")
   @Produces({ "application/json" })
   @ApiOperation(value = "Reset the test data in the SUV Test Server.", notes = "The Test Server Admin Reset endpoint allows a user of the Test Server "
         + "to reset the test data recorded for a specific user. This means that "
         + "all request, response and voucher data will be reset. This operation affects all data used by the "
         + "user identified by the HTTP Basic Auth username and password "
         + "combination. <em>This cannot be reversed.</em>", authorizations = {
               @Authorization(value = "httpBasic") }, tags = { "Test Server Admin", })
   @ApiResponses(value = { @ApiResponse(code = 200, message = "OK", response = ResetResponse.class),
         @ApiResponse(code = 400, message = "Bad Request", response = ResetResponse.class),
         @ApiResponse(code = 500, message = "Internal Server Error", response = ResetResponse.class) })
   public void reset(
         @ApiParam(value = "The activation confirmation information.", required = true) ResetRequest body,
         @Context SecurityContext securityContext,
         @Suspended AsyncResponse asyncResponse,
         @Context HttpHeaders httpHeaders,
         @Context UriInfo uriInfo,
         @Context HttpServletRequest httpServletRequest) {
      log.info(String.format("%s %s", httpServletRequest.getMethod(), uriInfo.getPath()));
      log.debug(String.format("%s %s\n%s", httpServletRequest.getMethod(), uriInfo.getPath(), body));
      Response rsp = SUVMessageHandlerFactory.getResetHandler(httpHeaders).handle(body);
      log.debug(String.format("Entity returned:\n%s", rsp.getEntity()));
      asyncResponse.resume(rsp);
   }
}
