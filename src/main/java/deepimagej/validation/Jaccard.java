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

package deepimagej.validation;

import java.text.DecimalFormat;
import java.util.ArrayList;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.Roi;
import ij.process.ImageProcessor;
import ij.process.ImageStatistics;

public class Jaccard extends AbstractLoss {


	public static void main(String arg[]) {
		ImagePlus ref = IJ.createImage("ref", 32, 200, 202, 32);
		ImagePlus test = IJ.createImage("test", 32, 200, 202, 32);
		ref.setRoi(new Roi(20, 30, 50, 50));
		ref.getProcessor().fill();
		
	}
	
	@Override
	public String getName() {
		return "Jaccard";
	}
	@Override
	public ArrayList<Double> compute(ImagePlus reference, ImagePlus test,Constants setting) {
		
		if(reference.getNChannels() > 1 || test.getNChannels() > 1) {
			IJ.error("Jaccard index can only be calculated on one channel images.");
			return null;
		}
		
		int nxr = reference.getWidth();
		int nyr = reference.getHeight();
			
		ArrayList<Double> res = new ArrayList<Double>(); 	
		
	
		int nzr = reference.getStack().getSize();
		int nzt = test.getStack().getSize();
		
		for (int z=1; z<=Math.max(nzr, nzt); z++) {
			int ir = Math.min(z, nzr);
			int it = Math.min(z, nzt);
			ImageProcessor ipt = test.getStack().getProcessor(it);
			ImageProcessor ipr = reference.getStack().getProcessor(ir);
			ImageStatistics aa = ipt.getStatistics();
			int difval = (int)aa.max + 1;
			double s, g, sum = 0.0, jaccard = 0.0, intersection=0.0;
			double globalJac = 0.0;
			
			for(int v=0; v < difval ; v++) {
				for (int x = 0; x < nxr; x++) {
					for (int y = 0; y < nyr; y++) {
						
						s =  ipr.getPixelValue(x, y);
						g = ipt.getPixelValue(x, y);
						if (Double.isNaN(g) || Double.isNaN(s)) {
							continue;
								
						}
						if( s == v && g == v) {
							intersection += 1;
							sum += 1;
							globalJac ++;
						}
						else if(s == v || g == v){
							sum += 1;
						}
						
					}
				}
				if (sum == 0) {
					// Workaround to avoid showing pixels that do not appear in the image
					jaccard = -1;
				} else {
					jaccard=(intersection)/(sum);
				}
				res.add(jaccard);
				intersection = 0;
				sum = 0;
			}
			res.add(globalJac / (nxr * nyr));
					
		}
		
		
		return res ;
	}


	@Override
	public ArrayList<Double> compose(ArrayList<Double> loss1, double w_1, ArrayList<Double> loss2, double w_2) {
		return null;
	}

	@Override
	public Boolean getSegmented() {
		return true;
	}

	@Override
	public String check(ImagePlus reference, ImagePlus test, Constants setting) {
		// TODO Auto-generated method stub
		
		//get the max of the first stack of the images
		double min_im1 = MinMax.getminimum(reference.getStack().getProcessor(1));
		double min_im2 = MinMax.getminimum(test.getStack().getProcessor(1));
		
		//get the first stack of the images
		ImageProcessor ipt = reference.getStack().getProcessor(1);
		ImageProcessor ipr = test.getStack().getProcessor(1);
		
		int nxr = reference.getWidth();
		int nyr = test.getHeight();
		
		if((min_im1<0)||(min_im2<0)) {
			
			return "For Jaccard, values must be positive";
		}
		double s,g;
		for (int x = 0; x < nxr; x++) {
			for (int y = 0; y < nyr; y++) {
				
				s =  ipr.getPixelValue(x, y);
				g = ipt.getPixelValue(x, y);
				if ((s%1)!=0.0||(g%1)!=0.0) {
					return "For Jaccard, values must be integer";
				}
			}
		}
		return "Valid";
	}
}


