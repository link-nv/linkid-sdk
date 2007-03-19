/*
 * Gemaakt met SharpDevelop.
 * Gebruiker: Frank Cornelis
 * Datum: 13/03/2007
 * Tijd: 16:29
 * 
 * Dit sjabloon wijzigen: Extra | Opties |Coderen | Standaard kop bewerken.
 */
namespace demo_ticket_client
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
			this.fromLabel = new System.Windows.Forms.Label();
			this.toLabel = new System.Windows.Forms.Label();
			this.serverAddressLabel = new System.Windows.Forms.Label();
			this.fromComboBox = new System.Windows.Forms.ComboBox();
			this.toComboBox = new System.Windows.Forms.ComboBox();
			this.statusLabelLabel = new System.Windows.Forms.Label();
			this.statusLabel = new System.Windows.Forms.Label();
			this.serverAddressTextBox = new System.Windows.Forms.TextBox();
			this.scanButton = new System.Windows.Forms.Button();
			this.outputTextBox = new System.Windows.Forms.TextBox();
			this.SuspendLayout();
			// 
			// fromLabel
			// 
			this.fromLabel.Location = new System.Drawing.Point(12, 9);
			this.fromLabel.Name = "fromLabel";
			this.fromLabel.Size = new System.Drawing.Size(100, 23);
			this.fromLabel.Text = "From:";
			// 
			// toLabel
			// 
			this.toLabel.Location = new System.Drawing.Point(12, 32);
			this.toLabel.Name = "toLabel";
			this.toLabel.Size = new System.Drawing.Size(100, 23);
			this.toLabel.Text = "To:";
			// 
			// serverAddressLabel
			// 
			this.serverAddressLabel.Location = new System.Drawing.Point(12, 55);
			this.serverAddressLabel.Name = "serverAddressLabel";
			this.serverAddressLabel.Size = new System.Drawing.Size(100, 23);
			this.serverAddressLabel.Text = "Server address:";
			// 
			// fromComboBox
			// 
			this.fromComboBox.Location = new System.Drawing.Point(118, 10);
			this.fromComboBox.Name = "fromComboBox";
			this.fromComboBox.Size = new System.Drawing.Size(121, 21);
			// 
			// toComboBox
			// 
			this.toComboBox.Location = new System.Drawing.Point(118, 33);
			this.toComboBox.Name = "toComboBox";
			this.toComboBox.Size = new System.Drawing.Size(121, 21);
			// 
			// statusLabelLabel
			// 
			this.statusLabelLabel.Location = new System.Drawing.Point(12, 90);
			this.statusLabelLabel.Name = "statusLabelLabel";
			this.statusLabelLabel.Size = new System.Drawing.Size(100, 23);
			this.statusLabelLabel.Text = "Status:";
			// 
			// statusLabel
			// 
			this.statusLabel.Location = new System.Drawing.Point(118, 90);
			this.statusLabel.Name = "statusLabel";
			this.statusLabel.Size = new System.Drawing.Size(295, 23);
			this.statusLabel.Text = "Started";
			// 
			// serverAddressTextBox
			// 
			this.serverAddressTextBox.Location = new System.Drawing.Point(118, 60);
			this.serverAddressTextBox.Name = "serverAddressTextBox";
			this.serverAddressTextBox.Size = new System.Drawing.Size(154, 20);
			// 
			// scanButton
			// 
			this.scanButton.Location = new System.Drawing.Point(338, 226);
			this.scanButton.Name = "scanButton";
			this.scanButton.Size = new System.Drawing.Size(75, 23);
			this.scanButton.Text = "Scan";
			this.scanButton.Click += new System.EventHandler(this.ScanButtonClick);
			// 
			// outputTextBox
			// 
			this.outputTextBox.Location = new System.Drawing.Point(12, 116);
			this.outputTextBox.Multiline = true;
			this.outputTextBox.Name = "outputTextBox";
			this.outputTextBox.ScrollBars = System.Windows.Forms.ScrollBars.Vertical;
			this.outputTextBox.Size = new System.Drawing.Size(401, 104);
			// 
			// MainForm
			// 
			this.AutoScaleDimensions = new System.Drawing.SizeF(96F, 96F);
			this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Dpi;
			this.ClientSize = new System.Drawing.Size(425, 261);
			this.Controls.Add(this.outputTextBox);
			this.Controls.Add(this.scanButton);
			this.Controls.Add(this.serverAddressTextBox);
			this.Controls.Add(this.statusLabel);
			this.Controls.Add(this.statusLabelLabel);
			this.Controls.Add(this.toComboBox);
			this.Controls.Add(this.fromComboBox);
			this.Controls.Add(this.serverAddressLabel);
			this.Controls.Add(this.toLabel);
			this.Controls.Add(this.fromLabel);
			this.Name = "MainForm";
			this.Text = "Demo Ticket Client";
			this.ResumeLayout(false);
		}
		private System.Windows.Forms.TextBox outputTextBox;
		private System.Windows.Forms.Button scanButton;
		private System.Windows.Forms.TextBox serverAddressTextBox;
		private System.Windows.Forms.Label statusLabel;
		private System.Windows.Forms.Label statusLabelLabel;
		private System.Windows.Forms.Label fromLabel;
		private System.Windows.Forms.ComboBox toComboBox;
		private System.Windows.Forms.Label toLabel;
		private System.Windows.Forms.Label serverAddressLabel;
		private System.Windows.Forms.ComboBox fromComboBox;
	}
}
