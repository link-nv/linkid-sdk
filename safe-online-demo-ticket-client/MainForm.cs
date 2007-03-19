/*
 * Gemaakt met SharpDevelop.
 * Gebruiker: Frank Cornelis
 * Datum: 13/03/2007
 * Tijd: 16:29
 * 
 * Dit sjabloon wijzigen: Extra | Opties |Coderen | Standaard kop bewerken.
 */

using System;
using System.Collections.Generic;
using System.Drawing;
using System.Windows.Forms;
using System.Threading;
using System.Runtime.InteropServices;

using dZine.Zineon;

namespace demo_ticket_client
{
	
	/// <summary>
	/// Description of MainForm.
	/// </summary>
	public partial class MainForm : Form
	{

		public static void Main(string[] args)
		{
			Application.Run(new MainForm());
		}
		
		public MainForm()
		{
			//
			// The InitializeComponent() call is required for Windows Forms designer support.
			//
			InitializeComponent();

			LoadData();
			
			//StartSmartCardThread();
		}
		
		private void LoadData() {
			Object[] places = new Object[] {
				"Aalst",
				"Gent",
				"Brussel"
			};
			foreach (Object place in places) {
				this.fromComboBox.Items.Add(place);
				this.toComboBox.Items.Add(place);
			}
			
			this.serverAddressTextBox.Text = "192.168.1.100";
		}
		
		public void SmartCardThreadProc() {
			SetStatusLabel("Starting...");
			
			AppendOutputMessage("Opening SCR1:");
			IntPtr hDevice = CoreDll.CreateFile("SCR1:", CoreDll.GENERIC_READ, 0, IntPtr.Zero, CoreDll.OPEN_EXISTING, 0, IntPtr.Zero);
			
			// source: winsmcrd.h
			uint IOCTL_SMARTCARD_IS_PRESENT = CoreDll.CTL_CODE(CoreDll.FILE_DEVICE_SMARTCARD, 10, CoreDll.METHOD_BUFFERED, CoreDll.FILE_ANY_ACCESS);
			uint IOCTL_SMARTCARD_GET_STATE = CoreDll.CTL_CODE(CoreDll.FILE_DEVICE_SMARTCARD, 14, CoreDll.METHOD_BUFFERED, CoreDll.FILE_ANY_ACCESS);
			
			// source: ZineonApi.h
			uint IOCTL_SMARTCARD_DETECTCARD = CoreDll.CTL_CODE(CoreDll.FILE_DEVICE_SMARTCARD, 3001, CoreDll.METHOD_BUFFERED, CoreDll.FILE_ANY_ACCESS);
			uint IOCTL_SMARTCARD_GEMCORE_OPEN = CoreDll.CTL_CODE(CoreDll.FILE_DEVICE_SMARTCARD, 3031, CoreDll.METHOD_BUFFERED, CoreDll.FILE_ANY_ACCESS);
			uint IOCTL_SMARTCARD_GEMCORE_CLOSE = CoreDll.CTL_CODE(CoreDll.FILE_DEVICE_SMARTCARD, 3032, CoreDll.METHOD_BUFFERED, CoreDll.FILE_ANY_ACCESS);
			
			int nBytes;
			int result;
			//AppendOutputMessage("Open GEMCORE IOCTL");
			//result = CoreDll.DeviceIoControl(hDevice, IOCTL_SMARTCARD_GEMCORE_OPEN, null, 0, null, 0, out nBytes, IntPtr.Zero);
			//AppendOutputMessage("Open Result: " + result);
			
			//AppendOutputMessage("Close GEMCORE IOCTL");
			//result = CoreDll.DeviceIoControl(hDevice, IOCTL_SMARTCARD_GEMCORE_CLOSE, null, 0, null, 0, out nBytes, IntPtr.Zero);
			//AppendOutputMessage("Close Result: " + result);
			
			//AppendOutputMessage("Close SCR1:");
			CoreDll.CloseHandle(hDevice);
		}
		
		delegate void StringParameterDelegate(String value);
		
		private void SetStatusLabel(string value) {
			if (InvokeRequired) {
				BeginInvoke(new StringParameterDelegate(SetStatusLabel), new Object[] { value});
				return;
			}
			this.statusLabel.Text = value;
		}
		
		private void AppendOutputMessage(String message) {
			if (InvokeRequired) {
				BeginInvoke(new StringParameterDelegate(AppendOutputMessage), new Object[] { message });
				return;
			}
			this.outputTextBox.Text += message + "\r\n";
			this.outputTextBox.ScrollToCaret();
		}
		
		private void StartSmartCardThread() {
			Thread smartCardThread = new Thread(SmartCardThreadProc);
			smartCardThread.IsBackground = true;
			smartCardThread.Start();
		}
		
		void ScanButtonClick(object sender, System.EventArgs e)
		{
			SetStatusLabel("Scan...");
			
			String MPAVersion = ZineonApi.GetMPAVersionInfo()[0];
			AppendOutputMessage("MPA version: " + MPAVersion);
			
			EID eid = new EID();
			//eid.Status += new EventHandler(updateEIDStatusMessage);
			try {
			 	eid.getPersonalData(); 	
			}
			catch(EIDException ex) {
				SetStatusLabel("EID error: " + ex.Message);
				return;
			}
			
			string nrn = eid.getNationalNumber();
			SetStatusLabel("NRN: " + nrn);
		}
		
		private void updateEIDStatusMessage(object sender, System.EventArgs e)
        {
			EIDStatusEvent eidStatusEvent = (EIDStatusEvent) e;
			AppendOutputMessage("EID status event: " + eidStatusEvent.status);
		}
 	}
}
