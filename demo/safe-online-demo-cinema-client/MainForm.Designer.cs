/*
 * Gemaakt met SharpDevelop.
 * Gebruiker: Frank Cornelis
 * Datum: 13/03/2007
 * Tijd: 16:29
 * 
 * Dit sjabloon wijzigen: Extra | Opties |Coderen | Standaard kop bewerken.
 */
namespace demo_cinema_client
{
	partial class MainForm
	{
		/// <summary>
		/// Designer variable used to keep track of non-visual components.
		/// </summary>
		private System.ComponentModel.IContainer components = null;
		
		/// <summary>
		/// Disposes resources used by the form.
		/// </summary>
		/// <param name="disposing">true if managed resources should be disposed; otherwise, false.</param>
		protected override void Dispose(bool disposing)
		{
			if (disposing) {
				if (components != null) {
					components.Dispose();
				}
			}
			base.Dispose(disposing);
		}
		
		/// <summary>
		/// This method is required for Windows Forms designer support.
		/// Do not change the method contents inside the source code editor. The Forms designer might
		/// not be able to load this method if it was changed manually.
		/// </summary>
		private void InitializeComponent()
		{
			this.filmLabel = new System.Windows.Forms.Label();
			this.theatreLabel = new System.Windows.Forms.Label();
			this.serverAddressLabel = new System.Windows.Forms.Label();
			this.filmComboBox = new System.Windows.Forms.ComboBox();
			this.theatreComboBox = new System.Windows.Forms.ComboBox();
			this.statusLabelLabel = new System.Windows.Forms.Label();
			this.statusLabel = new System.Windows.Forms.Label();
			this.serverAddressTextBox = new System.Windows.Forms.TextBox();
			this.outputTextBox = new System.Windows.Forms.TextBox();
			this.clearLogButton = new System.Windows.Forms.Button();
			this.logLabel = new System.Windows.Forms.Label();
			this.SuspendLayout();
			// 
			// fromLabel
			// 
			this.filmLabel.Location = new System.Drawing.Point(12, 13);
			this.filmLabel.Name = "filmLabel";
			this.filmLabel.Size = new System.Drawing.Size(100, 14);
			this.filmLabel.Text = "Film:";
			// 
			// toLabel
			// 
			this.theatreLabel.Location = new System.Drawing.Point(12, 37);
			this.theatreLabel.Name = "theatreLabel";
			this.theatreLabel.Size = new System.Drawing.Size(100, 13);
			this.theatreLabel.Text = "Theatre:";
			// 
			// serverAddressLabel
			// 
			this.serverAddressLabel.Location = new System.Drawing.Point(12, 63);
			this.serverAddressLabel.Name = "serverAddressLabel";
			this.serverAddressLabel.Size = new System.Drawing.Size(100, 14);
			this.serverAddressLabel.Text = "Server address:";
			// 
			// fromComboBox
			// 
			this.filmComboBox.Location = new System.Drawing.Point(118, 10);
			this.filmComboBox.Name = "filmComboBox";
			this.filmComboBox.Size = new System.Drawing.Size(121, 21);
			// 
			// toComboBox
			// 
			this.theatreComboBox.Location = new System.Drawing.Point(118, 33);
			this.theatreComboBox.Name = "theatreComboBox";
			this.theatreComboBox.Size = new System.Drawing.Size(121, 21);
			// 
			// statusLabelLabel
			// 
			this.statusLabelLabel.Location = new System.Drawing.Point(12, 90);
			this.statusLabelLabel.Name = "statusLabelLabel";
			this.statusLabelLabel.Size = new System.Drawing.Size(100, 16);
			this.statusLabelLabel.Text = "Status:";
			// 
			// statusLabel
			// 
			this.statusLabel.Location = new System.Drawing.Point(118, 90);
			this.statusLabel.Name = "statusLabel";
			this.statusLabel.Size = new System.Drawing.Size(295, 38);
			this.statusLabel.Text = "Started";
			// 
			// serverAddressTextBox
			// 
			this.serverAddressTextBox.Location = new System.Drawing.Point(118, 60);
			this.serverAddressTextBox.Name = "serverAddressTextBox";
			this.serverAddressTextBox.Size = new System.Drawing.Size(170, 20);
			this.serverAddressTextBox.GotFocus += new System.EventHandler(this.ServerAddressTextBoxGotFocus);
			// 
			// outputTextBox
			// 
			this.outputTextBox.Location = new System.Drawing.Point(12, 144);
			this.outputTextBox.Multiline = true;
			this.outputTextBox.Name = "outputTextBox";
			this.outputTextBox.ScrollBars = System.Windows.Forms.ScrollBars.Vertical;
			this.outputTextBox.Size = new System.Drawing.Size(401, 100);
			// 
			// clearLogButton
			// 
			this.clearLogButton.Location = new System.Drawing.Point(338, 250);
			this.clearLogButton.Name = "clearLogButton";
			this.clearLogButton.Size = new System.Drawing.Size(75, 23);
			this.clearLogButton.Text = "Clear Log";
			this.clearLogButton.Click += new System.EventHandler(this.ClearLogButtonClick);
			// 
			// logLabel
			// 
			this.logLabel.Location = new System.Drawing.Point(12, 121);
			this.logLabel.Name = "logLabel";
			this.logLabel.Size = new System.Drawing.Size(48, 20);
			this.logLabel.Text = "Log:";
			// 
			// MainForm
			// 
			this.AutoScaleDimensions = new System.Drawing.SizeF(96F, 96F);
			this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Dpi;
			this.ClientSize = new System.Drawing.Size(426, 285);
			this.Controls.Add(this.logLabel);
			this.Controls.Add(this.clearLogButton);
			this.Controls.Add(this.outputTextBox);
			this.Controls.Add(this.serverAddressTextBox);
			this.Controls.Add(this.statusLabel);
			this.Controls.Add(this.statusLabelLabel);
			this.Controls.Add(this.theatreComboBox);
			this.Controls.Add(this.filmComboBox);
			this.Controls.Add(this.serverAddressLabel);
			this.Controls.Add(this.theatreLabel);
			this.Controls.Add(this.filmLabel);
			this.Name = "MainForm";
			this.Text = "Demo Cinema Client";
			this.ResumeLayout(false);
		}
		private System.Windows.Forms.Label logLabel;
		private System.Windows.Forms.Button clearLogButton;
		private System.Windows.Forms.TextBox outputTextBox;
		private System.Windows.Forms.TextBox serverAddressTextBox;
		private System.Windows.Forms.Label statusLabel;
		private System.Windows.Forms.Label statusLabelLabel;
		private System.Windows.Forms.Label filmLabel;
		private System.Windows.Forms.ComboBox theatreComboBox;
		private System.Windows.Forms.Label theatreLabel;
		private System.Windows.Forms.Label serverAddressLabel;
		private System.Windows.Forms.ComboBox filmComboBox;
	}
}
