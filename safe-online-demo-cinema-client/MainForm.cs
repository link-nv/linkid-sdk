/*
 * Gemaakt met SharpDevelop.
 * Gebruiker: Frank Cornelis, Maarten Billemont
 * Datum: 07/04/2007
 * Tijd: 10:40
 * 
 * Dit sjabloon wijzigen: Extra | Opties |Coderen | Standaard kop bewerken.
 */

using System;
using System.Collections.Generic;
using System.Drawing;
using System.Windows.Forms;
using System.Threading;
using System.Runtime.InteropServices;
using System.Text;
using System.Net;

using dZine.Zineon;

namespace demo_cinema_client
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
			
			StartSmartCardThread();
		}
		
		private void LoadData() {
			Object[] films = new Object[] {
				"Journey to the Center of the Earth",
				"The Happening",
				"The Dark Knight",
				"Shaun Of The Dead",
				"Hellboy II: The Golden Army"
			};
			Object[] theatres = new Object[] {
				"Kinepolis Gent",
				"Kinepolis Kortrijk",
				"Kinepolis Hasselt",
				"Kinepolis Brussel",
				"Kinepolis Leuven"
			};
			
			foreach (Object film in films) {
				this.filmComboBox.Items.Add(film);
			}

			foreach (Object theatre in theatres) {
				this.theatreComboBox.Items.Add(theatre);
			}
			
			this.serverAddressTextBox.Text = "169.254.187.47";
			//this.serverAddressTextBox.Text = "192.168.5.100";
		}
		
		public void SmartCardThreadProc() {
			SetStatusLabel("Starting...");
			
			// source: winsmcrd.h
			uint IOCTL_SMARTCARD_IS_PRESENT = CoreDll.CTL_CODE(CoreDll.FILE_DEVICE_SMARTCARD, 10, CoreDll.METHOD_BUFFERED, CoreDll.FILE_ANY_ACCESS);
			uint IOCTL_SMARTCARD_GET_STATE = CoreDll.CTL_CODE(CoreDll.FILE_DEVICE_SMARTCARD, 14, CoreDll.METHOD_BUFFERED, CoreDll.FILE_ANY_ACCESS);
			
			// source: ZineonApi.h
			uint IOCTL_SMARTCARD_COMMAND = CoreDll.CTL_CODE(CoreDll.FILE_DEVICE_SMARTCARD, 3000, CoreDll.METHOD_BUFFERED, CoreDll.FILE_ANY_ACCESS);
			uint IOCTL_SMARTCARD_DETECTCARD = CoreDll.CTL_CODE(CoreDll.FILE_DEVICE_SMARTCARD, 3001, CoreDll.METHOD_BUFFERED, CoreDll.FILE_ANY_ACCESS);
			
			uint IOCTL_SMARTCARD_GEMCORE_OPEN = CoreDll.CTL_CODE(CoreDll.FILE_DEVICE_SMARTCARD, 3031, CoreDll.METHOD_BUFFERED, CoreDll.FILE_ANY_ACCESS);
			uint IOCTL_SMARTCARD_GEMCORE_CLOSE = CoreDll.CTL_CODE(CoreDll.FILE_DEVICE_SMARTCARD, 3032, CoreDll.METHOD_BUFFERED, CoreDll.FILE_ANY_ACCESS);
			uint IOCTL_SMARTCARD_GEMCORE_SPEED = CoreDll.CTL_CODE(CoreDll.FILE_DEVICE_SMARTCARD, 3030, CoreDll.METHOD_BUFFERED, CoreDll.FILE_ANY_ACCESS);
			
			uint IOCTL_SMARTCARD_7816_POWERUP = CoreDll.CTL_CODE(CoreDll.FILE_DEVICE_SMARTCARD, 3020, CoreDll.METHOD_BUFFERED, CoreDll.FILE_ANY_ACCESS);
			uint IOCTL_SMARTCARD_7816_POWERDOWN = CoreDll.CTL_CODE(CoreDll.FILE_DEVICE_SMARTCARD, 3021, CoreDll.METHOD_BUFFERED, CoreDll.FILE_ANY_ACCESS);
			uint IOCTL_SMARTCARD_7816_SELECT_DF = CoreDll.CTL_CODE(CoreDll.FILE_DEVICE_SMARTCARD, 3022, CoreDll.METHOD_BUFFERED, CoreDll.FILE_ANY_ACCESS);
			uint IOCTL_SMARTCARD_7816_SELECT_EF = CoreDll.CTL_CODE(CoreDll.FILE_DEVICE_SMARTCARD, 3023, CoreDll.METHOD_BUFFERED, CoreDll.FILE_ANY_ACCESS);
			uint IOCTL_SMARTCARD_7816_READ_BINARY = CoreDll.CTL_CODE(CoreDll.FILE_DEVICE_SMARTCARD, 3024, CoreDll.METHOD_BUFFERED, CoreDll.FILE_ANY_ACCESS);
			
			int nBytes;
			int result;
			//AppendOutputMessage("Open GEMCORE IOCTL");
			//result = CoreDll.DeviceIoControl(hDevice, IOCTL_SMARTCARD_GEMCORE_OPEN, null, 0, null, 0, out nBytes, IntPtr.Zero);
			//AppendOutputMessage("Open Result: " + result);
			
			//AppendOutputMessage("Close GEMCORE IOCTL");
			//result = CoreDll.DeviceIoControl(hDevice, IOCTL_SMARTCARD_GEMCORE_CLOSE, null, 0, null, 0, out nBytes, IntPtr.Zero);
			//AppendOutputMessage("Close Result: " + result);
			
			//AppendOutputMessage("Close SCR1:");
			
			//AppendOutputMessage("Opening SCR1:");
				IntPtr hDevice = CoreDll.CreateFile("SCR1:", CoreDll.GENERIC_READ | CoreDll.GENERIC_WRITE, 0, IntPtr.Zero, CoreDll.OPEN_EXISTING, 0, IntPtr.Zero);
				if (CoreDll.INVALID_HANDLE_VALUE == (uint)hDevice) {
					AppendOutputMessage("CreateFile failed");
					return;
				}
			
				result = CoreDll.DeviceIoControl(hDevice, IOCTL_SMARTCARD_GEMCORE_SPEED, null, 0, null, 0, out nBytes, IntPtr.Zero);
				if (0 == result) {
					AppendOutputMessage("GEMCORE_SPEED failed: " + result);
					CoreDll.CloseHandle(hDevice);
					return;
				}
				
			byte[] atr10 = {0x3B, 0x98, 0x94, 0x40,
				0x0A,  0xA5, 0x03, 0x01, 0x01, 0x01, 0xAD, 0x13,
				0x10};
			byte[] atr11 = { 0x3B, 0x98, 0x13, 0x40, 0x0A,
				0xA5, 0x03, 0x01, 0x01, 0x01, 0xAD, 0x13, 0x11};
				
			byte[] pBuffer = new byte[20];
			byte[] pCommandChangeTC1 = {0x12, 0x40, 0x94, 0x20, 0xFF, 0x00};
					
			byte[] pPath = { 0x3f, 0x00, 0xdf, 0x01 };
					
			byte[] pPersonalDataIndex = { 0x40, 0x31 };	
				
			byte[] data = new byte[500];
				
			while (true) {
				byte[] pAtr = new byte[20];
				//AppendOutputMessage("POWERUP");
				result = CoreDll.DeviceIoControl(hDevice, IOCTL_SMARTCARD_7816_POWERUP, null, 0, pAtr, 20, out nBytes, IntPtr.Zero);
				if (0 == result) {
					//AppendOutputMessage("POWERUP failed");
					SetStatusLabel("No smart card present.");
					Thread.Sleep(100);
					continue;
				}
				uint begin = CoreDll.GetTickCount();
				
				string film = this.GetFilm();
				string theatre = this.GetTheatre();
				
				SetStatusLabel("Reading smart card data...");
				
				bool applet11 = false;
				if (EqualArrays(atr11, pAtr)) {
					applet11 = true;
				}
				
				if (false == applet11) {
					
					result = CoreDll.DeviceIoControl(hDevice, IOCTL_SMARTCARD_COMMAND, pCommandChangeTC1, 6, pBuffer, 20, out nBytes, IntPtr.Zero);
					if (0 == result) {
						AppendOutputMessage("COMMAND failed");
						Thread.Sleep(1000);
						continue;
					}
					if (0x60 != pBuffer[0]) {
						AppendOutputMessage("Change TC1 failed");
						continue;
					}
				}
				
				// select dedicated file
				
				result = CoreDll.DeviceIoControl(hDevice, IOCTL_SMARTCARD_7816_SELECT_DF, pPath, 4, null, 0, out nBytes, IntPtr.Zero);
				if (0 == result) {
					AppendOutputMessage("SELECT_DF failed");
					Thread.Sleep(1000);
					continue;
				}
				
				
				result = CoreDll.DeviceIoControl(hDevice, IOCTL_SMARTCARD_7816_SELECT_EF, pPersonalDataIndex, 2, null, 0, out nBytes, IntPtr.Zero);
				if (0 == result) {
					AppendOutputMessage("SELECT_EF failed");
					Thread.Sleep(1000);
					continue;
				}
				
				
				result = CoreDll.DeviceIoControl(hDevice, IOCTL_SMARTCARD_7816_READ_BINARY, null, 0, data, 500, out nBytes, IntPtr.Zero);
				if (0 == result) {
					AppendOutputMessage("READ_BINARY failed");
					Thread.Sleep(1000);
					continue;
				}
				//AppendOutputMessage("Bytes extracted: " + nBytes);
				
				result = CoreDll.DeviceIoControl(hDevice, IOCTL_SMARTCARD_7816_POWERDOWN, null, 0, null, 0, out nBytes, IntPtr.Zero);
				if (0 == result) {
					AppendOutputMessage("POWERDOWN failed");
					Thread.Sleep(1000);
					continue;
				}
				
				const int maxFieldIdx = 9;
				int fieldIdx = 0;
				int idx = 0;
				string name = "";
				string givenNames = "";
				string nrn = "";
				do {
					fieldIdx = data[idx++];
					int size = data[idx++];
					switch(fieldIdx) {
						case 6:
							nrn = UnicodeEncoding.UTF8.GetString(data, idx, size);
							break;
						case 7:
							name = UnicodeEncoding.UTF8.GetString(data, idx, size);
							break;
						case 8:
							givenNames = UnicodeEncoding.UTF8.GetString(data, idx, size);
							break;
					}
					idx += size;
				} while (fieldIdx < maxFieldIdx);
				SetStatusLabel("Checking status for " + name + " " + givenNames + " ...");
				
				
				
				string serverAddress = this.GetServerAddress();
				
				string location = "http://" + serverAddress + ":8080/demo-cinema/CinemaServlet/?nrn=" + nrn 
					+ "&time=" + DateTime.Now.ToLongTimeString() + "&film=" + film + "&theatre=" + theatre;
				HttpWebRequest httpWebRequest = (HttpWebRequest)WebRequest.Create(location);
				httpWebRequest.Timeout = 1000 * 2;
				try {
					HttpWebResponse httpWebResponse = (HttpWebResponse)httpWebRequest.GetResponse();
					/*
					 * Don't forget to disable the firewall port 8080 here...
					 */
					if (HttpStatusCode.OK == httpWebResponse.StatusCode) {
						SetStatusLabel("User has valid cinema.", Color.Green);
					}
					else {
						// if (statusCode != OK) => WebException
						SetStatusLabel("User has NO valid cinema.", Color.Red);
					}
					//AppendOutputMessage("Status: " + httpWebResponse.StatusDescription);
					httpWebResponse.Close();
				}
				catch (WebException e) {
					if (WebExceptionStatus.ProtocolError == e.Status) {
						HttpWebResponse response = (HttpWebResponse) e.Response;
						if (HttpStatusCode.Unauthorized == response.StatusCode) {
							SetStatusLabel("User has NO valid cinema.", Color.Red);
						}
						else {
							SetStatusLabel("Protocol error: " + response.StatusCode);
							AppendOutputMessage("Status message: " + response.StatusDescription);
						}
					}
					else if (WebExceptionStatus.Timeout == e.Status) {
						SetStatusLabel("Cannot connect to server.");
					}
					else {
						SetStatusLabel("Connection error.");
						AppendOutputMessage("Error Status: " + e.Status);
					}
				}
				uint end = CoreDll.GetTickCount();
				AppendOutputMessage("Duration: " + (end - begin) + " ms.");
				
				do {
					result = CoreDll.DeviceIoControl(hDevice, IOCTL_SMARTCARD_7816_POWERUP, null, 0, pAtr, 20, out nBytes, IntPtr.Zero);
					Thread.Sleep(100);
				} while (result != 0);
				CoreDll.DeviceIoControl(hDevice, IOCTL_SMARTCARD_7816_POWERDOWN, null, 0, null, 0, out nBytes, IntPtr.Zero);
				
				//Thread.Sleep(1000);
			}
		}
		
		delegate void StringParameterDelegate(String value);
		
		private static bool EqualArrays(byte[] expected, byte[] actual) {
			for (int idx = 0; idx < expected.Length; idx++) {
				if (expected[idx] != actual[idx]) {
					return false;
				}
			}
			return true;
		}
		
		private void SetStatusLabel(string value) {
			if (InvokeRequired) {
				BeginInvoke(new StringParameterDelegate(SetStatusLabel), new Object[] { value});
				return;
			}
			this.statusLabel.Text = value;
			this.statusLabel.ForeColor = Color.Black;
		}
		
		delegate void StringColorParameterDelegate(String value, Color color);
		
		private void SetStatusLabel(string value, Color color) {
			if (InvokeRequired) {
				BeginInvoke(new StringColorParameterDelegate(SetStatusLabel), new Object[] {value, color});
				return;
			}
			this.statusLabel.Text = value;
			this.statusLabel.ForeColor = color;
		}
		
		delegate string GetStringDelegate();
		
		private string GetServerAddress() {
			if (InvokeRequired) {
				return (string) Invoke(new GetStringDelegate(GetServerAddress));
			}
			return this.serverAddressTextBox.Text;
		}
		
		private string GetFilm() {
			if (InvokeRequired) {
				return (string) Invoke(new GetStringDelegate(GetFilm));
			}
			return this.filmComboBox.Text;
		}
		
		private string GetTheatre() {
			if (InvokeRequired) {
				return (string) Invoke(new GetStringDelegate(GetTheatre));
			}
			return this.theatreComboBox.Text;
		}
		
		private void AppendOutputMessage(String message) {
			if (InvokeRequired) {
				BeginInvoke(new StringParameterDelegate(AppendOutputMessage), new Object[] { message });
				return;
			}
			this.outputTextBox.SelectedText += message + "\r\n";
			this.outputTextBox.ScrollToCaret();
		}
		
		private void StartSmartCardThread() {
			Thread smartCardThread = new Thread(SmartCardThreadProc);
			smartCardThread.IsBackground = true;
			smartCardThread.Start();
		}
		
		void ClearLogButtonClick(object sender, System.EventArgs e)
		{
			this.outputTextBox.Text = "";
		}
		
		void ServerAddressTextBoxGotFocus(object sender, System.EventArgs e)
		{
			CoreDll.SipShowIM(CoreDll.SIPF_ON);
		}
 	}
}