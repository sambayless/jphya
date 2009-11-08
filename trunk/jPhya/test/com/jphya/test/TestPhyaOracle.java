/*
Copyright (C) 2001-Present Dylan Menzies
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

package com.jphya.test;


import java.io.IOException;

import com.jphya.audio.DummyAudioOutputStream;
import com.jphya.body.Body;
import com.jphya.contact.Contact;
import com.jphya.contact.ContactDynamicData;
import com.jphya.impact.Impact;
import com.jphya.impact.ImpactDynamicData;
import com.jphya.resonator.ModalData;
import com.jphya.resonator.ModalResonator;
import com.jphya.resonator.Resonator;
import com.jphya.scene.Scene;
import com.jphya.signal.Block;
import com.jphya.surface.FunctionSurface;
import com.jphya.surface.GridFunction;
import com.jphya.surface.RandomFunction;
import com.jphya.surface.WavFunction;
import com.jphya.surface.WhiteFunction;

/**
 * This compares the output of JPhya against the original output of the Phya library for a battery of 
 * test cases.
 * @author Sam
 *
 */
public class TestPhyaOracle {

	public static final int N_BODIES = 5;

	

	// char modalDataFileName[] = "bintop.md";

	void multipleOutputCallback(Resonator res, float[] output) {
		// // Example multiple output callback
		// // Use userData in res to link to required info.
		// // PhysicalBody refers to an object from the dynamics library you
		// use.
		// // velocity might be used for doppler or directional radiation
		// effects.

		// Get 3D position, velocity and 3D sound buffer channel.

		// resUserData* d = (resUserData*)(res.getUserData());
		// float* position = d.PhysicalBody.position;
		// float* velocity = d.PhysicalBody.velocity;
		// float* audioChannel = d.audioChannel;
		// paLimiter* limiter = d.limiter;

		// You must copy the samples pointed to by output
		// - this buffer is valid only for this call.
		// Limiters are useful! Use paLimiter to make a set of limiters.

		// limiter.tick(output);
		// Your3DSoundEncode(audioChannel, position, velocity, output)
	}

	public static void main(String[] args) throws IOException {
		{
			// // Specify some different surfaces.
			// // FunSurfaces are derived from the general Surface class.
			// // A FunSurface takes a Fun, a stateless function describing the
			// surface profile.
			// // A number of parameters are available to taylor the sonic
			// behaviour of the surface as
			// // the contact dynamics vary.

			Scene scene = new Scene(new OracleAudioOutputStream());
			scene.setLimiter(null);
		//	scene.setFramesPerSecond(44100);
			// // Specify some different surfaces.
			// // FunSurfaces are derived from the general Surface class.
			// // A FunSurface takes a Fun, a stateless function describing the
			// surface profile.
			// // A number of parameters are available to taylor the sonic
			// behaviour of the surface as
			// // the contact dynamics vary.

			WhiteFunction whitefun = new WhiteFunction(); // White noise
			RandomFunction rndfun = new RandomFunction(); // 'Rnd' surface function.
			GridFunction gridfun = new GridFunction(); // 'Grid' surface function.
		//	WavFunction wavfun = new WavFunction(); // Surface described by a loaded audio
											// file.
			FunctionSurface whitesurf = new FunctionSurface(scene); // Function surface.. uses
														// surface functions,
														// combined with
			FunctionSurface rndsurf = new FunctionSurface(scene); // filter model for
													// slip/roll.
			FunctionSurface gridsurf = new FunctionSurface(scene);
			// FunSurface wavsurf = new FunSurface();

			whitesurf.setFun(whitefun); // White noise surface texture.
			whitesurf.setContactMasterGain(32000.0f); // Includes gain to
														// generate audio output
														// level.

			// The following define the velocity to surface-filter map.
			// This rises linearly to a maximum frequency value.
			whitesurf.setCutoffFreqAtRoll(10.0f); // Adjust the rel body vel to
													// filter cutoff mapping.
			whitesurf.setCutoffFreqRate(1000.0f); // Rate of change of cutoff
													// freq with slip speed.
			whitesurf.setCutoffFreq2AtRoll(10.0f); // Beef up rolling with
													// optional extra filter
													// layer.
			whitesurf.setCutoffFreq2Rate(1000.0f);
			// whitesurf.setBoostAtRoll(10.0f); // Boosts volume towards
			// rolling, simulates increased transfer.
			// whitesurf.setBoostBreakSlipSpeed(0.1f);
			whitesurf.setContactDirectGain(0.0f); // We can tune the settings
													// for each type of surface.
			whitesurf.setContactMasterGain(70000.0f);

			rndsurf.setFun(rndfun); // Random generator with variable 'bump'
									// frequency and width distributions.
			rndsurf.setContactMasterGain(20000.0f);
			rndsurf.setRateAtSpeed(440.0f, 1.0f); // Rate here means bumps per
													// second. Speed is the
													// contact speed for which
													// this is given.
			rndfun.setZeroRate(10.0f); // Rate of surface height returning to
										// zero, relative to main rate. Higher .
										// more spikey.
			// rndfun.setZeroRate(-1.0f); // Always returns to zero immediately
			// - max spikey.
			rndsurf.setCutoffFreqAtRoll(50.0f); // Adjust the rel body vel to
												// filter cutoff mapping.
			rndsurf.setCutoffFreqRate(1500.0f);
			rndsurf.setCutoffFreq2AtRoll(10.0f); // Beef up rolling with
													// optional extra filter
													// layer.
			rndsurf.setCutoffFreq2Rate(1000.0f);
			// rndsurf.setBoostAtRoll(10.0f); // Boosts volume towards rolling,
			// simulates increased transfer.
			// rndsurf.setBoostBreakSlipSpeed(0.1f);

			gridsurf.setFun(gridfun); // Grid bar function.
			gridfun.setMark(0.02f); // Width of each grid bar as fraction of bar
									// to bar distance.
			gridsurf.setContactMasterGain(20000.0f);
			gridsurf.setRateAtSpeed(44.0f, 1.0f); // Rate here means bars per
													// second.
			gridsurf.setCutoffFreqAtRoll(50.0f); // Adjust the rel body vel to
													// filter cutoff mapping.
			gridsurf.setCutoffFreqRate(20000.0f);

			/*
			 * 	gridsurf->setFun(gridfun);								// Grid bar function.
	gridfun->setMark(0.02f);								// Width of each grid bar as fraction of bar to bar distance.
	gridsurf->setContactMasterGain(20000.0f);
	gridsurf->setRateAtSpeed(44.0f, 1.0f);					// Rate here means bars per second.
	gridsurf->setCutoffFreqAtRoll(50.0f);					// Adjust the rel body vel to filter cutoff mapping.
	gridsurf->setCutoffFreqRate(20000.0f);

			 */
			
			/*
			 * wavsurf.setFun(wavfun); // Function loaded from a wav file, with
			 * interpolated read back. wavfun.readWav("../resource/byhand.wav");
			 * // wav file prepared using recording / synthesis / hand editing
			 * tools. wavfun.setInterpOff(); // Interpolation off keeps edges
			 * hard, good for specially prepared wavs.
			 * wavsurf.setContactMasterGain(2.0f); // This wav file has output
			 * gain level already, so bo extra required.
			 * wavsurf.setRateAtSpeed(0.04f, 1.0f); // Rate means wav seconds
			 * per actual seconds. wavsurf.setCutoffFreqAtRoll(50.0f); // Adjust
			 * the rel body vel to filter cutoff mapping.
			 * wavsurf.setCutoffFreqRate(2000.0f);
			 * wavsurf.setCutoffFreq2AtRoll(50.0f); // Adjust the rel body vel
			 * to filter cutoff mapping. wavsurf.setCutoffFreq2Rate(20000.0f);
			 */
			// wavsurf.setBoostAtRoll(10.0f);
			// rndsurf.setBoostBreakSlipSpeed(0.1f);
			// // Create resonators and bodies, set properties.
			// // Notice how different bodies can be made by frequency scaling a
			// single modal set.
			// // Also possible to have several bodies sharing the same
			// resonator, for complex systems.
			ModalData data = new ModalData();
			ModalResonator[] res = new ModalResonator[N_BODIES];
			Body[] body = new Body[N_BODIES];

			// Read modal data file.
			// Modal file created from a real recording, using modan.exe
			// The format is one line per mode: frequency, damping, amplitude.
			// (An e at the top indicates that it is produced by the evaluation
			// version of modan - values are encoded)
			// The first line contains factors that scale all the modes.
			// The file can be edited to select the most important modes.
			// Usually these are the loudest and/or least damped.

			data.read("res/bintop.md");

			int i, j;

			for (i = 0; i < N_BODIES; i++) {
				ModalResonator mr = res[i] = new ModalResonator(scene);
				mr.setData(data);
				mr.setQuietLevel(1.0f); // Determines at what rms envelope level
										// a resonator will be
				// faded out when no longer in contact, to save cpu.
				// Make bigger to save more cpu, but possibly truncate decays
				// notceably.

				// mr.setnActiveModes(10); // Can trade detail for speed.
				mr.setAuxAmpScale(.1f);
				mr.setAuxFreqScale(0.5f + 0.1f * i);
				body[i] = new Body();
				body[i].setResonator(mr); // NB Possible to have several bodies using
									// one res for efficiency.
				body[i].setSurface(whitesurf);
			}

			// // Temporary variables

			Contact contact = Contact.newContact(scene);
			Impact impact = Impact.newImpact(scene);
			ContactDynamicData contactData = new ContactDynamicData();
			ImpactDynamicData impactData = new ImpactDynamicData();

			float v;


			int trialRuns = 20;
			//------------------------------------------------------------------
			// -----------------------------
			// Quick impulse test
			//------------------------------------------------------------------
			// -----------------------------

			// goto impacts;

			System.out
					.printf("First recreate impulse using maximum hardness impact..\n\n");

			(body[0].getResonator()).setAuxFreqScale(1.0f);
			(body[0].getResonator()).setAuxAmpScale(0.1f);
			(body[0].getSurface()).setHardness(100000.0f); // Impulse time =
															// 1/hardness
															// (limited).

			impact = Impact.newImpact(scene);
			impact.setBody1(body[0]);
			impactData.relTangentSpeedAtImpact = 0; // No skid.
			impactData.impactImpulse = 1.0f;
			impact.setDynamicData(impactData);
			for (i = 0; i < trialRuns; i++)
				scene.generate();

			//------------------------------------------------------------------
			// -----------------------------
			//
			// Contact demonstration
			//
			// A new contact is created from the pool. The dynamic parameters
			// must
			// be continually updated, the faster the better.
			// 3 control speed parameters and one force parameter must be
			// updated using
			// information from each physics frame.
			// NB Utility/paGeomAPI.h can be used to calculate the speeds from
			// rigid body
			// dynamics information : velocity, angular velocity, contact
			// curvature.
			// Examples are given to highlight the two extremes of contact
			// behaviour:
			// total sliding and total rolling. In general contact behaviour at
			// any time is a mixture.
			// The contact should be deleted once the corresponding physical
			// contact is broken.
			//
			//------------------------------------------------------------------
			// -----------------------------
			//
			// Sliding
			//
			// Body1 models a small object sliding back and forth in a U shaped
			// Body2,
			// with oscillations decaying, simulating frictional loss of energy.
			// Body1 is sliding over Body2 with a single contact point,
			// so the contact point does not move relative to Body1,
			// but it does move relative to Body2.
			// At the contact point Body1 moves relative to Body2
			// (A variation is to have the contact moving relative to both,
			// simulating a wide contact area.)
			// body[1], body[2] are used for clarity.
			//
			//------------------------------------------------------------------
			// -----------------------------

			System.out.printf("Sliding with WhiteFun surface..\n\n");

			contact = Contact.newContact(scene);
			contact.setBody1(body[1]);
			contact.setBody2(body[2]);
			body[1].getResonator().setAuxDampScale(3.0f);
			body[2].getResonator().setAuxDampScale(3.0f);
			body[1].setSurface(whitesurf);
			body[2].setSurface(whitesurf);

			contactData.speedContactRelBody1 = 0;

			for (i = 0; i < trialRuns*2; i++) {
				v = (float) Math.exp(-(float) i / 500.0f)
						* ((float) Math.sin((float) i / 30.0f));

				contactData.contactForce = 0.05f * v; // Simulates change in
														// normal force due to
														// angle of 'U'.
				contactData.speedContactRelBody2 = v; // Sign of v can matter
														// here for some kinds
														// of surface.
				contactData.speedBody1RelBody2 = v; // Generally sign doesn't
													// matter here.
				contact.setDynamicData(contactData);

				scene.generate(); // Calculates 1 block of audio output and
									// plays it out.
				// Use paTick() to just calculate.
			}

			contact.fadeAndDelete();

			for (i = 0; i < trialRuns; i++)
				scene.generate(); // Wait.

		/*	wavIO out = new wavIO("outwav6.wav");
			float[] fData = bufStream.getFloats();
			ByteBuffer data2 = ByteBuffer.allocate(fData.length*2);
			ShortBuffer buf = data2.asShortBuffer();
			for(float f:fData)
			{
				int d = (int)f;
				d += 32767;
				data2.put(shortToByteArray(d));
				//data2.put(wavIO.shortToByteArray((short)f));
				//buf.put((short)f);
			}
			data2.flip();
			out.myData =data2.array();
			out.save();*/
			
			/*PlayTest2 p = new PlayTest2();
			p.execute(bufStream,Scene.nFramesPerSecond);*/
			
			//------------------------------------------------------------------
			// -----------------------------
			//
			// Rolling
			//
			// Body1 is like a cylinder rolling in a U shaped Body2.
			// The contact point now moves relative both objects,
			// but the relative speed between the bodies at the contact is zero.
			// This causes less friction, and the excitation signals are
			// 'damped'
			//
			//------------------------------------------------------------------
			// -----------------------------

			System.out.printf("Rolling with WhiteFun surface...\n\n");

			contact = Contact.newContact(scene);
			contact.setBody1(body[1]);
			contact.setBody2(body[2]);
			body[1].getResonator().setAuxDampScale(3.0f);
			body[2].getResonator().setAuxDampScale(3.0f);

			contactData.speedBody1RelBody2 = 0; // Characteristic of rolling.

			// whitesurf.setBoostAtZeroSlipSpeed(10);

			for (i = 0; i < trialRuns*2; i++) {
				v = (float) Math.exp(-(float) i / 500.0f)
						* (float) Math.sin((float) i / 30.0f);
				contactData.contactForce = .05f * v;
				contactData.speedContactRelBody1 = v;
				contactData.speedContactRelBody2 = v;
				contact.setDynamicData(contactData);

				scene.generate(); // Generates 1 block of output.
			}

			contact.fadeAndDelete();

			for (i = 0; i < trialRuns; i++)
				scene.generate();

			//------------------------------------------------------------------
			// -----------------------------

			System.out.printf("Sliding with RndFun surface..\n\n");

			body[1].setSurface(rndsurf);
			body[1].getResonator().setAuxDampScale(3.0f);
			body[2].setSurface(rndsurf);
			body[2].getResonator().setAuxDampScale(3.0f);

			contact = Contact.newContact(scene);
			contact.setBody1(body[1]);
			contact.setBody2(body[2]);
			contactData.speedContactRelBody1 = 0;

			for (i = 0; i < trialRuns*2; i++) {
				v = (float) Math.exp(-(float) i / 500.0f)
						* ((float) Math.sin((float) i / 30.0f));

				contactData.contactForce = 0.05f * v; // Simulates change in
														// normal force due to
														// angle of 'U'.
				contactData.speedContactRelBody2 = v;
				contactData.speedBody1RelBody2 = v;
				contact.setDynamicData(contactData);

				scene.generate(); // Generates 1 block of output.
			}

			contact.fadeAndDelete();

			for (i = 0; i < trialRuns; i++)
				scene.generate(); // Wait.

			//------------------------------------------------------------------
			// -----------------------------

			System.out.printf("Rolling with RndFun surface..\n\n");

			body[1].setSurface(rndsurf);
			body[1].getResonator().setAuxDampScale(3.0f);
			body[2].setSurface(rndsurf);
			body[2].getResonator().setAuxDampScale(3.0f);

			contact = Contact.newContact(scene);
			contact.setBody1(body[1]);
			contact.setBody2(body[2]);
			contactData.speedBody1RelBody2 = 0;

			for (i = 0; i < trialRuns*2; i++) {
				v = (float) Math.exp(-(float) i / 500.0f)
						* (float) Math.sin((float) i / 30.0f);
				contactData.contactForce = .05f * v;
				contactData.speedContactRelBody1 = v;
				contactData.speedContactRelBody2 = v;
				contact.setDynamicData(contactData);

				scene.generate(); // Generates 1 block of output.
			}

			contact.fadeAndDelete();

			for (i = 0; i < trialRuns; i++)
				scene.generate();

			//------------------------------------------------------------------
			// -----------------------------

			System.out.printf("Sliding with particle-like RndFun surface..\n\n");

			rndfun.setZeroRate(-1.0f); // Make surface particle-like
			rndsurf.setContactMasterGain(80000);
			// rndsurf.setBoostAtRoll(50.0f);
			rndsurf.setCutoffFreqAtRoll(50.0f); // Adjust the rel body vel to
												// filter cutoff mapping.
			rndsurf.setCutoffFreqRate(1500.0f);
			rndsurf.setCutoffFreq2AtRoll(50.0f); // Beef up rolling with
													// optional extra filter
													// layer.
			rndsurf.setCutoffFreq2Rate(1000.0f);

			body[1].setSurface(rndsurf);
			body[1].getResonator().setAuxDampScale(3.0f);
			body[2].setSurface(rndsurf);
			body[2].getResonator().setAuxDampScale(3.0f);

			contact = Contact.newContact(scene);
			contact.setBody1(body[1]);
			contact.setBody2(body[2]);
			contactData.speedContactRelBody1 = 0;

			for (i = 0; i < trialRuns*2; i++) {
				v = (float) Math.exp(-(float) i / 500.0f)
						* ((float) Math.sin((float) i / 30.0f));

				contactData.contactForce = 0.05f * v; // Simulates change in
														// normal force due to
														// angle of 'U'.
				contactData.speedContactRelBody2 = v;
				contactData.speedBody1RelBody2 = v;
				contact.setDynamicData(contactData);

				scene.generate(); // Generates 1 block of output.
			}

			contact.fadeAndDelete();

			for (i = 0; i < trialRuns; i++)
				scene.generate(); // Wait.

			//------------------------------------------------------------------
			// -----------------------------

			System.out
					.printf("Rolling with particle-like RndFun surface..\n\n");

			rndfun.setZeroRate(-1.0f); // Modify to make surface particle-like.
			rndsurf.setContactMasterGain(40000);

			body[1].setSurface(rndsurf);
			body[1].getResonator().setAuxDampScale(3.0f);
			body[2].setSurface(rndsurf);
			body[2].getResonator().setAuxDampScale(3.0f);

			contact = Contact.newContact(scene);
			contact.setBody1(body[1]);
			contact.setBody2(body[2]);
			contactData.speedBody1RelBody2 = 0;

			for (i = 0; i < trialRuns*2; i++) {
				v = (float) Math.exp(-(float) i / 500.0f)
						* (float) Math.sin((float) i / 30.0f);
				contactData.contactForce = .05f * v;
				contactData.speedContactRelBody1 = v;
				contactData.speedContactRelBody2 = v;
				contact.setDynamicData(contactData);

				scene.generate(); // Generates 1 block of output.
			}

			contact.fadeAndDelete();

			for (i = 0; i < trialRuns; i++)
				scene.generate();

			//------------------------------------------------------------------
			// -----------------------------

			System.out.printf("Sliding with GridFun surface..\n\n");

			body[1].setSurface(gridsurf);
			body[1].getResonator().setAuxDampScale(1.0f);
			body[1].getResonator().setAuxFreqScale(1.3f);
			body[2].setSurface(gridsurf);
			body[2].getResonator().setAuxDampScale(1.0f);
			body[2].getResonator().setAuxFreqScale(1.5f);

			contact = Contact.newContact(scene);
			contact.setBody1(body[1]);
			contact.setBody2(body[2]);
			contactData.speedContactRelBody1 = 0;

			for (i = 0; i < trialRuns*2; i++) {
				v = (float) Math.exp(-(float) i / 500.0f)
						* ((float) Math.sin((float) i / 30.0f));

				contactData.contactForce = 0.05f * v; // Simulates change in
														// normal force due to
														// angle of 'U'.
				contactData.speedContactRelBody2 = v;
				contactData.speedBody1RelBody2 = v;
				contact.setDynamicData(contactData);

				scene.generate(); // Generates 1 block of output.
			}

			contact.fadeAndDelete();

			for (i = 0; i < trialRuns; i++)
				scene.generate(); // Wait.

			//------------------------------------------------------------------
			// -----------------------------
			//this is pretty accurate up till here:
			System.out.printf("Rolling with GridFun surface..\n\n");

			body[1].setSurface(gridsurf);
			body[1].getResonator().setAuxDampScale(1.0f);
			body[1].getResonator().setAuxFreqScale(1.3f);
			body[2].setSurface(gridsurf);
			body[2].getResonator().setAuxDampScale(1.0f);
			body[2].getResonator().setAuxFreqScale(1.5f);

			contact = Contact.newContact(scene);
			contact.setBody1(body[1]);
			contact.setBody2(body[2]);
			contactData.speedContactRelBody1 = 0;

			contactData.speedBody1RelBody2 = 0;

			for (i = 0; i < trialRuns*2; i++) {
				v = (float) Math.exp(-((float) i) / 500.0f)
						* (float) Math.sin(((float) i) / 30.0f);
				contactData.contactForce = .05f * v;
				contactData.speedContactRelBody1 = v;
				contactData.speedContactRelBody2 = v;
				contact.setDynamicData(contactData);

				scene.generate(); // Generates 1 block of output.
			}

			contact.fadeAndDelete();

			for (i = 0; i < trialRuns; i++)
				scene.generate();

			//------------------------------------------------------------------
			// -----------------------------

			/*
			 * System.out.printf("Sliding with WavFun surface..\n\n");
			 * 
			 * body[1].setSurface(wavsurf);
			 * body[1].getRes().setAuxDampScale(3.0f);
			 * body[1].getRes().setAuxFreqScale(1.1f);
			 * body[2].setSurface(wavsurf);
			 * body[2].getRes().setAuxDampScale(3.0f);
			 * body[2].getRes().setAuxFreqScale(1.2f);
			 * 
			 * contact = Contact.newContact(); contact.setBody1(body[1]);
			 * contact.setBody2(body[2]); contactData.speedContactRelBody1 = 0;
			 * 
			 * for(i=0; i<2000; i++) { v = (float)Math.exp(-(float)i/500.0f)
			 * ((float)Math.sin((float)i/30.0f));
			 * 
			 * contactData.contactForce = 0.05f v; // Simulates change in normal
			 * force due to angle of 'U'. contactData.speedContactRelBody2 = v;
			 * contactData.speedBody1RelBody2 = v;
			 * contact.setDynamicData(contactData);
			 * 
			 * scene.Generate(); // Generates 1 block of output. }
			 * 
			 * contact.fadeAndDelete();
			 * 
			 * for(i=0; i<1000; i++) scene.Generate(); // Wait.
			 * 
			 * 
			 * //----------------------------------------------------------------
			 * -------------------------------
			 * 
			 * 
			 * System.out.printf("Rolling with WavFun surface..\n\n");
			 * 
			 * body[1].setSurface(wavsurf);
			 * body[1].getRes().setAuxDampScale(3.0f);
			 * body[1].getRes().setAuxFreqScale(1.1f);
			 * body[2].setSurface(wavsurf);
			 * body[2].getRes().setAuxDampScale(3.0f);
			 * body[2].getRes().setAuxFreqScale(1.2f);
			 * 
			 * contact = Contact.newContact(); contact.setBody1(body[1]);
			 * contact.setBody2(body[2]); contactData.speedContactRelBody1 = 0;
			 * 
			 * contactData.speedBody1RelBody2 = 0;
			 * 
			 * for(i=0; i<2000; i++) { v = (float)Math.exp(-(float)i/500.0f)
			 * (float)Math.sin((float)i/30.0f); contactData.contactForce = .05f
			 * v; contactData.speedContactRelBody1 = v;
			 * contactData.speedContactRelBody2 = v;
			 * contact.setDynamicData(contactData);
			 * 
			 * scene.Generate(); // Generates 1 block of output. }
			 * 
			 * contact.fadeAndDelete();
			 * 
			 * for(i=0; i<1000; i++) scene.Generate();
			 */

			//------------------------------------------------------------------
			// -----------------------------
			//
			// Impact demonstration
			//
			// Impacts should be created when a momentary impact is detected.
			// They delete themselves once finished.
			// 'FunSurfaces' support momentary contact generation for skid type
			// effects,
			// but this is quite a crude effect, because this contact generator
			// is not dynamically updated. A FunSurface does not skid by
			// default.
			// It is better to generate this kind of effect by with a
			// dynamically updated contact following a detected impact.
			//
			// The damping is randomly changed to simulate object touching.
			// This effect can be generated automatically when contacts are
			// made,
			// provided surface damping factors have been set.

			// One body has frequency modulation added. This can be used to
			// model object deformation,
			// Which causes modes to move around.

			// Impulse-dependent hardness response added, ie nonlinear impact.
			// Makes sound brighter when impact is harder.

			// Add skid properties to surface:
/*
			whitesurf.setSkidGain(0.1f);
			whitesurf.setSkidImpulseToForceRatio(0.2f);
			whitesurf.setSkidThickness(0.01f);
			whitesurf.setSkidMinTime(0.01f);
			whitesurf.setSkidMaxTime(0.05f);

			// Make impacts brighter above an impulse threshold, to model
			// non-linear surfaces.
			whitesurf.setHardness(400.0f);
			whitesurf.setImpulseToHardnessBreakpoint(1.0f);
			whitesurf.setImpulseToHardnessScale(1000.0f);

			for (i = 0; i < N_BODIES; i++)
				body[i].setSurface(whitesurf);

			System.out
					.printf("Nonlinear surface : increasing impulse causes increasing impact hardness and brightness..\n\n");

			float impulse = 0.5f;
			for (i = 0; i < 10; i++) {
				impact = Impact.newImpact();
				if (impact != null) {
					impact.setBody1(body[0]);
					impact.setBody2(null);
					impactData.relTangentSpeedAtImpact = 0.0f;
					impulse *= 1.3f;
					impactData.impactImpulse = impulse;
					impact.setDynamicData(impactData);
				}
				for (j = 0; j < 200; j++)
					scene.Generate();
			}
*/
			/*
			 * System.out.printf("Impacts with randomized damping, skids and frequency modulation..\n\n"
			 * );
			 * 
			 * while(1) {
			 * 
			 * if (Math.random() < .0f) { impact = Impact.newImpact(); if
			 * (impact) { int body1; int body2;
			 * 
			 * body1 = paRnd(0, N_BODIES-1); impact.setBody1(body[body1]);
			 * 
			 * body2 = paRnd(0, N_BODIES-1); impact.setBody2(body[body2]);
			 * 
			 * if (paRnd(0.0f, 1.0f) < .8)
			 * body[body1].getRes().setAuxDampScale(paRnd(0.5f, 3.0f));
			 * 
			 * impactData.relTangentSpeedAtImpact = paRnd(0.0f, 10.0f); // For
			 * skidding. impactData.relNormalSpeedAtImpact = paRnd(0.0f, 1.0f);
			 * impactData.impactImpulse = paRnd(0.0f,2.0f);
			 * impact.setDynamicData(&impactData); } }
			 */
			// // Frequency modulation
			// // Could be driven by deformable body stress.
/*			body[0].getRes().setAuxFreqScale(
					1.0f + 0.01f * (float) Math.sin((float) i / 10) + 0.01f
							* (float) Math.sin((float) i / 7));
			i++;

			scene.Generate();*/

			// Sleep(50); // Dynamics tick. Use if running audio thread. In
			// milliseconds

			/*
			 * // Monitor resource useage.
			 * 
			 * System.out.printf("Number of blocks being used = %d\n",
			 * Block.pool.getnActiveObjects() );
			 * System.out.printf("Number of contacts being used = %d\n",
			 * Contact.pool.getnActiveObjects() );
			 * System.out.printf("Number of impacts being used = %d\n",
			 * Impact.pool.getnActiveObjects() );
			 * System.out.printf("Number of resonators being used = %d\n",
			 * Res.activeResList.getnMembers() );
			 */

		}
		
		// // Destroy resources

		/*
		 * Block.pool.deallocate(); Contact.pool.deallocate();
		 * Impact.pool.deallocate(); FunSurface.contactGenPool.deallocate();
		 * FunSurface.impactGenPool.deallocate();
		 * Res.activeResList.deallocate();
		 */

		/*
		 * delete whitefun; delete rndfun; delete gridfun; delete wavfun; delete
		 * whitesurf; delete rndsurf; delete gridsurf; delete wavsurf; delete
		 * data;
		 * 
		 * for(i=0; i< N_BODIES; i++) { delete res[i]; delete body[i]; }
		 */

		}

		
	

}
