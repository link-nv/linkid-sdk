using System;
using System.Collections;
using System.Collections.Generic;

namespace safe_online_sdk_dotnet
{
    public enum Currency {EUR};

    public class PaymentContext
    {
        public static readonly String AMOUNT_KEY           = "PaymentContext.amount";
        public static readonly String CURRENCY_KEY         = "PaymentContext.currency";
        public static readonly String DESCRIPTION_KEY      = "PaymentContext.description";
        public static readonly String PROFILE_KEY          = "PaymentContext.profile";
        public static readonly String VALIDATION_TIME_KEY  = "PaymentContext.validationTime";
        public static readonly String ADD_LINK_KEY         = "PaymentContext.addLinkKey";

        // amount to pay, carefull amount is in cents!!
        public Double amount { get; set; }
        public Currency currency { get; set; }
        public String description { get; set; }

        // optional payment profile
        private String paymentProfile { get; set; }

        // maximum time to wait for payment validation, if not specified defaults to 5s
        public int paymentValidationTime { get; set; }

        // whether or not to display a link to linkID's payment method page if the linkID user has no payment methods added, default is true
        public Boolean showAddPaymentMethodLink { get; set; }

        public PaymentContext(Double amount, Currency currency, String description, 
            String paymentProfile, int paymentValidationTime, Boolean showAddPaymentMethodLink)
        {
            this.amount = amount;
            this.currency = currency;
            this.description = description;
            this.paymentProfile = paymentProfile;
            this.paymentValidationTime = paymentValidationTime;
            this.showAddPaymentMethodLink = showAddPaymentMethodLink;
        }

        public PaymentContext(double amount, Currency currency, String description, String paymentProfile)
        {
            this.amount = amount;
            this.currency = currency;
            this.description = description;
            this.paymentProfile = paymentProfile;
            this.paymentValidationTime = 5;
            this.showAddPaymentMethodLink = true;
        }

        public PaymentContext(double amount, Currency currency)
        {
            this.amount = amount;
            this.currency = currency;
            this.paymentValidationTime = 5;
            this.showAddPaymentMethodLink = true;
        }

        public Dictionary<string, string> toDictionary()
        {
            Dictionary<string, string> dictionary = new Dictionary<string, string>();
            dictionary.Add(AMOUNT_KEY, amount.ToString());
            dictionary.Add(CURRENCY_KEY, currency.ToString());
            if (null != description)
                dictionary.Add(DESCRIPTION_KEY, description);
            if (null != paymentProfile)
                dictionary.Add(PROFILE_KEY, paymentProfile);
            dictionary.Add(VALIDATION_TIME_KEY, paymentValidationTime.ToString());
            dictionary.Add(ADD_LINK_KEY, showAddPaymentMethodLink.ToString());
            return dictionary;
        }
    }
}