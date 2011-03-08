/*
 * SafeOnline project.
 * 
 * Copyright 2006-2008 	Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

using System;
using System.Security.Cryptography;
using System.Security.Cryptography.Xml;
using System.Xml.Serialization;
using System.IO;
using System.Xml;
using System.Text;
using System.Collections;
using System.Collections.Generic;

using AttributeWSNamespace;
using System.Security.Cryptography.X509Certificates;

namespace safe_online_sdk_dotnet
{
    /// <summary>
    /// Saml2AuthUtil
    /// 
    /// This utility class generates a SAML v2.0 AuthenticationRequest with HTTP Browser Post binding
    /// and validates the returned SAML v2.0 Response.
    /// </summary>
    public class Saml2AuthUtil
    {
        private readonly RSACryptoServiceProvider key;

        private string expectedChallenge;

        private string expectedAudience;

        public Saml2AuthUtil(RSACryptoServiceProvider key)
        {
            this.key = key;
        }

        public string getChallenge()
        {
            return this.expectedChallenge;
        }

        public String getAudience()
        {
            return this.expectedAudience;
        }

        /// <summary>
        /// Generates a SAML v2.0 Authentication Request with HTTP Browser Post Binding. The return string containing the request
        /// is already Base64 encoded.
        /// </summary>
        /// <param name="applicationName"></param>
        /// <param name="applicationPools">Optional list of application pools used for session tracking</param>
        /// <param name="applicationFriendlyName">Optional friendly name to be displayed in LinkID authentication pages</param>
        /// <param name="serviceProviderUrl">The URL that will handle the returned SAML response</param>
        /// <param name="identityProviderUrl">The LinkID authentication entry URL</param>
        /// <param name="devices">Option device restriction list</param>
        /// <param name="ssoEnabled"></param>
        /// <returns>Base64 encoded SAML request</returns>
        public string generateEncodedAuthnRequest(string applicationName, List<string> applicationPools, string applicationFriendlyName,
                                          string serviceProviderUrl, string identityProviderUrl, List<string> devices,
                                          bool ssoEnabled)
        {
            string samlRequest = generateAuthnRequest(applicationName, applicationPools, applicationFriendlyName, serviceProviderUrl,
                  identityProviderUrl, devices, ssoEnabled);
            return Convert.ToBase64String(Encoding.ASCII.GetBytes(samlRequest));
        }

        /// <summary>
        /// Generates a SAML v2.0 Authentication Request with HTTP Browser Post Binding. The return string containing the request
        /// is NOT Base64 encoded.
        /// </summary>
        /// <param name="applicationName"></param>
        /// <param name="applicationPools">Optional list of application pools used for session tracking</param>
        /// <param name="applicationFriendlyName">Optional friendly name to be displayed in LinkID authentication pages</param>
        /// <param name="serviceProviderUrl">The URL that will handle the returned SAML response</param>
        /// <param name="identityProviderUrl">The LinkID authentication entry URL</param>
        /// <param name="devices">Option device restriction list</param>
        /// <param name="ssoEnabled"></param>
        /// <returns>SAML request</returns>
        public string generateAuthnRequest(string applicationName, List<string> applicationPools, string applicationFriendlyName,
                                          string serviceProviderUrl, string identityProviderUrl, List<string> devices,
                                          bool ssoEnabled)
        {
            this.expectedChallenge = Guid.NewGuid().ToString();
            this.expectedAudience = applicationName;

            AuthnRequestType authnRequest = new AuthnRequestType();
            authnRequest.ForceAuthn = !ssoEnabled;
            authnRequest.ID = this.expectedChallenge;
            authnRequest.Version = "2.0";
            authnRequest.IssueInstant = DateTime.UtcNow;

            NameIDType issuer = new NameIDType();
            issuer.Value = applicationName;
            authnRequest.Issuer = issuer;

            authnRequest.AssertionConsumerServiceURL = serviceProviderUrl;
            authnRequest.ProtocolBinding = Saml2Constants.SAML2_BINDING_HTTP_POST;

            authnRequest.Destination = identityProviderUrl;

            if (null != applicationFriendlyName)
            {
                authnRequest.ProviderName = applicationFriendlyName;
            }
            else
            {
                authnRequest.ProviderName = applicationName;
            }

            NameIDPolicyType nameIdPolicy = new NameIDPolicyType();
            nameIdPolicy.AllowCreate = true;
            nameIdPolicy.AllowCreateSpecified = true;
            authnRequest.NameIDPolicy = nameIdPolicy;

            if (null != devices)
            {
                RequestedAuthnContextType requestedAuthnContext = new RequestedAuthnContextType();
                requestedAuthnContext.Items = devices.ToArray();
                authnRequest.RequestedAuthnContext = requestedAuthnContext;
            }

            if (null != applicationPools)
            {
                ConditionsType conditions = new ConditionsType();
                AudienceRestrictionType audienceRestriction = new AudienceRestrictionType();
                audienceRestriction.Audience = applicationPools.ToArray();
                conditions.Items = new ConditionAbstractType[] { audienceRestriction };
                authnRequest.Conditions = conditions;
            }

            XmlSerializerNamespaces ns = new XmlSerializerNamespaces();
            ns.Add("samlp", Saml2Constants.SAML2_PROTOCOL_NAMESPACE);
            ns.Add("saml", Saml2Constants.SAML2_ASSERTION_NAMESPACE);

            XmlRootAttribute xRoot = new XmlRootAttribute();
            xRoot.ElementName = "AuthnRequest";
            xRoot.Namespace = Saml2Constants.SAML2_PROTOCOL_NAMESPACE;
            XmlSerializer serializer = new XmlSerializer(typeof(AuthnRequestType), xRoot);
            MemoryStream memoryStream = new MemoryStream();
            XmlTextWriter xmlTextWriter = new XmlTextWriter(memoryStream, Encoding.UTF8);
            serializer.Serialize(xmlTextWriter, authnRequest, ns);

            XmlDocument document = new XmlDocument();
            memoryStream.Seek(0, SeekOrigin.Begin);
            document.Load(memoryStream);

            string signedAuthnRequest = Saml2Util.signDocument(document, key, authnRequest.ID);
            xmlTextWriter.Close();
            return signedAuthnRequest;
        }


        /// <summary>
        /// Validates a Base64 encoded SAML v2.0 Response.
        /// </summary>
        /// <param name="encodedSamlResponse"></param>
        /// <param name="wsLocation"></param>
        /// <param name="appCertificate"></param>
        /// <param name="linkidCertificate"></param>
        /// <returns>AuthenticationProtocolContext containing linkID userId, authenticated device(s) and optional dictionary of linkID attributes</returns>
        public AuthenticationProtocolContext validateEncodedAuthnResponse(string encodedSamlResponse, string wsLocation,
                                                                         X509Certificate2 appCertificate,
                                                                         X509Certificate2 linkidCertificate)
        {
            ;
            byte[] samlResponseData = Convert.FromBase64String(encodedSamlResponse);
            string samlResponse = Encoding.UTF8.GetString(samlResponseData);
            return validateAuthnResponse(samlResponse, wsLocation, appCertificate, linkidCertificate);
        }

        /// <summary>
        /// Validates a base64 decoded SAML v2.0 Response.
        /// </summary>
        /// <param name="encodedSamlResponse"></param>
        /// <param name="wsLocation"></param>
        /// <param name="appCertificate"></param>
        /// <param name="linkidCertificate"></param>
        /// <returns>AuthenticationProtocolContext containing linkID userId, authenticated device(s) and optional dictionary of linkID attributes</returns>
        public AuthenticationProtocolContext validateAuthnResponse(string samlResponse, string wsLocation,
                                                                  X509Certificate2 appCertificate, X509Certificate2 linkidCertificate)
        {
            STSClient stsClient = new STSClientImpl(wsLocation, appCertificate, linkidCertificate);
            bool result = stsClient.validateToken(samlResponse, TrustDomainType.NODE);
            if (false == result)
            {
                return null;
            }

            XmlRootAttribute xRoot = new XmlRootAttribute();
            xRoot.ElementName = "Response";
            xRoot.Namespace = Saml2Constants.SAML2_PROTOCOL_NAMESPACE;

            TextReader reader = new StringReader(samlResponse);
            XmlSerializer serializer = new XmlSerializer(typeof(ResponseType), xRoot);
            ResponseType response = (ResponseType)serializer.Deserialize(reader);
            reader.Close();

            if (!response.InResponseTo.Equals(this.expectedChallenge))
            {
                throw new AuthenticationException("SAML response is not a response belonging to the original request.");
            }

            if (!response.Status.StatusCode.Value.Equals(Saml2Constants.SAML2_STATUS_SUCCESS))
            {
                return new AuthenticationProtocolContext(); ;
            }

            String subjectName;
            foreach (object item in response.Items)
            {
                AssertionType assertion = (AssertionType)item;
                SubjectType subject = assertion.Subject;
                NameIDType nameId = (NameIDType)subject.Items[0];
                subjectName = nameId.Value;

                AudienceRestrictionType audienceRestriction = (AudienceRestrictionType)assertion.Conditions.Items[0];
                if (null == audienceRestriction.Audience)
                {
                    throw new AuthenticationException("No Audiences found in AudienceRestriction");
                }

                if (!audienceRestriction.Audience[0].Equals(this.expectedAudience))
                {
                    throw new AuthenticationException("Audience name not correct, expected: " + this.expectedAudience);
                }

                List<String> authenticatedDevices = new List<String>();
                Dictionary<String, List<AttributeSDK>> attributes = null;
                foreach (StatementAbstractType statement in assertion.Items)
                {
                    if (statement is AttributeStatementType)
                    {
                        AttributeStatementType attributeStatement = (AttributeStatementType)statement;
                        attributes = getAttributes(attributeStatement);
                    }
                    else if (statement is AuthnStatementType)
                    {
                        AuthnStatementType authnStatement = (AuthnStatementType)statement;
                        authenticatedDevices.Add((String)authnStatement.AuthnContext.Items[0]);
                    }
                }

                return new AuthenticationProtocolContext(subjectName, authenticatedDevices, attributes);
            }

            return null;
        }

        private Dictionary<string, List<AttributeSDK>> getAttributes(AttributeStatementType attributeStatement)
        {
            Dictionary<string, List<AttributeSDK>> attributeMap = new Dictionary<string, List<AttributeSDK>>();

            foreach (object item in attributeStatement.Items)
            {
                AttributeType attributeType = (AttributeType)item;
                AttributeSDK attribute = getAttribute(attributeType);

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
            return attributeMap;
        }

        public static AttributeSDK getAttribute(AttributeType attributeType)
        {
            Boolean multivalued = isMultivalued(attributeType);
            String attributeId = getAttributeId(attributeType);
            String attributeName = attributeType.Name;

            AttributeSDK attribute = new AttributeSDK(attributeId, attributeName);

            if (attributeType.AttributeValue.Length == 0) return attribute;

            if (attributeType.AttributeValue[0] is XmlNode[])
            {
                // compound
                List<AttributeSDK> compoundMembers = new List<AttributeSDK>();
                foreach (XmlNode attributeNode in (XmlNode[])attributeType.AttributeValue[0])
                {
                    if (isAttributeElement(attributeNode))
                    {
                        AttributeSDK member = new AttributeSDK(attributeId,
                            getXmlAttribute(attributeNode, WebServiceConstants.ATTRIBUTE_NAME_ATTRIBUTE));
                        member.setValue(attributeNode.InnerText);
                        compoundMembers.Add(member);
                    }
                }
                attribute.setValue(new Compound(compoundMembers));
            }
            else if (attributeType.AttributeValue[0] is AttributeType)
            {
                // also compound but through WS
                AttributeType compoundValue = (AttributeType)attributeType.AttributeValue[0];
                List<AttributeSDK> compoundMembers = new List<AttributeSDK>();
                foreach (Object memberObject in compoundValue.AttributeValue)
                {
                    AttributeType memberType = (AttributeType)memberObject;
                    AttributeSDK member = new AttributeSDK(attributeId, memberType.Name, memberType.AttributeValue[0]);
                    compoundMembers.Add(member);
                }
                attribute.setValue(new Compound(compoundMembers));
            }
            else
            {
                // single/multi valued
                attribute.setValue(attributeType.AttributeValue[0]);
            }
            return attribute;
        }

        public static Boolean isAttributeElement(XmlNode node)
        {
            return node.NamespaceURI.Equals("urn:oasis:names:tc:SAML:2.0:assertion") && node.LocalName.Equals("Attribute");
        }

        public static String getXmlAttribute(XmlNode attributeNode, String localName)
        {
            if (null == attributeNode.Attributes) return null;

            foreach (XmlAttribute attribute in attributeNode.Attributes)
            {
                if (attribute.LocalName.Equals(localName))
                {
                    return attribute.Value;
                }
            }
            return null;
        }

        public static String getAttributeId(AttributeType attribute)
        {
            String attributeId = null;
            XmlAttribute[] xmlAttributes = attribute.AnyAttr;
            if (null != xmlAttributes)
            {
                foreach (XmlAttribute xmlAttribute in xmlAttributes)
                {
                    if (xmlAttribute.LocalName.Equals(WebServiceConstants.ATTRIBUTE_ID_ATTRIBUTE))
                    {
                        attributeId = xmlAttribute.Value;
                    }
                }
            }
            return attributeId;
        }

        public static bool isMultivalued(AttributeType attribute)
        {
            bool multivalued = false;
            XmlAttribute[] xmlAttributes = attribute.AnyAttr;
            if (null != xmlAttributes)
            {
                foreach (XmlAttribute xmlAttribute in xmlAttributes)
                {
                    if (xmlAttribute.LocalName.Equals(WebServiceConstants.MUTLIVALUED_ATTRIBUTE))
                    {
                        multivalued = Boolean.Parse(xmlAttribute.Value);
                    }
                }
            }
            return multivalued;
        }
    }
}
