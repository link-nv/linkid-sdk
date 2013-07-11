using System;
using System.Collections;
using System.Collections.Generic;

namespace safe_online_sdk_dotnet
{
    public enum PaymentState { STARTED, WAITING_FOR_UPDATE, FAILED, PAYED };

    public class PaymentResponse
    {
        public static readonly String LOCAL_NAME = "PaymentResponse";

        public static readonly String TXN_ID_KEY    = "PaymentResponse.txnId";
        public static readonly String STATE_KEY     = "PaymentResponse.state";

        public String txnId { get; set; }
        public PaymentState paymentState { get; set; }

        public PaymentResponse(String txnId, PaymentState paymentState)
        {
            this.txnId = txnId;
            this.paymentState = paymentState;
        }

        public static PaymentResponse fromDictionary(Dictionary<string, string> dictionary)
        {
            if (null == dictionary[TXN_ID_KEY])
                throw new RuntimeException("Payment response's transaction ID field is not present!");
            if (null == dictionary[STATE_KEY])
                throw new RuntimeException("Payment response's state field is not present!");

            return new PaymentResponse(dictionary[TXN_ID_KEY], parse(dictionary[STATE_KEY])); 
        }

        public static PaymentState parse(String paymentStateString)
        {
            if (paymentStateString.Equals(PaymentState.STARTED.ToString()))
                return PaymentState.STARTED;
            if (paymentStateString.Equals(PaymentState.WAITING_FOR_UPDATE.ToString()))
                return PaymentState.WAITING_FOR_UPDATE;
            if (paymentStateString.Equals(PaymentState.FAILED.ToString()))
                return PaymentState.FAILED;
            if (paymentStateString.Equals(PaymentState.PAYED.ToString()))
                return PaymentState.PAYED;

            throw new RuntimeException("Unsupported payment state! " + paymentStateString);
        }
    }
}