// Copyright (C) 2010  Paul Miller. This file is part of TX Modular system distributed under the terms of the GNU General Public License (see file LICENSE).

TXChorusSt : TXModuleBase {		// Chorus module 

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
	classvar	<arrBufferSpecs;
	classvar	<maxDelaytime = 0.5;	//	delay time up to 0.5 secs.

*initClass{
	arrInstances = [];		
	//	set class specific variables
	defaultName = "Chorus St";
	moduleRate = "audio";
	moduleType = "insert";
	noInChannels = 2;			
	arrCtlSCInBusSpecs = [ 
		["LFO rate", 1, "modLFOFreq", 0],
		["LFO depth", 1, "modLFODepth", 0],
		["Dry-Wet Mix", 1, "modWetDryMix", 0]
	];	
	noOutChannels = 2;
	arrOutBusSpecs = [ 
		["Out L + R", [0,1]], 
		["Out L only", [0]], 
		["Out R only", [1]] 
	];	
} 

*new{ arg argInstName;
	 ^super.new.init(argInstName);
} 

init {arg argInstName;
	var holdControlSpec, holdControlSpec2;
	//	set  class specific instance variables
	extraLatency = 0.2;	// allow extra time when recreating
	arrSynthArgSpecs = [
		["in", 0, 0],
		["out", 0, 0],
		["lfoFreq", 0.2, defLagTime],
		["lfoFreqMin", 0.01, defLagTime],
		["lfoFreqMax", 10, defLagTime],
		["lfoDepth", 0.1, defLagTime],
		["wetDryMix", 1.0, defLagTime],
		["modLFOFreq", 0, defLagTime],
		["modLfoDepth", 0, defLagTime],
		["modWetDryMix", 0, defLagTime],
	]; 
	arrOptions = [0, 2];
	arrOptionData = [
		TXLFO.arrOptionData,
		[	
			["2 layers", 2],
			["3 layers", 3],
			["4 layers", 4],
			["5 layers", 5],
			["6 layers", 6],
			["7 layers", 7],
		],
];
	synthDefFunc = { 
		arg in, out, lfoFreq, lfoFreqMin, lfoFreqMax, lfoDepth, wetDryMix, 
			modLFOFreq, modLfoDepth, modWetDryMix;
		var outLfo, outLFOFreq, outLfoDepth, outLFOFunction, outVolRamp;
		var inputL, inputR, numLayers, outSoundL, outSoundR, mixCombined;
		numLayers = this.getSynthOption(1);
		outLFOFreq = ( (lfoFreqMax/lfoFreqMin) ** ((lfoFreq + modLFOFreq).max(0.001).min(1)) ) * lfoFreqMin;
		outLfoDepth = (lfoDepth + modLfoDepth).max(0).min(1);
		// select function based on arrOptions
		outLFOFunction = arrOptionData.at(0).at(arrOptions.at(0)).at(1);
		outLfo = {arg i; 
			outLFOFunction.value(outLFOFreq * [0.923, 1.087, 0.982, 1.036, 0.959, 1.044, 1.015].at(i)).range(0, 1)
			* outLfoDepth
		} ! numLayers;
		mixCombined = (wetDryMix + modWetDryMix).max(0).min(1);
		inputL = InFeedback.ar(in,1);
		outSoundL = DelayC.ar(inputL, 0.4, 0.05+(outLfo*0.3), mixCombined,
			inputL * (1-mixCombined));
		outSoundL = Mix(outSoundL); 
		inputR = InFeedback.ar(in+1,1);
		outSoundR = DelayC.ar(inputR, 0.4, 0.05+(outLfo*0.3), mixCombined,
			inputR * (1-mixCombined));
		outSoundR = Mix(outSoundR); 
		outVolRamp = EnvGen.kr(Env.new([0, 0, 1], [0.1, 0.1]), 1);
		// use tanh as a limiter to stop blowups
		Out.ar(out, outVolRamp * LeakDC.ar([outSoundL, outSoundR].tanh, 0.995));
	};
	holdControlSpec = ControlSpec.new(0.midicps, 127.midicps, \exp );
	holdControlSpec2 = ControlSpec.new(0.01, 1.0, \exp );
	guiSpecArray = [
		["SynthOptionPopupPlusMinus", "No. Layers", arrOptionData, 1, 300], 
		["SpacerLine", 4], 
		["SynthOptionPopup", "LFO Wave", arrOptionData, 0], 
		["SpacerLine", 4], 
		["TXMinMaxSliderSplit", "LFO Rate", ControlSpec(0.01, 100, \exp, 0, 1, units: " Hz"), 
			"lfoFreq", "lfoFreqMin", "lfoFreqMax", nil, TXLFO.arrLFOFreqRanges], 
		["SpacerLine", 4], 
		["EZslider", "LFO Depth", ControlSpec(0, 1), "lfoDepth"],
		["SpacerLine", 4], 
		["WetDryMixSlider"], 
	];
	arrActionSpecs = this.buildActionSpecs(guiSpecArray);
	//	use base class initialise 
	this.baseInit(this, argInstName);
	//	make buffers, load the synthdef and create the synth
	this.makeBuffersAndSynth(arrBufferSpecs);
}

}

