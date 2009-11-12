/*
Copyright (C) 2001, Dylan Menzies
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

package com.jphya.signal;

import com.jphya.scene.Scene;

public class MeasureGain {

	// float paMeasureGain( void (*filter)(Block* input,Blockoutput) );

	public float measureGain(Filter f,Scene scene)// this is being passed a function pointer for filtering...									
	{

		int nTicks;
		int nMaxTicks;
		float sampleTime = 1.0f; // This ensures good response down to 10Hz.

		float sigSum = 0;
		float refSum = 0;

		Block ref = Block.newBlock(scene);// Static allocation originally?
		Block sig = Block.newBlock(scene);

		nMaxTicks = (int) (sampleTime * scene.getFPSFrames());

		for (nTicks = 0; nTicks < nMaxTicks; nTicks++) {
			ref.fillWithNoise();

			f.filter(ref, sig);
			sig.square();
			sigSum += sig.sum(); // Add sum of all samples.

			ref.square();
			refSum += ref.sum();
		}

		float gain = (float) Math.sqrt(sigSum / refSum);

		return gain;

	}
}
