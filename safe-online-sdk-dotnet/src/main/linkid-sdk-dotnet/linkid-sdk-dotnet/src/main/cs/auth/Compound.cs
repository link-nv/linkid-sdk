using System;
using System.Collections.Generic;
using System.Text;

namespace safe_online_sdk_dotnet
{
    public class Compound
    {
        private List<AttributeSDK> members;

        public Compound(List<AttributeSDK> members)
        {
            this.members = members;
        }

        /**
         * @return list of this compound value's members
         */
        public List<AttributeSDK> getMembers()
        {
            return members;
        }

    }
}
