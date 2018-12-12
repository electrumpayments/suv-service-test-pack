package io.electrum.suv.server.util;

import java.io.IOException;

import io.electrum.suv.api.models.RefundRequest;
import io.electrum.suv.api.models.RefundResponse;
import io.electrum.vas.JsonUtil;

public class RefundModelUtils extends SUVModelUtils {
   public static RefundResponse refundRspFromReq(RefundRequest refundRequest) throws IOException {
      RefundResponse refundResponse =
            JsonUtil.deserialize(JsonUtil.serialize(refundRequest, RefundRequest.class), RefundResponse.class);

      updateWithRandomizedIdentifiers(refundResponse);
      refundResponse.setSlipData(createRandomizedSlipData());

      // TODO Confirm not needed to populate more fields (optional)
      return refundResponse;
   }

}
