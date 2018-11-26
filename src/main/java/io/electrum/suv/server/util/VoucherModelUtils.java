package io.electrum.suv.server.util;

import java.util.concurrent.ConcurrentHashMap;

import io.electrum.airtime.api.model.VoucherRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VoucherModelUtils extends AirtimeModelUtils {
   static Logger log = LoggerFactory.getLogger(VoucherModelUtils.class);

   public static boolean isVoucherProvisioned(
         String voucherId,
         ConcurrentHashMap<RequestKey, VoucherRequest> provisionRecords,
         String username,
         String password) {
      RequestKey provisionKey = new RequestKey(username, password, RequestKey.VOUCHERS_RESOURCE, voucherId.toString());
      log.debug(String.format("Searching for provision record under following key: %s", provisionKey.toString()));
      return provisionRecords.get(provisionKey) != null;
   }

}
