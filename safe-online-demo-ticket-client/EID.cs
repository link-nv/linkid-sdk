using System;
using System.Text;
using System.Runtime.InteropServices;
using System.Drawing;
using System.IO;

namespace dZine.Zineon
{
    public class EID
    {
        #region DllImports

        /* MPA2 */

        //[DllImport("ZineonApi.dll", EntryPoint="EID_PowerUp", SetLastError=true)]
        // fix by LIN.K
        [DllImport("ZineonApi.dll", EntryPoint="EIDPowerUp", SetLastError=true)]
        private static extern int EID_PowerUp_MPA2();

        //[DllImport("ZineonApi.dll", EntryPoint="EID_PowerDown", SetLastError=true)]
        // fix by LIN.K
        [DllImport("ZineonApi.dll", EntryPoint="EIDPowerDown", SetLastError=true)]
        private static extern int EID_PowerDown_MPA2();

        //[DllImport("ZineonApi.dll", EntryPoint="EID_GetPicture", SetLastError=true)]
        // fix by LIN.K
        [DllImport("ZineonApi.dll", EntryPoint="EIDGetPicture", SetLastError=true)]
        private static extern int EID_GetPicture_MPA2(byte[] buffer, uint size);

        //[DllImport("ZineonApi.dll", EntryPoint="EID_GetPersonalDataW", SetLastError=true)]
        // fix by LIN.K
        [DllImport("ZineonApi.dll", EntryPoint="EIDGetPersonalDataW", SetLastError=true)]
        private static extern int EID_GetPersonalDataW_MPA2(byte[] buffer, uint size);

        //[DllImport("ZineonApi.dll", EntryPoint="EID_GetAddressW", SetLastError=true)]
        // fix by LIN.K
        [DllImport("ZineonApi.dll", EntryPoint="EIDGetAddressW", SetLastError=true)]
        private static extern int EID_GetAddressW_MPA2(byte[] buffer, uint size);

        /* MPA1 */

        [DllImport("bpic.dll", EntryPoint="EID_PowerUp", SetLastError=true)]
        private static extern int EID_PowerUp_MPA1();

        [DllImport("bpic.dll", EntryPoint="EID_PowerDown", SetLastError=true)]
        private static extern int EID_PowerDown_MPA1();

        [DllImport("bpic.dll", EntryPoint="EID_GetPicture", SetLastError=true)]
        private static extern int EID_GetPicture_MPA1(byte[] buffer, uint size);

        [DllImport("bpic.dll", EntryPoint="EID_GetPersonalDataW", SetLastError=true)]
        private static extern int EID_GetPersonalDataW_MPA1(byte[] buffer, uint size);

        [DllImport("bpic.dll", EntryPoint="EID_GetAddressW", SetLastError=true)]
        private static extern int EID_GetAddressW_MPA1(byte[] buffer, uint size);      
        
        #endregion

        public event EventHandler Status;

        private byte[] personalDataBuffer = new byte[1093];
        private byte[] addressBuffer = new byte[228];
        private byte[] pictureBuffer = new byte[10240];

        /*
         * This version of getData limits the amout of data retrieved from the BeID
         * in order to make it as fast as possible.
         */ 
        public void getPersonalData() {
        	int result;
        	
        	result = EID.EID_PowerUp_MPA2();
        	if (0 == result) {
        		throw new EIDException("Powerup failed");
        	}
        	
        	result = EID.EID_GetPersonalDataW_MPA2(personalDataBuffer , 1093 );
            if( result == 0 ) 
            {
                EID.EID_PowerDown_MPA2();
                throw new EIDException("Get Personal Data Failed");
            }
        	EID.EID_PowerDown_MPA2();
        }
        
        public void getData()
        {
            String MPAVersion = ZineonApi.GetMPAVersionInfo()[0];
            int result = 0;
            
            Status( this , new EIDStatusEvent(EIDStatusEvent.statusType.PoweringUp));
            if( MPAVersion.CompareTo("MPA1") == 0) 
                result = EID.EID_PowerUp_MPA1();
            else if( MPAVersion.CompareTo("MPA2") == 0 ) 
                result = EID.EID_PowerUp_MPA2();
			// begin fix by LIN.K
			else if (MPAVersion.CompareTo("2") == 0)
				result = EID.EID_PowerUp_MPA2();
			// end fix by LIN.K
            if( result == 0 ) throw new EIDException("Powerup Failed");

            Status( this , new EIDStatusEvent(EIDStatusEvent.statusType.GettingPersonalData));
            if( MPAVersion.CompareTo("MPA1") == 0) 
                result = EID.EID_GetPersonalDataW_MPA1( personalDataBuffer , 1093 );
            // fix by LIN.K
            else if( MPAVersion.CompareTo("MPA2") == 0 || MPAVersion.CompareTo("2") == 0) 
                result = EID.EID_GetPersonalDataW_MPA2( personalDataBuffer , 1093 );
            if( result == 0 ) 
            {
                powerDown(MPAVersion);
                throw new EIDException("Get Personal Data Failed");
            }

            Status( this , new EIDStatusEvent(EIDStatusEvent.statusType.GettingAddress));
            if( MPAVersion.CompareTo("MPA1") == 0) 
                result = EID.EID_GetAddressW_MPA1( addressBuffer , 228 );
            else if( MPAVersion.CompareTo("MPA2") == 0 || MPAVersion.CompareTo("2") == 0) 
                result = EID.EID_GetAddressW_MPA2( addressBuffer , 228 );
            if( result == 0 ) 
            {
                powerDown(MPAVersion);
                throw new EIDException("Get Address Failed");
            }

            Status( this , new EIDStatusEvent(EIDStatusEvent.statusType.GettingPicture));
            if( MPAVersion.CompareTo("MPA1") == 0) 
                result = EID.EID_GetPicture_MPA1( pictureBuffer , 10240 );
            else if( MPAVersion.CompareTo("MPA2") == 0 || MPAVersion.CompareTo("2") == 0) 
                result = EID.EID_GetPicture_MPA2( pictureBuffer , 10240 );
            if( result == 0 ) 
            {
                powerDown(MPAVersion);
                throw new EIDException("Get Picture Failed");
            }

            powerDown(MPAVersion);

        }

        private void powerDown(string MPAVersion)
        {
            Status( this , new EIDStatusEvent(EIDStatusEvent.statusType.PoweringDown));
            if( MPAVersion.CompareTo("MPA1") == 0)
                EID.EID_PowerDown_MPA1();
            else if( MPAVersion.CompareTo("MPA2") == 0 || MPAVersion.CompareTo("2") == 0)
                EID.EID_PowerDown_MPA2();
            Status( this , new EIDStatusEvent(EIDStatusEvent.statusType.Ready));
        }

        public string getCardNumber()
        {
            return UnicodeEncoding.Unicode.GetString( personalDataBuffer , 0 , 26 );
        }

        public string getChipNumber()
        {
            return UnicodeEncoding.Unicode.GetString( personalDataBuffer , 26 , 34 );
        }

        public string getNationalNumber()
        {
            return UnicodeEncoding.Unicode.GetString( personalDataBuffer , 60 , 24 );
        }

        public string getValidFrom()
        {
            return UnicodeEncoding.Unicode.GetString( personalDataBuffer , 84 , 22 );
        }

        public string getValidUntil()
        {
            return UnicodeEncoding.Unicode.GetString( personalDataBuffer , 106 , 22 );
        }

        public string getDeliveryMunicipality()
        {
            return UnicodeEncoding.Unicode.GetString( personalDataBuffer , 128 , 102 );
        }

        public string getLastName()
        {
            return UnicodeEncoding.Unicode.GetString( personalDataBuffer , 230 , 182 );
        }

        public string getFirstNames()
        {
            return UnicodeEncoding.Unicode.GetString( personalDataBuffer , 412 , 152 );
        }

        public string getThirdName()
        {
            return UnicodeEncoding.Unicode.GetString( personalDataBuffer , 564 , 8 );
        }

        public string getNationality()
        {
            return UnicodeEncoding.Unicode.GetString( personalDataBuffer , 572 , 132 );
        }

        public string getBirthLocation()
        {
            return UnicodeEncoding.Unicode.GetString( personalDataBuffer , 704 , 122 );
        }

        public string getBirthDate()
        {
            return UnicodeEncoding.Unicode.GetString( personalDataBuffer , 826 , 26 );
        }

        public string getSex()
        {
            return UnicodeEncoding.Unicode.GetString( personalDataBuffer , 852 , 4 );
        }

        public string getNobleCondition()
        {
            return UnicodeEncoding.Unicode.GetString( personalDataBuffer , 856 , 62 );
        }

        public string getDocumentType()
        {
            return UnicodeEncoding.Unicode.GetString( personalDataBuffer , 918 , 12 );
        }

        public string getSpecialStatus()
        {
            return UnicodeEncoding.Unicode.GetString( personalDataBuffer , 930 , 14 );
        }

        public byte[] getHashPhoto()
        {
            byte[] tmp = new byte[21];
            Array.Copy( personalDataBuffer , 944 , tmp , 0 , 21 );
            return tmp;
        }

        public byte[] getpSignature()
        {
            byte[] tmp = new byte[128];
            Array.Copy( personalDataBuffer , 965 , tmp , 0 , 128 );
            return tmp;
        }   

        public string getStreetNumber()
        {
            return UnicodeEncoding.Unicode.GetString( addressBuffer , 0 , 122 );
        }

        public string getZipCode()
        {
            return UnicodeEncoding.Unicode.GetString( addressBuffer , 122 , 10 );
        }

        public string getMunicipality()
        {
            return UnicodeEncoding.Unicode.GetString( addressBuffer , 132 , 96 );
        }

        public Image getPicture()
        {
            return new Bitmap( new MemoryStream(pictureBuffer) );
        }
    }

    public class EIDException : Exception
    {
        public EIDException(string msg) : base(msg) {}
    }

    public class EIDStatusEvent : EventArgs
    {
        public enum statusType
        {
            PoweringUp,
            PoweringDown,
            GettingPersonalData,
            GettingAddress,
            GettingPicture,
            Ready
        }

        public statusType status;

        public EIDStatusEvent(statusType stat)
        {
            status = stat;
        }
    }
}
