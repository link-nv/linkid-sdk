import java.lang.System;
import javafx.ui.*;

class StatusBarModel {
	attribute identity: String;
	attribute location: String;
}

var statusBarModel = StatusBarModel {
	identity: "unknown"
	location: "unknown"
};

var appMenu = MenuBar {
	menus: Menu {
	text: "File"
	mnemonic: F
	items: 
	[  
		MenuItem {
			text: "Load Certificate"
			mnemonic: L
			accelerator: {
				modifier: ALT
				keyStroke: F2
			}
			action: operation() {
				System.err.println("Load Certificate");
			}
	},	
		MenuItem {
			text: "Exit"
			mnemonic: X
			accelerator: {
				modifier: ALT
				keyStroke: F4
			}
			action: operation() {
				System.exit(0);
			}
		}
	]}
};


var statusBarWindow = GroupPanel {
	var statusRow = Row { alignment: BASELINE }
	var identityColumn = Column {
		alignment: TRAILING
	}
	var locationColumn = Column {
		alignment: LEADING
		resizable: true
	}
	rows: [statusRow]
	columns: [identityColumn, locationColumn]
	content: 
 		[TextField {
			row: statusRow
			column: identityColumn
			columns: 25
			value: bind statusBarModel.identity
 		},
		TextField {
			row: statusRow
			column: locationColumn
			columns: 25
			value: bind statusBarModel.location
		}]
};

var contentWindow = FlowPanel {
	content:
	 		[TextField {
			columns: 25
			value: bind statusBarModel.identity
 		},
		TextField {
			columns: 25
			value: bind statusBarModel.location
		}]
};

Frame {
	centerOnScreen: true
	onClose: operation() {System.exit(0);}
	title: "Safe Online Java FX Application Console"
	height: 700
	width: 1100
	background: black
	
	content : SplitPane {
		orientation: VERTICAL
		content :
			[SplitView {
				weight: 0.90
				content: contentWindow
			},
			SplitView {
				weight: 0.10
				content: statusBarWindow
			}]
	}
	
	visible: true 
}