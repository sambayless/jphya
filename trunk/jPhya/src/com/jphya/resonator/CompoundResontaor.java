/*
Copyright (c) 2009, Sam Bayless
All rights reserved.

Redistribution and use in source and binary forms, with or without modification,
 are permitted provided that the following conditions are met:

 * Redistributions of source code must retain the above copyright notice, 
   this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice, 
   this list of conditions and the following disclaimer in the documentation and/or 
   other materials provided with the distribution.
 * Neither the name of 'JPhya' nor the names of its contributors may be used 
   to endorse or promote products derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS 
OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY 
AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR 
CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL 
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; 
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.jphya.resonator;

import java.util.ArrayList;

import com.jphya.distance.DistanceModel;
import com.jphya.scene.Scene;
import com.jphya.signal.Block;

/**
 * This is a resonator that combines multiple subresonators.
 * @author Sam
 *
 */
public class CompoundResontaor extends Resonator {

	private ArrayList<Resonator> resonators = new ArrayList<Resonator>();
	
	public CompoundResontaor(Scene scene) {
		super(scene);
	}
	
	public void addResonator(Resonator r)
	{
		resonators.add(r);
	}
	
	public void removeResonator(Resonator r)
	{
		resonators.remove(r);
	}
	
	@Override
	public float estimateVolume() {
		//return the sum of the resonators (or should it be the max)
		float estimate = 0f;
		for(Resonator r:this.resonators)
			estimate+= r.estimateVolume();
		return estimate;
	}

	@Override
	public int getTimeCost() {
		int timeCost = 0;
		for(Resonator r:this.resonators)
			timeCost+= r.getTimeCost();
		return timeCost;
	}

	@Override
	public boolean isQuiet() {
		boolean quiet = false;
		
		for(Resonator r:this.resonators)
			quiet |= r.isQuiet();//the resonator is quiet only if each and every sub resonator is quiet
		
		return quiet;
	}

	@Override
	public void setAuxAmpScale(float s) {
		for(Resonator r:this.resonators)
			r.setAuxAmpScale(s);
	}

	@Override
	public void setAuxDampScale(float s) {
		for(Resonator r:this.resonators)
			r.setAuxDampScale(s);
	}

	@Override
	public void setAuxFreqScale(float s) {
		for(Resonator r:this.resonators)
			r.setAuxFreqScale(s);
	}

	@Override
	public void setQuietLevel(float l) {
		for(Resonator r:this.resonators)
			r.setQuietLevel(l);
	}

	@Override
	public void setOutput(Block output) {
		super.setOutput(output);
		for(Resonator r:this.resonators)
			r.setOutput(output);
	}

	@Override
	public void setInput(Block input) {
		super.setInput(input);
		for(Resonator r:this.resonators)
			r.setInput(input);
	}

	@Override
	public Block tick() {
		getOutput().zero(); // Could be made faster by overwriting output on mode
		for(Resonator r:this.resonators)
		{
		
			
				r.tickAdd();
			
		}
		DistanceModel dist = super.body.getDistanceModel();
		dist.applyDistanceModel(body.getPreviousDistance(), body.getCurrentDistance(), getOutput());
		return getOutput();
	}

	@Override
	public Block tickAdd() {
		for(Resonator r:this.resonators)
		{
			if(!r.isQuiet())
				r.tickAdd();
		}
		return getOutput();
	}

	@Override
	public void zero() {
		for(Resonator r:this.resonators)
			r.zero();
	}

}
