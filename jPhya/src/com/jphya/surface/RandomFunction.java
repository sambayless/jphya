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
import com.jphya.utility.Rnd;

public class RandomFunction extends Function{


	
	private	float m_zeroRate;
	private	boolean m_fastZero;
	
	public	RandomFunction(){ 
			m_zeroRate = 0; 
			m_fastZero = false;
		}
		// ZeroRate determines the probability of the surface profile returning to zero.
		// A higher value gives a shorter average 'bump'.
		// It is relative to the main rate, so that as speed changes,
		// the duration of the bumps follows this.
		// -1 forces the fastest possible bump.

	public	void setZeroRate(float p){ 
			m_zeroRate = p;
			if (p == -1) m_fastZero = true;  else m_fastZero = false;
		}

	
	public void tick(FunctionContactGenerator gen){

		FloatBuffer out = gen.getOutput().getStart();
		float r = gen.getM_rate() /gen.getScene().getFPS(); //! Per rate adjust. Better to derive GridSurface and overide SetRateAtSpeed to include per sample factor.

		float zr;
		boolean fz =m_fastZero;
		float y;
		int i;

		y = gen.getM_y1();
		r = (r>0)?r:-r;
		zr = r * m_zeroRate;		// As rate increases, zero_rate increases and size of bumps decreases.

		for(i = 0; i < gen.getScene().getNFrames(); i++)
		{

			if (Rnd.random(0.0f, 1.0f) < r) y = Rnd.random(-1.0f,1.0f);
			else if (fz || Rnd.random(0.0f, 1.0f) < zr) y = 0.0f;	

			out.put(i, y);
		}

		gen.setM_y1(y);
	}
	
/*	private float Rnd(float from, float to)
	{
		return (to+from)/2f;
	//	return (float) Math.random()*(to-from)+from;
	}*/
	
}
