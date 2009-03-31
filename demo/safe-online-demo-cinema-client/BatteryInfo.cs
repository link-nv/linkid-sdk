using System;

namespace dZine.Zineon
{

    public class BatteryInfo
    {
        public short temperature;
        public ushort voltage;
        public short current;
        public short averageCurrent;
        public ushort relativeStateOfCharge;
        public ushort absoluteStateOfCharge;
        public ushort remainingCapacity;
        public ushort fullChargeCapacity;
        public ushort chargingCurrent;
        public ushort chargingVoltage;
        public ushort batteryStatus;
        public ushort cycleCount;     
        public ushort designCapacity;
        public ushort designVoltage;
        public ushort manufactureDate;
        public ushort serialNumber;
        public string manufactureName;
        public string deviceName;
        public string deviceChemistryString;
        public byte deviceChemistryNumber;
        public string manufactureData;
        public ushort manufactureDay;
        public ushort manufactureMonth;
        public ushort manufactureYear;
	}
}
