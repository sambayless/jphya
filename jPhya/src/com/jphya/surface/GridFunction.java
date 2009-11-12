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
package com.jphya.surface;

import java.nio.FloatBuffer;

import com.jphya.contact.FunctionContactGenerator;
import com.jphya.scene.Scene;
import com.jphya.signal.Block;

public class GridFunction extends Function{
	
	private	float m_cut;					// Determines mark/space ratio, or 'grid bar width'

	
	public	GridFunction(){
		m_cut = -0.5f; 
		}	// Initially even mark/space.



	public	void setMark(float m) { m_cut = m - 1.0f; }		// Set mark as a fraction of 1.
	


	public void tick(FunctionContactGenerator gen){

		FloatBuffer out = gen.getOutput().getStart();
		float x = gen.getM_x();
		float r = gen.getM_rate() /((float)gen.getScene().getFPS()); //! Per rate adjust. Better to derive GridSurface and overide SetRateAtSpeed to include per sample factor.
		int i;
		

		for(i = 0; i < gen.getScene().getNFrames(); i++)
		{
			x += r;
			//out[i] = ((float)( (int)(x-(float)(int)x - m_cut) )-0.5f);
			out.put(i,(((float)( (int)(x-(float)((int)x) - m_cut) ))-0.5f));
			//out[i] = (((float)( ((int)(x-(float)(((int)x) - m_cut)))) )-0.5f);
//			out[i] = ((float)(int(x) % 2)-0.5f);		// Even mark/space
		}

		gen.setM_x( x);			// Save state.
	}
}
