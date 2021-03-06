// Copyright (C) 2005  Paul Miller. This file is part of TX Modular system distributed under the terms of the GNU General Public License (see file LICENSE).


TXWiiTrigger : TXModuleBase {

	classvar <>arrInstances;	
	classvar <defaultName;  		// default module name
	classvar <moduleRate;			// "audio" or "control"
	classvar <moduleType;			// "source", "insert", "bus",or  "channel"
	classvar <noInChannels;			// no of input channels 
	classvar <arrAudSCInBusSpecs; 	// audio side-chain input bus specs 
	classvar <>arrCtlSCInBusSpecs; 	// control side-chain input bus specs
	classvar <noOutChannels;		// no of output channels 
	classvar <arrOutBusSpecs; 		// output bus specs
	classvar	<guiWidth=500;
	classvar	defaultActionStep;
	classvar  arrOscStrings;
	classvar  arrOscArgValues;
	classvar	arrWiiNames;
	
	var	displayOption;
	var	oscControlRoutine;
	var	<>oscString;
	var 	arrActions;
	var	oscResponder;

*initClass{
	arrInstances = [];		
	//	set class specific variables
	defaultName = "Wii Trig Darwiin";
	moduleRate = "control";
	moduleType = "action";
	noInChannels = 0;
	noOutChannels = 0;
	arrWiiNames = [
		" Select..." ,
		"Wii Button A: ON" ,
		"Wii Button A: OFF" ,
		"Wii Button B: ON" ,
		"Wii Button B: OFF" ,
		"Wii Button Up: ON" ,
		"Wii Button Up: OFF" ,
		"Wii Button Down: ON" ,
		"Wii Button Down: OFF" ,
		"Wii Button Left: ON" ,
		"Wii Button Left: OFF" ,
		"Wii Button Right: ON" ,
		"Wii Button Right: OFF" ,
		"Wii Button -: ON" ,
		"Wii Button -: OFF" ,
		"Wii Button +: ON" ,
		"Wii Button +: OFF" ,
		"Wii Button Home: ON" ,
		"Wii Button Home: OFF" ,
		"Wii Button 1: ON" ,
		"Wii Button 1: OFF" ,
		"Wii Button 2: ON" ,
		"Wii Button 2: OFF" ,
		"Nunchuk Button Z: ON" ,
		"Nunchuk Button Z: OFF" ,
		"Nunchuk Button C: ON" ,
		"Nunchuk Button C: OFF" ,
	];
	arrOscStrings = [
		"dummy" ,
		"/wii/button/a" ,
		"/wii/button/a" ,
		"/wii/button/b" ,
		"/wii/button/b" ,
		"/wii/button/up" ,
		"/wii/button/up" ,
		"/wii/button/down" ,
		"/wii/button/down" ,
		"/wii/button/left" ,
		"/wii/button/left" ,
		"/wii/button/right" ,
		"/wii/button/right" ,
		"/wii/button/minus" ,
		"/wii/button/minus" ,
		"/wii/button/plus" ,
		"/wii/button/plus" ,
		"/wii/button/home" ,
		"/wii/button/home" ,
		"/wii/button/one" ,
		"/wii/button/one" ,
		"/wii/button/two" ,
		"/wii/button/two" ,
		"/nunchuk/button/z" ,
		"/nunchuk/button/z" ,
		"/nunchuk/button/c" ,
		"/nunchuk/button/c" ,
	];
	arrOscArgValues = [0,1,0,1,0,1,0,1,0,1,0,1,0,1,0,1,0,1,0,1,0,1,0,1,0,1,0,1,0,1,];
} 

*new{ arg argInstName;
	 ^super.new.init(argInstName);
} 

init {arg argInstName;
	//	set  class specific instance variables
	displayOption = "showWiiButton";
	arrActions = [99,0,0,0,0,0,0, nil].dup(10);
	arrSynthArgSpecs = [
		["wiiButton", 0, 0],
		["OSCString", "/example/text", 0],
		["useOSCArgs1", 0],
		["useOSCArgs2", 0],
		["useOSCArgs3", 0],
		["useOSCArgs4", 0],
		["useOSCArgs5", 0],
		["useOSCArgs6", 0],
		["useOSCArgs7", 0],
		["useOSCArgs8", 0],
		["useOSCArgs9", 0],
		["useOSCArgs10", 0],
	]; 
	synthDefFunc = { arg out, controller=0;
		var mixOut=0;
		Out.ar(out, mixOut);
	};
	this.buildGuiSpecArray;
	arrActionSpecs = this.buildActionSpecs([
		["TXPopupAction", "Wii button", arrWiiNames, "wiiButton", { arg view; this.setOscString(view.value); }],
	]);	
	//	use base class initialise 
	this.baseInit(this, argInstName);
	this.oscControlActivate;
}

buildGuiSpecArray {
	guiSpecArray = [
		["ActionButton", "Wii Button", {displayOption = "showWiiButton"; 
			this.buildGuiSpecArray; system.showView;}, 130, 
			TXColor.white, this.getButtonColour(displayOption == "showWiiButton")], 
		["Spacer", 3], 
		["ActionButton", "Actions 1-5", {displayOption = "showActions1-5"; 
			this.buildGuiSpecArray; system.showView;}, 130, 
			TXColor.white, this.getButtonColour(displayOption == "showActions1-5")], 
		["Spacer", 3], 
		["ActionButton", "Actions 6-10", {displayOption = "showActions6-10"; 
			this.buildGuiSpecArray; system.showView;}, 130, 
			TXColor.white, this.getButtonColour(displayOption == "showActions6-10")], 
		["Spacer", 3], 
		["DividingLine"], 
		["SpacerLine", 6], 
	];
	if (displayOption == "showWiiButton", {
		guiSpecArray = guiSpecArray ++[
		["TXStaticText", "Please note:", 
			"DarwiinRemote OSC Preferences - set Port to  " ++ NetAddr.langPort.asString],
		["SpacerLine", 4], 
		["TXPopupAction", "Wii button", arrWiiNames, "wiiButton", { arg view; this.setOscString(view.value); }],
		];
	});
	if (displayOption == "showActions1-5", {
		guiSpecArray = guiSpecArray ++[
			["TXActionView", arrActions, 0], 
			["DividingLine"], 
			["TXActionView", arrActions, 1], 
			["DividingLine"], 
			["TXActionView", arrActions, 2], 
			["DividingLine"], 
			["TXActionView", arrActions, 3], 
			["DividingLine"], 
			["TXActionView", arrActions, 4], 
			["DividingLine"], 
		];
	});
	if (displayOption == "showActions6-10", {
		guiSpecArray = guiSpecArray ++[
			["TXActionView", arrActions, 5], 
			["DividingLine"], 
			["TXActionView", arrActions, 6], 
			["DividingLine"], 
			["TXActionView", arrActions, 7], 
			["DividingLine"], 
			["TXActionView", arrActions, 8], 
			["DividingLine"], 
			["TXActionView", arrActions, 9], 
			["DividingLine"], 
		];
	});
}

getButtonColour { arg colour2Boolean;
	if (colour2Boolean == true, {
		^TXColor.sysGuiCol4;
	},{
		^TXColor.sysGuiCol1;
	});
}

runAction {this.oscControlActivate}   //	override base class

pauseAction {this.oscControlDeactivate}   //	override base class

extraSaveData {	
	^[oscString, arrActions];
	
}
loadExtraData {arg argData;  // override default method
	oscString = argData.at(0); 
	arrActions = argData.at(1); 
	this.oscControlActivate;
}

setOscString { arg argWiiButton;
	var holdOscString;
	holdOscString = arrOscStrings. at(argWiiButton);
	// set current value in module
	this.oscString = holdOscString;
	// store current data to synthArgSpecs
	this.setSynthArgSpec("OSCString", holdOscString);
	// activate osc responder
	this.oscControlActivate;
}

oscControlActivate { 
	//	stop any previous responder 
	this.oscControlDeactivate;
	oscResponder = OSCresponderNode(nil, oscString.asSymbol, { arg time, responder, msg;

//	For testing  - post details
//	"TXWiiController : ".postln;
//	[time, responder, msg].postln;

		// if value matches, run actions
		if (msg.at(1) == arrOscArgValues.at(this.getSynthArgSpec("wiiButton")), {
			this.performActions(msg);
		});
		
	}).add;
}

oscControlDeactivate { 
	if (oscResponder.notNil, {
		oscResponder.remove;
	});
 }

deleteModuleExtraActions {     
	this.oscControlActivate;
}

rebuildSynth { 
	// override base class method
}

performActions { arg oscMsg;
	arrActions.do({ arg item, i;
		var holdModuleID, holdModule, holdActionInd, holdArrActionItems, holdActionText, 
			holdAction, holdVal1, holdVal2, holdVal3, holdVal4, actionArg1, actionArg2, actionArg3, actionArg4,
			oscArg1, oscArg2, oscArg3, oscArg4, holdIndex, holdItems;
		holdModuleID = item.at(0);
		holdActionInd = item.at(1);
		holdVal1 = item.at(2);
		holdVal2 = item.at(3);
		holdVal3 = item.at(4);
		holdVal4 = item.at(5);
		holdActionText = item.at(7);
		holdModule = system.getModuleFromID(holdModuleID);
		if (holdModule != 0, {
			holdArrActionItems = holdModule.arrActionSpecs.collect({arg item, i; item.actionName;});
			// if text found, match action string with text, else use numerical value
				if (holdActionText.notNil, {
					holdActionInd = holdArrActionItems.indexOfEqual(holdActionText) ? holdActionInd;
					holdAction = holdModule.arrActionSpecs.at(holdActionInd);
				},{
					// if text not found, use number but only select older actions with legacyType == 1
					holdAction = holdModule.arrActionSpecs.select({arg item, i; item.legacyType == 1}).at(holdActionInd);
				});

			actionArg1 = holdVal1;
			actionArg2 = holdVal2;
			actionArg3 = holdVal3;
			actionArg4 = holdVal4;
			oscArg1 = oscMsg.at(1);
			oscArg2 = oscMsg.at(2);
			oscArg3 = oscMsg.at(3);
			oscArg4 = oscMsg.at(4);
			
			// if action type is commandAction then value it with arguments
			if (holdAction.actionType == \commandAction, {
				holdAction.actionFunction.value(actionArg1, actionArg2, actionArg3, actionArg4);
			});
			// if action type is valueAction then value it with arguments
			if (holdAction.actionType == \valueAction, {
				holdAction.setValueFunction.value(actionArg1, actionArg2, actionArg3, actionArg4);
			});
		});
	});
	//	gui update
//	system.flagGuiUpd;
}	// end of performActions

}

