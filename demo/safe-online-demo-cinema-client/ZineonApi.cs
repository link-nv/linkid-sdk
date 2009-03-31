using System;
using System.Runtime.InteropServices;
using System.Threading;
using System.Text;

namespace dZine.Zineon
{

/// <summary>
/// 
/// </summary>
    public class ZineonApi
    {
        /// <summary>
        /// PCMCIA config flags
        /// </summary>
        public const uint PCMCIA_ENABLE = 0x0001;
        public const uint PCMCIA_CARDPRESENT = 0x0002;
        public const uint PCMCIA_POWERED = 0x0004;    
        public const uint PCMCIA_BLOCK_SUSPEND = 0x0010;
        public const uint PCMCIA_POWERONRESUME = 0x0020;

        /// <summary>
        /// Battery Status - Alarm bits
        /// </summary>
        public const uint OVER_CHARGED_ALARM = 0x8000;
        public const uint TERMINATE_CHARGE_ALARM = 0x4000;
        public const uint OVER_TEMP_ALARM = 0x1000;
        public const uint TERMINATE_DISCHARGE_ALARM = 0x0800;
        public const uint REMAINING_CAPACITY_ALARM = 0x0200;
        public const uint REMAINING_TIME_ALARAM = 0x0100;

        /// <summary>
        /// Battery Status - Status bits
        /// </summary>
        public const uint INITIALIZED = 0x0080;
        public const uint DISCHARGING = 0x0040;
        public const uint FULLY_CHARGED = 0x0020;
        public const uint FULLY_DISCHARGED = 0x0010;
    
        /// <summary>
        /// Battery Status - Error codes
        /// </summary>
        public const uint UNKNOWN_ERROR = 0x0007;
        public const uint BAD_SIZE = 0x0006;
        public const uint OVERFLOW_UNDERFLOW = 0x0005;
        public const uint ACCESS_DENIED = 0x0004;
        public const uint UNSUPPORTED_COMMAND = 0x0003;
        public const uint BUSY = 0x0001;
        public const uint OK = 0x0000;

        /// <summary>
        /// Windows events
        /// </summary>
        public const uint WM_USER = 0x0400;
        public const uint WM_BUTTON_EVENT = WM_USER + 2001;
        
        [ StructLayout( LayoutKind.Sequential ) ]    
        private struct LocalPCMCIAParams
        {
            public UInt32 dwSlot; 
            public UInt32 dwConfig;
            public UInt32 dwManufacturerSize;
            public IntPtr wcsManufacturer;
            public UInt32 dwProductSize;
            public IntPtr wcsProduct; 
        }

        [ StructLayout( LayoutKind.Sequential ) ]
        private struct LocalBatteryInfo
        {
            public UInt16 usTemperature;
            public UInt16 usVoltage;
            public Int16 nCurrent;
            public Int16 nAverageCurrent;
            public UInt16 usRelativeStateOfCharge;
            public UInt16 usAbsoluteStateOfCharge;
            public UInt16 usRemainingCapacity;
            public UInt16 usFullChargeCapacity;
            public UInt16 usChargingCurrent;
            public UInt16 usChargingVoltage;
            public UInt16 usBatteryStatus;
            public UInt16 usCycleCount;     
            public UInt16 usDesignCapacity;
            public UInt16 usDesignVoltage;
            public UInt16 usManufactureDate;
            public UInt16 usSerialNumber;
            public Byte mfn1;
            public Byte mfn2;
            public Byte mfn3;
            public Byte mfn4;
            public Byte mfn5;
            public Byte mfn6;
            public Byte mfn7;
            public Byte mfn8;
            public Byte mfn9;
            public Byte mfn10;
            public Byte mfn11;
            public Byte mfn12;
            public Byte dn1;
            public Byte dn2;
            public Byte dn3;
            public Byte dn4;
            public Byte dn5;
            public Byte dn6;
            public Byte dn7;
            public Byte dn8;
            public Byte dc1;
            public Byte dc2;
            public Byte dc3;
            public Byte dc4;
            public Byte dc5;
            public Byte ucDeviceChemistry;
            public Byte mfd1;
            public Byte mfd2;
            public Byte mfd3;
            public Byte mfd4;
            public Byte mfd5;
            public Byte mfd6;
            public Byte mfd7;
            public Byte mfd8;
            public UInt16 usManufactureDay;
            public UInt16 usManufactureMonth;
            public UInt16 usManufactureYear;
        }

        [ StructLayout( LayoutKind.Sequential ) ]
        private struct NDISUIO_QUERY_OID
        {
            public uint oid;
            public string deviceName;
        }

    #region DllImports

    [DllImport("MPASystem.dll", EntryPoint="GetPcmciaParams", SetLastError=true)]
    private static extern int GetPcmciaParamsUnsafe(ref LocalPCMCIAParams pcmciaParams);    

    [DllImport("MPASystem.dll", EntryPoint="SetPcmciaParams", SetLastError=true)]
    private static extern int SetPcmciaParamsUnsafe(ref LocalPCMCIAParams pcmciaParams);
    
    [DllImport ("MPASystem.dll", EntryPoint="BatteryGetInfo", SetLastError=true)]
    private static extern int BatteryGetInfoUnsafe(ref LocalBatteryInfo batteryInfo);

    [DllImport ("MPASystem.dll", EntryPoint="EnableDisplay", SetLastError=true)]
    private static extern int EnableDisplayUnsafe(int bOn);

    [DllImport ("MPASystem.dll", EntryPoint="IsDisplayEnabled", SetLastError=true)]
    private static extern int IsDisplayEnabledUnsafe();

    [DllImport ("MPASystem.dll", EntryPoint="SystemGetRotation", SetLastError=true)]
    private static extern int SystemGetRotationUnsafe();

    [DllImport ("MPASystem.dll", EntryPoint="SystemSetRotation", SetLastError=true)]
    private static extern int SystemSetRotationUnsafe(int rotation);

    /*
     * Voor backlight intensiteit aan te passen, zie vss:\projects.ce\wince500\platform\dzine_zineon_3_0\src\advbacklightdll\advbacklight.cpp line 496 -> 515
     */

    [DllImport ("MPASystem.dll", EntryPoint="GetMPAVersionInfo", SetLastError=true)]
    private static extern int GetMPAVersionInfoUnsafe(byte[] buffer);

    [DllImport ("MPASystem.dll", EntryPoint="GetRomVersionBootloader", SetLastError=true)]
    private static extern int GetRomVersionBootloaderUnsafe(byte[] buffer);

    [DllImport ("MPASystem.dll", EntryPoint="GetRomVersionKernel", SetLastError=true)]
    private static extern int GetRomVersionKernelUnsafe(byte[] buffer);

    #endregion

    public static bool GetPcmciaParams(PCMCIAParams pcmciaParams)
    {
        LocalPCMCIAParams localPCParams = new LocalPCMCIAParams();

        localPCParams.dwSlot = pcmciaParams.slot;
        localPCParams.wcsManufacturer = CoreDll.LocalAlloc(CoreDll.LPTR , 80);
        localPCParams.dwManufacturerSize = 80;
        localPCParams.wcsProduct = CoreDll.LocalAlloc(CoreDll.LPTR , 80);
        localPCParams.dwProductSize = 80;

        int result = GetPcmciaParamsUnsafe(ref localPCParams);
        
        if(result>0)
        {
            pcmciaParams.config = localPCParams.dwConfig;
            pcmciaParams.manufacturer = Memory.ptrToString( localPCParams.wcsManufacturer, 40 );
            pcmciaParams.product = Memory.ptrToString( localPCParams.wcsProduct, 40 );

            CoreDll.LocalFree( localPCParams.wcsManufacturer );
            CoreDll.LocalFree( localPCParams.wcsProduct );

            return true;
        }

        CoreDll.LocalFree( localPCParams.wcsManufacturer );
        CoreDll.LocalFree( localPCParams.wcsProduct );

        return false;
    }

    public static bool SetPcmciaParams(PCMCIAParams pcmciaParams)
    {
        LocalPCMCIAParams localPCParams = new LocalPCMCIAParams();
        localPCParams.dwSlot = pcmciaParams.slot;
        localPCParams.dwConfig = pcmciaParams.config;
        localPCParams.dwManufacturerSize = 0;
        localPCParams.dwProductSize = 0;
        
        int result = SetPcmciaParamsUnsafe(ref localPCParams);
        // We must sleep a bit to prevent memory access violation...
        Thread.Sleep(500);

        return (result>0 ? true:false);
    }

    public static bool BatteryGetInfo(BatteryInfo batInfo) 
    {
        LocalBatteryInfo localBatInfo = new LocalBatteryInfo();
        int result = BatteryGetInfoUnsafe(ref localBatInfo);
        if(result>0)
        {
            batInfo.temperature = (short)( (localBatInfo.usTemperature - 2731.5) / 10 );
            batInfo.voltage = localBatInfo.usVoltage;
            batInfo.current = localBatInfo.nCurrent;
            batInfo.averageCurrent = localBatInfo.nAverageCurrent;
            batInfo.relativeStateOfCharge = localBatInfo.usRelativeStateOfCharge;
            batInfo.absoluteStateOfCharge = localBatInfo.usAbsoluteStateOfCharge;
            batInfo.remainingCapacity = localBatInfo.usRemainingCapacity;
            batInfo.fullChargeCapacity = localBatInfo.usFullChargeCapacity;
            batInfo.chargingCurrent = localBatInfo.usChargingCurrent;
            batInfo.chargingVoltage = localBatInfo.usChargingVoltage;
            batInfo.batteryStatus = localBatInfo.usBatteryStatus;
            batInfo.cycleCount = localBatInfo.usCycleCount;     
            batInfo.designCapacity = localBatInfo.usDesignCapacity;
            batInfo.designVoltage = localBatInfo.usDesignVoltage;
            batInfo.manufactureDate = localBatInfo.usManufactureDate;
            batInfo.serialNumber = localBatInfo.usSerialNumber;

            char[] buffer = new char[12];

            buffer[0] = (char)localBatInfo.mfn1;
            buffer[1] = (char)localBatInfo.mfn2;
            buffer[2] = (char)localBatInfo.mfn3;
            buffer[3] = (char)localBatInfo.mfn4;
            buffer[4] = (char)localBatInfo.mfn5;
            buffer[5] = (char)localBatInfo.mfn6;
            buffer[6] = (char)localBatInfo.mfn7;
            buffer[7] = (char)localBatInfo.mfn8;
            buffer[8] = (char)localBatInfo.mfn9;
            buffer[9] = (char)localBatInfo.mfn10;
            buffer[10] = (char)localBatInfo.mfn11;
            buffer[11] = (char)localBatInfo.mfn12;
            batInfo.manufactureName = new string(buffer);

            buffer[0] = (char)localBatInfo.dn1;
            buffer[1] = (char)localBatInfo.dn2;
            buffer[2] = (char)localBatInfo.dn3;
            buffer[3] = (char)localBatInfo.dn4;
            buffer[4] = (char)localBatInfo.dn5;
            buffer[5] = (char)localBatInfo.dn6;
            buffer[6] = (char)localBatInfo.dn7;
            buffer[7] = (char)localBatInfo.dn8;
            batInfo.deviceName = new string( buffer, 0 , 8);

            buffer[0] = (char)localBatInfo.dc1;
            buffer[1] = (char)localBatInfo.dc2;
            buffer[2] = (char)localBatInfo.dc3;
            buffer[3] = (char)localBatInfo.dc4;
            buffer[4] = (char)localBatInfo.dc5;
            batInfo.deviceChemistryString = new string( buffer , 0 , 5);

            batInfo.deviceChemistryNumber = localBatInfo.ucDeviceChemistry;

            buffer[0] = (char)localBatInfo.mfd1;
            buffer[1] = (char)localBatInfo.mfd2;
            buffer[2] = (char)localBatInfo.mfd3;
            buffer[3] = (char)localBatInfo.mfd4;
            buffer[4] = (char)localBatInfo.mfd5;
            buffer[5] = (char)localBatInfo.mfd6;
            buffer[6] = (char)localBatInfo.mfd7;
            buffer[7] = (char)localBatInfo.mfd8;
            batInfo.manufactureData = new string( buffer, 0 , 8);

            batInfo.manufactureDay = localBatInfo.usManufactureDay;
            batInfo.manufactureMonth = localBatInfo.usManufactureMonth;
            batInfo.manufactureYear = localBatInfo.usManufactureYear;

            return true;
        }

        return false;
    }

    public static bool EnableDisplay(bool on)
    {
        int BOOL;
        BOOL = (on ? 1 : 0) ;
        return ( EnableDisplayUnsafe(BOOL)>0 ? true : false );
    }

    public static bool IsDisplayEnabled()
    {
        return ( IsDisplayEnabledUnsafe()>0 ? true : false );
    }

    public static int SystemGetRotation()
    {
        return SystemGetRotationUnsafe();
    }

    public static bool SystemSetRotation(int rotation)
    {
        return ( SystemSetRotationUnsafe(rotation)>0 ? true : false );
    }

    public static string[] GetMPAVersionInfo()
    {
        string[] tmpStrings = new string[3];
        byte[] tmpBytes = new byte[49];
        GetMPAVersionInfoUnsafe(tmpBytes);
        tmpStrings[0] = System.Text.ASCIIEncoding.ASCII.GetString(tmpBytes , 0 , 25);
        tmpStrings[1] = System.Text.ASCIIEncoding.ASCII.GetString(tmpBytes , 25 , 12);
        tmpStrings[2] = System.Text.ASCIIEncoding.ASCII.GetString(tmpBytes , 37 , 12);
        return tmpStrings;
    }

    public static string[] GetRomVersionKernel()
    {
        string[] tmpStrings = new string[3];
        byte[] tmpBytes = new byte[40];
        GetRomVersionKernelUnsafe(tmpBytes);
        tmpStrings[0] = System.Text.ASCIIEncoding.ASCII.GetString(tmpBytes , 0 , 16);
        tmpStrings[1] = System.Text.ASCIIEncoding.ASCII.GetString(tmpBytes , 16 , 12);
        tmpStrings[2] = System.Text.ASCIIEncoding.ASCII.GetString(tmpBytes , 28 , 12);
        return tmpStrings;
    }

    public static string[] GetRomVersionBootloader()
    {
        string[] tmpStrings = new string[3];
        byte[] tmpBytes = new byte[40];
        GetRomVersionBootloaderUnsafe(tmpBytes);
        tmpStrings[0] = System.Text.ASCIIEncoding.ASCII.GetString(tmpBytes , 0 , 16);
        tmpStrings[1] = System.Text.ASCIIEncoding.ASCII.GetString(tmpBytes , 16 , 12);
        tmpStrings[2] = System.Text.ASCIIEncoding.ASCII.GetString(tmpBytes , 28 , 12);
        return tmpStrings;
    }

}

}
