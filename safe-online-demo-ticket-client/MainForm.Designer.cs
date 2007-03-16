/*
 * Gemaakt met SharpDevelop.
 * Gebruiker: Frank Cornelis
 * Datum: 13/03/2007
 * Tijd: 16:29
 * 
 * Dit sjabloon wijzigen: Extra | Opties |Coderen | Standaard kop bewerken.
 */
namespace test_client
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
			this.fromComboBox.Location = new System.Drawing.Point(118, 8);
			this.fromComboBox.Name = "fromComboBox";
			this.fromComboBox.Size = new System.Drawing.Size(121, 21);
			this.fromComboBox.TabIndex = 3;
			// 
			// toComboBox
			// 
			this.toComboBox.Location = new System.Drawing.Point(118, 32);
			this.toComboBox.Name = "toComboBox";
			this.toComboBox.Size = new System.Drawing.Size(121, 21);
			this.toComboBox.TabIndex = 4;
			// 
			// MainForm
			// 
			this.AutoScaleDimensions = new System.Drawing.SizeF(96F, 96F);
			this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Dpi;
			this.ClientSize = new System.Drawing.Size(425, 261);
			this.Controls.Add(this.toComboBox);
			this.Controls.Add(this.fromComboBox);
			this.Controls.Add(this.serverAddressLabel);
			this.Controls.Add(this.toLabel);
			this.Controls.Add(this.fromLabel);
			this.Name = "MainForm";
			this.Text = "Demo Ticket Client";
			this.ResumeLayout(false);
		}
		private System.Windows.Forms.Label fromLabel;
		private System.Windows.Forms.ComboBox toComboBox;
		private System.Windows.Forms.Label toLabel;
		private System.Windows.Forms.Label serverAddressLabel;
		private System.Windows.Forms.ComboBox fromComboBox;
	}
}
