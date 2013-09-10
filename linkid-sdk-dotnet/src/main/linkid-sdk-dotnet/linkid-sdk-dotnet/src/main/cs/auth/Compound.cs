using System;
using System.Collections.Generic;
using System.Text;

namespace safe_online_sdk_dotnet
{
    public class Compound
    {
        public Dictionary<String, AttributeSDK> membersMap { get; set; }
        public List<AttributeSDK> members { get; set; }

        public Compound(List<AttributeSDK> members)
        {
            this.members = members;
            membersMap = new Dictionary<string,AttributeSDK>();
            foreach (AttributeSDK member in members)
            {
                membersMap.Add(member.getAttributeName(), member);
            }
        }

    }
}
