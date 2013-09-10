/*
 * SafeOnline project.
 * 
 * Copyright 2006-2008 	Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

using System;
using System.Net;
using System.Net.Security;
using System.Security.Cryptography.X509Certificates;
using System.ServiceModel;
using System.ServiceModel.Channels;
using System.ServiceModel.Description;
using System.ServiceModel.Dispatcher;
using System.ServiceModel.Security;
using System.ServiceModel.Security.Tokens;
using System.Xml;
using System.Collections;
using System.Collections.Generic;
using AttributeWSNamespace;

namespace safe_online_sdk_dotnet
{
    /// <summary>
    /// Client implementation for the linkID Attribute Web Service.
    /// </summary>
    public class AttributeClientImpl : AttributeClient
    {
        private SAMLAttributePortClient client;

        public AttributeClientImpl(string location, X509Certificate2 appCertificate, X509Certificate2 linkidCertificate)
        {
            string address = "https://" + location + "/linkid-ws/attrib";
            EndpointAddress remoteAddress = new EndpointAddress(address);

            this.client = new SAMLAttributePortClient(new LinkIDBinding(linkidCertificate), remoteAddress);


            this.client.ClientCredentials.ClientCertificate.Certificate = appCertificate;
            this.client.ClientCredentials.ServiceCertificate.DefaultCertificate = linkidCertificate;
            // To override the validation for our self-signed test certificates
            this.client.ClientCredentials.ServiceCertificate.Authentication.CertificateValidationMode = X509CertificateValidationMode.None;

            this.client.Endpoint.Contract.ProtectionLevel = ProtectionLevel.Sign;

        }

        public List<AttributeSDK> getAttributes(String userId, String attributeName)
        {
            Dictionary<String, List<AttributeSDK>> attributeMap = new Dictionary<string, List<AttributeSDK>>();
            AttributeQueryType request = getAttributeQuery(userId, new string[] { attributeName });
            ResponseType response = getResponse(request);
            checkStatus(response);
            getAttributeValues(response, attributeMap);
            return attributeMap[attributeName];
        }

        public void getAttributes(String userId, Dictionary<String, List<AttributeSDK>> attributeMap)
        {
            String[] attributeNames = new String[attributeMap.Keys.Count];
            attributeMap.Keys.CopyTo(attributeNames, 0);
            AttributeQueryType request = getAttributeQuery(userId, attributeNames);
            ResponseType response = getResponse(request);
            checkStatus(response);
            getAttributeValues(response, attributeMap);
        }

        public Dictionary<String, List<AttributeSDK>> getAttributes(String userId)
        {
            Dictionary<String, List<AttributeSDK>> attributeMap = new Dictionary<string, List<AttributeSDK>>();
            AttributeQueryType request = getAttributeQuery(userId, new String[] { });
            ResponseType response = getResponse(request);
            checkStatus(response);
            getAttributeValues(response, attributeMap);
            return attributeMap;
        }

        private AttributeQueryType getAttributeQuery(string userId, string[] attributeNames)
        {
            AttributeQueryType attributeQuery = new AttributeQueryType();
            SubjectType subject = new SubjectType();
            NameIDType subjectName = new NameIDType();
            subjectName.Value = userId;
            subject.Items = new Object[] { subjectName };
            attributeQuery.Subject = subject;

            if (null != attributeNames)
            {
                List<AttributeType> attributes = new List<AttributeType>();
                foreach (string attributeName in attributeNames)
                {
                    AttributeType attribute = new AttributeType();
                    attribute.Name = attributeName;
                    attributes.Add(attribute);
                }
                attributeQuery.Attribute = attributes.ToArray();
            }
            return attributeQuery;
        }

        private ResponseType getResponse(AttributeQueryType request)
        {
            return this.client.AttributeQuery(request);
        }

        private void checkStatus(ResponseType response)
        {
            StatusType status = response.Status;
            StatusCodeType statusCode = status.StatusCode;
            if (!statusCode.Value.Equals(Saml2Constants.SAML2_STATUS_SUCCESS))
            {
                Console.WriteLine("status code: " + statusCode.Value);
                Console.WriteLine("status message: " + status.StatusMessage);
                StatusCodeType secondLevelStatusCode = statusCode.StatusCode;
                if (null != secondLevelStatusCode)
                {
                    if (secondLevelStatusCode.Value.Equals(Saml2Constants.SAML2_STATUS_INVALID_ATTRIBUTE_NAME_OR_VALUE))
                    {
                        throw new AttributeNotFoundException();
                    }
                    else if (secondLevelStatusCode.Value.Equals(Saml2Constants.SAML2_STATUS_REQUEST_DENIED))
                    {
                        throw new RequestDeniedException();
                    }
                    else if (secondLevelStatusCode.Value.Equals(Saml2Constants.SAML2_STATUS_ATTRIBUTE_UNAVAILABLE))
                    {
                        throw new AttributeUnavailableException();
                    }
                    Console.WriteLine("second level status code: " + secondLevelStatusCode.Value);
                }

                throw new RuntimeException("error: " + statusCode.Value);
            }
        }

        private static void getAttributeValues(ResponseType response, Dictionary<String, List<AttributeSDK>> attributeMap)
        {

            if (null == response.Items || response.Items.Length == 0)
            {
                throw new RuntimeException("No assertions in response");
            }
            AssertionType assertion = (AssertionType)response.Items[0];
            if (null == assertion.Items || assertion.Items.Length == 0)
            {
                throw new RuntimeException("No statements in response assertion");
            }
            AttributeStatementType attributeStatement = (AttributeStatementType)assertion.Items[0];

            foreach (Object attributeObject in attributeStatement.Items)
            {
                AttributeType attributeType = (AttributeType)attributeObject;
                AttributeSDK attribute = Saml2AuthUtil.getAttribute(attributeType);

                List<AttributeSDK> attributes;
                if (!attributeMap.ContainsKey(attribute.getAttributeName()))
                {
                    attributes = new List<AttributeSDK>();
                }
                else
                {
                    attributes = attributeMap[attribute.getAttributeName()];
                }
                attributes.Add(attribute);
                attributeMap.Remove(attribute.getAttributeName());
                attributeMap.Add(attribute.getAttributeName(), attributes);
            }
        }
    }
}
