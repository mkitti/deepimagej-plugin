/*
 * DeepImageJ
 * 
 * https://deepimagej.github.io/deepimagej/
 *
 * Conditions of use: You are free to use this software for research or educational purposes. 
 * In addition, we expect you to include adequate citations and acknowledgments whenever you 
 * present or publish results that are based on it.
 * 
 * Reference: DeepImageJ: A user-friendly plugin to run deep learning models in ImageJ
 * E. Gomez-de-Mariscal, C. Garcia-Lopez-de-Haro, L. Donati, M. Unser, A. Munoz-Barrutia, D. Sage. 
 * Submitted 2019.
 *
 * Bioengineering and Aerospace Engineering Department, Universidad Carlos III de Madrid, Spain
 * Biomedical Imaging Group, Ecole polytechnique federale de Lausanne (EPFL), Switzerland
 *
 * Corresponding authors: mamunozb@ing.uc3m.es, daniel.sage@epfl.ch
 *
 */

/*
 * Copyright 2019. Universidad Carlos III, Madrid, Spain and EPFL, Lausanne, Switzerland.
 * 
 * This file is part of DeepImageJ.
 * 
 * DeepImageJ is free software: you can redistribute it and/or modify it under the terms of 
 * the GNU General Public License as published by the Free Software Foundation, either 
 * version 3 of the License, or (at your option) any later version.
 * 
 * DeepImageJ is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 * See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with DeepImageJ. 
 * If not, see <http://www.gnu.org/licenses/>.
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


