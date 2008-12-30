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
	/// Client implementation for the OLAS Attribute Web Service.
	/// </summary>
	public class AttributeClientImpl : AttributeClient
	{
		private SAMLAttributePortClient client;
		
		public AttributeClientImpl(string location, string appPfxPath, string appPfxPassword, string olasCertPath)
		{
			X509Certificate2 appCertificate = new X509Certificate2(appPfxPath, appPfxPassword);
			X509Certificate2 olasCertificate = new X509Certificate2(olasCertPath);

			ServicePointManager.ServerCertificateValidationCallback = 
				new RemoteCertificateValidationCallback(WCFUtil.AnyCertificateValidationCallback);
			string address = "https://" + location + "/safe-online-ws/attrib";
			EndpointAddress remoteAddress = new EndpointAddress(address);
			
			this.client = new SAMLAttributePortClient(new OlasBinding(olasCertificate), remoteAddress);
			
	
			this.client.ClientCredentials.ClientCertificate.Certificate = appCertificate;
			this.client.ClientCredentials.ServiceCertificate.DefaultCertificate = olasCertificate;
			// To override the validation for our self-signed test certificates
			this.client.ClientCredentials.ServiceCertificate.Authentication.CertificateValidationMode = X509CertificateValidationMode.None;
			
			this.client.Endpoint.Contract.ProtectionLevel = ProtectionLevel.Sign;

		}
		
		public T getAttributeValue<T>(string userId, string attributeName) {
			AttributeQueryType request = getAttributeQuery(userId, new string[] {attributeName});
			ResponseType response = getResponse(request);
			checkStatus(response);
			return getAttributeValue<T>(response);
		}
		
		public void getAttributeValues(String userId, Dictionary<string, object> attributes) {
			
			List<string> attributeNames = new List<string>();
			foreach(string attributeName in attributes.Keys) {
				attributeNames.Add(attributeName);
			}
			AttributeQueryType request = getAttributeQuery(userId, attributeNames.ToArray());
			ResponseType response = getResponse(request);
			checkStatus(response);
			getAttributeValues(response, attributes);
		}
		
		public Dictionary<string, object> getAttributeValues(string userId) {
			Dictionary<string, object> attributes = new Dictionary<string, object>();
			AttributeQueryType request = getAttributeQuery(userId, null);
			ResponseType response = getResponse(request);
			checkStatus(response);
			getAttributeValues(response, attributes);
			return attributes;
		}
		
		private AttributeQueryType getAttributeQuery(string userId, string[] attributeNames) {
			AttributeQueryType attributeQuery = new AttributeQueryType();
			
			SubjectType subject = new SubjectType();
			NameIDType subjectName = new NameIDType();
			subjectName.Value = userId;
			subject.Items = new Object[] {subjectName};
			attributeQuery.Subject = subject;
			
			if ( null != attributeNames ) {
				List<AttributeType> attributes = new List<AttributeType>();
				foreach(string attributeName in attributeNames) {
					AttributeType attribute = new AttributeType();
					attribute.Name = attributeName;
					attributes.Add(attribute);
				}
				attributeQuery.Attribute = attributes.ToArray();
			}
			return attributeQuery;
		}
		
		private ResponseType getResponse(AttributeQueryType request) {
			return this.client.AttributeQuery(request);
		}
		
		private void checkStatus(ResponseType response) {
			StatusType status = response.Status;
			StatusCodeType statusCode = status.StatusCode;
			if ( ! statusCode.Value.Equals(Saml2Constants.SAML2_STATUS_SUCCESS) ) {
				Console.WriteLine("status code: " + statusCode.Value);
				Console.WriteLine("status message: " + status.StatusMessage);
				StatusCodeType secondLevelStatusCode = statusCode.StatusCode;
				if ( null != secondLevelStatusCode ) {
					if ( secondLevelStatusCode.Value.Equals(Saml2Constants.SAML2_STATUS_INVALID_ATTRIBUTE_NAME_OR_VALUE) ) {
						throw new AttributeNotFoundException();
					} else if (secondLevelStatusCode.Value.Equals(Saml2Constants.SAML2_STATUS_REQUEST_DENIED) ) {
						throw new RequestDeniedException();
					} else if ( secondLevelStatusCode.Value.Equals(Saml2Constants.SAML2_STATUS_ATTRIBUTE_UNAVAILABLE) ) {
						throw new AttributeUnavailableException();
					}
					Console.WriteLine("second level status code: " +secondLevelStatusCode.Value);
				}
				
				throw new RuntimeException("error: " + statusCode.Value);
			}
		}
		
		private T getAttributeValue<T>(ResponseType response) {

			Type valueType = typeof(T);
			
			if ( null == response.Items || response.Items.Length == 0 ) {
				throw new RuntimeException("No assertions in response");
			}
			AssertionType assertion = (AssertionType) response.Items[0];
			if ( null == assertion.Items || assertion.Items.Length == 0 ) {
				throw new RuntimeException("No statements in response assertion");
			}
			AttributeStatementType attributeStatement = (AttributeStatementType) assertion.Items[0];
			AttributeType attribute = (AttributeType) attributeStatement.Items[0];
			object[] attributeValues = attribute.AttributeValue;
			if ( null == attributeValues ) {
				return default(T);
			}
			
			if ( isMultivalued(attribute) ^ valueType.IsArray) {
				throw new RuntimeException("multivalued and [] type mismatch");
			}
			
			if ( valueType.IsArray) {
				/*
				 *  Multivalued attribute
				 */
				Type componentType = valueType.GetElementType();
				Array result = Array.CreateInstance(componentType, attributeValues.Length);
				
				int idx = 0;
				foreach( object attributeValue in attributeValues) {
					if ( attributeValue is AttributeType ) {
						AttributeType compoundAttribute = (AttributeType) attributeValue;
						Dictionary<string, object> map = new Dictionary<string, object>();
						if ( ! (componentType.IsInstanceOfType(map)) ) {
							throw new RuntimeException("ValueClass is not of type Dictionary<string,object>[]");
						}

						foreach(object memberAttributeObject in compoundAttribute.AttributeValue ) {
							AttributeType memberAttribute = (AttributeType) memberAttributeObject;
							string memberName = memberAttribute.Name;
							object memberValue = memberAttribute.AttributeValue[0];
							map.Add(memberName, memberValue);
						}
						result.SetValue(map, idx);
					} else {
						result.SetValue(attributeValue, idx);
					}
					idx++;
				}
				
				object t = (object) result;
				return (T) t;
			}
			
			/*
			 * Single Valued attribute
			 */
			object value = attributeValues[0];
			if ( null == value ) return default(T);
			
			if ( false == valueType.IsInstanceOfType(value) ) {
				throw new RuntimeException("expected type: " + valueType.Name + "; actual type: " + value.GetType().Name);
			}
			return (T) value;
		}
		
		private void getAttributeValues(ResponseType response, Dictionary<string, object> attributes) {
			
			if ( null == response.Items || response.Items.Length == 0 ) {
				throw new RuntimeException("No assertions in response");
			}
			AssertionType assertion = (AssertionType) response.Items[0];
			if ( null == assertion.Items || assertion.Items.Length == 0 ) {
				throw new RuntimeException("No statements in response assertion");
			}

			AttributeStatementType attributeStatement = (AttributeStatementType) assertion.Items[0];
			object[] attributeObjects = attributeStatement.Items;
			if ( null == attributeObjects ) {
				return;
			}
			
			foreach( object attributeObject in attributeObjects ) {
				AttributeType attribute = (AttributeType) attributeObject;
				string attributeName = attribute.Name;
				object[] attributeValues = attribute.AttributeValue;
				object attributeValue;
				if ( isMultivalued(attribute) ) {
					if ( attributeValues[0] is AttributeType ) {
						// compounded
						List<Dictionary<string, object>> result = new List<Dictionary<string, object>>();
						foreach(object compoundAttributeValue in attributeValues) {
							AttributeType compoundAttribute = (AttributeType) compoundAttributeValue;
							Dictionary<string, object> map = new Dictionary<string, object>();
							foreach(object memberAttributeObject in compoundAttribute.AttributeValue ) {
								AttributeType memberAttribute = (AttributeType) memberAttributeObject;
								string memberName = memberAttribute.Name;
								object memberValue = memberAttribute.AttributeValue[0];
								map.Add(memberName, memberValue);
							}
							result.Add(map);
						}
						attributeValue = result.ToArray();
					} else {
						// multi-valued not compound 
						// We use the first attribute value to determine the type of the array to be returned
						Array result = Array.CreateInstance(attributeValues[0].GetType(), attributeValues.Length);
						for(int idx = 0; idx < attributeValues.Length; idx++) {
							result.SetValue(attributeValues[idx], idx);
						}
						attributeValue = result;
					}
				} else {
					// Single-valued, here we depend on the xsi:type typing
					attributeValue = attributeValues[0];
				}
				attributes[attributeName] = attributeValue;
			}
		}
		
		private bool isMultivalued(AttributeType attribute) {
			bool multivalued = false;
			XmlAttribute[] xmlAttributes = attribute.AnyAttr;
			if ( null != xmlAttributes ) {
 				foreach(XmlAttribute xmlAttribute in xmlAttributes ) {
					if ( xmlAttribute.LocalName.Equals(WebServiceConstants.MUTLIVALUED_ATTRIBUTE) ) {
						multivalued = Boolean.Parse(xmlAttribute.Value);
					}
				}
			}
			return multivalued;
		}
	}
}
