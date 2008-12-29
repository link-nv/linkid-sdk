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
using System.Collections.Generic;
using DataWSNamespace;


namespace safe_online_sdk_dotnet
{
	/// <summary>
	/// Client implementation of the OLAS Data Web Service.
	/// 
	/// This is only a proof-of-concept implementation.
	/// </summary>
	public class DataClientImpl : DataClient
	{
		private DataServicePortClient client;
		
		public DataClientImpl(string location, string appPfxPath, string appPfxPassword, string olasCertPath)
		{
			X509Certificate2 appCertificate = new X509Certificate2(appPfxPath, appPfxPassword);
			X509Certificate2 olasCertificate = new X509Certificate2(olasCertPath);
			
			ServicePointManager.ServerCertificateValidationCallback = 
				new RemoteCertificateValidationCallback(WCFUtil.AnyCertificateValidationCallback);
			string address = "https://" + location + "/safe-online-ws/data";
			EndpointAddress remoteAddress = new EndpointAddress(address);
			
			this.client = new DataServicePortClient(new OlasBinding(olasCertificate), remoteAddress);
			
			this.client.ClientCredentials.ClientCertificate.Certificate = appCertificate;
			this.client.ClientCredentials.ServiceCertificate.DefaultCertificate = olasCertificate;
			// To override the validation for our self-signed test certificates
			this.client.ClientCredentials.ServiceCertificate.Authentication.CertificateValidationMode = X509CertificateValidationMode.None;
			
			this.client.Endpoint.Contract.ProtectionLevel = ProtectionLevel.Sign;
			this.client.Endpoint.Contract.Behaviors.Add(new SignTargetIdentityBehavior());
			this.client.Endpoint.Behaviors.Add(new LoggingBehavior());
		}
		
		public void setAttributeValue(string userId, string attributeName, object attributeValue) {
			setTargetIdentity(userId);

			ModifyType modify = new ModifyType();
			ModifyItemType modifyItem = new ModifyItemType();
			modifyItem.objectType = DataServiceConstants.ATTRIBUTE_OBJECT_TYPE;
			
			SelectType select = new SelectType();
			select.Value = attributeName;
			modifyItem.Select = select;
			
			AppDataType newData = new AppDataType();
			AttributeType attribute = new AttributeType();
			attribute.Name = attributeName;
			setAttributeValue(attributeValue, attribute, false);
			newData.Attribute = attribute;
			modifyItem.NewData = newData;
			
			modify.ModifyItem = new ModifyItemType[]{modifyItem};
			
			ModifyResponseType response = this.client.Modify(modify);
			
			StatusType status = response.Status;
			Console.WriteLine("status: " + status.code);
			if ( ! status.code.Equals(DataServiceConstants.TOPLEVEL_STATUS_CODE_OK) ) {
				if ( null != status.Status && status.Status.Length > 0 ) {
					StatusType secondLevelStatus = status.Status[0];
					Console.WriteLine("second level status: " + secondLevelStatus.code);
					Console.WriteLine("second level status comment: " + secondLevelStatus.comment);
					if ( secondLevelStatus.code.Equals(DataServiceConstants.SECONDLEVEL_STATUS_CODE_INVALID_DATA) ) {
						throw new RuntimeException("attribute value type incorrect");
					} else if ( secondLevelStatus.code.Equals(DataServiceConstants.SECONDLEVEL_STATUS_CODE_DOES_NOT_EXIST) ) {
						throw new AttributeNotFoundException();
					}
				}
			Console.WriteLine("status comment: " + status.comment);
			throw new RuntimeException("could not set the attribute");
			}		
		}
		
		public T getAttributeValue<T>(string userId, string attributeName) {
			Type valueType = typeof(T);
			
			setTargetIdentity(userId);

			// create query
			QueryType query = new QueryType();
			QueryItemType queryItem = new QueryItemType();
			query.QueryItem = new QueryItemType[] {queryItem};
			
			queryItem.objectType = DataServiceConstants.ATTRIBUTE_OBJECT_TYPE;
			SelectType select = new SelectType();
			select.Value = attributeName;
			queryItem.Select = select;
			
			// do query
			QueryResponseType response = this.client.Query(query);
			
			// check status
			StatusType status = response.Status;
			Console.WriteLine("status: " + status.code);
			if ( status.code.Equals(DataServiceConstants.TOPLEVEL_STATUS_CODE_FAILED) ) {
				if ( null == status.Status || status.Status.Length == 0 ) {
					throw new RuntimeException("ID-WSF DST error");
				}
				StatusType secondLevelStatus = status.Status[0];
				if ( secondLevelStatus.code.Equals(DataServiceConstants.SECONDLEVEL_STATUS_CODE_NOT_AUTHORIZED) ) {
					throw new RequestDeniedException();
				} else if ( secondLevelStatus.code.Equals(DataServiceConstants.SECONDLEVEL_STATUS_CODE_DOES_NOT_EXIST) ) {
					throw new SubjectNotFoundException();
				}
				throw new RuntimeException("unknown error occurred");
			} else if (status.code.Equals(DataServiceConstants.TOPLEVEL_STATUS_CODE_OK) ) {
				// do nothing
			} else {
				throw new RuntimeException("Unknown top level statuc code: " + status.code);
			}
			
			// get value
			if ( null == response.Data || response.Data.Length == 0 ) {
				Console.WriteLine("no data entry");
				return default(T);
			}
			AttributeType attribute = response.Data[0].Attribute;
			if ( null == attribute ) {
				return default(T);
			}
			
			if ( isMultivalued(attribute) ^ valueType.IsArray) {
				throw new RuntimeException("multivalued and [] type mismatch");
			}

			object[] attributeValues = attribute.AttributeValue;
			if ( null == attribute.AttributeValue[0]) {
				return default(T);
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

						String attributeId = getCompoundAttributeId(compoundAttribute);
						if ( null != attributeId ) {
							map.Add(WebServiceConstants.ATTRIBUTE_ID_KEY, attributeId);
						}
						
						if ( null != compoundAttribute.AttributeValue ) {
							foreach(object memberAttributeObject in compoundAttribute.AttributeValue ) {
								AttributeType memberAttribute = (AttributeType) memberAttributeObject;
								string memberName = memberAttribute.Name;
								object memberValue = memberAttribute.AttributeValue[0];
								map.Add(memberName, memberValue);
							}
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
		
		public void createAttribute(string userId, string attributeName, object attributeValue) {

			setTargetIdentity(userId);

			CreateType create = new CreateType();
			CreateItemType createItem = new CreateItemType();
			create.CreateItem = new CreateItemType[] {createItem};
			
			createItem.objectType = DataServiceConstants.ATTRIBUTE_OBJECT_TYPE;
			AppDataType newData = new AppDataType();
			AttributeType attribute = new AttributeType();
			attribute.Name = attributeName;
			setAttributeValue(attributeValue, attribute, true);
			newData.Attribute = attribute;
			createItem.NewData = newData;
			
			CreateResponseType response = this.client.Create(create);
			
			StatusType status = response.Status;
			Console.WriteLine("status: " + status.code);
			if ( !status.code.Equals(DataServiceConstants.TOPLEVEL_STATUS_CODE_OK) ) {
				throw new RuntimeException("error occurred while creating attribute");
			}
		}
		
		public void removeAttribute(string userId, string attributeName, string attributeId) {
			Console.WriteLine("remove attribute " + attributeName + " for subject " + userId);
			setTargetIdentity(userId);
			
			DeleteType delete = new DeleteType();
			DeleteItemType deleteItem = new DeleteItemType();
			deleteItem.objectType = DataServiceConstants.ATTRIBUTE_OBJECT_TYPE;
			SelectType select = new SelectType();
			select.Value = attributeName;
			if ( null != attributeId ) {
				setCompoundId(select, attributeId);
			}
			deleteItem.Select = select;
			delete.DeleteItem = new DeleteItemType[]{deleteItem};
			
			DeleteResponseType response = this.client.Delete(delete);
			
			StatusType status = response.Status;
			Console.WriteLine("status: " + status.code);
			if ( !status.code.Equals(DataServiceConstants.TOPLEVEL_STATUS_CODE_OK) ) {
				throw new RuntimeException("error occurred while removing attribute: " + status.comment);
			}		
		}
		
		private void setAttributeValue(object attributeValue, AttributeType targetAttribute, bool isNewAttribute) {
			if ( null == attributeValue ) {
				return;
			}
			if ( attributeValue is Dictionary<string, object> ) {
				// compounded attribute
				AttributeType compoundAttribute = 
					createCompoundAttribute((Dictionary<string, object>)attributeValue, isNewAttribute);
				targetAttribute.AttributeValue = new object[]{compoundAttribute};
				return;
			} else if ( attributeValue.GetType().IsArray ) {
				setMultivalued(targetAttribute);
				if ( attributeValue.GetType().GetElementType().IsInstanceOfType(DateTime.UtcNow) ) {
					// convert array member per member if DateTime as this is a struct and cannot convert from DateTime[] to Object[] cause of this.
					DateTime[] values = (DateTime[]) attributeValue;
					object[] attributeValues = new object[values.Length];
					for (int idx = 0; idx < values.Length; idx++ ) {
						attributeValues[idx] = (object) values[idx];
					}
					targetAttribute.AttributeValue = attributeValues;
				} else {
					targetAttribute.AttributeValue = (object[])attributeValue;
				}
			} else {
				targetAttribute.AttributeValue = new object[]{attributeValue};
			}
		}
		
		private AttributeType createCompoundAttribute(Dictionary<string, object> attributeValueMap, 
		                                              bool isNewAttribute) {
			AttributeType compoundAttribute = new AttributeType();
			if ( false == isNewAttribute && ! attributeValueMap.ContainsKey(WebServiceConstants.ATTRIBUTE_ID_KEY) ) {
				throw new RuntimeException("Missing " + WebServiceConstants.ATTRIBUTE_ID_KEY + 
				                           " entry in compound dictionary value map");
			}
			if ( attributeValueMap.ContainsKey(WebServiceConstants.ATTRIBUTE_ID_KEY) ) {
				setCompoundId(compoundAttribute, (string) attributeValueMap[WebServiceConstants.ATTRIBUTE_ID_KEY]);
			}
			
			List<AttributeType> attributeValues = new List<AttributeType>();
			foreach(string key in attributeValueMap.Keys) {
				if ( ! key.Equals(WebServiceConstants.ATTRIBUTE_ID_KEY) ) {
					AttributeType memberAttribute = new AttributeType();
					memberAttribute.Name = key;
					memberAttribute.AttributeValue = new object[] {attributeValueMap[key]};
					attributeValues.Add(memberAttribute);
				}
			}
			compoundAttribute.AttributeValue = attributeValues.ToArray();
			return compoundAttribute;	
		}
		
		private void setCompoundId(AttributeType attribute, string id) {
			XmlDocument d = new XmlDocument();
			attribute.AnyAttr = new XmlAttribute[1];
			attribute.AnyAttr[0] = d.CreateAttribute(WebServiceConstants.SAFE_ONLINE_SAML_PREFIX, 
			                                         WebServiceConstants.COMPOUND_ATTRIBUTE_ID, 
			                                         WebServiceConstants.SAFE_ONLINE_SAML_NAMESPACE);
			attribute.AnyAttr[0].Value = id;
		}

		private void setCompoundId(SelectType select, string id) {
			XmlDocument d = new XmlDocument();
			select.AnyAttr = new XmlAttribute[1];
			select.AnyAttr[0] = d.CreateAttribute(WebServiceConstants.SAFE_ONLINE_SAML_PREFIX, 
			                                         WebServiceConstants.COMPOUND_ATTRIBUTE_ID, 
			                                         WebServiceConstants.SAFE_ONLINE_SAML_NAMESPACE);
			select.AnyAttr[0].Value = id;
		}

		private void setMultivalued(AttributeType attribute) {
			XmlDocument d = new XmlDocument();
			attribute.AnyAttr = new XmlAttribute[1];
			attribute.AnyAttr[0] = d.CreateAttribute(WebServiceConstants.SAFE_ONLINE_SAML_PREFIX, 
			                                         WebServiceConstants.MUTLIVALUED_ATTRIBUTE, 
			                                         WebServiceConstants.SAFE_ONLINE_SAML_NAMESPACE);
			attribute.AnyAttr[0].Value = "true";
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
		
		private string getCompoundAttributeId(AttributeType compoundAttribute) {
			XmlAttribute[] xmlAttributes = compoundAttribute.AnyAttr;
			if ( null != xmlAttributes ) {
				foreach(XmlAttribute xmlAttribute in xmlAttributes) {
					if ( xmlAttribute.LocalName.Equals(WebServiceConstants.COMPOUND_ATTRIBUTE_ID) ) {
						return xmlAttribute.Value;
					}
				}
			}
			return null;		
		}
		
		private void setTargetIdentity(string userId) {
			// Add TargetIdentity SOAP header
			this.client.Endpoint.Behaviors.RemoveAll<TargetIdentityBehavior>();
			this.client.Endpoint.Behaviors.Add(new TargetIdentityBehavior(userId));

		}
	}
}
