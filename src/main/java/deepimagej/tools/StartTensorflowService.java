/*
 * DeepImageJ
 * 
 * https://deepimagej.github.io/deepimagej/
 * 
 * Reference: DeepImageJ: A user-friendly environment to run deep learning models in ImageJ
 * E. Gomez-de-Mariscal, C. Garcia-Lopez-de-Haro, W. Ouyang, L. Donati, M. Unser, E. Lundberg, A. Munoz-Barrutia, D. Sage. 
 * Submitted 2021.
 * Bioengineering and Aerospace Engineering Department, Universidad Carlos III de Madrid, Spain
 * Biomedical Imaging Group, Ecole polytechnique federale de Lausanne (EPFL), Switzerland
 * Science for Life Laboratory, School of Engineering Sciences in Chemistry, Biotechnology and Health, KTH - Royal Institute of Technology, Sweden
 * 
 * Authors: Carlos Garcia-Lopez-de-Haro and Estibaliz Gomez-de-Mariscal
 *
 */

/*
 * BSD 2-Clause License
 *
 * Copyright (c) 2019-2021, DeepImageJ
 * All rights reserved.
 *	
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *	  this list of conditions and the following disclaimer in the documentation
 *	  and/or other materials provided with the distribution.
 *	
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package deepimagej.tools;

import org.scijava.Context;
import org.scijava.service.SciJavaService;

import ij.IJ;
import net.imagej.ImageJService;
import net.imagej.tensorflow.TensorFlowService;

public class StartTensorflowService {
	
	private static TensorFlowService tfService;
	private static boolean newContext = false;


	/*
	 * Try to load tf using IMageJ-Tensorflow manager. If it fails
	 * notify that we are on IJ! and that the tf library will be loaded 
	 * from the jars library using libtensorflow.jar and libtensorflow_jni.jar
	 */
	public static String loadTfLibrary() {
		Context ctx = (Context) IJ.runPlugIn("org.scijava.Context", "");
		if (ctx == null) {
			ctx = new Context(ImageJService.class, SciJavaService.class);
			newContext = true;
		}
		tfService = ctx.service(TensorFlowService.class);
		if (!tfService.getStatus().isLoaded()) {
			tfService.initialize();
			tfService.loadLibrary();
			if (tfService.getStatus().isLoaded()) {
				return tfService.getStatus().getInfo();
			} else {
				IJ.log(tfService.getStatus().getInfo());
				return "";
			}
		}
		return tfService.getStatus().getInfo();
	}
	
	public static TensorFlowService getTfService() {
		return tfService;
	}
	
	public static void closeTfService() {
		tfService.dispose();
		System.out.println("[DEBUG] Close Tensorflow services");
	}
}
