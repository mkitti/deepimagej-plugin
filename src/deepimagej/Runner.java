package deepimagej;

import java.util.List;
import java.util.concurrent.Callable;

import org.tensorflow.SavedModelBundle;
import org.tensorflow.Session;
import org.tensorflow.Tensor;
import org.tensorflow.framework.SignatureDef;
import org.tensorflow.framework.TensorInfo;

import deepimagej.tools.ArrayOperations;
import deepimagej.tools.CompactMirroring;
import deepimagej.tools.Log;
import deepimagej.tools.NumFormat;
import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;

public class Runner implements Callable<ImagePlus> {

	private ImagePlus		imp;
	private DeepPlugin		dp;
	private RunnerProgress	rp;
	private Log				log;
	private int				currentPatch = 0;
	private int				totalPatch = 0;

	public Runner(DeepPlugin dp, RunnerProgress rp, ImagePlus imp, Log log) {
		this.dp = dp;
		this.rp = rp;
		this.log = log;
		this.imp = imp;
		log.print("constructor runner");
	}

	public Runner(DeepPlugin dp, RunnerProgress rp, Log log) {
		this.dp = dp;
		this.rp = rp;
		this.log = log;
		this.imp = null;
	}

	@Override
	public ImagePlus call() {
		log.print("call runner");
		if (imp == null)
			imp = WindowManager.getCurrentImage();
		if (imp == null) {
			rp.stop();
			return null;
		}
		if (log.getLevel() >= 1)
			rp.setVisible(true);

		int nx = imp.getWidth();
		int ny = imp.getHeight();
		log.print("image size " + nx + "x" + ny);
		Parameters params = dp.params;

		if (3 * nx < params.patch || 3 * ny < params.patch) {
			IJ.log("Error patch size is too large " + params.patch);
			rp.stop();
			return null;
		}
		log.print("patch size " + params.patch);

		// To define the runtime for config.xml. Starting time
		long startingTime = System.nanoTime();
		// Create the image that is going to be fed to the graph
		ImagePlus impatch = null;
		String in1 = params.inputs[0];
		String outputTitle = dp.getName() + " of " + imp.getTitle();
		int[] dim = params.inDimensions;
		String[] outputs = params.outputs;

		SavedModelBundle model = dp.getModel();
		log.print("model " + (model == null));
		
		ImagePlus out = null;
		SignatureDef sig = TensorFlowModel.getSignatureFromGraph(model, TensorFlowModel.returnStringSig(dp.params.graph));
		log.print("sig " + (sig == null));

		// Order of the dimensions. For example "NHWC"-->Batch size, Height, Width, Channels
		String inputForm = params.inputForm[0];
		// Order of the dimensions. For example "NHWC"-->Batch size, Height, Width, Channels
		String outputForm = params.outputForm[0];
		int nChannels = Integer.parseInt((String) params.channels);
		int overlap = params.padding;

		int channel_pos = ArrayOperations.indexOf(inputForm.split(""), "C");
		int[] inDim = imp.getDimensions();
		if (inDim[2] != nChannels && dim[channel_pos] != -1) {
			IJ.log("Error in nChannel " + nChannels);
			rp.stop();
			return out;
		}
		
		int roi = params.patch - overlap * 2;
		int npx = nx / roi;
		int npy = ny / roi;
		if (nx % roi != 0)
			npx++;
		if (ny % roi != 0)
			npy++;
		currentPatch = 0;
		totalPatch = npx * npy;

		int[] padding = ArrayOperations.findAddedPixels(nx, ny, overlap, roi);
		ImagePlus mirrorImage = CompactMirroring.mirrorXY(imp, padding[0], padding[1], padding[2], padding[3]);
		if (log.getLevel() == 3) {
			mirrorImage.setTitle("Extended image");
			mirrorImage.getProcessor().resetMinAndMax();
			mirrorImage.show();
		}
		
		// If the roi of the patch is bigger than the actual image wanted, consider all the
		// remaining pixels as overlap (padding). Consider that now there might be then different
		// padding for X and Y
		int overlapX = overlap;
		int roiX = roi;
		if (roiX > nx) {
			roiX = nx;
			padding[0] = (params.patch - nx) / 2;
			overlapX = (params.patch - nx) / 2;
		}
		
		int overlapY = overlap;
		int roiY = roi;
		if (roiY > ny) {
			roiY = ny;
			padding[2] = (params.patch - ny) / 2;
			overlapY = (params.patch - ny) / 2;
		}

		String outputName;
		log.print("start " + npx + "x" + npy);
		
		for (int i = 0; i < npx; i++) {
			for (int j = 0; j < npy; j++) {
				
				currentPatch++;
				log.print("currentPatch " + currentPatch);
				if (rp.isStopped()) {
					rp.stop();
					return out;
				}
				
				int xMirrorStartPatch;
				int yMirrorStartPatch;
				
				int xImageStartPatch;
				int xImageEndPatch;
				int yImageStartPatch;
				int yImageEndPatch;
				int leftoverPixelsX;
				int leftoverPixelsY;
				if (i < npx -1 || npx == 1) {
					xMirrorStartPatch = padding[0] + roiX*i;

					xImageStartPatch = roiX*i;
					xImageEndPatch = roiX*(i + 1);
					leftoverPixelsX = overlapX;
				} else {
					xMirrorStartPatch = nx + padding[0] - roiX;

					xImageStartPatch = roiX*i;
					xImageEndPatch = nx;
					leftoverPixelsX = overlapX + roiX - (xImageEndPatch - xImageStartPatch);
				}
				
				if (j < npy - 1 || npy == 1) {
					yMirrorStartPatch = padding[2] + roiY*j;

					yImageStartPatch = roiY*j;
					yImageEndPatch = roiY*(j + 1);
					leftoverPixelsY = overlapY;
				} else {
					yMirrorStartPatch = ny + padding[2] - roiY;

					yImageStartPatch = roiY*j;
					yImageEndPatch = ny;
					leftoverPixelsY = overlapY + roiY - (yImageEndPatch - yImageStartPatch);
				}
				
				ImagePlus patch = ArrayOperations.extractPatch(mirrorImage, params.patch, xMirrorStartPatch,
																yMirrorStartPatch, overlapX, overlapY, nChannels);
				log.print("Extract Patch (" + (i + 1) + ", " + (j + 1) + ") patch size: " + patch.getWidth() + "x" + patch.getHeight() + " pixels");
				if (log.getLevel() == 3) {
					patch.setTitle("Patch (" + i + "," + j + ")");
					patch.getProcessor().resetMinAndMax();
					patch.show();
				}
				Tensor<?> inputTensor = ImagePlus2Tensor.imPlus2tensor(patch, inputForm, nChannels);
				Session.Runner sess = model.session().runner();
				sess = sess.feed(opName(sig.getInputsOrThrow(in1)), inputTensor);
				for (int k = 0; k < outputs.length; k++) {
					outputName = outputs[k];
					sess = sess.fetch(opName(sig.getOutputsOrThrow(outputName)));
					log.print("Session fetch " + k);
				}
				try {
					List<Tensor<?>> fetches = sess.run();
					for (int counter = 0; counter < outputs.length; counter++) {
						log.print("Session run " + (counter+1) + "/"  + outputs.length);
						Tensor<?> result = fetches.get(counter);
						impatch = ImagePlus2Tensor.tensor2ImagePlus(result, outputForm);
						counter++;
					}
				}
				catch (Exception ex) {
					IJ.log("Error in the TensorFlow library");
					IJ.log(ex.getMessage());
					rp.stop();
					return out;
				}
				if (out == null) {
					int[] dims = impatch.getDimensions();
					out = IJ.createHyperStack(outputTitle, nx, ny, dims[2], dims[3], dims[4], 32);
					out.getProcessor().resetMinAndMax();
					out.show();
				}
				ArrayOperations.imagePlusReconstructor(out, impatch, xImageStartPatch, xImageEndPatch, yImageStartPatch, yImageEndPatch,
						leftoverPixelsX, leftoverPixelsY);
				log.print("Create Output ");
				if (out != null)
					out.getProcessor().resetMinAndMax();
				if (rp.isStopped()) {
					rp.stop();
					return out;
				}
			}
		}
		// To define the runtime. End time
		long endTime = System.nanoTime();
		params.runtime = NumFormat.seconds(endTime - startingTime);
		// Set Parameter params.memoryPeak
		params.memoryPeak = NumFormat.bytes(rp.getPeakmem());
		// Set Parameter  params.outputSize
		params.outputSize = Integer.toString(nx) + "x" + Integer.toString(ny);
		rp.stop();
		
		return out;
	}

	private String opName(final TensorInfo t) {
		final String n = t.getName();
		if (n.endsWith(":0")) {
			return n.substring(0, n.lastIndexOf(":0"));
		}
		return n;
	}

	public String[] assignCharacter(String form, int[] expandedDim, int[] imPlusDim) {
		int rank = expandedDim.length;
		String auxKey = "empty";
		String[] auxArray = createAuxArr(rank, auxKey);
		int start2find = 0;
		for (int i = 0; i < rank; i++) {
			char dim = form.charAt(i);
			int value = valueOfChar(imPlusDim, dim);
			auxArray = namePosition(dim, expandedDim, value, auxArray, auxKey, start2find);
		}
		return auxArray;
	}

	public String[] namePosition(char dirName, int[] imageDims, int dimValue, String[] outArray, String keyWord, int start) {
		// This method writes a character representing a dimension in the position where
		// it corresponds.
		// Names for the dimensions: "W"-->nx; "H"-->ny; "C"-->nc; "D"--> nz, "N"-->nb.
		// Example: image_dims= [256, 128, 3], dim_name = 'C', dim_value = 3, out_array
		// = ["nul, "nul", "nul"].
		// The output will be out_array = ["nul", "nul", "C"]

		int index = ArrayOperations.indexOf(imageDims, dimValue, start);
		if (outArray[index] == keyWord) {
			outArray[index] = String.valueOf(dirName);
		}
		else {
			outArray = namePosition(dirName, imageDims, dimValue, outArray, keyWord, index + 1);
		}
		return outArray;
	}

	public int valueOfChar(int[] imPlusDim, char dimName) {
		// This method takes advantage that the dimensions retrieved from an ImagePlus
		// always have the shape [nx, ny, nc, nz, nt] in order to retrieve the value
		// for the dimension specified by the given character. It also assumes that the
		// batch
		// size is always 1.
		// "W"-->nx; "H"-->ny; "C"-->nc; "D"--> nz, "N" = 1.
		int value = 0;
		if (dimName == 'W') {
			value = imPlusDim[0];
		}
		else if (dimName == 'H') {
			value = imPlusDim[1];
		}
		else if (dimName == 'C') {
			value = imPlusDim[2];
		}
		else if (dimName == 'D') {
			value = imPlusDim[3];
		}
		else if (dimName == 'N') {
			value = 1;
		}
		return value;
	}

	public String[] createAuxArr(int size, String keyword) {
		// This method creates an auxiliar< string array with the where every entry is
		// the word inputs as keyword
		String[] aux_array = new String[size];
		for (int i = 0; i < size; i++) {
			aux_array[i] = keyword;
		}
		return aux_array;
	}

	public int getCurrentPatch() {
		return currentPatch;
	}

	public int getTotalPatch() {
		return totalPatch;
	}

}
